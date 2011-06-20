package monografia.desempenho;

import java.util.HashMap;

public class Analise {
	
	private Analise(){
		// previnir instanciação errada
	}
	
	private static int posicao = 0;
	private static int[] lista = new int[500];
	//private HashMap<Integer, Tempos> tempos;
	
		
	public static void acumulaValor(final int valor){
		
		lista[posicao] = valor;
		posicao++;		
		
	}
	
	public static int retornaMedia(){
		
		int total = 0;
		
		for(int i=0; i<lista.length; i++){
			total = total + lista[i];
		}
		
		return (total / lista.length);
	}
}
