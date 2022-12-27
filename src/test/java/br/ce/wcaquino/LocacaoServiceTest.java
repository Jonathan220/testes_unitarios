package br.ce.wcaquino;

import static org.hamcrest.CoreMatchers.is;
import java.util.Date;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.servicos.LocacaoService;
import br.ce.wcaquino.utils.DataUtils;

public class LocacaoServiceTest {

	@Rule
	public ErrorCollector error = new ErrorCollector();

    @Test
	public void testeLocacao() {
		//cenario
		LocacaoService locacaoService = new LocacaoService();
		Usuario usuario = new Usuario("usuario 1");
		Filme filme = new Filme("filme 1", 2, 5.0);

		//acao
		Locacao locacao = locacaoService.alugarFilme(usuario, filme);

		//verificacao
		error.checkThat(locacao.getValor(), is(5.0));
		error.checkThat(DataUtils.isMesmaData(locacao.getDataLocacao(), new Date()), is(true));
		error.checkThat(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterDataComDiferencaDias(1)), is(true));
	}
}
