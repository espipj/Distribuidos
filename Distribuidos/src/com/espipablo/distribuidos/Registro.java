package com.espipablo.distribuidos;

import java.util.Comparator;

public class Registro implements Comparable<Registro>{
	public long tiempo;
	public String registro;

	@Override
	public int compareTo(Registro o) {
		// TODO Auto-generated method stub
		return Long.compare(this.tiempo, ((Registro)o).tiempo);
	}
	
	

}
