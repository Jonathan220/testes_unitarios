package br.ce.wcaquino.servicos;

import static br.ce.wcaquino.matchers.MatchersProprios.caiNumaSegunda;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import br.ce.wcaquino.builders.FilmeBuilder;
import br.ce.wcaquino.builders.UsuarioBuilder;
import br.ce.wcaquino.daos.LocacaoDao;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.utils.DataUtils;

@PrepareForTest({LocacaoService.class})
@RunWith(PowerMockRunner.class)
public class LocacaoServiceTest_powermock {

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
		MockitoAnnotations.initMocks(this);
		locacaoService = PowerMockito.spy(locacaoService);
	}

    @Test
	public void deveAlugarFilmeComSucesso() throws Exception {
		//cenario
		Usuario usuario = UsuarioBuilder.umUsuario().agora();
		List<Filme> filmes = Arrays.asList(FilmeBuilder.umfilme().comValor(5.0).agora());

		PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(DataUtils.obterData(6, 1, 2023));
		
		//acao
		Locacao locacao = locacaoService.alugarFilme(usuario, filmes);
			
		//verificacao
		error.checkThat(locacao.getValor(), is(5.0));
		// error.checkThat(locacao.getDataLocacao(), MatchersProprios.ehHoje());
		// error.checkThat(locacao.getDataRetorno(), MatchersProprios.ehHojeComDiferencaDias(1));
		error.checkThat(DataUtils.isMesmaData(locacao.getDataLocacao(), DataUtils.obterData(6, 1, 2023)), is(true));
		error.checkThat(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterData(7, 1, 2023)), is(true));
	}
	
	@Test
	public void deveDevolverNaSegundaAoAlugarNoSabado() throws Exception{

		Usuario usuario = UsuarioBuilder.umUsuario().agora();
		List<Filme> filmes = Arrays.asList(FilmeBuilder.umfilme().agora());

		PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(DataUtils.obterData(7, 1, 2023));
		
		Locacao retorno = locacaoService.alugarFilme(usuario, filmes);

		assertThat(retorno.getDataRetorno(), caiNumaSegunda());
		// PowerMockito.verifyNew(Date.class, Mockito.times(2)).withNoArguments();
		PowerMockito.verifyStatic(Calendar.class, Mockito.times(2));
		Calendar.getInstance();

	}

	@Test
	public void deveAlugarFilmeSemCalcularValor() throws Exception{
		//cenario
		Usuario usuario = UsuarioBuilder.umUsuario().agora();
		List<Filme> filmes = Arrays.asList(FilmeBuilder.umfilme().agora());

		PowerMockito.doReturn(1.0).when(locacaoService, "calcularValorLocacao", filmes);
		
		//acao
		Locacao locacao = locacaoService.alugarFilme(usuario, filmes);

		//verificacao
		Assert.assertThat(locacao.getValor(), is(1.0));
		PowerMockito.verifyPrivate(locacaoService).invoke("calcularValorLocacao", filmes);

	}

	@Test
	public void deveCalcularValorLocacao() throws Exception{
		//cenario
		List<Filme> filmes = Arrays.asList(FilmeBuilder.umfilme().agora());

		//acao
		Double valor = (Double) Whitebox.invokeMethod(locacaoService, "calcularValorLocacao", filmes);

		//verificacao
		assertThat(valor, is(4.0));
	}

}
