/**
 * Evento do escalonador. O tipo vem de onde e para onde o cliente vai:
 *   origem == null              -> CHEGADA externa na fila destino
 *   destino == null             -> SAÍDA da fila origem para o exterior
 *   origem e destino preenchidos -> PASSAGEM da origem para o destino
 * "ordem" desempata eventos no mesmo instante (o primeiro agendado é tratado antes).
 */
record Evento(double tempo, long ordem, Fila origem, Fila destino) implements Comparable<Evento> {

    @Override
    public int compareTo(Evento outro) {
        int c = Double.compare(tempo, outro.tempo);
        return c != 0 ? c : Long.compare(ordem, outro.ordem);
    }
}
