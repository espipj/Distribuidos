package com.espipablo.distribuidos;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import org.json.JSONArray;

public class Proceso extends Thread {

	protected int pi;
	protected int ci;
	protected int ti;
	protected int total;
	protected Semaphore respuesta;
	protected String estado;
	protected Fichero fichero;
	protected JSONArray procesos;
	protected Queue<Integer> cola;
	
	protected Object colaBlock;
	protected Object statusBlock;
	protected Object ciBlock;
	
	protected static final String LIB = "LIBERADA";
	protected static final String TOM = "TOMADA";
	protected static final String BUS = "BUSCADA";
	
	protected static final double MAXSC = 0.3f;
	protected static final double MINSC = 0.1f;
	protected static final double MAXPROC = 0.5f;
	protected static final double MINPROC = 0.3f;


	Proceso(int id, int total, Fichero fichero, JSONArray procesos) {
		this.pi = id;
		this.ci = 0;
		this.ti = 0;
		this.estado = Proceso.LIB;
		this.total = total;
		this.procesos = procesos;
		this.respuesta = new Semaphore(0);
		this.cola = new LinkedList<Integer>();

		colaBlock = new Object();
		statusBlock = new Object();
		ciBlock = new Object();

		this.fichero = fichero;
	}

	public void run() {
		// 100 accesos a la sección crítica
		for (int i = 0; i < 100; i++) {
			System.out.println("Soy: " + this.pi + " Ronda: " + i);
			try {
				Thread.sleep((long) (((MAXPROC - MINPROC) * Math.random() + MINPROC) * 1000));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			entrarEnSC();
		}
	}

	/*
	 * Al recibir una petición <Tj, pj> en pi Ci = max(Ci, Tj) + 1 // LC2 si (
	 * estado = TOMADA o (estado = BUSCADA y (Ti, pi) < (Tj, pj)*) ) pon en cola
	 * la petición, por parte de pj si no responde inmediatamente a pj
	 */
	public void recibirPeticion(int tj, int pj) {
		// System.out.println("Recibiendo peticion en " + this.pi + " de " +
		// pj);

		synchronized (this.ciBlock) {
			ci = max(ci, tj) + 1;
		}

		synchronized (this.colaBlock) {
			synchronized (this.statusBlock) {
				if (estado.equals(Proceso.TOM) || (estado.equals(Proceso.BUS) && compareT(tj, pj))) {
					// Poner en cola
					cola.add(pj);
					return;
				}
			}
		}

		responderPeticion(pj);
	}

	public void recibirRespuesta() {
		// System.out.println("Respuesta recibida en: " + this.pi);
		this.respuesta.release();
	}

	/*
	 * estado = BUSCADA; Ti = Ci Multidifusión de la petición <Ti, pi> de
	 * entrada en SC Espera hasta que (nº de respuestas = (N-1)); estado =
	 * TOMADA; Ci = Ci + 1 // LC1
	 */
	public void entrarEnSC() {
		// Cojo estado buscando pero no cojo ti. En la línea 99 daría true
		// si mi t es menor que la suya y luego yo cogería una t mayor que la
		// suya, por tanto el tampoco me respondería
		synchronized (this.statusBlock) {
			this.estado = Proceso.BUS;
			this.ti = this.ci;
		}

		// System.out.println("Buscando en " + this.pi);

		for (int i = 1; i < procesos.length() + 1; i++) {
			// No me mando peticiones a mi mismo
			if (i == this.pi) {
				//System.out.println(procesos.getString(i - 1));
				continue;
			}
//			 System.out.println("http://" + procesos.getString(i-1) +
//			 ":8080/Distribuidos/despachador/peticion?id=" + i + "&tj=" +
//			 this.ti + "&from=" + this.pi);
			Util.request("http://" + procesos.getString(i - 1) + ":8080/Distribuidos/despachador/peticion?id=" + i + "&tj="
					+ this.ti + "&from=" + this.pi);
		}

		try {
			// Esperamos por todas las demás máquinas
			this.respuesta.acquire(this.total - 1);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		this.estado = Proceso.TOM;
		synchronized (this.ciBlock) {
			this.ci++;
		}
		
		// DENTRO DE LA SECCIÓN CRÍTICA
		this.escribirEntradaSC();

		System.err.println("SOY " + this.pi);
		try {
			Thread.sleep((long) (((MAXSC - MINSC) * Math.random() + MINSC) * 1000));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		this.salirSC();
	}

	public void salirSC() {
		this.escribirSalidaSC();
		this.estado = Proceso.LIB;
		// FUERA DE LA SECCIÓN CRÍTICA

		// Bloqueamos el objeto cola para que no nos llegue alguna petición mientras estamos procesando la cola y hacemos un clear()
		synchronized (this.colaBlock) {
			for (int p : cola) {
				responderPeticion(p);
			}
			cola.clear();
		}
	}

	// Respondemos a una petición de acceso a la sección crítica
	protected void responderPeticion(int p) {
		// System.out.println("Soy: " + this.pi + " Respondiendo a: " + p);
		Util.request("http://" + procesos.getString(p - 1) + ":8080/Distribuidos/despachador/respuesta?id=" + p);
	}

	// Escribimos una entrada de acceso a la sección crítica
	protected void escribirEntradaSC() {
		this.fichero.anadirRegistro("P" + this.pi + " E", System.currentTimeMillis());
		// this.fichero.escribirEntradaSC(this.pi);
	}

	// Escribimos una salida de acceso a la sección crítica
	protected void escribirSalidaSC() {
		this.fichero.anadirRegistro("P" + this.pi + " S", System.currentTimeMillis());
		// this.fichero.escribirSalidaSC(this.pi);
	}

	// Caculamos cuál es el proceso con un identificador mayor
	protected int max(int num1, int num2) {
		return num1 > num2 ? num1 : num2;
	}

	// *(Ti, pi) < (Tj, pj) implica que Ti < Tj o que T = Tj y pi < pj
	protected boolean compareT(int tj, int pj) {
		// System.out.println(this.ti + "|" + tj + " " + this.pi + "|" + pj);
		if (ti < tj || (ti == tj && pi < pj)) {
			return true;
		}
		return false;
	}

}
