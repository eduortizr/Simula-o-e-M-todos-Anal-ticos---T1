import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Uso: java -cp out Main <modelo.yml>
 * Com 'seeds', roda uma simulação por semente (rndnumbersPerSeed aleatórios cada) e, se houver mais de uma,
 * imprime também a média. Com 'rndnumbers', roda uma vez usando exatamente aqueles números.
 */
public class Main {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("uso: java -cp out Main <modelo.yml>");
            System.exit(1);
        }
        try {
            Modelo modelo = LeitorModelo.ler(Path.of(args[0]));
            List<Resultado> resultados = new ArrayList<>();

            if (!modelo.sementes.isEmpty()) {
                for (int i = 0; i < modelo.sementes.size(); i++) {
                    long semente = modelo.sementes.get(i);
                    GeradorAleatorio rnd = GeradorAleatorio.comSemente(semente, modelo.rndnumbersPerSeed);
                    Resultado r = new Simulador(modelo, rnd).executar();
                    r.imprimir(System.out, "SIMULAÇÃO #" + (i + 1) + " (semente " + semente + ")");
                    resultados.add(r);
                }
                if (resultados.size() > 1) {
                    Resultado.media(resultados).imprimir(System.out,
                            "MÉDIA DE " + resultados.size() + " SIMULAÇÕES");
                }
            } else {
                Resultado r = new Simulador(modelo, GeradorAleatorio.comLista(modelo.rndnumbers)).executar();
                r.imprimir(System.out, "SIMULAÇÃO (rndnumbers do modelo)");
            }
        } catch (Exception e) {
            System.err.println("erro: " + e.getMessage());
            System.exit(1);
        }
    }
}
