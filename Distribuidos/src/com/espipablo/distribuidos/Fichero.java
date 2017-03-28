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
	protected String url;
	public long offset;
	public long delay;
	
	Fichero(int maquina, String url, long offset, long delay) {
		this.maquina = maquina;
		this.file = new File(filePath(maquina+".log"));
		System.out.println(this.file.getAbsolutePath());
		this.semFinalRegistro = new Semaphore(0);
    	this.registros=new ArrayList<Registro>();
    	this.url = url;
    	this.offset = offset;
    	this.delay = delay;
	}
	
	public void run() {
        // Meter esto en fichero y que extienda de Thread
        try {
			semFinalRegistro.acquire(400);
			
			if (this.maquina != 0)
			{
				NTP.ntp(url);
				/*NTP.offset/delay son offset y delay calculados al final, en this.offset/delay estan los iniciales
				 * (pasados en la inicialización del fichero)*/
				this.corregirTiempos(NTP.offset, NTP.delay);
			}
			
			Collections.sort(registros);
			/*for (Registro registro : registros) {
				System.out.println(registro.registro + registro.tiempo);
			}*/
			escribirRegistros();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void escribirRegistros() {
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter(this.file));
			for (Registro registro : registros) {
					bw.write(registro.registro + " " + registro.tiempo + "\n");
			}
		    bw.close();
		    
		    Util.request("http://" + url + ":8080/Distribuidos/despachador/offset?id=" + this.maquina + "&offset=" + this.offset);
		} catch (IOException e) {
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
	
	public String filePath(String n) {
        String filepath;
        if (!System.getProperty("os.name").toLowerCase().contains("linux")) {
            filepath = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "tiempos" + File.separator + n;
            String x = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "tiempos";
            File f = new File(x);
            f.mkdir();
        } else {
            filepath = System.getProperty("user.home") + File.separator + "Escritorio" + File.separator + "tiempos" + File.separator + n;
            String x = System.getProperty("user.home") + File.separator + "Escritorio" + File.separator + "tiempos";
            File f = new File(x);
            f.mkdir();
        }
        return filepath;
    }
	


	public void corregirTiempos(long o1, long d1) {
		this.offset = (this.offset + o1) / 2;
		this.delay = (this.delay + d1) / 2;
		for (Registro registro : registros) {
			registro.tiempo+=this.delay;
		}

	}
}
