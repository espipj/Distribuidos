package com.espipablo.distribuidos;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.concurrent.Semaphore;

import org.json.JSONArray;

public class Finalizador extends Thread {
	
	public Semaphore semReadyEnd;
	protected int totalMaquinas;
	protected JSONArray procesos;
	public double[] delay;
	
	Finalizador(int total, JSONArray procesos) {
		this.semReadyEnd = new Semaphore(0);
		this.procesos = procesos;
		this.totalMaquinas = total;
		this.delay = new double[3];
	}
	
	public void run() {
		try {
			System.out.println(totalMaquinas);
			// Esperamos a que todas las máquinas hayan escrito en memoria su log
			semReadyEnd.acquire(totalMaquinas);
			
			// Hacemos una petición GET para coger el fichero de log de las demás máquinas (no principales)
			for (int i=2; i < procesos.length(); i+=2) {
				JSONArray tiempos = new JSONArray(Util.request("http://" + procesos.getString(i) + ":8080/Distribuidos/despachador/fichero"));
				
				BufferedWriter bw;
				bw = new BufferedWriter(new FileWriter(new File(System.getProperty("user.home")
						 	            		+ File.separator + "tiempos" 
						 	            		+ File.separator
						 	            		+ i/2
						 	            		+ ".log")));
				
				// Escribimos los logs en local para tratarlos de forma más sencilla
				for (int j=0; j < tiempos.length(); j++) {
					bw.write(tiempos.getString(j));
					bw.newLine();
				}
			    bw.close();
			}
			
			System.out.println("Ejecutando comprobador... ");
			/*System.out.println(String.valueOf(delay[1]));
			System.out.println(String.valueOf(delay[2]));*/
			
			String[] CMD_ARRAY = new String[]
					{
						System.getProperty("user.home")+"/Z/Distribuidos/PractObligatoria/Distribuidos/juntar.sh"
						, String.valueOf(delay[1])
						, String.valueOf(delay[2])
					};
			ProcessBuilder pb = new ProcessBuilder(CMD_ARRAY);
			pb.redirectOutput(Redirect.INHERIT);
			pb.redirectError(Redirect.INHERIT);
			pb.start();
			
			System.out.println("Terminado.");
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
	}

}
