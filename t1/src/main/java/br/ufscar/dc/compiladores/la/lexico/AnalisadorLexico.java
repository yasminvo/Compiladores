package br.ufscar.dc.compiladores.la.lexico;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Analisador léxico para a linguagem LA (Linguagem Algorítmica).
 *
 * Responsabilidades:
 *   - Percorrer o código-fonte caractere a caractere
 *   - Reconhecer e classificar os tokens da linguagem
 *   - Ignorar espaços em branco e comentários bem formados
 *   - Interromper a análise e reportar a linha em caso de erro léxico
 *
 * Tokens reconhecidos:
 *   Palavras reservadas – algoritmo, fim_algoritmo, declare, tipo, registro,
 *     fim_registro, procedimento, fim_procedimento, funcao, fim_funcao,
 *     se, entao, fim_se, senao, caso, seja, fim_caso,
 *     enquanto, faca, fim_enquanto, para, ate, fim_para,
 *     leia, escreva, escreval, retorne,
 *     inteiro, real, logico, literal, verdadeiro, falso,
 *     e, ou, nao, mod, div, constante
 *   IDENT    – identificadores: [a-zA-Z][a-zA-Z0-9_]*
 *   NUM_INT  – inteiros:  [0-9]+
 *   NUM_REAL – reais:     [0-9]+.[0-9]+
 *   CADEIA   – strings entre aspas duplas (mesmo incluindo as aspas no lexema)
 *   Símbolos – ( ) [ ] , : . .. <- = <> < > <= >= + - * / ^
 *
 * Erros léxicos reportados (análise interrompida):
 *   "Linha N: X - simbolo nao identificado"
 *   "Linha N: comentario nao fechado"
 *   "Linha N: cadeia nao fechada"
 */
public class AnalisadorLexico {

    // -----------------------------------------------------------------------
    // Palavras reservadas da linguagem LA
    // -----------------------------------------------------------------------
    private static final Set<String> PALAVRAS_RESERVADAS = new HashSet<>(Arrays.asList(
            "algoritmo", "fim_algoritmo",
            "declare", "tipo",
            "registro", "fim_registro",
            "procedimento", "fim_procedimento",
            "funcao", "fim_funcao",
            "se", "entao", "fim_se", "senao",
            "caso", "seja", "fim_caso",
            "enquanto", "faca", "fim_enquanto",
            "para", "ate", "fim_para",
            "leia", "escreva", "escreval",
            "retorne", "constante",
            "inteiro", "real", "logico", "literal",
            "verdadeiro", "falso",
            "e", "ou", "nao",
            "mod", "div"
    ));

    // -----------------------------------------------------------------------
    // Estado interno do analisador
    // -----------------------------------------------------------------------

    /** Código-fonte completo (normalizado para \n) */
    private final String fonte;

    /** Posição atual de leitura no código-fonte */
    private int pos;

    /** Número da linha atual (começa em 1) */
    private int linha;

    /** Flag que indica se um erro léxico foi encontrado */
    private boolean erroEncontrado;

    /** Acumula a saída (tokens e/ou mensagem de erro) */
    private final StringBuilder saida;

    // -----------------------------------------------------------------------
    // Construtor
    // -----------------------------------------------------------------------

    /**
     * Cria o analisador para o código-fonte fornecido.
     *
     * @param fonte código-fonte a ser analisado (qualquer terminador de linha)
     */
    public AnalisadorLexico(String fonte) {
        // Normaliza terminadores de linha para facilitar a contagem de linhas
        this.fonte = fonte.replace("\r\n", "\n").replace("\r", "\n");
        this.pos             = 0;
        this.linha           = 1;
        this.erroEncontrado  = false;
        this.saida           = new StringBuilder();
    }

    // -----------------------------------------------------------------------
    // Interface pública
    // -----------------------------------------------------------------------

