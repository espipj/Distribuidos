package com.espipablo.distribuidos;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;

@Path("/despachador")
@Singleton
public class Despachador {

	protected Proceso p1;
	protected Proceso p2;
	protected static final int TOTALPROC = 4;
	protected int maquina;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/peticion")
    public String peticion(@QueryParam(value="id") int id, @QueryParam(value="from") int from, @QueryParam(value="tj") int tj) {    	
        if (id == 1) {
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
        if (id == 1) {
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
    	System.out.println("Inicializando la m�quina " + maquina);
    	
    	this.maquina = maquina;
    	JSONArray procesos;
    	
    	Fichero fichero = new Fichero(this.maquina);
    	
    	if (this.maquina == 0) {
    		procesos = new JSONArray();
    		try {
				procesos.put(InetAddress.getLocalHost());
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "Error en la inicialización del proceso principal";
			}
    		procesos.put(ip2);
    		//procesos.put(ip3);
    		// URLEncoder.encode(query, "UTF-8");

    		Util.request("http://" + ip2 + ":8080/Distribuidos/despachador/inicializar?id=" + 1 + "&json=" + procesos.toString());
    		//Util.request("http://" + ip3 + ":8080/Distribuidos/despachador/inicializar?id=" + 2 + "&json=" + procesos.toString());
    	} else {
    		procesos = new JSONArray(json);
    	}
    	
        p1 = new Proceso(maquina * 2 + 1, TOTALPROC, fichero, procesos);
        p2 = new Proceso(maquina * 2 + 2, TOTALPROC, fichero, procesos);
        
        p1.start();
        p2.start();
        
        return "";
    }

}
