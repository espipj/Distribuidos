package com.espipablo.distribuidos;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
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
			ProcessBuilder pb = new ProcessBuilder(System.getProperty("user.home")+"/Z/Distribuidos/PractObligatoria/Distribuidos/juntar.sh", String.valueOf(delay[0]));
			pb.redirectOutput(Redirect.INHERIT);
			pb.redirectError(Redirect.INHERIT);
			Process p = pb.start();
			//No funciona con los delays por algun motivo
			//Runtime.getRuntime().exec(System.getProperty("user.home")+"/Z/Distribuidos/PractObligatoria/Distribuidos/juntar.sh",delay[1]);//+ delays
			System.out.println("Terminado.");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
