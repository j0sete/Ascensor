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
				//System.out.println("Ha dormir un poquito");
				Control.llamada.acquire();
				//System.out.println("Alguien necesita de mis servicios!");
				
				if(Control.llamadasPiso[pisoActual]){
					System.out.println("Ascensor "+pisoActual+" : Abro las puertas");
					abrirPuerta();
				}
				else{
					Control.llamada.acquire();
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
						
						cambiarSentido();
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
	private static void cambiarSentido() throws InterruptedException {
		boolean ok = true;
		
		if(subir){
			Control.mutex.acquire();
			for(int i = (numPisos-1); i > pisoActual; i--){
				if(Control.llamadasPiso[i]){
					subir = true;
					ok = false;
					break;
				}
			}
			Control.mutex.release();
			
		}
		else{
			Control.mutex.acquire();
			for(int j = 0; j < pisoActual; j++){
				if(Control.llamadasPiso[j]){
					bajar = true;
					ok = false;
					break;
				}
			}
			Control.mutex.release();
		}
		if(ok){
			subir = false;
			bajar = false;
		}
	}
	private static void selSentido() throws InterruptedException {
		int control = -1;
		
		if(pisoActual == 0 || pisoActual == (numPisos-1)){
			if(pisoActual == 0){
				subir = true;
				bajar = false;
			}
			else{
				subir = false;
				bajar = true;
			}
		}
		else{
			Control.mutex.acquire();
			for(int i = 0; i < numPisos;i++){
				if(Control.llamadasPiso[i]){
					control = i;
					break;
				}
			}
			Control.mutex.release();
			if(control < pisoActual && control != -1){
				bajar = true;
				subir = false;
			}
			else{
				if(control != -1){
					bajar = false;
					subir = true;
				}
				else{
					subir = false;
					bajar = false;
				}
			}
		}
		
	}
}