    /**
     * Executa a análise léxica completa do código-fonte.
     *
     * @return string com um token por linha; em caso de erro, os tokens
     *         reconhecidos até o ponto do erro seguidos da mensagem de erro
     */
    public String analisar() {
        while (pos < fonte.length() && !erroEncontrado) {
            char c = charAtual();

            // --- Espaços em branco (incluindo nova linha) ---
            if (Character.isWhitespace(c)) {
                if (c == '\n') linha++;
                pos++;
                continue;
            }

            // --- Comentário: { ... } ---
            if (c == '{') {
                processarComentario();
                continue;
            }

            // --- Cadeia de caracteres: "..." ---
            if (c == '"') {
                processarCadeia();
                continue;
            }

            // --- Número inteiro ou real ---
            if (Character.isDigit(c)) {
                processarNumero();
                continue;
            }

            // --- Identificador ou palavra reservada ---
            if (Character.isLetter(c)) {
                processarIdentificador();
                continue;
            }

            // --- Símbolo especial ---
            if (processarSimbolo()) {
                continue;
            }

            // --- Símbolo não reconhecido: reporta erro e encerra ---
            reportarErro(String.format("Linha %d: %c - simbolo nao identificado", linha, c));
        }

        return saida.toString();
    }

    // -----------------------------------------------------------------------
    // Métodos auxiliares de leitura
    // -----------------------------------------------------------------------

    /** Retorna o caractere na posição atual sem avançar. */
    private char charAtual() {
        return fonte.charAt(pos);
    }

    /** Retorna o próximo caractere (pos+1) ou '\0' se estiver no fim. */
    private char proximoChar() {
        return (pos + 1 < fonte.length()) ? fonte.charAt(pos + 1) : '\0';
    }

    // -----------------------------------------------------------------------
    // Processamento de cada categoria de token
    // -----------------------------------------------------------------------

    /**
     * Processa um comentário delimitado por chaves { }.
     *
     * Comentários devem ser fechados antes do fim da linha. Se a chave de
     * fechamento não for encontrada antes de '\n' ou do fim do arquivo,
     * é gerado o erro "comentario nao fechado".
     */
    private void processarComentario() {
        int linhaInicio = linha;
        pos++; // avança além de '{'

        while (pos < fonte.length()) {
            char c = charAtual();

            if (c == '}') {
                pos++; // avança além de '}'
                return; // comentário fechado com sucesso
            }

            if (c == '\n') {
                // Fim de linha sem fechar o comentário
                reportarErro(String.format("Linha %d: comentario nao fechado", linhaInicio));
                return;
            }

            pos++;
        }

        // Chegou ao fim do arquivo sem fechar o comentário
        reportarErro(String.format("Linha %d: comentario nao fechado", linhaInicio));
    }

    /**
     * Processa uma cadeia de caracteres entre aspas duplas.
     *
     * O lexema resultante inclui as aspas de abertura e fechamento.
     * Se a cadeia não for fechada antes de '\n' ou do fim do arquivo,
     * é gerado o erro "cadeia nao fechada".
     */
    private void processarCadeia() {
        int linhaCadeia = linha;
        StringBuilder lexema = new StringBuilder();
        lexema.append('"');
        pos++; // avança além da aspa de abertura

        while (pos < fonte.length()) {
            char c = charAtual();

            if (c == '"') {
                lexema.append('"');
                pos++; // avança além da aspa de fechamento
                emitirToken(lexema.toString(), "CADEIA");
                return;
            }

            if (c == '\n') {
                // Fim de linha sem fechar a cadeia
                reportarErro(String.format("Linha %d: cadeia nao fechada", linhaCadeia));
                return;
            }

            lexema.append(c);
            pos++;
        }

        // Chegou ao fim do arquivo sem fechar a cadeia
        reportarErro(String.format("Linha %d: cadeia nao fechada", linhaCadeia));
    }

