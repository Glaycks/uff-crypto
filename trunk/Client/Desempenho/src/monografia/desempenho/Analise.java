package monografia.desempenho;

public class Analise {
	
	private static int posicao = 0;
	private static int[] lista = new int[500];
		
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
