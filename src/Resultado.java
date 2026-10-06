import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Cópia das estatísticas ao fim de uma simulação (ou a média de várias) e a impressão do relatório. */
class Resultado {
    private static final Locale BR = Locale.forLanguageTag("pt-BR");

    record FilaResultado(String nome, String notacao, String chegada, String atendimento,
                         double[] tempos, double perdas) {
    }

    final List<FilaResultado> filas;
    final double tempoGlobal;
    final int aleatorios;

    private Resultado(List<FilaResultado> filas, double tempoGlobal, int aleatorios) {
        this.filas = filas;
        this.tempoGlobal = tempoGlobal;
        this.aleatorios = aleatorios;
    }

    static Resultado de(Modelo m, double tempoGlobal, int aleatorios) {
        List<FilaResultado> filas = new ArrayList<>();
        for (Fila f : m.filas.values()) {
            // fila finita mostra todos os estados 0..K, infinita mostra até o maior estado visitado
            int estados = f.capacidade == Fila.INFINITA ? f.tempos.size() : Math.max(f.tempos.size(), f.capacidade + 1);
            double[] tempos = new double[estados];
            for (int i = 0; i < f.tempos.size(); i++) tempos[i] = f.tempos.get(i);

            String chegada = m.chegadas.containsKey(f) ? intervalo(f.minChegada, f.maxChegada) : null;
            filas.add(new FilaResultado(f.nome, f.notacao(), chegada,
                    intervalo(f.minAtendimento, f.maxAtendimento), tempos, f.perdas));
        }
        return new Resultado(filas, tempoGlobal, aleatorios);
    }

    /** Média de várias execuções (uma por semente), estado a estado. */
    static Resultado media(List<Resultado> execucoes) {
        int n = execucoes.size();
        List<FilaResultado> filas = new ArrayList<>();
        for (int q = 0; q < execucoes.get(0).filas.size(); q++) {
            FilaResultado base = execucoes.get(0).filas.get(q);
            int estados = 0;
            for (Resultado r : execucoes) estados = Math.max(estados, r.filas.get(q).tempos.length);
            double[] tempos = new double[estados];
            double perdas = 0;
            for (Resultado r : execucoes) {
                FilaResultado f = r.filas.get(q);
                for (int i = 0; i < f.tempos.length; i++) tempos[i] += f.tempos[i] / n;
                perdas += f.perdas / n;
            }
            filas.add(new FilaResultado(base.nome, base.notacao, base.chegada, base.atendimento, tempos, perdas));
        }
        double tempo = execucoes.stream().mapToDouble(r -> r.tempoGlobal).average().orElse(0);
        int aleatorios = (int) execucoes.stream().mapToInt(r -> r.aleatorios).average().orElse(0);
        return new Resultado(filas, tempo, aleatorios);
    }

    void imprimir(PrintStream out, String titulo) {
        String linha = "=".repeat(60);
        out.println(linha);
        out.println(titulo);
        out.println(linha);
        for (FilaResultado f : filas) {
            out.println();
            out.printf("Fila %s (%s)%n", f.nome, f.notacao);
            if (f.chegada != null) out.println("Chegadas:    " + f.chegada);
            out.println("Atendimento: " + f.atendimento);
            out.printf("%8s %18s %15s%n", "Estado", "Tempo", "Probabilidade");
            for (int i = 0; i < f.tempos.length; i++) {
                double prob = tempoGlobal > 0 ? 100 * f.tempos[i] / tempoGlobal : 0;
                out.printf(BR, "%8d %18.4f %14.2f%%%n", i, f.tempos[i], prob);
            }
            out.println("Perdas: " + numero(f.perdas));
        }
        out.println();
        out.printf(BR, "Tempo global da simulação: %.4f%n", tempoGlobal);
        out.println("Aleatórios usados: " + aleatorios);
        out.println();
    }

    private static String intervalo(double min, double max) {
        return numero(min) + " .. " + numero(max);
    }

    private static String numero(double v) {
        return v == Math.rint(v) ? String.valueOf((long) v) : String.format(BR, "%.2f", v);
    }
}
