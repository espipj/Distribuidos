package com.espipablo.distribuidos;

import java.io.IOException;
import java.util.concurrent.Semaphore;

public class Finalizador extends Thread {
	
	public Semaphore semReadyEnd;
	protected int totalMaquinas;
	public long[] delay;
	
	Finalizador(int total) {
		this.semReadyEnd = new Semaphore(0);
		this.totalMaquinas = total;
		this.delay = new long[3];
	}
	
	public void run() {
		
		try {
			semReadyEnd.acquire(totalMaquinas);
			
			String delays = "";
			for (int i=1; i < delay.length; i++) {
				delays += delay[i] + " ";
			}
			
			System.out.println("Ejecutando... ");
			Runtime.getRuntime().exec(System.getProperty("user.home")+"/Z/Distribuidos/PractObligatoria/Distribuidos/juntar.sh " + delays);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
