package br.ce.wcaquino.servicos;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import br.ce.wcaquino.exceptions.DivisaoPorZeroException;

public class CalculadoraTest {

    @Rule
    public ExpectedException expectException = ExpectedException.none(); 

    private Calculadora calc;

    @Before
    public void setup(){
        calc = new Calculadora();
    }

    @Test
    public void deveSomarDoisValoresComSucesso(){
        int a = 2;
        int b = 5;

        int resultado = calc.somar(a, b);

        assertThat(resultado, is(7));
    }

    @Test
    public void deveSubtrairDoisValoresComSucesso(){
        int a = 3;
        int b = 2;

        int resultado = calc.subtrair(a, b);

        assertThat(resultado, is(1));
    }

    @Test
    public void deveDividirComSucesso() throws DivisaoPorZeroException{
        int a = 4;
        int b = 2;

        double resultado = calc.dividir(a, b);

        assertThat(resultado, is(2.0));
    }

    @Test
    public void DeveLancarExcecaoAoDividirPorZero() throws DivisaoPorZeroException{
        int a = 10;
        int b = 0;

        expectException.expect(DivisaoPorZeroException.class);
        expectException.expectMessage("Não pode dividir por zero!");

        calc.dividir(a, b);
        
    }

    @Test
    public void deveMultiplicarDoisValoresComSucesso(){
        int a = 2;
        int b = 2;

        int resultado = calc.multiplicar(a, b);

        assertThat(resultado, is(4));
    }
}
