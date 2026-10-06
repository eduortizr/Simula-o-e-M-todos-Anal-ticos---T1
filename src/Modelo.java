import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Tudo o que vem do .yml. 
class Modelo {
    final Map<String, Fila> filas = new LinkedHashMap<>();
    final Map<Fila, Double> chegadas = new LinkedHashMap<>(); // fila -> instante da 1ª chegada externa
    final List<Double> rndnumbers = new ArrayList<>();
    final List<Long> sementes = new ArrayList<>();
    int rndnumbersPerSeed = 100000;

    Fila fila(String nome) {
        return filas.computeIfAbsent(nome, Fila::new);
    }
}
