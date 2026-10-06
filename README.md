# Simulador de Rede de Filas — T1

Trabalho de Simulação e Métodos Analíticos (PUCRS, 2026/2) feito por Eduardo Ortiz.

Simulador por eventos discretos de redes de filas. Ele lê o modelo de um arquivo `.yml` no
**mesmo formato do simulador do Módulo 3** (`simulator.jar`) e aceita qualquer topologia:
qualquer número de filas, servidores e capacidades (finita ou infinita), tempos de chegada e
de atendimento com distribuição uniforme e roteamento probabilístico entre as filas (inclusive
de uma fila para ela mesma e saída para o exterior).

## Requisitos

- Java 17 ou mais novo (JDK, porque precisa do `javac`). Para conferir: `java -version` e `javac -version`.
- Não usa nenhuma biblioteca externa.

## Como rodar

No terminal (Prompt de Comando, PowerShell ou terminal do Linux/macOS), dentro da pasta do projeto:

```
javac -encoding UTF-8 -d out src/*.java
java -cp out Main modelo_t1.yml
```

O primeiro comando compila o código para a pasta `out/`. O segundo roda a simulação do
modelo e mostra o resultado no terminal. Para salvar em arquivo:

```
java -cp out Main modelo_t1.yml > resultado.txt
```

Para simular outro modelo, é só passar outro `.yml`: `java -cp out Main outro_modelo.yml`.

## Formato do modelo (`.yml`)

```yaml
!PARAMETERS
arrivals:          # fila onde chega o 1º cliente de fora e em que instante
   Q1: 2.0

queues:
   Q1:
      servers: 1
      minArrival: 2.0    # só nas filas que recebem clientes de fora
      maxArrival: 4.0
      minService: 1.0
      maxService: 2.0
   Q2:
      servers: 2
      capacity: 5        # opcional; sem 'capacity' a fila é infinita
      minService: 4.0
      maxService: 6.0

network:           # roteamento; o que falta para 1.0 é a chance de sair do sistema
-  source: Q1
   target: Q2
   probability: 0.2

rndnumbersPerSeed: 100000   # quantos aleatórios usar em cada simulação
seeds:                      # uma simulação por semente
- 1
```

Em vez de `seeds` + `rndnumbersPerSeed`, dá para passar uma lista fixa de números em
`rndnumbers:` (um `- 0.123` por linha). Nesse caso, a simulação usa exatamente esses números,
na ordem. Se o arquivo tiver `seeds`, a lista `rndnumbers` é ignorada (igual ao `simulator.jar`).
Com mais de uma semente, o programa mostra cada simulação e no final a média delas.

## Saída

Para cada fila, o programa mostra:

- a distribuição de probabilidade dos estados (número de clientes na fila);
- o tempo acumulado em cada estado;
- o número de clientes perdidos (que chegaram com a fila cheia).

No final, mostra o tempo global da simulação e quantos aleatórios foram usados.
A simulação termina quando acaba o último número aleatório disponível (no T1, o 100.000º).

## Modelo do T1

O arquivo `modelo_t1.yml` traz a rede pedida no enunciado:

- **Q1**: G/G/1, chegadas entre 2 e 4, atendimento entre 1 e 2 → Q2 (0,2), Q3 (0,8)
- **Q2**: G/G/2/5, atendimento entre 4 e 6 → Q1 (0,3), Q3 (0,5), sai (0,2)
- **Q3**: G/G/2/10, atendimento entre 5 e 15 → Q2 (0,7), sai (0,3)

Todas as filas começam vazias e o primeiro cliente chega em 2,0. A simulação usa a semente 1
e 100.000 aleatórios. O resultado está em `resultado_t1.txt`.

## Estrutura do código (`src/`)

| Arquivo | O que faz |
|---|---|
| `Main.java` | Ponto de entrada: lê o modelo, roda uma simulação por semente e mostra os resultados |
| `LeitorModelo.java` | Lê o `.yml` (sem biblioteca externa) e monta o modelo |
| `Modelo.java` | Filas, chegadas iniciais, sementes e aleatórios do modelo |
| `Fila.java` | Estado de uma fila: parâmetros, clientes, perdas e tempo acumulado por estado |
| `Rota.java` | Uma ligação da rede (destino e probabilidade) |
| `Evento.java` / `Escalonador.java` | Eventos agendados e o escalonador (sempre pega o próximo no tempo) |
| `GeradorAleatorio.java` | Gerador congruente linear com semente (ou lista fixa de números) |
| `Simulador.java` | Algoritmo de simulação: eventos de chegada, saída e passagem |
| `Resultado.java` | Calcula as probabilidades e mostra a saída |

### Como a simulação funciona

Cada evento tem uma origem e um destino:

- **Chegada** do exterior: o cliente entra na fila (ou é perdido se ela estiver cheia) e a próxima chegada é agendada.
- **Saída** da fila para o exterior: libera o servidor; se há cliente esperando, ele começa a ser atendido.
- **Passagem** de uma fila para outra: saída da origem seguida de chegada no destino.

Quando um atendimento começa, o simulador sorteia para onde o cliente vai depois (pela
probabilidade acumulada das rotas) e o tempo de atendimento, e agenda o evento. Antes de
cada evento, o tempo que passou é somado ao estado atual de todas as filas.

## Validação

Usando a mesma lista de aleatórios (`rndnumbers`), a saída deste simulador é idêntica à do
`simulator.jar` do Módulo 3: mesmas probabilidades, tempos por estado, perdas e tempo global.
Foram testados o modelo do T1, o `model.yml` de exemplo do Módulo 3 e outros modelos, com até
100.000 aleatórios.
