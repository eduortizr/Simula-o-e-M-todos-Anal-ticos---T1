import java.util.List;

/**
 * Fonte dos números pseudoaleatórios em [0, 1), com um limite de quantos podem ser usados.
 * Ou gera pelo método congruente linear (do M2), a partir de uma semente,
 * ou devolve uma lista fixa de números (opção "rndnumbers" do modelo).
 */
class GeradorAleatorio {
    // X(n+1) = (a * X(n) + c) mod M — parametros do Numerical Recipes
    private static final long A = 1664525L;
    private static final long C = 1013904223L;
    private static final long M = 1L << 32;

    private long x;
    private final List<Double> fixos;
    private final int limite;
    private int usados;

    private GeradorAleatorio(long semente, List<Double> fixos, int limite) {
        this.x = semente;
        this.fixos = fixos;
        this.limite = limite;
    }

    static GeradorAleatorio comSemente(long semente, int limite) {
        return new GeradorAleatorio(semente, null, limite);
    }

    static GeradorAleatorio comLista(List<Double> numeros) {
        return new GeradorAleatorio(0, numeros, numeros.size());
    }

    boolean temProximo() {
        return usados < limite;
    }

    int usados() {
        return usados;
    }

    double proximo() {
        if (!temProximo()) throw new IllegalStateException("acabaram os números aleatórios");
        usados++;
        if (fixos != null) return fixos.get(usados - 1);
        x = (A * x + C) % M;
        return (double) x / M;
    }

    /** Valor uniforme entre a e b: a + (b - a) * U(0,1). */
    double uniforme(double a, double b) {
        return a + (b - a) * proximo();
    }
}
