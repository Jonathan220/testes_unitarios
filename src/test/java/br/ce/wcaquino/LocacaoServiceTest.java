package br.ce.wcaquino;

import static org.hamcrest.CoreMatchers.is;
import java.util.Date;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;

import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.servicos.LocacaoService;
import br.ce.wcaquino.utils.DataUtils;

public class LocacaoServiceTest {

	@Rule
	public ErrorCollector error = new ErrorCollector();

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

    @Test
	public void testeLocacao() throws Exception {
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

	/*
	 * Utilizado se a exceção for única
	 */
	@Test(expected = FilmeSemEstoqueException.class)
	public void deveLancarExcecaoQuandoNaoHouverEstoqueElegante() throws Exception{
		//cenario
		LocacaoService locacaoService = new LocacaoService();
		Usuario usuario = new Usuario("usuario 1");
		Filme filme = new Filme("filme 1", 0, 5.0);

		//acao
		Locacao locacao = locacaoService.alugarFilme(usuario, filme);
	}

	@Test
	public void deveLancarExcecaoQuandoNaoHouverEstoqueRobusta() {
		//cenario
		LocacaoService locacaoService = new LocacaoService();
		Usuario usuario = new Usuario("usuario 1");
		Filme filme = new Filme("filme 1", 0, 5.0);

		//acao
		try {
			Locacao locacao = locacaoService.alugarFilme(usuario, filme);
			Assert.fail("Deveria ter lancado uma excecao!!");
		} catch (Exception e) {
			Assert.assertThat(e.getMessage(), is("Filme sem estoque"));
		}
	}

	@Test
	public void deveLancarExcecaoQuandoNaoHouverEstoqueNova() throws Exception{
		//cenario
		LocacaoService locacaoService = new LocacaoService();
		Usuario usuario = new Usuario("usuario 1");
		Filme filme = new Filme("filme 1", 0, 5.0);
		
		expectedException.expect(Exception.class);
		expectedException.expectMessage("Filme sem estoque");
		
		//acao
		Locacao locacao = locacaoService.alugarFilme(usuario, filme);
	}
}
