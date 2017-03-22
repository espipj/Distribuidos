package com.espipablo.distribuidos;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Calendar;
import java.util.Date;

@Path("/despachador")
@Singleton
public class Despachador {

	protected Proceso p1;
	protected Proceso p2;
	protected int maquina;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/peticion")
    public String peticion(@QueryParam(value="id") int id, @QueryParam(value="tj") int tj) {    	
        if (id == 1) {
        	p1.recibirPeticion(tj, id);
        } else {
        	p2.recibirPeticion(tj, id);        	
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
    public String inicializar(@QueryParam(value="id") int maquina) {
    	System.out.println("Inicializando la máquina " + maquina);
    	
    	this.maquina = maquina;
    	
    	Fichero fichero = new Fichero(this.maquina);
    	
    	String[] procesos = new String[3];
    	procesos[1] = "localhost";
    	procesos[2] = "localhost";
    	
        p1 = new Proceso(1, 2, fichero, procesos);
        p2 = new Proceso(2, 2, fichero, procesos);
        
        p1.start();
        p2.start();
        
        return "";
    }

}
