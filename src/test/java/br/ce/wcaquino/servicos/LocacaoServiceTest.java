package br.ce.wcaquino.servicos;

import static br.ce.wcaquino.matchers.MatchersProprios.caiNumaSegunda;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import br.ce.wcaquino.builders.FilmeBuilder;
import br.ce.wcaquino.builders.LocacaoBuilder;
import br.ce.wcaquino.builders.UsuarioBuilder;
import br.ce.wcaquino.daos.LocacaoDao;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocacaoException;
import br.ce.wcaquino.matchers.MatchersProprios;
import br.ce.wcaquino.utils.DataUtils;

public class LocacaoServiceTest {

	@Rule
	public ErrorCollector error = new ErrorCollector();

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@InjectMocks
	private LocacaoService locacaoService;

	@Mock
	private LocacaoDao dao;
	
	@Mock
	private SPCService spcService;
	
	@Mock
	private EmailService emailService;

	@Before
	public void setup(){
		MockitoAnnotations.openMocks(this);
	}

    @Test
	public void deveAlugarFilmeComSucesso() throws Exception {
		Assume.assumeFalse(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));
		//cenario
		Usuario usuario = UsuarioBuilder.umUsuario().agora();
		List<Filme> filmes = Arrays.asList(FilmeBuilder.umfilme().comValor(5.0).agora());

		//acao
		Locacao locacao = locacaoService.alugarFilme(usuario, filmes);
			
		//verificacao
		error.checkThat(locacao.getValor(), is(5.0));
		error.checkThat(locacao.getDataLocacao(), MatchersProprios.ehHoje());
		error.checkThat(locacao.getDataRetorno(), MatchersProprios.ehHojeComDiferencaDias(1));
	}

	/*
	 * Utilizado se a exceção for única
	 */
	@Test(expected = FilmeSemEstoqueException.class)
	public void deveLancarExcecaoAoAlugarFilmeSemEstoque() throws Exception{
		//cenario
		Usuario usuario = UsuarioBuilder.umUsuario().agora();
		List<Filme> filmes = Arrays.asList(FilmeBuilder.umfilme().semEstoque().agora());

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
		List<Filme> filmes = Arrays.asList(FilmeBuilder.umfilme().agora());

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
		Usuario usuario = UsuarioBuilder.umUsuario().agora();

		expectedException.expect(LocacaoException.class);
		expectedException.expectMessage("Filme vazio");
	
		//acao
		locacaoService.alugarFilme(usuario, null);

	}
	
	@Test
	public void deveDevolverNaSegundaAoAlugarNoSabado() throws LocacaoException, FilmeSemEstoqueException{
		Assume.assumeTrue(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));

		Usuario usuario = UsuarioBuilder.umUsuario().agora();
		List<Filme> filmes = Arrays.asList(FilmeBuilder.umfilme().agora());

		Locacao retorno = locacaoService.alugarFilme(usuario, filmes);

		assertThat(retorno.getDataRetorno(), caiNumaSegunda());
	}

	@Test
	public void naoDeveAlugarFilmeParaNegativadoSPC() throws Exception {
		//cenario
		Usuario usuario = UsuarioBuilder.umUsuario().agora();
		List<Filme> filmes = Arrays.asList(FilmeBuilder.umfilme().agora());

		Mockito.when(spcService.possuiNegativado(Mockito.any(Usuario.class))).thenReturn(true);

		//acao
		try {
			locacaoService.alugarFilme(usuario, filmes);
		//verificacao
			Assert.fail();
		} catch (LocacaoException e) {
			Assert.assertThat(e.getMessage(), is("Usuario negativado"));
		}

		verify(spcService).possuiNegativado(usuario);

	}

	@Test
	public void deveEnviarEmailParaLocacoesAtrasadas(){
		//cenario
		Usuario usuario = UsuarioBuilder.umUsuario().agora();
		Usuario usuario2 = UsuarioBuilder.umUsuario().comNome("Usuario 2").agora();
		List<Locacao> locacoes = Arrays.asList(
			LocacaoBuilder
			.umaLocacao()
			.comUsuario(usuario)
			.atrasado()
			.comDataRetorno(DataUtils.obterDataComDiferencaDias(-2))
			.agora(),
			LocacaoBuilder
			.umaLocacao()
			.comUsuario(usuario2)
			.agora());

		Mockito.when(dao.obterLocacoesPendentes()).thenReturn(locacoes);

		//acao
		locacaoService.notificarAtrasos();

		//verificacao
		Mockito.verify(emailService).notificarAtraso(usuario);
		Mockito.verify(emailService, Mockito.never()).notificarAtraso(usuario2);
	}

	@Test
	public void deveTratarErroNoSPC() throws Exception{
		//cenario
		Usuario usuario = UsuarioBuilder.umUsuario().agora();
		List<Filme> filmes = Arrays.asList(FilmeBuilder.umfilme().agora());

		Mockito.when(spcService.possuiNegativado(usuario)).thenThrow(new Exception("Falha no sistema"));

		//verificacao
		expectedException.expect(LocacaoException.class);
		expectedException.expectMessage("Problemas com o SPC, tente novamente");

		//acao
		locacaoService.alugarFilme(usuario, filmes);
	}
}
