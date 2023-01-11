package br.ce.wcaquino.servicos;

import static br.ce.wcaquino.utils.DataUtils.adicionarDias;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.ce.wcaquino.daos.LocacaoDao;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocacaoException;
import br.ce.wcaquino.utils.DataUtils;

public class LocacaoService {
	
	private LocacaoDao dao;

	private SPCService spcService;
	private EmailService emailService;

	public Locacao alugarFilme(Usuario usuario, List<Filme> filmes) throws LocacaoException, FilmeSemEstoqueException {

		if(usuario == null){
			throw new LocacaoException("Usuario vazio");
		}

		if(filmes == null || filmes.isEmpty()){
			throw new LocacaoException("Filme vazio");
		}

		for(Filme filme: filmes){
			if(filme.getEstoque() == 0){
				throw new FilmeSemEstoqueException();
			}
		}

		boolean negativado;

		try {
			negativado = spcService.possuiNegativado(usuario);
		} catch (Exception e) {
			throw new LocacaoException("Problemas com o SPC, tente novamente");
		}

		if(negativado){
			throw new LocacaoException("Usuario negativado");
		}

		Locacao locacao = new Locacao();
		locacao.setFilmes(filmes);
		locacao.setUsuario(usuario);
		locacao.setDataLocacao(Calendar.getInstance().getTime());
		Double valorTotal = calcularValorLocacao(filmes);
		locacao.setValor(valorTotal);

		//Entrega no dia seguinte
		Date dataEntrega = Calendar.getInstance().getTime();
		dataEntrega = adicionarDias(dataEntrega, 1);
		if(DataUtils.verificarDiaSemana(dataEntrega, Calendar.SUNDAY)){
			dataEntrega = adicionarDias(dataEntrega, 1);
		}
		locacao.setDataRetorno(dataEntrega);
		
		//Salvando a locacao...	
		dao.salvar(locacao);
		
		return locacao;
	}

	private Double calcularValorLocacao(List<Filme> filmes) {
		System.out.println("Entrou aqui");
		Double valorTotal = 0d;
		for(int i = 0; i < filmes.size(); i++){
			Filme filme = filmes.get(i);
			double valorFilme = filme.getPrecoLocacao();
			switch(i){
				case 2 : valorFilme = valorFilme * 0.75; break;
				case 3 : valorFilme = valorFilme * 0.5; break;
				case 4 : valorFilme = valorFilme * 0.25; break;
				case 5 : valorFilme = 0; break;
			}
			valorTotal = valorTotal + valorFilme;
		}
		return valorTotal;
	}

	public void notificarAtrasos(){
		List<Locacao> locacoes = dao.obterLocacoesPendentes();
		for(Locacao locacao : locacoes){
			if(locacao.getDataRetorno().before(new Date())){
				emailService.notificarAtraso(locacao.getUsuario());
			}
		}
	}

	public void prorrogarLocacao(Locacao locacao, int dias){
		Locacao novaLocacao = new Locacao();
		novaLocacao.setUsuario(locacao.getUsuario());
		novaLocacao.setFilmes(locacao.getFilmes());
		novaLocacao.setDataLocacao(new Date());
		novaLocacao.setDataRetorno(DataUtils.obterDataComDiferencaDias(dias));
		novaLocacao.setValor(locacao.getValor() * dias);
		dao.salvar(novaLocacao);
	}

}