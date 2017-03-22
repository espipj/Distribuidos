package com.espipablo.distribuidos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class Proceso extends Thread {
	
	protected int pi;
	protected int ci;
	protected int ti;
	protected int total;
	protected Semaphore respuesta;
	protected String estado;
	protected Fichero fichero;
	protected String[] procesos;
	protected Queue<Integer> cola;
	protected static final String LIB = "LIBERADA";
	protected static final String TOM = "TOMADA";
	protected static final String BUS = "BUSCADA";
	protected static final double MAXSC = 0.3f;
	protected static final double MINSC = 0.1f;
	protected static final double MAXPROC = 0.5f;
	protected static final double MINPROC = 0.3f;
	
	Proceso(int id, int total, Fichero fichero, String[] procesos) {
		this.pi = id;
		this.ti = ti;
		this.ci = 0;
		this.estado = Proceso.LIB;
		this.total = total;
		this.procesos = procesos;
		this.respuesta = new Semaphore(0);
		this.cola = new LinkedList();
		
		String ruta = this.pi + ".log";
		this.fichero = fichero;
	}
	
	public void run() {
		for (int i=0; i < 100; i++) {
			try {
				Thread.sleep((long) (((MAXPROC - MINPROC) * Math.random() + MINPROC) * 1000));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			entrarEnSC();
		}
	}
	
	/*
	 	Al recibir una petición <Tj, pj> en pi
		Ci = max(Ci, Tj) + 1 // LC2
		si ( estado = TOMADA o
		(estado = BUSCADA y (Ti, pi) < (Tj, pj)*) )
		pon en cola la petición, por parte de pj
		si no
		responde inmediatamente a pj 
	 */
	public void recibirPeticion(int tj, int pj) {
		ci = max(ci, tj) + 1;
		if (estado.equals(Proceso.TOM) || (estado.equals(Proceso.BUS) && compareT(tj, pj))) {
			// Poner en cola
			cola.add(pj);
			return;
		}
		
		responderPeticion(pj);
	}
	
	public void recibirRespuesta() {
		this.respuesta.release();
	}
	
	public void accederSC() {
		try {
			this.respuesta.acquire(2);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		// Entrar en SC
		this.estado = Proceso.TOM;
		this.ci++;
		
		this.entrarEnSC();
		try {
			Thread.sleep((long) (((MAXSC - MINSC) * Math.random() + MINSC) * 1000));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.salirSC();
		// Escribir en fichero
	}
	
	public void salirSC() {
		this.estado = Proceso.LIB;
		for (int p: cola) {
			responderPeticion(p);
		}
	}
	
	/*
	 	estado = BUSCADA;
		Ti = Ci
		Multidifusión de la petición <Ti, pi> de entrada en SC
		Espera hasta que (nº de respuestas = (N-1));
		estado = TOMADA;
		Ci = Ci + 1 // LC1
	 */
	public void entrarEnSC() {
		this.estado = Proceso.BUS;
		this.ti = this.ci;
		for (int i=1; i < procesos.length; i++) {
			// No me mando peticiones a mí mismo
			if (i == this.pi) {
				continue;
			}
			
			request("http://" + procesos[i] + ":8080/Distribuidos/despachador/peticion?id=" + i + "&tj=" + this.ti);
		}
		
		accederSC();
	}
	
	protected void responderPeticion(int p) {
		request("http://" + procesos[p] + ":8080/Distribuidos/despachador/respuesta?id=" + p);
	}
	
	protected void escribirEntradaSC() {
		this.fichero.escribirEntradaSC(this.pi);
	}
	
	protected void escribirSalidaSC() {
		this.fichero.escribirSalidaSC(this.pi);
	}
	
	protected int max(int num1, int num2) {
		return num1 > num2 ? num1 : num2;
	}
	
	// *(Ti, pi) < (Tj, pj) implica que Ti < Tj o que T = Tj y pi < pj
	protected boolean compareT(int tj, int pj) {
		if (ti < tj || (ti == tj && pi < pj)) {
			return true;
		}
		return false;
	}
	
	private String request(String urlS) {
        URL url = null;
        try {
            url = new URL(urlS);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            conn.setRequestMethod("GET");
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
        try {
            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String output;
        String result = "";
        try {
            while ((output = br.readLine()) != null) {
                result += output;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        conn.disconnect();
        return result;

    }
	
}
