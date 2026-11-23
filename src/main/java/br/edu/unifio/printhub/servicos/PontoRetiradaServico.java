package br.edu.unifio.printhub.servicos;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.unifio.printhub.dtos.PontoRetiradaRequisicao;
import br.edu.unifio.printhub.dtos.PontoRetiradaResposta;
import br.edu.unifio.printhub.entidades.PontoRetirada;
import br.edu.unifio.printhub.entidades.StatusLote;
import br.edu.unifio.printhub.excecoes.RecursoNaoEncontradoException;
import br.edu.unifio.printhub.excecoes.RegraNegocioException;
import br.edu.unifio.printhub.repositorios.LoteRepositorio;
import br.edu.unifio.printhub.repositorios.PedidoRepositorio;
import br.edu.unifio.printhub.repositorios.PontoRetiradaRepositorio;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PontoRetiradaServico {

    private final PontoRetiradaRepositorio pontoRetiradaRepositorio;
    private final PedidoRepositorio pedidoRepositorio;
    private final LoteRepositorio loteRepositorio;

    @Transactional(readOnly = true)
    public List<PontoRetiradaResposta> listar() {
        return pontoRetiradaRepositorio.findAll(Sort.by("nome")).stream().map(PontoRetiradaResposta::de).toList();
    }

    @Transactional(readOnly = true)
    public PontoRetiradaResposta buscarPorId(Integer id) {
        return PontoRetiradaResposta.de(buscarEntidade(id));
    }

    @Transactional
    public PontoRetiradaResposta criar(PontoRetiradaRequisicao dados) {
        PontoRetirada ponto = new PontoRetirada();
        copiar(dados, ponto);
        return PontoRetiradaResposta.de(pontoRetiradaRepositorio.save(ponto));
    }

    /**
     * Atualiza o ponto. Mudar o custo de entrega não altera lotes já abertos,
     * porque o lote guarda uma cópia do custo no momento em que foi aberto.
     */
    @Transactional
    public PontoRetiradaResposta atualizar(Integer id, PontoRetiradaRequisicao dados) {
        PontoRetirada ponto = buscarEntidade(id);
        // Regra: não dá para desativar um ponto que ainda está juntando pedidos num lote.
        if (Boolean.FALSE.equals(dados.ativo()) && Boolean.TRUE.equals(ponto.getAtivo())
                && loteRepositorio.existsByPontoRetiradaIdAndStatus(id, StatusLote.EM_FORMACAO)) {
            throw new RegraNegocioException(
                    "O ponto " + ponto.getNome() + " tem um lote em formação; feche o lote antes de desativar o ponto");
        }
        copiar(dados, ponto);
        return PontoRetiradaResposta.de(ponto);
    }

    @Transactional
    public void excluir(Integer id) {
        PontoRetirada ponto = buscarEntidade(id);
        // Regra: ponto com pedidos ou lotes faz parte do histórico e não pode sumir.
        if (pedidoRepositorio.existsByPontoRetiradaId(id) || loteRepositorio.existsByPontoRetiradaId(id)) {
            throw new RegraNegocioException("O ponto " + ponto.getNome()
                    + " tem pedidos ou lotes registrados e não pode ser excluído; desative-o com ativo = false");
        }
        pontoRetiradaRepositorio.delete(ponto);
    }

    @Transactional(readOnly = true)
    public PontoRetirada buscarEntidade(Integer id) {
        return pontoRetiradaRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ponto de retirada", id));
    }

    private void copiar(PontoRetiradaRequisicao dados, PontoRetirada ponto) {
        ponto.setNome(dados.nome().trim());
        ponto.setEndereco(dados.endereco().trim());
        ponto.setBairro(dados.bairro().trim());
        ponto.setCidade(dados.cidade().trim());
        ponto.setCustoEntrega(dados.custoEntrega());
        ponto.setAtivo(dados.ativo() == null ? Boolean.TRUE : dados.ativo());
    }
}
