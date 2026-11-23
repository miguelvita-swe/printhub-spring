package br.edu.unifio.printhub.servicos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.unifio.printhub.dtos.LotePrazoRequisicao;
import br.edu.unifio.printhub.dtos.LoteRequisicao;
import br.edu.unifio.printhub.dtos.LoteResposta;
import br.edu.unifio.printhub.entidades.Lote;
import br.edu.unifio.printhub.entidades.Pedido;
import br.edu.unifio.printhub.entidades.PontoRetirada;
import br.edu.unifio.printhub.entidades.StatusLote;
import br.edu.unifio.printhub.entidades.StatusPedido;
import br.edu.unifio.printhub.excecoes.RecursoNaoEncontradoException;
import br.edu.unifio.printhub.excecoes.RegraNegocioException;
import br.edu.unifio.printhub.repositorios.LoteRepositorio;
import lombok.RequiredArgsConstructor;

/**
 * Funcionalidade específica do PrintHub: o lote coletivo de entrega.
 *
 * Os pedidos aceitos de um mesmo ponto de retirada entram no lote aberto desse ponto.
 * Quando o lote completa 3 pedidos (ou o administrador fecha no prazo), ele fecha e
 * o custo de entrega do ponto é dividido entre os pedidos. Assim cada consumidor
 * paga só uma parte do frete.
 */
@Service
@RequiredArgsConstructor
public class LoteServico {

    private final LoteRepositorio loteRepositorio;
    private final PontoRetiradaServico pontoRetiradaServico;

    // ---------- CRUD ----------

    @Transactional(readOnly = true)
    public List<LoteResposta> listar() {
        return loteRepositorio.findAll(Sort.by(Sort.Direction.DESC, "abertoEm")).stream()
                .map(LoteResposta::de).toList();
    }

    @Transactional(readOnly = true)
    public LoteResposta buscarPorId(Integer id) {
        return LoteResposta.de(buscarEntidade(id));
    }

    /** Consulta personalizada: lotes de um ponto, com filtro opcional de situação. */
    @Transactional(readOnly = true)
    public List<LoteResposta> listarPorPonto(Integer pontoId, StatusLote status) {
        pontoRetiradaServico.buscarEntidade(pontoId); // 404 se o ponto não existir
        List<Lote> lotes = status == null
                ? loteRepositorio.findByPontoRetiradaIdOrderByAbertoEmDesc(pontoId)
                : loteRepositorio.findByPontoRetiradaIdAndStatusOrderByAbertoEmDesc(pontoId, status);
        return lotes.stream().map(LoteResposta::de).toList();
    }

    /** Abertura manual de um lote para um ponto. */
    @Transactional
    public LoteResposta criar(LoteRequisicao dados) {
        PontoRetirada ponto = pontoRetiradaServico.buscarEntidade(dados.pontoRetiradaId());
        if (!ponto.getAtivo()) {
            throw new RegraNegocioException("O ponto " + ponto.getNome() + " está desativado");
        }
        // Regra: cada ponto tem no máximo um lote em formação por vez.
        if (loteRepositorio.existsByPontoRetiradaIdAndStatus(ponto.getId(), StatusLote.EM_FORMACAO)) {
            throw new RegraNegocioException("O ponto " + ponto.getNome() + " já tem um lote em formação");
        }
        Lote lote = novoLote(ponto);
        if (dados.prazo() != null) {
            lote.setPrazo(dados.prazo());
        }
        return LoteResposta.de(loteRepositorio.save(lote));
    }

    /** Muda o prazo de um lote que ainda está juntando pedidos. */
    @Transactional
    public LoteResposta atualizarPrazo(Integer id, LotePrazoRequisicao dados) {
        Lote lote = buscarEntidade(id);
        exigirEmFormacao(lote, "ter o prazo alterado");
        lote.setPrazo(dados.prazo());
        return LoteResposta.de(lote);
    }

    @Transactional
    public void excluir(Integer id) {
        Lote lote = buscarEntidade(id);
        // Regra: só um lote vazio pode ser excluído; com pedidos ele faz parte do histórico.
        if (!lote.getPedidos().isEmpty()) {
            throw new RegraNegocioException("O lote " + codigo(lote) + " tem " + lote.getPedidos().size()
                    + " pedido(s) e não pode ser excluído");
        }
        loteRepositorio.delete(lote);
    }

    // ---------- Operações do lote coletivo ----------

