package com.espipablo.distribuidos;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Semaphore;

public class Fichero extends Thread implements ControladorRegistro {

	protected int maquina;
	protected File file;
	protected Semaphore semFinalRegistro;
	protected ArrayList<Registro> registros;
	
	Fichero(int maquina) {
		this.maquina = maquina;
		this.file = new File(maquina + ".log");
		this.semFinalRegistro = new Semaphore(0);
    	this.registros=new ArrayList<Registro>();
	}
	
	public void run() {
        // Meter esto en fichero y que extienda de Thread
        try {
			semFinalRegistro.acquire(400);
			Collections.sort(registros);
			for (Registro registro : registros) {
				System.out.println(registro.registro + registro.tiempo);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void escribirEntradaSC(int pi) {
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
	}

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
}
