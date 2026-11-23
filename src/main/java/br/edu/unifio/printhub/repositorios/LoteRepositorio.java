package br.edu.unifio.printhub.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.unifio.printhub.entidades.Lote;
import br.edu.unifio.printhub.entidades.StatusLote;

public interface LoteRepositorio extends JpaRepository<Lote, Integer> {

    /** Lote aberto (em formação) de um ponto, se existir. Cada ponto tem no máximo um. */
    Optional<Lote> findFirstByPontoRetiradaIdAndStatus(Integer pontoRetiradaId, StatusLote status);

    /** Consulta personalizada (método derivado): lotes de um ponto numa situação. */
    List<Lote> findByPontoRetiradaIdAndStatusOrderByAbertoEmDesc(Integer pontoRetiradaId, StatusLote status);

    /** Consulta personalizada (método derivado): todos os lotes de um ponto. */
    List<Lote> findByPontoRetiradaIdOrderByAbertoEmDesc(Integer pontoRetiradaId);

    boolean existsByPontoRetiradaIdAndStatus(Integer pontoRetiradaId, StatusLote status);

    boolean existsByPontoRetiradaId(Integer pontoRetiradaId);
}
