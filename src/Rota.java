/** Roteamento de uma fila para outra (destino == null significa exterior). */
record Rota(Fila destino, double probabilidade) {
}
