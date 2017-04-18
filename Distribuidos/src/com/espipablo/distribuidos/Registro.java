package com.espipablo.distribuidos;

public class Registro implements Comparable<Registro>{
	public double tiempo;
	public String registro;

	@Override
	public int compareTo(Registro o) {
		// TODO Auto-generated method stub
		return Double.compare(this.tiempo, ((Registro)o).tiempo);
	}
	
	

}
