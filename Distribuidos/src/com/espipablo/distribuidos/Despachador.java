package com.espipablo.distribuidos;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.Semaphore;

@Path("/despachador")
@Singleton
public class Despachador {

	protected Proceso p1;
	protected Proceso p2;
	protected static final int TOTALPROC = 6;
	protected int maquina;    
	public static final String DEL= ":";
	protected Semaphore semReadyNTP, semReadyStart=new Semaphore(0);
	protected Finalizador finalizador;
	
	// Endpoint encargado de gestionar una petición de acceso a la sección crítica
    // Las delega en el proceso correspondiente
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

    // Endpoint encargado de recibir las respuestas de petición de acceso a la sección crítica
    // Las delega en el proceso correspondiente
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
    public String inicializar(@QueryParam(value="id") int maquina, @QueryParam(value="json") String json, @QueryParam(value="ip1") String ip1, @QueryParam(value="ip2") String ip2, @QueryParam(value="ip3") String ip3) {
				
    	System.out.println("Inicializando la máquina " + maquina);
    	
    	this.maquina = maquina;
    	JSONArray procesos;
    	
    	Fichero fichero = null;
    	if (this.maquina == 0) {
    		// Semáforo que indicará cuando todos los hijos están listos
    		semReadyStart=new Semaphore(0);
    		
    		// Array que usaremos para indicarle a los demás procesos cuáles son todas las IP
    		procesos = new JSONArray();
			procesos.put(ip1);
			procesos.put(ip1);
    		procesos.put(ip2);
    		procesos.put(ip2);
    		procesos.put(ip3);
    		procesos.put(ip3);

    		// Fichero de escritura de log de esta máquina
        	fichero = new Fichero(this.maquina, (String) procesos.get(0), 0, 0);
        	
        	// Hilo encargado de recibir los log de las demás máquinas y escribirlos en esta máquina en local
        	finalizador = new Finalizador(TOTALPROC / 2, procesos);
        	finalizador.start();
    		
        	// Procesos del despachador
            p1 = new Proceso(maquina * 2 + 1, TOTALPROC, fichero, procesos);
            p2 = new Proceso(maquina * 2 + 2, TOTALPROC, fichero, procesos);
            
            // Listo para recibir peticiones

    		try {
    			// Hilos encargados de inicializar las demás máquinas
    			RequestThread r1=new RequestThread("http://" + ip2 + ":8080/Distribuidos/despachador/inicializar?id=1" + "&json=" + URLEncoder.encode(procesos.toString(), "UTF-8"));
				RequestThread r2=new RequestThread("http://" + ip3 + ":8080/Distribuidos/despachador/inicializar?id=2" + "&json=" + URLEncoder.encode(procesos.toString(), "UTF-8"));
				r1.start();
				r2.start();

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
            
    		try {
    			// Esperaremos a que lleguen los 10 NTP de cada máquina antes de iniciar la ejecución del programa
    			// También esperamos a que ambas máquinas nos manden su mensaje de que están listas para ejecutar el algoritmo
    			semReadyStart.acquire(
    					10 * ((TOTALPROC / 2) - 1) // 10 peticiones por máquina menos la principal
    							+ (TOTALPROC / 2 - 1)); // Una petición por máquina menos la principal indicando que está todo listo para empezar
    			
    			// Le indicamos a los hijos que podemos comenzar la ejecución
    			RequestThread r1=new RequestThread("http://" + ip2 + ":8080/Distribuidos/despachador/ready");
    			RequestThread r2=new RequestThread("http://" + ip3 + ":8080/Distribuidos/despachador/ready");
				r1.start();
				r2.start();
    		} catch (InterruptedException e) {
    			e.printStackTrace();
    		}
    		
    	} else {
    		// Procesamos el Array que nos manda la máquina principal
    		procesos = new JSONArray(json);

    		// Ejecutamos NTP
    		System.out.println("Ejecutando NTP.");
        	this.ejecutarNTP((String) procesos.get(0));
        	
        	// Inicializamos el fichero encargado del log en esta máquina
        	fichero = new Fichero(this.maquina, (String) procesos.get(0), NTP.offset, NTP.delay);
        	
        	// Inicializamos los procesos de este despachador
            p1 = new Proceso(maquina * 2 + 1, TOTALPROC, fichero, procesos);
            p2 = new Proceso(maquina * 2 + 2, TOTALPROC, fichero, procesos);
            
            // Le indico a la máquina principal que estoy listo
            Util.request("http://" + (String) procesos.get(0) + ":8080/Distribuidos/despachador/ready");

            try {
				semReadyStart.acquire(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}	
    	}
        
    	// Inicializamos el hilo encargado de gestionar las escrituras en el fichero e inicializamos los procesos
    	fichero.start();
        p1.start();
        p2.start();
        
        return "";   
    }

    // Endpoint encargado de gestionar las peticiones de NTP
	@Path("NTP")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String pedirTiempo(){
		String r;
		long t1,t2;
		
		t1 = System.currentTimeMillis();
		t2 = System.currentTimeMillis();
		
		r = String.valueOf(t1)+DEL+String.valueOf(t2);
		
		System.out.println("Recibo NTP");
		this.semReadyStart.release();
		return r;
	}

	// Endpoint encargado de almacenar los delays de los demás procesos en la máquina principal
	@Path("delay")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getOffset(@QueryParam(value="delay") int delay, @QueryParam(value="id") int maquina) {
		finalizador.delay[maquina] = delay;
		finalizador.semReadyEnd.release();
		return "";
	}
	
	// Endpoint que indicará cuando los procesos están listos para iniciar la ejecución del algoritmo
	@Path("ready")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String ready() {
		semReadyStart.release();
		return "";
	}
	
	// Endpoint que lee el fichero de log de esta máquina y lo devuelve como string
	@Path("fichero")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String readFile() throws IOException  {
		return Util.readFileToString(System.getProperty("user.home")
            		+ File.separator + "tiempos" 
            		+ File.separator 
            		+ this.maquina
            		+ ".log");
	}
	
	public String ejecutarNTP(String s){
		NTP.ntp(s);
		return "";
	}
	
	// Clase encargada de lanzar peticiones http en un hilo nuevo
	private class RequestThread extends Thread {
		private String url;
		
		public RequestThread(String s) {
			this.url = s;
		}
		
		@Override
		public void run() {
			Util.request(this.url);
		}
		
	}

}
