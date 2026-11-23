package br.edu.unifio.printhub.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.edu.unifio.printhub.entidades.Pedido;
import br.edu.unifio.printhub.entidades.StatusPedido;

public interface PedidoRepositorio extends JpaRepository<Pedido, Integer> {

    /** Consulta personalizada (método derivado): pedidos numa situação, do mais novo ao mais antigo. */
    List<Pedido> findByStatusOrderByCriadoEmDesc(StatusPedido status);

    /**
     * Consulta personalizada (JPQL com @Query): pedidos que um consumidor fez,
     * com filtro opcional de situação (quando status for nulo, traz todos).
     */
    @Query("""
            select p from Pedido p
            where p.consumidor.id = :consumidorId
              and (:status is null or p.status = :status)
            order by p.criadoEm desc
            """)
    List<Pedido> buscarDoConsumidor(@Param("consumidorId") Integer consumidorId,
            @Param("status") StatusPedido status);

    boolean existsByPontoRetiradaId(Integer pontoRetiradaId);

    boolean existsByConsumidorIdOrProdutorId(Integer consumidorId, Integer produtorId);
}
