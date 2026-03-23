package br.ufscar.dc.compiladores.la.lexico;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Ponto de entrada do analisador léxico para a linguagem LA.
 *
 * Uso:
 *   java -jar compilador.jar <arquivo_entrada> <arquivo_saida>
 *
 * Argumento 1 – caminho completo do arquivo de entrada (código-fonte LA)
 * Argumento 2 – caminho completo do arquivo de saída  (tokens / erros)
 *
 * A saída é sempre gravada no arquivo; nada é impresso no terminal.
 * Em caso de erro léxico, os tokens reconhecidos até o ponto do erro
 * são seguidos pela mensagem de erro e a análise é encerrada.
 */
public class Main {

    public static void main(String[] args) {
        // Valida a quantidade de argumentos obrigatórios
        if (args.length != 2) {
            System.err.println("Uso: java -jar compilador.jar <arquivo_entrada> <arquivo_saida>");
            System.exit(1);
        }

        String caminhoEntrada = args[0];
        String caminhoSaida   = args[1];

        try {
            // Lê o código-fonte em UTF-8
            String codigoFonte = new String(
                    Files.readAllBytes(Paths.get(caminhoEntrada)),
                    StandardCharsets.UTF_8
            );

            // Executa a análise léxica
            AnalisadorLexico lexer = new AnalisadorLexico(codigoFonte);
            String resultado = lexer.analisar();

            // Grava o resultado no arquivo de saída em UTF-8
            try (PrintWriter escritor = new PrintWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(caminhoSaida),
                            StandardCharsets.UTF_8
                    )
            )) {
                escritor.print(resultado);
            }

        } catch (IOException e) {
            System.err.println("Erro ao processar arquivo: " + e.getMessage());
            System.exit(1);
        }
    }
}
