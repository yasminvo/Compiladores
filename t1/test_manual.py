"""
Teste manual rápido para verificar a lógica do analisador léxico
ANTES de compilar com Java. Reimplements a mesma lógica em Python.

Execute com:  python3 t1/test_manual.py
"""

import re

PALAVRAS_RESERVADAS = {
    "algoritmo", "fim_algoritmo",
    "declare", "tipo", "registro", "fim_registro",
    "procedimento", "fim_procedimento", "funcao", "fim_funcao",
    "se", "entao", "fim_se", "senao",
    "caso", "seja", "fim_caso",
    "enquanto", "faca", "fim_enquanto",
    "para", "ate", "fim_para",
    "leia", "escreva", "escreval", "retorne", "constante",
    "inteiro", "real", "logico", "literal", "verdadeiro", "falso",
    "e", "ou", "nao", "mod", "div",
}

def analisar(fonte: str) -> str:
    fonte = fonte.replace("\r\n", "\n").replace("\r", "\n")
    pos, linha, saida = 0, 1, []
    n = len(fonte)

    def cur():  return fonte[pos] if pos < n else '\0'
    def prox(): return fonte[pos + 1] if pos + 1 < n else '\0'

    def emit(lexema, tipo):
        if tipo in ("IDENT", "NUM_INT", "NUM_REAL", "CADEIA"):
            saida.append(f"<'{lexema}',{tipo}>")
        else:
            saida.append(f"<'{lexema}','{tipo}'>")

    while pos < n:
        c = cur()

        # Whitespace
        if c in (' ', '\t', '\r'):
            pos += 1; continue
        if c == '\n':
            linha += 1; pos += 1; continue

        # Comentário
        if c == '{':
            li = linha; pos += 1
            while pos < n:
                if cur() == '}': pos += 1; break
                if cur() == '\n':
                    saida.append(f"Linha {li}: comentario nao fechado")
                    return '\n'.join(saida) + '\n'
                pos += 1
            else:
                saida.append(f"Linha {li}: comentario nao fechado")
                return '\n'.join(saida) + '\n'
            continue

        # Cadeia
        if c == '"':
            li = linha; lex = '"'; pos += 1
            while pos < n:
                if cur() == '"':  lex += '"'; pos += 1; emit(lex, "CADEIA"); break
                if cur() == '\n':
                    saida.append(f"Linha {li}: cadeia nao fechada")
                    return '\n'.join(saida) + '\n'
                lex += cur(); pos += 1
            else:
                saida.append(f"Linha {li}: cadeia nao fechada")
                return '\n'.join(saida) + '\n'
            continue

        # Número
        if c.isdigit():
            lex = ''
            while pos < n and cur().isdigit(): lex += cur(); pos += 1
            if pos < n and cur() == '.' and prox().isdigit():
                lex += '.'; pos += 1
                while pos < n and cur().isdigit(): lex += cur(); pos += 1
                emit(lex, "NUM_REAL")
            else:
                emit(lex, "NUM_INT")
            continue

        # Identificador / palavra reservada
        if c.isalpha():
            lex = ''
            while pos < n and (cur().isalnum() or cur() == '_'):
                lex += cur(); pos += 1
            emit(lex, lex if lex in PALAVRAS_RESERVADAS else "IDENT")
            continue

        # Símbolos
        matched = True
        if c == '<':
            if prox() == '-':   emit("<-","<-"); pos += 2
            elif prox() == '>': emit("<>","<>"); pos += 2
            elif prox() == '=': emit("<=","<="); pos += 2
            else:               emit("<","<");   pos += 1
        elif c == '>':
            if prox() == '=':   emit(">=",">="); pos += 2
            else:               emit(">",">");   pos += 1
        elif c == '.':
            if prox() == '.':   emit("..",".."); pos += 2
            else:               emit(".",".");   pos += 1
        elif c in '()[],:+-*/^=':
            emit(c, c); pos += 1
        else:
            matched = False

        if not matched:
            saida.append(f"Linha {linha}: {c} - simbolo nao identificado")
            return '\n'.join(saida) + '\n'

    return '\n'.join(saida) + ('\n' if saida else '')


# ---- casos de teste ----

CASO1_ENTRADA = """\
{ leitura de nome e idade }
algoritmo
declare
  nome: literal
declare
  idade: inteiro
{ leitura do teclado }
leia(nome)
leia(idade)
{ saida na tela }
escreva(nome, " tem ", idade, " anos.")
fim_algoritmo
"""

CASO1_ESPERADO = """\
<'algoritmo','algoritmo'>
<'declare','declare'>
<'nome',IDENT>
<':',':'>
<'literal','literal'>
<'declare','declare'>
<'idade',IDENT>
<':',':'>
<'inteiro','inteiro'>
<'leia','leia'>
<'(','('>
<'nome',IDENT>
<')',')'>
<'leia','leia'>
<'(','('>
<'idade',IDENT>
<')',')'>
<'escreva','escreva'>
<'(','('>
<'nome',IDENT>
<',',','>
<'" tem "',CADEIA>
<',',','>
<'idade',IDENT>
<',',','>
<'" anos."',CADEIA>
<')',')'>
<'fim_algoritmo','fim_algoritmo'>
"""

CASO2_ENTRADA = """\
algoritmo
declare
  nome: inteiro
  nome~ literal
fim_algoritmo
"""

CASO2_ESPERADO = """\
<'algoritmo','algoritmo'>
<'declare','declare'>
<'nome',IDENT>
<':',':'>
<'inteiro','inteiro'>
<'nome',IDENT>
Linha 4: ~ - simbolo nao identificado
"""

CASO3_ENTRADA = "algoritmo\n{ comentario nao fechado\nfim_algoritmo\n"
CASO3_ESPERADO = "<'algoritmo','algoritmo'>\nLinha 2: comentario nao fechado\n"

CASO4_ENTRADA = 'algoritmo\nx <- "cadeia nao fechada\nfim_algoritmo\n'
CASO4_ESPERADO = "<'algoritmo','algoritmo'>\n<'x',IDENT>\n<'<-','<-'>\nLinha 2: cadeia nao fechada\n"

casos = [
    ("T1 – saída correta", CASO1_ENTRADA, CASO1_ESPERADO),
    ("T2 – símbolo inválido", CASO2_ENTRADA, CASO2_ESPERADO),
    ("T3 – comentário não fechado", CASO3_ENTRADA, CASO3_ESPERADO),
    ("T4 – cadeia não fechada", CASO4_ENTRADA, CASO4_ESPERADO),
]

ok = 0
for nome, entrada, esperado in casos:
    resultado = analisar(entrada)
    if resultado == esperado:
        print(f"  ✓  {nome}")
        ok += 1
    else:
        print(f"  ✗  {nome}")
        print("    ESPERADO:")
        for l in esperado.splitlines(): print(f"      {repr(l)}")
        print("    OBTIDO:")
        for l in resultado.splitlines(): print(f"      {repr(l)}")

print(f"\n{ok}/{len(casos)} casos passaram.")
