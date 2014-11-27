package pkgAscensor;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;

public class Persona extends Thread {
	
	private Random r = new Random();
	private int pasear;
	private int maxPaseo = 2;
	private int pisoActual;
	private int numeroDePisos = -1;
	private boolean modo;
	private int[] datosFichero;
	
	private static int semilla1, semilla2;
	
	Persona(boolean mode, int[] datos){
		this.datosFichero = datos;
		pisoActual = 0;
		modo = mode;
	}
	
	Persona(boolean mode, int paseos, int numPisos, int tiempoSimula){
		pisoActual = 0;
		maxPaseo = paseos;
		Calendar fecha = new GregorianCalendar();
		int contador = (fecha.get(Calendar.SECOND)) % 100000;
		semilla1 = 1;
		semilla2 = 1;
		for(int i = 0; i<contador; i++){
			semilla2 = semilla1 + semilla2;
			semilla1 = semilla2 - semilla1;
		}
		numeroDePisos = numPisos;
		modo = mode;
	}
	
	public void run(){
		
		int nuevaPlanta;
		int numPaseos;
		int contador = 1;
		
		if(modo){
			contador--;
			try{
				while(!Control.fin && contador < datosFichero.length){
					System.out.println("Voy a darme mi paseo numero "+(contador+1)+" del dia");
					pasear = datosFichero[contador];
					Thread.sleep(pasear*1000);
					nuevaPlanta = datosFichero[contador+1];
					Control.llamadaPiso(pisoActual);
					Ascensor.personasPiso[pisoActual].acquire();
					Thread.sleep(1000);
					Control.pulsarBoton(nuevaPlanta);
					Ascensor.personasAscensor[nuevaPlanta].acquire();
					pisoActual = nuevaPlanta;
					contador+=2;
				}
			} catch(InterruptedException ie){ }
		}
		else{
			while(!Control.fin){
				try{
					pasear = 1000 + r.nextInt(1000*maxPaseo);
					Thread.sleep(pasear);
					nuevaPlanta = plantaDestino();
					Control.llamadaPiso(pisoActual);
					Ascensor.personasPiso[pisoActual].acquire();
					Thread.sleep(1000);
					Control.pulsarBoton(nuevaPlanta);
					Ascensor.personasAscensor[nuevaPlanta].acquire();
					pisoActual = nuevaPlanta;
					contador++;
				} catch(InterruptedException ie){ 
					System.out.println("Fallo en run() de Persona"); 
				}
			}
		}
		Control.contador++;
		System.out.println("Personas "+Thread.currentThread().getId()+" adios!!");
	}
	
	private int plantaDestino(){
		int numeroAleatorio;
		do{
			numeroAleatorio = semilla2 % numeroDePisos;
			semilla2 += semilla1;
			semilla1 = semilla2 - semilla1;
		}while(numeroAleatorio == pisoActual);
		
		return numeroAleatorio;
	}
}