    /** Fechamento manual, por exemplo quando vence o prazo de 7 dias sem completar 3 pedidos. */
    @Transactional
    public LoteResposta fechar(Integer id) {
        Lote lote = buscarEntidade(id);
        exigirEmFormacao(lote, "ser fechado");
        if (lote.getPedidos().isEmpty()) {
            throw new RegraNegocioException("O lote " + codigo(lote) + " não tem pedidos para fechar");
        }
        fecharEDividirEntrega(lote);
        return LoteResposta.de(lote);
    }

    /** O lote chegou ao ponto: os pedidos ficam disponíveis para o consumidor retirar. */
    @Transactional
    public LoteResposta receberNoPonto(Integer id) {
        Lote lote = buscarEntidade(id);
        if (lote.getStatus() != StatusLote.FECHADO) {
            throw new RegraNegocioException("Só um lote FECHADO pode ser recebido no ponto; o lote "
                    + codigo(lote) + " está " + lote.getStatus());
        }
        lote.setStatus(StatusLote.ENTREGUE_NO_PONTO);
        for (Pedido pedido : lote.getPedidos()) {
            pedido.setStatus(StatusPedido.DISPONIVEL_RETIRADA);
        }
        return LoteResposta.de(lote);
    }

    // ---------- Usados pelo PedidoServico ----------

    /** Devolve o lote em formação do ponto ou abre um novo, copiando o custo de entrega atual. */
    @Transactional
    public Lote obterLoteAberto(PontoRetirada ponto) {
        return loteRepositorio.findFirstByPontoRetiradaIdAndStatus(ponto.getId(), StatusLote.EM_FORMACAO)
                .orElseGet(() -> loteRepositorio.save(novoLote(ponto)));
    }

    /** Coloca o pedido no lote. No 3º pedido, o lote fecha e a entrega é dividida. */
    @Transactional
    public void adicionarPedido(Lote lote, Pedido pedido) {
        exigirEmFormacao(lote, "receber pedidos");
        lote.getPedidos().add(pedido);
        pedido.setLote(lote);
        pedido.setPosicaoLote(lote.getPedidos().size());
        if (lote.getPedidos().size() >= Lote.LIMITE_PEDIDOS) {
            fecharEDividirEntrega(lote);
        }
    }

    /** Tira um pedido cancelado do lote e reorganiza as posições dos que ficaram. */
    @Transactional
    public void removerPedido(Lote lote, Pedido pedido) {
        exigirEmFormacao(lote, "ter pedidos removidos");
        lote.getPedidos().remove(pedido);
        pedido.setLote(null);
        pedido.setPosicaoLote(null);
        int posicao = 1;
        for (Pedido restante : lote.getPedidos()) {
            restante.setPosicaoLote(posicao++);
        }
    }

    /** Quando todos os pedidos do lote foram retirados, o lote está concluído. */
    @Transactional
    public void concluirSeTodosRetirados(Lote lote) {
        boolean todosRetirados = lote.getPedidos().stream()
                .allMatch(pedido -> pedido.getStatus() == StatusPedido.RETIRADO);
        if (todosRetirados) {
            lote.setStatus(StatusLote.CONCLUIDO);
        }
    }

    @Transactional(readOnly = true)
    public Lote buscarEntidade(Integer id) {
        return loteRepositorio.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Lote", id));
    }

    // ---------- Auxiliares ----------

    private void fecharEDividirEntrega(Lote lote) {
        lote.setStatus(StatusLote.FECHADO);
        lote.setFechadoEm(LocalDateTime.now());
        List<BigDecimal> taxas = RateioEntrega.dividir(lote.getCustoEntrega(), lote.getPedidos().size());
        for (int i = 0; i < lote.getPedidos().size(); i++) {
            lote.getPedidos().get(i).setTaxaEntrega(taxas.get(i));
        }
    }

    private Lote novoLote(PontoRetirada ponto) {
        LocalDateTime agora = LocalDateTime.now();
        Lote lote = new Lote();
        lote.setPontoRetirada(ponto);
        lote.setCustoEntrega(ponto.getCustoEntrega());
        lote.setStatus(StatusLote.EM_FORMACAO);
        lote.setAbertoEm(agora);
        lote.setPrazo(agora.plusDays(Lote.DIAS_ABERTO));
        return lote;
    }

    private void exigirEmFormacao(Lote lote, String acao) {
        if (lote.getStatus() != StatusLote.EM_FORMACAO) {
            throw new RegraNegocioException("O lote " + codigo(lote) + " está " + lote.getStatus()
                    + " e não pode mais " + acao);
        }
    }

    private String codigo(Lote lote) {
        return String.format("LOTE-%03d", lote.getId());
    }
}
