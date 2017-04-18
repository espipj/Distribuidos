package com.espipablo.distribuidos;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Semaphore;

public class Fichero extends Thread implements ControladorRegistro {

	protected int maquina;
	protected File file;
	protected Semaphore semFinalRegistro;
	protected ArrayList<Registro> registros;
	protected String url;
	public double offset;
	public double delay;
	
	Fichero(int maquina, String url, double offset, long delay) {
		this.maquina = maquina;
		this.file = new File(Util.filePath(maquina + ".log"));
		System.out.println(this.file.getAbsolutePath());
		this.semFinalRegistro = new Semaphore(0);
    	this.registros = new ArrayList<Registro>();
    	this.url = url;
    	this.offset = offset;
    	this.delay = delay;
	}
	
	public void run() {
        try {
        	// Esperamos a que se realicen las 400 escrituras de las 100 iteraciones (100 entradas y 100 salidas por cada proceso)
			semFinalRegistro.acquire(400);
			
			if (this.maquina != 0)
			{
				NTP.ntp(url);
				/*NTP.offset/delay son offset y delay calculados al final, en this.offset/delay estan los iniciales
				 * (pasados en la inicialización del fichero)*/
				// Corregimos los tiempos de acuerdo a lo obtenido después de haber ejecutado NTP
				this.corregirTiempos(NTP.offset, NTP.delay);
			}
			
			// Ordenamos los registros
			Collections.sort(registros);
			/*for (Registro registro : registros) {
				System.out.println(registro.registro + registro.tiempo);
			}*/
			// Finalmente los escribimos en un fichero
			escribirRegistros();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	// Función encargada de escribir los registros en un fichero
	protected void escribirRegistros() {
		BufferedWriter bw;
		try {
			NumberFormat formatter=new DecimalFormat("#0.00");
			bw = new BufferedWriter(new FileWriter(this.file));
			for (Registro registro : registros) {
					bw.write(registro.registro + " " + formatter.format(registro.tiempo) + "\n");
			}
		    bw.close();
		    
		    // Le indicamos al proceso principal cuál es el delay que tenemos respecto de él
		    Util.request("http://" + url + ":8080/Distribuidos/despachador/delay?id=" + this.maquina + "&delay=" + this.delay);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*public void escribirEntradaSC(int pi) {
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter(this.file));
		    bw.write("P" + pi + " E " + System.currentTimeMillis());
		    bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void escribirSalidaSC(int pi) {
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter(this.file));
		    bw.write("P" + pi + " S " + System.currentTimeMillis());
		    bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/

	// Añadimos registro al ArrayList
	@Override
	public void anadirRegistro(String s, long l) {
		semFinalRegistro.release();
		Registro r = new Registro();
		r.registro = s;
		r.tiempo = l;

		synchronized (this) {
			registros.add(r);
		}
		
	}
	
	// Corregimos los tiempos de los registros basándonos en el offset obtenido
	public void corregirTiempos(double o1, long d1) {
		this.offset = (this.offset + o1) / 2;
		this.delay = (this.delay + d1) / 2;
		for (Registro registro : registros) {
			registro.tiempo += this.offset;
		}

	}
}
