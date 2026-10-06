class Simulador {
    private final Modelo modelo;
    private final GeradorAleatorio rnd;
    private final Escalonador escalonador = new Escalonador();
    private double tempoGlobal;

    Simulador(Modelo modelo, GeradorAleatorio rnd) {
        this.modelo = modelo;
        this.rnd = rnd;
    }

    Resultado executar() {
        for (Fila f : modelo.filas.values()) f.reiniciar();
        modelo.chegadas.forEach((fila, tempo) -> escalonador.agenda(tempo, null, fila));

        while (rnd.temProximo()) {
            Evento e = escalonador.proximo();
            if (e == null) break;
            acumulaTempo(e.tempo());

            if (e.origem() == null) chegada(e.destino());
            else if (e.destino() == null) saida(e.origem());
            else passagem(e.origem(), e.destino());
        }
        return Resultado.de(modelo, tempoGlobal, rnd.usados());
    }

    private void acumulaTempo(double tempo) {
        double delta = tempo - tempoGlobal;
        for (Fila f : modelo.filas.values()) f.acumulaTempo(delta);
        tempoGlobal = tempo;
    }

    private void chegada(Fila fila) {
        entra(fila);
        if (rnd.temProximo()) {
            escalonador.agenda(tempoGlobal + rnd.uniforme(fila.minChegada, fila.maxChegada), null, fila);
        }
    }

    private void saida(Fila fila) {
        sai(fila);
    }

    private void passagem(Fila origem, Fila destino) {
        sai(origem);
        entra(destino);
    }

    // Cliente chega na fila: entra se houver espaço (e é atendido se houver servidor livre), senão é perdido. 
    private void entra(Fila fila) {
        if (fila.clientes < fila.capacidade) {
            fila.clientes++;
            if (fila.clientes <= fila.servidores) iniciaAtendimento(fila);
        } else {
            fila.perdas++;
        }
    }

    // Cliente termina o atendimento: se há gente esperando, o servidor liberado pega o próximo. 
    private void sai(Fila fila) {
        fila.clientes--;
        if (fila.clientes >= fila.servidores) iniciaAtendimento(fila);
    }

    /**
     * Sorteia para onde o cliente vai ao terminar e o tempo de atendimento, e agenda esse evento.
     * A ordem (primeiro o destino, depois o tempo) é a mesma do simulator.jar do M3, o que faz
     * os dois darem resultados idênticos com a mesma lista de aleatórios.
     */
    private void iniciaAtendimento(Fila fila) {
        if (!rnd.temProximo()) return;
        Fila destino = sorteiaDestino(fila);
        if (!rnd.temProximo()) return;
        double fim = tempoGlobal + rnd.uniforme(fila.minAtendimento, fila.maxAtendimento);
        escalonador.agenda(fim, fila, destino);
    }

    /**
     * Roteamento: sorteia U(0,1) e vê em que faixa de probabilidade acumulada ele cai (texto do M8).
     * Só gasta aleatório quando há mais de um caminho possível.
     */
    private Fila sorteiaDestino(Fila fila) {
        if (fila.rotas.isEmpty()) return null;
        if (fila.rotas.size() == 1 && fila.rotas.get(0).probabilidade() >= 1.0) {
            return fila.rotas.get(0).destino();
        }
        double sorteio = rnd.proximo();
        double soma = 0;
        for (Rota r : fila.rotas) {
            soma += r.probabilidade();
            if (sorteio < soma) return r.destino();
        }
        return null; // a probabilidade que sobra é a de sair para o exterior
    }
}
