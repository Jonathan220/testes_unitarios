package br.ce.wcaquino.servicos;

import br.ce.wcaquino.entidades.Usuario;

public interface SPCService {
    public boolean possuiNegativado(Usuario usuario) throws Exception;
}
