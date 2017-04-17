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
	public long[] delay;
	
	Finalizador(int total, JSONArray procesos) {
		this.semReadyEnd = new Semaphore(0);
		this.procesos = procesos;
		this.totalMaquinas = total;
		this.delay = new long[3];
	}
	
	public void run() {
		
		try {
			System.out.println(totalMaquinas);
			semReadyEnd.acquire(totalMaquinas);
			
			for (int i=1; i < procesos.length(); i++) {
				BufferedWriter bw;
				bw = new BufferedWriter(new FileWriter(new File(System.getProperty("user.home")
	            		+ File.separator + "tiempos" 
	            		+ File.separator
	            		+ i
	            		+ ".log")));
				bw.write(Util.request("http://" + procesos.getString(i) + ":8080/Distribuidos/despachador/fichero"));
			    bw.close();
			}
			
			System.out.println("Ejecutando... ");
			System.out.println(String.valueOf(delay[1]));
			System.out.println(String.valueOf(delay[2]));
			
			String[] CMD_ARRAY=new String[]
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
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
