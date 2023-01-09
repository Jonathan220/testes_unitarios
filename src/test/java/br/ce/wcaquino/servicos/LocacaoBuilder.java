package br.ce.wcaquino.servicos;

import java.util.Arrays;
import java.util.Date;

import br.ce.wcaquino.builders.FilmeBuilder;
import br.ce.wcaquino.builders.UsuarioBuilder;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.utils.DataUtils;

public class LocacaoBuilder {
    private Locacao locacao;

    private LocacaoBuilder(){}

    public static LocacaoBuilder umaLocacao(){
        LocacaoBuilder builder = new LocacaoBuilder();
        builder.locacao = new Locacao();
        builder.locacao.setDataLocacao(new Date());
        builder.locacao.setDataRetorno(DataUtils.obterDataComDiferencaDias(1));
        builder.locacao.setFilmes(Arrays.asList(FilmeBuilder.umfilme().agora()));
        builder.locacao.setUsuario(UsuarioBuilder.umUsuario().agora());
        builder.locacao.setValor(4.0);
        return builder;
    }

    public Locacao agora(){
        return locacao;
    }
}
