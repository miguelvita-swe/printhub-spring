package br.edu.unifio.printhub.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.edu.unifio.printhub.entidades.Produto;

public record ProdutoResposta(
        Integer id,
        String nome,
        String descricao,
        String material,
        String cor,
        BigDecimal preco,
        Integer estoque,
        Integer diasProducao,
        Boolean ativo,
        Integer categoriaId,
        String categoriaNome,
        Integer produtorId,
        String produtorNome,
        LocalDateTime criadoEm) {

    public static ProdutoResposta de(Produto produto) {
        return new ProdutoResposta(produto.getId(), produto.getNome(), produto.getDescricao(), produto.getMaterial(),
                produto.getCor(), produto.getPreco(), produto.getEstoque(), produto.getDiasProducao(),
                produto.getAtivo(), produto.getCategoria().getId(), produto.getCategoria().getNome(),
                produto.getProdutor().getId(), produto.getProdutor().getNome(), produto.getCriadoEm());
    }
}
