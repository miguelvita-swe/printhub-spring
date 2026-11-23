package br.edu.unifio.printhub.dtos;

import br.edu.unifio.printhub.entidades.Categoria;

/** O que a API devolve sobre uma categoria. */
public record CategoriaResposta(Integer id, String nome, String descricao) {

    public static CategoriaResposta de(Categoria categoria) {
        return new CategoriaResposta(categoria.getId(), categoria.getNome(), categoria.getDescricao());
    }
}
