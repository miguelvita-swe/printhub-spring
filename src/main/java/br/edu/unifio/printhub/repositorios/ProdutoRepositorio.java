package br.edu.unifio.printhub.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.edu.unifio.printhub.entidades.Produto;

public interface ProdutoRepositorio extends JpaRepository<Produto, Integer> {

    /** Consulta personalizada (método derivado): produtos de uma categoria, em ordem alfabética. */
    List<Produto> findByCategoriaIdOrderByNome(Integer categoriaId);

    /** Consulta personalizada (método derivado): busca por parte do nome, sem diferenciar maiúsculas. */
    List<Produto> findByNomeContainingIgnoreCaseOrderByNome(String nome);

    /**
     * Consulta personalizada (JPQL com @Query): produtos ativos com estoque abaixo do limite,
     * do menor estoque para o maior. Ajuda o produtor a saber o que precisa imprimir.
     */
    @Query("select p from Produto p where p.ativo = true and p.estoque < :limite order by p.estoque, p.nome")
    List<Produto> buscarComEstoqueAbaixoDe(@Param("limite") Integer limite);

    boolean existsByCategoriaId(Integer categoriaId);

    boolean existsByProdutorId(Integer produtorId);
}
