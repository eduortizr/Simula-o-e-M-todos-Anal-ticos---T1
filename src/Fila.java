import java.util.ArrayList;
import java.util.List;

// Uma fila G/G/c/K do modelo: parâmetros, rotas de saída, estado atual e estatísticas. 
public class Fila {
    public static final int INFINITA = Integer.MAX_VALUE;

    final String nome;
    int servidores = 1;
    int capacidade = INFINITA;
    double minChegada = -1, maxChegada = -1;   // só  sao usados se a fila recebe chegadas externas
    double minAtendimento = -1, maxAtendimento = -1;
    final List<Rota> rotas = new ArrayList<>(); // o que falta para somar 1 vai para o exterior

    // durante a simulação
    int clientes;
    int perdas;
    final List<Double> tempos = new ArrayList<>(); // tempos.get(i) = tempo acumulado com i clientes

    Fila(String nome) {
        this.nome = nome;
    }

    void reiniciar() {
        clientes = 0;
        perdas = 0;
        tempos.clear();
    }

    // Soma delta ao tempo do estado atual (a lista cresce sob demanda, por causa das filas infinitas).
    void acumulaTempo(double delta) {
        while (tempos.size() <= clientes) tempos.add(0.0);
        tempos.set(clientes, tempos.get(clientes) + delta);
    }

    String notacao() {
        return "G/G/" + servidores + (capacidade == INFINITA ? "" : "/" + capacidade);
    }
}
