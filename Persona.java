package pkgAscensor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.Scanner;

public class Persona extends Thread {
	
	private Random r = new Random();
	private int pasear;
	private int maxPaseo = 2;
	private int pisoActual;
	private final int numeroDePisos;
	private static int tiempoSimulacion;
	private static volatile int saltos;
	private static boolean modo;
	private static volatile String fichero;
	
	private static int semilla;
	
	Persona(boolean mode, int paseos, int numPisos, int tiempoSimula, String fichero, int linea){
		pisoActual = 0;
		this.fichero = fichero;
		saltos = linea;
		maxPaseo = paseos;
		Calendar fecha = new GregorianCalendar();
		semilla = (fecha.get(Calendar.MINUTE) + fecha.get(Calendar.SECOND) + fecha.get(Calendar.MILLISECOND)) % 100000;
		numeroDePisos = numPisos;
		modo = mode;
		tiempoSimulacion = tiempoSimula;
	}
	
	public void run(){
		
		int nuevaPlanta;
		int numPaseos;
		int i = 0;
		int contador = 1;
		Scanner sc = null;
		
		if(modo){
			try{
				sc = new Scanner(new File(fichero));
				sc.nextLine();
				while(i < saltos){
					sc.nextLine();
					i++;
				}
				numPaseos = sc.nextInt();
				while(!Control.fin && contador <= numPaseos){
					System.out.println("Voy a darme mi paseo numero "+contador+" del dia");
					pasear = sc.nextInt();
					Thread.sleep(pasear);
					nuevaPlanta = sc.nextInt();
					Control.llamadaPiso(pisoActual);
					Ascensor.personasPiso[pisoActual].acquire();
					Thread.sleep(1000);
					System.out.println("Persona = "+nuevaPlanta);
					Control.pulsarBoton(nuevaPlanta);
					Ascensor.personasAscensor[nuevaPlanta].acquire();
					pisoActual = nuevaPlanta;
					contador++;
				}
			} catch(InterruptedException ie){ } catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
			numeroAleatorio = semilla % numeroDePisos;
			semilla = (semilla+1) % 100000;
		}while(numeroAleatorio == pisoActual);
		
		return numeroAleatorio;
	}
}
