package br.ce.wcaquino.builders;

import br.ce.wcaquino.entidades.Filme;

public class FilmeBuilder {
    private Filme filme;
    
    private FilmeBuilder(){}

    public static FilmeBuilder umfilme(){
        FilmeBuilder builder = new FilmeBuilder();
        builder.filme = new Filme();
        builder.filme.setNome("Filme 1");
        builder.filme.setEstoque(2);
        builder.filme.setPrecoLocacao(4.0);
        return builder;
    }

    public FilmeBuilder comValor(double valor){
        filme.setPrecoLocacao(valor);
        return this;
    }

    public FilmeBuilder semEstoque(){
        filme.setEstoque(0);
        return this;
    }

    public Filme agora(){
        return this.filme;
    }
}
