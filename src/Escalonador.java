import java.util.PriorityQueue;

// Lista de eventos futuros, sempre devolvendo o de menor tempo. 
class Escalonador {
    private final PriorityQueue<Evento> eventos = new PriorityQueue<>();
    private long contador;

    void agenda(double tempo, Fila origem, Fila destino) {
        eventos.add(new Evento(tempo, contador++, origem, destino));
    }
//Remove e devolve o próximo evento, ou null se não houver nenhum. 
    Evento proximo() {
        return eventos.poll();
    }
}
