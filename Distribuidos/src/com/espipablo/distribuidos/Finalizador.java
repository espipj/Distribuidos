package com.espipablo.distribuidos;

import java.io.IOException;
import java.util.concurrent.Semaphore;

public class Finalizador extends Thread {
	
	public Semaphore semReadyEnd;
	protected int totalMaquinas;
	public long[] offset;
	
	Finalizador(int total) {
		this.semReadyEnd = new Semaphore(0);
		this.totalMaquinas = total;
		this.offset = new long[3];
	}
	
	public void run() {
		
		try {
			semReadyEnd.acquire(totalMaquinas);
			
			String offsets = "";
			for (int i=1; i < offset.length; i++) {
				offsets += offset[i] + " ";
			}
			
			System.out.println("Ejecutando... ");
			Runtime.getRuntime().exec("~/Z/Distribuidos/PractObligatoria/Distribuidos/juntar.sh " + offsets + " > resultado.log");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
