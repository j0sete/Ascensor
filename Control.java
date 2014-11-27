package pkgAscensor;

import java.util.concurrent.Semaphore;

public class Control extends Thread {
	
	private static volatile int tiempoSimulacion; 
	private static volatile int numPisos;
	private static volatile int numPersonas;
	
	public static boolean fin = false;
	public static int contador = 0;
	public static int maxPiso = 0;
	
	public static Semaphore llamada = new Semaphore(0, true);
	public static Semaphore mutex = new Semaphore(1);
	
	public static boolean[] llamadasPiso;
	public static volatile int[] personasEnPlanta;
	public static volatile int[] personasEnAscensor;
	
	Control(int numPisos, int tiempoSimula, int personas){
		this.numPisos = numPisos;
		numPersonas = personas;
		tiempoSimulacion = tiempoSimula;
		llamadasPiso = new boolean[numPisos];
		personasEnPlanta = new int[numPisos];
		personasEnAscensor = new int[numPisos];
	}
	
	public void run(){
		long tiempo, tiempo2, tiempoEjecucion;
		tiempo = System.currentTimeMillis();
		
		do{
			tiempo2 = System.currentTimeMillis();
			tiempoEjecucion = (tiempo2-tiempo)/1000;
			if( ((tiempo2-tiempo)%1000) >= 500 )
				tiempoEjecucion++;
		}while(tiempoEjecucion < tiempoSimulacion);
		fin = true;
		
		do{
			for(int i = 0; i < numPisos; i++){
				Ascensor.personasPiso[i].release();
				Ascensor.personasAscensor[i].release();
			}
		}while(contador < numPersonas);
		
		System.out.println("Control: ADIOS!");
	}
	
	public static void llamadaPiso(int piso) {
		try{
			System.out.println("Control: Han llamado del piso "+piso);
			mutex.acquire();
			llamadasPiso[piso] = true;
			personasEnPlanta[piso]++;
			if(piso > Ascensor.pisoActual && piso > maxPiso && Ascensor.subir)
				maxPiso = piso;
			else{
				if(piso < Ascensor.pisoActual && piso < maxPiso && Ascensor.bajar)
					maxPiso = piso;
				else{
					maxPiso=-1;
				}
			}
		}
		catch(InterruptedException ie) { 
			System.out.println("Fallo en try llamaPiso de Control"); 
		}
		llamada.release();
		mutex.release();
	}
	
	public static void pulsarBoton(int piso){
		System.out.println("Control: Han pulsado el boton y quieren ir a la planta "+piso);
		try{
			mutex.acquire();
			llamadasPiso[piso] = true;
			personasEnAscensor[piso]++;
			mutex.release();
			llamada.release();
		}
		catch(InterruptedException ie){
			System.out.println("Fallo en try pulsarBoton");
		}
		llamada.release();
	}
	

}
