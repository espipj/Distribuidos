package com.espipablo.distribuidos;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Fichero {

	protected int maquina;
	protected File file;
	
	Fichero(int maquina) {
		this.maquina = maquina;
		this.file = new File(maquina + ".log");
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
}