    /**
     * Processa um literal numérico inteiro (NUM_INT) ou real (NUM_REAL).
     *
     * Inteiro: [0-9]+
     * Real:    [0-9]+.[0-9]+   (ponto decimal seguido de ao menos um dígito)
     */
    private void processarNumero() {
        StringBuilder lexema = new StringBuilder();

        // Lê a parte inteira
        while (pos < fonte.length() && Character.isDigit(charAtual())) {
            lexema.append(charAtual());
            pos++;
        }

        // Verifica se é número real: ponto seguido de dígito
        if (pos < fonte.length() && charAtual() == '.' && Character.isDigit(proximoChar())) {
            lexema.append('.');
            pos++; // avança além do ponto

            while (pos < fonte.length() && Character.isDigit(charAtual())) {
                lexema.append(charAtual());
                pos++;
            }
            emitirToken(lexema.toString(), "NUM_REAL");
        } else {
            emitirToken(lexema.toString(), "NUM_INT");
        }
    }

    /**
     * Processa um identificador ou palavra reservada.
     *
     * Identificador: [a-zA-Z][a-zA-Z0-9_]*
     *
     * Se o lexema obtido constar na lista de palavras reservadas, o tipo
     * emitido é o próprio lexema; caso contrário, o tipo é IDENT.
     */
    private void processarIdentificador() {
        StringBuilder lexema = new StringBuilder();

        // Identificadores: letras, dígitos e sublinhados após a primeira letra
        while (pos < fonte.length()
                && (Character.isLetterOrDigit(charAtual()) || charAtual() == '_')) {
            lexema.append(charAtual());
            pos++;
        }

        String valor = lexema.toString();
        if (PALAVRAS_RESERVADAS.contains(valor)) {
            // Para palavras reservadas, tipo = lexema (ex: 'algoritmo','algoritmo')
            emitirToken(valor, valor);
        } else {
            emitirToken(valor, "IDENT");
        }
    }

    /**
     * Tenta reconhecer um símbolo especial na posição atual.
     *
     * Símbolos multi-caractere têm prioridade (ex: "<-" antes de "<").
     *
     * @return true se um símbolo foi reconhecido, false caso contrário
     */
    private boolean processarSimbolo() {
        char c = charAtual();

        switch (c) {
            // Símbolos de um único caractere sem ambiguidade
            case '(': case ')':
            case '[': case ']':
            case ',':
            case '+': case '-':
            case '*': case '/':
            case '^':
            case '=':
                emitirToken(String.valueOf(c), String.valueOf(c));
                pos++;
                return true;

            // ':' – apenas dois-pontos simples na LA
            case ':':
                emitirToken(":", ":");
                pos++;
                return true;

            // '<' pode ser '<-', '<>', '<=' ou apenas '<'
            case '<':
                if (proximoChar() == '-') {
                    emitirToken("<-", "<-");
                    pos += 2;
                } else if (proximoChar() == '>') {
                    emitirToken("<>", "<>");
                    pos += 2;
                } else if (proximoChar() == '=') {
                    emitirToken("<=", "<=");
                    pos += 2;
                } else {
                    emitirToken("<", "<");
                    pos++;
                }
                return true;

            // '>' pode ser '>=' ou apenas '>'
            case '>':
                if (proximoChar() == '=') {
                    emitirToken(">=", ">=");
                    pos += 2;
                } else {
                    emitirToken(">", ">");
                    pos++;
                }
                return true;

            // '.' pode ser '..' (intervalo de array) ou apenas '.' (acesso a registro)
            case '.':
                if (proximoChar() == '.') {
                    emitirToken("..", "..");
                    pos += 2;
                } else {
                    emitirToken(".", ".");
                    pos++;
                }
                return true;

            default:
                return false; // símbolo não reconhecido
        }
    }

    // -----------------------------------------------------------------------
    // Saída e erros
    // -----------------------------------------------------------------------

    /**
     * Adiciona um token à saída acumulada.
     *
     * @param lexema valor textual do token
     * @param tipo   categoria do token
     */
    private void emitirToken(String lexema, String tipo) {
        saida.append(new Token(lexema, tipo, linha)).append("\n");
    }

    /**
     * Registra um erro léxico: adiciona a mensagem à saída e sinaliza
     * que a análise deve ser interrompida.
     *
     * @param mensagem mensagem de erro formatada
     */
    private void reportarErro(String mensagem) {
        saida.append(mensagem).append("\n");
        erroEncontrado = true;
    }
}
