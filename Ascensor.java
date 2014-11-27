package pkgAscensor;

import java.util.concurrent.Semaphore;

public class Ascensor extends Thread {
	
	private volatile static int numPisos;
	private volatile static int tiempoSimulacion;
	private volatile static int numPersonas;
	
	public static int pisoActual = 0;
	public static boolean subir = true;
	public static boolean bajar = false;
	public static boolean activo = false;
	
	public static Semaphore[] personasPiso;
	public static Semaphore[] personasAscensor;
	
	Ascensor(int numPisos, int tiempoSimula, int personas) {
		this.numPisos = numPisos;
		numPersonas = personas;
		personasPiso = new Semaphore[numPisos];
		personasAscensor = new Semaphore[numPisos];
		tiempoSimulacion = tiempoSimula;
		for(int i = 0; i < numPisos; i++){
			personasPiso[i] = new Semaphore(0,true);
			personasAscensor[i] = new Semaphore(0,true);
		}
	}
	
	public void run() {
		
		long tiempo = System.currentTimeMillis();
		long tiempo2, tiempoEjecucion;
		
		try {
			Control.llamada.acquire(); //se queda dormido si no hay llamadas
			System.out.println("Ascensor: Abro las puertas");
			abrirPuerta();
			
			while(true){
				Control.llamada.acquire();
				
				if(Control.llamadasPiso[pisoActual]){
					System.out.println("Ascensor "+pisoActual+" : Abro las puertas");
					abrirPuerta();
				}
				else{
					selSentido();
				
					while(subir || bajar){
						if(subir){
							subirPlanta();
							System.out.println("Ascensor: Subo una planta");
						}
						if(bajar){
							bajarPlanta();
							System.out.println("Ascensor: Bajo una planta");
						}

						Control.mutex.acquire();
						if(Control.llamadasPiso[pisoActual]){
							Control.mutex.release();
							abrirPuerta();
						}
						else
							Control.mutex.release();
						
						selSentido();
					}
				}
				tiempo2 = System.currentTimeMillis();
				tiempoEjecucion = (tiempo2-tiempo)/1000;
				if( ((tiempo2-tiempo)%1000) >= 500 )
					tiempoEjecucion++;
				if(tiempoEjecucion >= tiempoSimulacion)
					break;
			}
			
		} catch (InterruptedException e) { System.out.println("Fallo try en ascensor"); }
		while(Control.contador < numPersonas);
		System.out.println("Ascensor: Adios!");
	}
	
	private static void abrirPuerta() throws InterruptedException {
		Thread.sleep(1000);
		while(Control.personasEnPlanta[pisoActual] > 0){
			Control.mutex.acquire();
			personasPiso[pisoActual].release();
			Control.personasEnPlanta[pisoActual]--;
			System.out.println("Ascensor: Dejo pasar a las personas en la planta "+pisoActual);
			Control.llamadasPiso[pisoActual] = false;
			Control.mutex.release();
		}
		while(Control.personasEnAscensor[pisoActual] > 0){
			Control.mutex.acquire();
			personasAscensor[pisoActual].release();
			Control.personasEnAscensor[pisoActual]--;
			System.out.println("Ascensor: Dejo bajar a las personas en la planta "+pisoActual);
			Control.llamadasPiso[pisoActual] = false;
			Control.mutex.release();
		}
	}
	private static void subirPlanta() throws InterruptedException {
		Control.mutex.acquire();
		pisoActual++;
		if(pisoActual == Control.maxPiso)
			cambiarSentido();
		Control.mutex.release();
		Thread.sleep(1000);
	}
	private static void bajarPlanta() throws InterruptedException {
		Control.mutex.acquire();
		pisoActual--;
		if(pisoActual == Control.maxPiso)
			cambiarSentido();
		Control.mutex.release();
		Thread.sleep(1000);
	}
	private static void cambiarSentido() {
		if(subir){
			subir = false;
			bajar = true;
		}
		else{
			if(bajar){
				subir = true;
				bajar = false;
			}
		}
	}
	private static void selSentido() throws InterruptedException {
		int control;
		
		Control.mutex.acquire();
		for(control = 0; control < numPisos; control++){
			if(Control.llamadasPiso[control] || Control.personasEnAscensor[control] > 0)
				break;
		}
			
		if(control < pisoActual){
			subir = false;
			bajar = true;
		}
		else{
			if(control < numPisos){
				subir = true;
				bajar = false;
			}
			else{
				subir = false;
				bajar = false;
			}
		}
		Control.mutex.release();
	}
}