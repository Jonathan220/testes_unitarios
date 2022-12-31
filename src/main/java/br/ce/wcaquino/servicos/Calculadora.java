package br.ce.wcaquino.servicos;

import br.ce.wcaquino.exceptions.DivisaoPorZeroException;

public class Calculadora {

    public int somar(int a, int b) {
        return a + b;
    }

    public int subtrair(int a, int b) {
        return a - b;
    }

    public double dividir(int a, int b) throws DivisaoPorZeroException {
        if(b == 0){
            throw new DivisaoPorZeroException("Não pode dividir por zero!");
        }
        return a/b;
    }

    public int multiplicar(int a, int b) {
        return a * b;
    }

}
