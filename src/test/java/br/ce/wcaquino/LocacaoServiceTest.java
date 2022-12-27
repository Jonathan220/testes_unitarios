package br.ce.wcaquino;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.Date;

import org.junit.Test;

import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.servicos.LocacaoService;
import br.ce.wcaquino.utils.DataUtils;

public class LocacaoServiceTest {

    @Test
	public void teste() {
		//cenario
		LocacaoService locacaoService = new LocacaoService();
		Usuario usuario = new Usuario("usuario 1");
		Filme filme = new Filme("filme 1", 2, 5.0);

		//acao
		Locacao locacao = locacaoService.alugarFilme(usuario, filme);

		//verificacao
		assertThat(locacao.getValor(), is(5.0));
		assertThat(DataUtils.isMesmaData(locacao.getDataLocacao(), new Date()), is(true));
		assertThat(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterDataComDiferencaDias(1)), is(true));
	}
}
