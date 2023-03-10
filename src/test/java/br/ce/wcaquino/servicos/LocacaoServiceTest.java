package br.ce.wcaquino.servicos;

import static br.ce.wcaquino.matchers.MatchersProprios.caiNumaSegunda;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.powermock.api.mockito.PowerMockito;
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

	@InjectMocks @Spy
	private LocacaoService locacaoService;

	@Mock
	private LocacaoDao dao;
	
	@Mock
	private SPCService spcService;
	
	@Mock
	private EmailService emailService;

	@Before
	public void setup(){
		MockitoAnnotations.initMocks(this);
		locacaoService = PowerMockito.spy(locacaoService);
		System.out.println("Iniciando2...");
		CalculadoraTest.ordem.append("2");
	}

	@After
    public void tearDown(){
        System.out.println("finalizando2...");
    }

	@AfterClass
    public static void tearDownClass(){
        System.out.println(CalculadoraTest.ordem.toString());
    }

    @Test
	public void deveAlugarFilmeComSucesso() throws Exception {
		//cenario
		Usuario usuario = UsuarioBuilder.umUsuario().agora();
		List<Filme> filmes = Arrays.asList(FilmeBuilder.umfilme().comValor(5.0).agora());

		Mockito.doReturn(DataUtils.obterData(6, 1, 2023)).when(locacaoService).obterData();

		//acao
		Locacao locacao = locacaoService.alugarFilme(usuario, filmes);
			
		//verificacao
		error.checkThat(locacao.getValor(), is(5.0));
		error.checkThat(DataUtils.isMesmaData(locacao.getDataLocacao(), DataUtils.obterData(6, 1, 2023)), is(true));
		error.checkThat(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterData(7, 1, 2023)), is(true));
	}

	/*
	 * Utilizado se a exce????o for ??nica
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
	 * A fun????o ?? capaz de continuar o fluxo mesmo ap??s o lan??amento da exce??ao esperada
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
	 * Utilizada quando se quer capturar a exce????o de forma r??pida
	 * Ap??s o lan??amento do exce????o a fun????o para
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
	public void deveDevolverNaSegundaAoAlugarNoSabado() throws Exception{

		Usuario usuario = UsuarioBuilder.umUsuario().agora();
		List<Filme> filmes = Arrays.asList(FilmeBuilder.umfilme().agora());

		Mockito.doReturn(DataUtils.obterData(7, 1, 2023)).when(locacaoService).obterData();

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

	@Test
	public void deveProrrogarUmaLocacao(){
		//cenario
		Locacao locacao = LocacaoBuilder.umaLocacao().agora();
		
		//acao
		locacaoService.prorrogarLocacao(locacao, 3);

		//verificacao
		ArgumentCaptor<Locacao> argCapt = ArgumentCaptor.forClass(Locacao.class);
		Mockito.verify(dao).salvar(argCapt.capture());
		Locacao locacaoRetornada = argCapt.getValue();

		error.checkThat(locacaoRetornada.getValor(), is(12.0));
		error.checkThat(locacaoRetornada.getDataLocacao(), MatchersProprios.ehHoje());
		error.checkThat(locacaoRetornada.getDataRetorno(), MatchersProprios.ehHojeComDiferencaDias(3));
	}

	@Test
	public void deveCalcularValorLocacao() throws Exception{
		//cenario
		List<Filme> filmes = Arrays.asList(FilmeBuilder.umfilme().agora());

		//acao
		Class<LocacaoService> clazz = LocacaoService.class;
		Method metodo = clazz.getDeclaredMethod("calcularValorLocacao", List.class);
		metodo.setAccessible(true);
		Double valor = (Double) metodo.invoke(locacaoService, filmes);

		//verificacao
		assertThat(valor, is(4.0));
	}

}
