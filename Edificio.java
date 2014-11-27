package pkgAscensor;

import java.util.Scanner;

public class Edificio {
	
	private static volatile int numPisos, numPersonas, maxPaseo, tiempoSimula;
	private static volatile boolean esDeterminista;
	
	private static volatile String fichero;

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		ayuda();
		datos();
		mostrarDatos();
		
		Control control = new Control(numPisos, tiempoSimula, numPersonas);
		Ascensor ascensor = new Ascensor(numPisos, tiempoSimula, numPersonas);
		
		control.start();
		ascensor.start();
		
		for(int i = 0; i < numPersonas; i++){
			Persona p = new Persona(esDeterminista, maxPaseo, numPisos, tiempoSimula, fichero, i);
			p.start();
		}
		
		try {
			control.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ascensor.interrupt();
		
	}
	
	private static void ayuda(){
		System.out.println("--------------------------------------------------------------------------------------");
		System.out.println("Programa control ascensor:");
		System.out.println("Autor: Jose Manuel Rodriguez Montes\n");
		System.out.println("Numero de pisos tiene que ser mayor que 0");
		System.out.println("Numero de personas tiene que ser mayor que 0");
		System.out.println("Los paseos tiene que ser mayor que 0");
		System.out.println("El tiempo de simulacion tiene que ser mayor que 0");
		System.out.println("El último valor será 1 si queremos que le programa sea determinista y 0 para aleatorio");
		System.out.println("Si no se introducen los datos correctos se le pedirán de nuevo");
		System.out.println("--------------------------------------------------------------------------------------");
	}
	
	private static void datos(){
		
		Scanner sc = new Scanner(System.in);
		boolean error = false;
		
		System.out.println("Introduzca el numero de pisos:");
		do{
			numPisos = sc.nextInt();
			if(numPisos <= 1)
				System.out.println("Dato erróneo, intente de nuevo:");
		}while(numPisos <= 1);
		sc.nextLine();
		
		System.out.println("Introduzca el numero de personas:");
		do{
			numPersonas = sc.nextInt();
			if(numPersonas <= 0)
				System.out.println("Dato erróneo, intente de nuevo:");
		}while(numPersonas <= 0);
		sc.nextLine();
		
		System.out.println("Introduzca el tiempo, en seg. que estarán paseando como máximo las personas");
		do{
			maxPaseo = sc.nextInt();
			if(maxPaseo <= 0)
				System.out.println("Dato erróneo, intente de nuevo:");
		}while(maxPaseo <= 0);
		sc.nextLine();
		
		System.out.println("Tiempo de simulacion en seg.:");
		do{
			tiempoSimula = sc.nextInt();
			if(tiempoSimula <= 0)
				System.out.println("Dato erróneo, intente de nuevo:");
		}while(tiempoSimula <= 0);
		sc.nextLine();
		
		System.out.println("Forma de ejecucion: 1 -> determinista");
		System.out.println("                    0 -> aleatorio");
		do{
			switch(sc.nextInt()){
			case 0:
				esDeterminista = false;
				error = false;
				break;
			case 1:
				esDeterminista = true;
				error = false;
				break;
			default:
				error = true;
				System.out.println("Dato erróneo, intente de nuevo");
			}
		}while(error);
		sc.nextLine();
		if(esDeterminista){
			System.out.println("Escriba el path:");
			fichero = sc.nextLine();
			System.out.println("Fichero: "+fichero);
		}
	}
	
	private static void mostrarDatos(){
		System.out.println("Numero de plantas que tiene el edificio = " + numPisos);
		System.out.println("Numero de trabajadores en el edificio = " + numPersonas);
		System.out.println("Los paseos tienen una duracion de = " + maxPaseo + " segundos");
		System.out.println("El tiempo se dimulacion será de = " + tiempoSimula + " segundos");
		if(esDeterminista)
			System.out.println("El programa es determinista\n");
		else
			System.out.println("El programa no es determinista\n");
	}

}
