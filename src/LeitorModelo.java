import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Lê o .yml no mesmo formato do simulador do módulo 3 (simulator.jar).
 * Não usa biblioteca de YAML: o formato é fixo e simples, então e so olhar a seção e a indentação.
 */
class LeitorModelo {

    static Modelo ler(Path arquivo) throws IOException {
        Modelo m = new Modelo();
        Set<String> definidas = new LinkedHashSet<>(); // ordem da seção 'queues'
        String secao = "";
        Fila filaAtual = null;
        Map<String, String> rota = null;

        for (String bruta : Files.readAllLines(arquivo, StandardCharsets.UTF_8)) {
            String linha = semComentario(bruta);
            if (linha.isBlank() || linha.startsWith("!")) continue;
            String txt = linha.trim();

            // linha sem recuo e sem "-" abre uma seção
            if (!Character.isWhitespace(linha.charAt(0)) && !txt.startsWith("-")) {
                adicionaRota(m, rota);
                rota = null;
                String[] kv = chaveValor(txt);
                secao = kv[0];
                if (secao.equals("rndnumbersPerSeed")) m.rndnumbersPerSeed = (int) numero(kv[1]);
                continue;
            }

            switch (secao) {
                case "arrivals" -> {
                    String[] kv = chaveValor(txt);
                    m.chegadas.put(m.fila(kv[0]), numero(kv[1]));
                }
                case "queues" -> {
                    String[] kv = chaveValor(txt);
                    if (kv[1].isEmpty()) {
                        filaAtual = m.fila(kv[0]);
                        definidas.add(kv[0]);
                    } else {
                        parametro(filaAtual, kv[0], kv[1]);
                    }
                }
                case "network" -> {
                    if (txt.startsWith("-")) { // começa uma nova ligação
                        adicionaRota(m, rota);
                        rota = new HashMap<>();
                        txt = txt.substring(1).trim();
                    }
                    String[] kv = chaveValor(txt);
                    rota.put(kv[0], kv[1]);
                }
                case "rndnumbers" -> m.rndnumbers.add(numero(txt.substring(1)));
                case "seeds" -> m.sementes.add((long) numero(txt.substring(1)));
                default -> { } // seção desconhecida: ignora
            }
        }
        adicionaRota(m, rota);
        valida(m, definidas);
        // As faixas do sorteio de roteamento vão da menor probabilidade para a maior (o exterior fica
        // sempre por último), como faz o simulator.jar. Estatisticamente a ordem não importa, mas assim
        // a mesma lista de aleatórios dá exatamente o mesmo resultado nos dois simuladores.
        for (Fila f : m.filas.values()) f.rotas.sort(Comparator.comparingDouble(Rota::probabilidade));

        // relatório na ordem em que as filas foram declaradas em 'queues'
        Map<String, Fila> ordenadas = new LinkedHashMap<>();
        for (String nome : definidas) ordenadas.put(nome, m.filas.get(nome));
        m.filas.clear();
        m.filas.putAll(ordenadas);
        return m;
    }

    private static void parametro(Fila f, String chave, String valor) {
        if (f == null) throw new IllegalArgumentException("parâmetro '" + chave + "' fora de uma fila");
        double v = numero(valor);
        switch (chave) {
            case "servers" -> f.servidores = (int) v;
            case "capacity" -> f.capacidade = (int) v;
            case "minArrival" -> f.minChegada = v;
            case "maxArrival" -> f.maxChegada = v;
            case "minService" -> f.minAtendimento = v;
            case "maxService" -> f.maxAtendimento = v;
            default -> throw new IllegalArgumentException("parâmetro desconhecido na fila " + f.nome + ": " + chave);
        }
    }

    private static void adicionaRota(Modelo m, Map<String, String> rota) {
        if (rota == null) return;
        Fila origem = m.fila(rota.get("source"));
        Fila destino = m.fila(rota.get("target"));
        origem.rotas.add(new Rota(destino, numero(rota.get("probability"))));
    }

    private static void valida(Modelo m, Set<String> definidas) {
        for (Fila f : m.filas.values()) {
            if (!definidas.contains(f.nome)) {
                throw new IllegalArgumentException("fila " + f.nome + " é usada mas não está em 'queues'");
            }
            if (f.minAtendimento < 0 || f.maxAtendimento < f.minAtendimento) {
                throw new IllegalArgumentException("fila " + f.nome + ": minService/maxService inválidos");
            }
            double soma = f.rotas.stream().mapToDouble(Rota::probabilidade).sum();
            if (soma > 1.0 + 1e-9) {
                throw new IllegalArgumentException("fila " + f.nome + ": probabilidades de roteamento somam " + soma);
            }
        }
        for (Fila f : m.chegadas.keySet()) {
            if (f.minChegada < 0 || f.maxChegada < f.minChegada) {
                throw new IllegalArgumentException("fila " + f.nome + " recebe chegadas mas não tem minArrival/maxArrival");
            }
        }
        if (m.chegadas.isEmpty()) throw new IllegalArgumentException("nenhuma chegada externa em 'arrivals'");
        if (m.sementes.isEmpty() && m.rndnumbers.isEmpty()) {
            throw new IllegalArgumentException("informe 'seeds' (com 'rndnumbersPerSeed') ou 'rndnumbers'");
        }
    }

    private static String semComentario(String linha) {
        int i = linha.indexOf('#');
        return i < 0 ? linha : linha.substring(0, i);
    }

    /** "chave: valor" -> {chave, valor}; valor vazio quando a linha só abre um bloco. */
    private static String[] chaveValor(String txt) {
        int i = txt.indexOf(':');
        if (i < 0) throw new IllegalArgumentException("linha inválida no modelo: " + txt);
        return new String[] { txt.substring(0, i).trim(), txt.substring(i + 1).trim() };
    }

    private static double numero(String s) {
        return Double.parseDouble(s.trim().replace(',', '.'));
    }
}
