package com.espipablo.distribuidos;

public class NTP {
	public static final int REP = 10;
	public static long offset, delay;

	public static void ntp(String s) {
		long d = Long.MAX_VALUE, o = 0;
		for (int i = 0; i < REP; i++) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String aux;
			long t0, t1, t2, t3;
			long auxO, auxD;
			t0 = System.currentTimeMillis();
			aux = Util.request("http://" + s + ":8080/Distribuidos/despachador/NTP");
			System.out.println(aux);
			t3 = System.currentTimeMillis();
			String[] a = aux.split(Despachador.DEL);
			t1 = Long.valueOf(a[0]);
			t2 = Long.valueOf(a[1]);
			auxO = determinarO(t0, t1, t2, t3);
			auxD = determinarD(t0, t1, t2, t3);
			System.out.println("temp Offset: " + auxO + " temp Delay: " + auxD);
			if (auxD < d) {
				d = auxD;
				o = auxO;
			}

		}
		System.out.println("Offset: " + String.valueOf(o) + " Delay: " + String.valueOf(d));
		offset = o;
		delay = d;

	}

	public static long determinarO(long t0, long t1, long t2, long t3) {

		return (((t1 - t0) + (t2 - t3)) / 2);

	}

	public static long determinarD(long t0, long t1, long t2, long t3) {

		return ((t1 - t0) + (t3 - t2));

	}
}
