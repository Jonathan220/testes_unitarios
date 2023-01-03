package br.ce.wcaquino.servicos;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;

import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocacaoException;
import br.ce.wcaquino.utils.DataUtils;

public class LocacaoServiceTest {

	@Rule
	public ErrorCollector error = new ErrorCollector();

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private LocacaoService locacaoService;

	@Before
	public void setup(){
		locacaoService = new LocacaoService();
	}

    @Test
	public void deveAlugarFilmeComSucesso() throws Exception {
		Assume.assumeFalse(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));
		//cenario
		Usuario usuario = new Usuario("usuario 1");
		List<Filme> filmes = Arrays.asList( new Filme("filme 1", 2, 5.0));

		//acao
		Locacao locacao = locacaoService.alugarFilme(usuario, filmes);
			
		//verificacao
		error.checkThat(locacao.getValor(), is(5.0));
		error.checkThat(DataUtils.isMesmaData(locacao.getDataLocacao(), new Date()), is(true));
		error.checkThat(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterDataComDiferencaDias(1)), is(true));
	}

	/*
	 * Utilizado se a exceção for única
	 */
	@Test(expected = FilmeSemEstoqueException.class)
	public void deveLancarExcecaoAoAlugarFilmeSemEstoque() throws Exception{
		//cenario
		Usuario usuario = new Usuario("usuario 1");
		List<Filme> filmes = Arrays.asList(new Filme("filme 1", 0, 5.0));

		//acao
		locacaoService.alugarFilme(usuario, filmes);
	}

	/*
	 * Utilizado quando se quer obter maior controle sobre o teste
	 * A função é capaz de continuar o fluxo mesmo após o lançamento da exceçao esperada
	 */
	@Test
	public void deveLancarExcecaoQuandoUsuarioForVazio() throws FilmeSemEstoqueException{
		//cenario
		List<Filme> filmes = Arrays.asList(new Filme("filme 1", 1, 5.0));

		//acao
		try {
			locacaoService.alugarFilme(null, filmes);
			fail();
		} catch (LocacaoException e) {
			assertThat(e.getMessage(), is("Usuario vazio"));
		} 
	}

	/*
	 * Utilizada quando se quer capturar a exceção de forma rápida
	 * Após o lançamento do exceção a função para
	 */
	@Test
	public void deveLancarExcecaoQuandoFilmeForVazio() throws LocacaoException, FilmeSemEstoqueException{
		//cenario
		Usuario usuario = new Usuario("usuario 1");

		expectedException.expect(LocacaoException.class);
		expectedException.expectMessage("Filme vazio");
	
		//acao
		locacaoService.alugarFilme(usuario, null);

	}
	
	@Test
	public void deveDevolverNaSegundaAoAlugarNoSabado() throws LocacaoException, FilmeSemEstoqueException{
		Assume.assumeTrue(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));

		Usuario usuario = new Usuario("usuario 1");
		List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 1, 5.0));

		Locacao retorno = locacaoService.alugarFilme(usuario, filmes);

		boolean ehSegunda = DataUtils.verificarDiaSemana(retorno.getDataRetorno(), Calendar.MONDAY);
		assertTrue(ehSegunda);
	}
}
