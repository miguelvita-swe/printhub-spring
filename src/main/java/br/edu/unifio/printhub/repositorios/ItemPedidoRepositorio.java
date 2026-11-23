package br.edu.unifio.printhub.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.unifio.printhub.entidades.ItemPedido;

public interface ItemPedidoRepositorio extends JpaRepository<ItemPedido, Integer> {

    boolean existsByProdutoId(Integer produtoId);
}
