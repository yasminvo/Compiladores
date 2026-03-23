# Compilador LA — Construção de Compiladores (DC/UFSCar)

Repositório com os trabalhos práticos da disciplina **Construção de Compiladores**,
ministrada pelo Prof. Daniel Lucrédio no Departamento de Computação da UFSCar.

O compilador é desenvolvido incrementalmente ao longo dos trabalhos T1 a T5,
implementando as fases de um compilador para a linguagem **LA** (_Linguagem Algorítmica_),
desenvolvida pelo Prof. Jander no DC/UFSCar.

## Membros do grupo

<!-- TODO: preencha com os nomes e RAs do grupo -->
- Yasmin Victoria Oliveira — 812308
- Anna Carolina Brito Santos Farias - 811448
- Vitor Yuki Inumaru Ferreira — 794041

---

## Trabalhos

| # | Fase | Diretório | Status |
|---|------|-----------|--------|
| T1 | Analisador Léxico | [`t1/`](t1/) | ✅ |
| T2 | Analisador Sintático | `t2/` | — |
| T3 | Análise Semântica | `t3/` | — |
| T4 | Geração de Código | `t4/` | — |
| T5 | Otimização | `t5/` | — |

---

## T1 — Analisador Léxico

### Descrição

Implementação de um **analisador léxico** para a linguagem LA. O programa lê um
arquivo de código-fonte, reconhece todos os tokens e grava a lista de tokens
identificados em um arquivo de saída. Em caso de erro léxico, interrompe a
execução e reporta a linha onde o erro ocorre.

### Pré-requisitos

| Ferramenta | Versão mínima | Como instalar |
|-----------|---------------|---------------|
| Java (JDK) | 11 | [adoptium.net](https://adoptium.net) ou `brew install --cask temurin` (macOS) |
| Apache Maven | 3.6 | [maven.apache.org](https://maven.apache.org) ou `brew install maven` (macOS) |

Verifique as versões instaladas:

```bash
java -version
mvn -version
```

### Como compilar

```bash
cd t1
mvn package
```

O JAR executável será gerado em `t1/target/compilador.jar`.

### Como executar

```bash
java -jar t1/target/compilador.jar <arquivo_entrada> <arquivo_saida>
```

- **Argumento 1** — caminho completo do arquivo de entrada (código-fonte LA)
- **Argumento 2** — caminho completo do arquivo de saída (tokens identificados)

A saída é **sempre** gravada no arquivo; nada é impresso no terminal.

#### Exemplo

Dado o arquivo `entrada.txt`:

```
{ exemplo simples }
algoritmo
declare
  x: inteiro
leia(x)
escreva(x)
fim_algoritmo
```

Executando:

```bash
java -jar t1/target/compilador.jar entrada.txt saida.txt
```

Conteúdo gerado em `saida.txt`:

```
<'algoritmo','algoritmo'>
<'declare','declare'>
<'x',IDENT>
<':',':'>
<'inteiro','inteiro'>
<'leia','leia'>
<'(','('>
<'x',IDENT>
<')',')'>
<'escreva','escreva'>
<'(','('>
<'x',IDENT>
<')',')'>
<'fim_algoritmo','fim_algoritmo'>
```

#### Exemplo com erro léxico

Arquivo com o símbolo inválido `~`:

```
algoritmo
declare
  nome~ literal
fim_algoritmo
```

Saída gerada:

```
<'algoritmo','algoritmo'>
<'declare','declare'>
<'nome',IDENT>
Linha 3: ~ - simbolo nao identificado
```

### Estrutura do projeto

```
t1/
├── src/
│   └── main/
│       └── java/
│           └── br/ufscar/dc/compiladores/la/lexico/
│               ├── Main.java              # Ponto de entrada; lê args e arquivos
│               ├── AnalisadorLexico.java  # Núcleo do analisador léxico
│               └── Token.java             # Representação e formatação de tokens
├── pom.xml                                # Build Maven
└── target/
    └── compilador.jar                     # Gerado após `mvn package`
```

### Tokens reconhecidos

| Categoria | Exemplos | Tipo na saída |
|-----------|----------|---------------|
| Palavras reservadas | `algoritmo`, `declare`, `se`, `inteiro` … | `'palavra'` |
| Identificadores | `nome`, `x`, `total` | `IDENT` |
| Inteiros | `0`, `42`, `100` | `NUM_INT` |
| Reais | `3.14`, `0.5` | `NUM_REAL` |
| Cadeias | `"texto"`, `" anos."` | `CADEIA` |
| Símbolos | `(`, `)`, `[`, `]`, `,`, `:`, `<-`, `=`, `<>`, `<=`, `>=`, `+`, `-` … | `'símbolo'` |

### Erros léxicos reportados

| Situação | Mensagem |
|----------|----------|
| Símbolo desconhecido | `Linha N: X - simbolo nao identificado` |
| Comentário não fechado na linha | `Linha N: comentario nao fechado` |
| Cadeia não fechada na linha | `Linha N: cadeia nao fechada` |
