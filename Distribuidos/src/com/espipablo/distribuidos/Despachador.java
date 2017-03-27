package com.espipablo.distribuidos;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Semaphore;

@Path("/despachador")
@Singleton
public class Despachador implements ControladorRegistro {

	protected Proceso p1;
	protected Proceso p2;
	protected static final int TOTALPROC = 4;
	protected int maquina;    
	public static final String DEL= ":";
	protected Semaphore semNTP,semFinalRegistro;
	protected ArrayList<Registro> registros;
	

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/peticion")
    public String peticion(@QueryParam(value="id") int id, @QueryParam(value="from") int from, @QueryParam(value="tj") int tj) {    	
        if (id % 2 == 1) {
        	p1.recibirPeticion(tj, from);
        } else {
        	p2.recibirPeticion(tj, from);        	
        }
        
        return "";
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/respuesta")
    public String respuesta(@QueryParam(value="id") int id) {    	
        if (id % 2 == 1) {
        	p1.recibirRespuesta();
        } else {
        	p2.recibirRespuesta();       	
        }
        
        return "";
        
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/inicializar")
    public String inicializar(@QueryParam(value="id") int maquina, @QueryParam(value="json") String json, @QueryParam(value="ip2") String ip2, @QueryParam(value="ip3") String ip3) {
    	registros=new ArrayList<Registro>();
    	semFinalRegistro=new Semaphore(0);
    	System.out.println("Inicializando la máquina " + maquina);
    	
    	this.maquina = maquina;
    	JSONArray procesos;
    	
    	Fichero fichero = new Fichero(this.maquina);
    	
    	if (this.maquina == 0) {
    		procesos = new JSONArray();
    		try {
				procesos.put(InetAddress.getLocalHost().getHostAddress());
				procesos.put(InetAddress.getLocalHost().getHostAddress());
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "Error en la inicialización del proceso principal";
			}
    		procesos.put(ip2);
    		procesos.put(ip2);
    		//procesos.put(ip3);

    		try {
				Util.request("http://" + ip2 + ":8080/Distribuidos/despachador/inicializar?id=" + 1 + "&json=" + 
						URLEncoder.encode(procesos.toString(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		//Util.request("http://" + ip3 + ":8080/Distribuidos/despachador/inicializar?id=" + 2 + "&json=" + procesos.toString());
    		
    		try {
    			semNTP=new Semaphore(0);
    			semNTP.acquire(10*((TOTALPROC/2)-1));
    		} catch (InterruptedException e1) {
    			// TODO Auto-generated catch block
    			e1.printStackTrace();
    		}
    		
    	} else {
    		procesos = new JSONArray(json);
    		System.out.println("Ejecutando NTP.");
        	this.ejecutarNTP((String) procesos.get(0));
        	
        	
        	
    	}
    	
        p1 = new Proceso(maquina * 2 + 1, TOTALPROC, fichero, procesos,this);
        p2 = new Proceso(maquina * 2 + 2, TOTALPROC, fichero, procesos,this);
        
        p1.start();
        p2.start();
        
        try {
			semFinalRegistro.acquire(400);
			Collections.sort(registros);
			for (Registro registro : registros) {
				System.out.println(registro.registro + registro.tiempo);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return "";   
    }
    

	@Path("NTP")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String pedirTiempo(){
		String r;
		long t1,t2;
		t1=System.currentTimeMillis();
		Random rn = new Random();
		t2=System.currentTimeMillis();
		r=String.valueOf(t1)+DEL+String.valueOf(t2);
		return r;
	}
	
	@Path("EjecutarNTP")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String ejecutarNTP(String s){
		NTP.ntp(s);
		
		return "";
		
	}

	@Override
	public void anadirRegistro(String s, long l) {
		semFinalRegistro.release();
		Registro r = new Registro();
		r.registro=s;
		r.tiempo=l;
		// TODO Auto-generated method stub
		synchronized (this) {
			registros.add(r);
			
		}
		
	}

}
