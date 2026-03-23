package br.ufscar.dc.compiladores.la.lexico;

/**
 * Representa um token identificado pelo analisador léxico da linguagem LA.
 *
 * Cada token possui:
 *   - lexema: o trecho exato do código-fonte que originou o token
 *   - tipo:   a categoria gramatical do token
 *   - linha:  número da linha no fonte onde o token foi encontrado
 *
 * Regra de formatação da saída (conforme especificação):
 *   - IDENT, NUM_INT, NUM_REAL, CADEIA → <'lexema',TIPO>  (tipo sem aspas)
 *   - Palavras reservadas e símbolos   → <'lexema','tipo'> (tipo com aspas simples)
 */
public class Token {

    /** Valor textual do token tal como aparece no código-fonte */
    private final String lexema;

    /**
     * Tipo do token. Para palavras reservadas e símbolos, o tipo é o
     * próprio valor (ex: "algoritmo", ":"). Para os demais, são as
     * constantes: IDENT, NUM_INT, NUM_REAL, CADEIA.
     */
    private final String tipo;

    /** Linha do código-fonte onde o token começa */
    private final int linha;

    public Token(String lexema, String tipo, int linha) {
        this.lexema = lexema;
        this.tipo   = tipo;
        this.linha  = linha;
    }

    public String getLexema() { return lexema; }
    public String getTipo()   { return tipo;   }
    public int    getLinha()  { return linha;  }

    /**
     * Gera a representação textual do token no formato exigido pela especificação.
     * Identificadores, números e cadeias usam tipo sem aspas; demais usam com aspas.
     */
    @Override
    public String toString() {
        if (tipo.equals("IDENT") || tipo.equals("NUM_INT")
                || tipo.equals("NUM_REAL") || tipo.equals("CADEIA")) {
            return String.format("<'%s',%s>", lexema, tipo);
        }
        return String.format("<'%s','%s'>", lexema, tipo);
    }
}
