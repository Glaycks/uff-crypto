package monografia.desempenho;

public class Analise {

    private static int posicao = 0;
    private static long[] lista = new long[500];

    public static void acumulaValor(final long valor) {

        Analise.lista[Analise.posicao] = valor;
        Analise.posicao++;

    }

    public static double retornaMedia() {

        long total = 0;

        for (int i = 0; i < Analise.lista.length; i++) {
            total = total + Analise.lista[i];
        }

        return total / Analise.lista.length;
    }
}
