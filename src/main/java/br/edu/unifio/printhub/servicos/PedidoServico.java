package br.edu.unifio.printhub.servicos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.unifio.printhub.dtos.ItemPedidoRequisicao;
import br.edu.unifio.printhub.dtos.ItemPedidoResposta;
import br.edu.unifio.printhub.dtos.PedidoRequisicao;
import br.edu.unifio.printhub.dtos.PedidoResposta;
import br.edu.unifio.printhub.entidades.ItemPedido;
import br.edu.unifio.printhub.entidades.Lote;
import br.edu.unifio.printhub.entidades.Pedido;
import br.edu.unifio.printhub.entidades.PontoRetirada;
import br.edu.unifio.printhub.entidades.Produto;
import br.edu.unifio.printhub.entidades.StatusLote;
import br.edu.unifio.printhub.entidades.StatusPedido;
import br.edu.unifio.printhub.entidades.Usuario;
import br.edu.unifio.printhub.excecoes.RecursoNaoEncontradoException;
import br.edu.unifio.printhub.excecoes.RegraNegocioException;
import br.edu.unifio.printhub.repositorios.PedidoRepositorio;
import lombok.RequiredArgsConstructor;

/**
 * Regras do pedido: criação, alteração, itens e mudanças de situação
 * (aceitar, rejeitar, cancelar e retirar).
 */
@Service
@RequiredArgsConstructor
public class PedidoServico {

    private final PedidoRepositorio pedidoRepositorio;
    private final UsuarioServico usuarioServico;
    private final ProdutoServico produtoServico;
    private final PontoRetiradaServico pontoRetiradaServico;
    private final LoteServico loteServico;

    // ---------- Consultas ----------

    @Transactional(readOnly = true)
    public List<PedidoResposta> listar() {
        return pedidoRepositorio.findAll(Sort.by(Sort.Direction.DESC, "criadoEm")).stream()
                .map(PedidoResposta::de).toList();
    }

    @Transactional(readOnly = true)
    public PedidoResposta buscarPorId(Integer id) {
        return PedidoResposta.de(buscarEntidade(id));
    }

    /** Consulta personalizada: pedidos numa situação. */
    @Transactional(readOnly = true)
    public List<PedidoResposta> listarPorStatus(StatusPedido status) {
        return pedidoRepositorio.findByStatusOrderByCriadoEmDesc(status).stream().map(PedidoResposta::de).toList();
    }

    /** Consulta personalizada (JPQL): pedidos de um consumidor, com filtro opcional de situação. */
    @Transactional(readOnly = true)
    public List<PedidoResposta> listarDoConsumidor(Integer consumidorId, StatusPedido status) {
        usuarioServico.buscarEntidade(consumidorId); // 404 se o consumidor não existir
        return pedidoRepositorio.buscarDoConsumidor(consumidorId, status).stream().map(PedidoResposta::de).toList();
    }

    // ---------- CRUD do pedido ----------

    @Transactional
    public PedidoResposta criar(PedidoRequisicao dados) {
        Pedido pedido = new Pedido();
        pedido.setConsumidor(validarConsumidor(dados.consumidorId()));
        pedido.setPontoRetirada(validarPonto(dados.pontoRetiradaId()));
        pedido.setStatus(StatusPedido.AGUARDANDO_ACEITACAO);
        pedido.setCriadoEm(LocalDateTime.now());
        definirItens(pedido, dados.itens());
        return PedidoResposta.de(pedidoRepositorio.save(pedido));
    }

    /** Refaz o pedido (ponto e itens). Só enquanto o produtor ainda não respondeu. */
    @Transactional
    public PedidoResposta atualizar(Integer id, PedidoRequisicao dados) {
        Pedido pedido = buscarEntidade(id);
        exigirAguardandoAceitacao(pedido, "ser alterado");
        if (!pedido.getConsumidor().getId().equals(dados.consumidorId())) {
            throw new RegraNegocioException("O consumidor de um pedido não pode ser trocado");
        }
        pedido.setPontoRetirada(validarPonto(dados.pontoRetiradaId()));
        definirItens(pedido, dados.itens());
        return PedidoResposta.de(pedidoRepositorio.saveAndFlush(pedido));
    }

    @Transactional
    public void excluir(Integer id) {
        Pedido pedido = buscarEntidade(id);
        // Regra: pedido que entrou num lote faz parte da entrega e do rateio; só sai cancelando.
        Set<StatusPedido> podeExcluir = Set.of(StatusPedido.AGUARDANDO_ACEITACAO, StatusPedido.REJEITADO,
                StatusPedido.CANCELADO);
        if (!podeExcluir.contains(pedido.getStatus())) {
            throw new RegraNegocioException("O pedido " + id + " está " + pedido.getStatus()
                    + " e não pode ser excluído; só pedidos aguardando aceitação, rejeitados ou cancelados");
        }
        pedidoRepositorio.delete(pedido);
    }

    // ---------- Itens do pedido ----------

    @Transactional(readOnly = true)
    public List<ItemPedidoResposta> listarItens(Integer pedidoId) {
        return buscarEntidade(pedidoId).getItens().stream().map(ItemPedidoResposta::de).toList();
    }

    @Transactional(readOnly = true)
    public ItemPedidoResposta buscarItem(Integer pedidoId, Integer itemId) {
        return ItemPedidoResposta.de(buscarItemEntidade(buscarEntidade(pedidoId), itemId));
    }

    @Transactional
    public PedidoResposta adicionarItem(Integer pedidoId, ItemPedidoRequisicao dados) {
        Pedido pedido = buscarEntidade(pedidoId);
        exigirAguardandoAceitacao(pedido, "receber itens");
        boolean jaTem = pedido.getItens().stream().anyMatch(i -> i.getProduto().getId().equals(dados.produtoId()));
        if (jaTem) {
            throw new RegraNegocioException("O produto " + dados.produtoId()
                    + " já está no pedido; altere a quantidade do item existente");
        }
        Produto produto = produtoServico.buscarEntidade(dados.produtoId());
        validarProduto(produto, dados.quantidade(), pedido.getConsumidor());
        if (!produto.getProdutor().getId().equals(pedido.getProdutor().getId())) {
            throw new RegraNegocioException("Todos os itens de um pedido precisam ser do mesmo produtor ("
                    + pedido.getProdutor().getNome() + ")");
        }
        pedido.getItens().add(novoItem(pedido, produto, dados.quantidade()));
        recalcularValor(pedido);
        // flush grava agora para o item novo já voltar com id na resposta.
        return PedidoResposta.de(pedidoRepositorio.saveAndFlush(pedido));
    }

    @Transactional
    public PedidoResposta alterarQuantidade(Integer pedidoId, Integer itemId, Integer quantidade) {
        Pedido pedido = buscarEntidade(pedidoId);
        exigirAguardandoAceitacao(pedido, "ter itens alterados");
        ItemPedido item = buscarItemEntidade(pedido, itemId);
        validarEstoque(item.getProduto(), quantidade);
        item.setQuantidade(quantidade);
        recalcularValor(pedido);
        return PedidoResposta.de(pedido);
    }

    @Transactional
    public PedidoResposta removerItem(Integer pedidoId, Integer itemId) {
        Pedido pedido = buscarEntidade(pedidoId);
        exigirAguardandoAceitacao(pedido, "ter itens removidos");
        ItemPedido item = buscarItemEntidade(pedido, itemId);
        if (pedido.getItens().size() == 1) {
            throw new RegraNegocioException("O pedido precisa de pelo menos um item; para desistir, cancele o pedido");
        }
        pedido.getItens().remove(item);
        recalcularValor(pedido);
        return PedidoResposta.de(pedido);
    }

    // ---------- Mudanças de situação ----------

    /**
     * O produtor aceita o pedido: o estoque é reservado (baixado) e o pedido
     * entra no lote coletivo do ponto de retirada. No 3º pedido, o lote fecha.
     */
    @Transactional
    public PedidoResposta aceitar(Integer id) {
        Pedido pedido = buscarEntidade(id);
        exigirAguardandoAceitacao(pedido, "ser aceito");
        if (!pedido.getPontoRetirada().getAtivo()) {
            throw new RegraNegocioException("O ponto " + pedido.getPontoRetirada().getNome()
                    + " foi desativado; o pedido não pode ser aceito");
        }
        // O estoque pode ter mudado desde a criação do pedido: confere tudo antes de baixar qualquer item.
        for (ItemPedido item : pedido.getItens()) {
            validarEstoque(item.getProduto(), item.getQuantidade());
        }
        for (ItemPedido item : pedido.getItens()) {
            Produto produto = item.getProduto();
            produto.setEstoque(produto.getEstoque() - item.getQuantidade());
        }
        pedido.setStatus(StatusPedido.ACEITO);
        pedido.setAceitoEm(LocalDateTime.now());

        Lote lote = loteServico.obterLoteAberto(pedido.getPontoRetirada());
        loteServico.adicionarPedido(lote, pedido);
        return PedidoResposta.de(pedido);
    }

    @Transactional
    public PedidoResposta rejeitar(Integer id, String motivo) {
        Pedido pedido = buscarEntidade(id);
        exigirAguardandoAceitacao(pedido, "ser rejeitado");
        pedido.setStatus(StatusPedido.REJEITADO);
        pedido.setMotivoRejeicao(motivo.trim());
        return PedidoResposta.de(pedido);
    }

    /**
     * O consumidor desiste. Pode cancelar enquanto o pedido aguarda aceitação ou,
     * se já foi aceito, enquanto o lote ainda está em formação. Nesse caso o estoque volta.
     */
    @Transactional
    public PedidoResposta cancelar(Integer id) {
        Pedido pedido = buscarEntidade(id);
        if (pedido.getStatus() == StatusPedido.ACEITO && pedido.getLote() != null
                && pedido.getLote().getStatus() == StatusLote.EM_FORMACAO) {
            for (ItemPedido item : pedido.getItens()) {
                Produto produto = item.getProduto();
                produto.setEstoque(produto.getEstoque() + item.getQuantidade());
            }
            loteServico.removerPedido(pedido.getLote(), pedido);
        } else if (pedido.getStatus() != StatusPedido.AGUARDANDO_ACEITACAO) {
            throw new RegraNegocioException("O pedido " + id + " está " + pedido.getStatus()
                    + " e não pode mais ser cancelado; o cancelamento vale até o lote fechar");
        }
        pedido.setStatus(StatusPedido.CANCELADO);
        return PedidoResposta.de(pedido);
    }

    /** O consumidor retirou o pedido no ponto. */
    @Transactional
    public PedidoResposta retirar(Integer id) {
        Pedido pedido = buscarEntidade(id);
        if (pedido.getStatus() != StatusPedido.DISPONIVEL_RETIRADA) {
            throw new RegraNegocioException("O pedido " + id + " está " + pedido.getStatus()
                    + "; só pedidos DISPONIVEL_RETIRADA podem ser retirados");
        }
        pedido.setStatus(StatusPedido.RETIRADO);
        pedido.setRetiradoEm(LocalDateTime.now());
        loteServico.concluirSeTodosRetirados(pedido.getLote());
        return PedidoResposta.de(pedido);
    }

    @Transactional(readOnly = true)
    public Pedido buscarEntidade(Integer id) {
        return pedidoRepositorio.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Pedido", id));
    }

    // ---------- Regras auxiliares ----------

    /**
     * Valida e grava os itens do pedido. Itens de produtos que continuam no pedido são
     * atualizados no lugar; os outros são removidos ou incluídos.
     */
    private void definirItens(Pedido pedido, List<ItemPedidoRequisicao> itensPedidos) {
        Set<Integer> vistos = new HashSet<>();
        for (ItemPedidoRequisicao item : itensPedidos) {
            if (!vistos.add(item.produtoId())) {
                throw new RegraNegocioException("O produto " + item.produtoId()
                        + " aparece mais de uma vez; use um único item com a quantidade total");
            }
        }

        Usuario produtor = null;
        Map<Integer, Integer> quantidades = itensPedidos.stream()
                .collect(Collectors.toMap(ItemPedidoRequisicao::produtoId, ItemPedidoRequisicao::quantidade));
        Map<Integer, Produto> produtos = itensPedidos.stream()
                .map(item -> produtoServico.buscarEntidade(item.produtoId()))
                .collect(Collectors.toMap(Produto::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));

        for (Produto produto : produtos.values()) {
            validarProduto(produto, quantidades.get(produto.getId()), pedido.getConsumidor());
            // Regra: um pedido vai para um único produtor, que aceita ou rejeita o pedido inteiro.
            if (produtor == null) {
                produtor = produto.getProdutor();
            } else if (!produtor.getId().equals(produto.getProdutor().getId())) {
                throw new RegraNegocioException(
                        "Todos os itens de um pedido precisam ser do mesmo produtor; faça um pedido para cada produtor");
            }
        }
        pedido.setProdutor(produtor);

        pedido.getItens().removeIf(item -> !produtos.containsKey(item.getProduto().getId()));
        for (Produto produto : produtos.values()) {
            Integer quantidade = quantidades.get(produto.getId());
            pedido.getItens().stream()
                    .filter(item -> item.getProduto().getId().equals(produto.getId()))
                    .findFirst()
                    .ifPresentOrElse(item -> {
                        item.setQuantidade(quantidade);
                        item.setPrecoUnitario(produto.getPreco());
                    }, () -> pedido.getItens().add(novoItem(pedido, produto, quantidade)));
        }
        recalcularValor(pedido);
    }

    private void validarProduto(Produto produto, Integer quantidade, Usuario consumidor) {
        if (!produto.getAtivo()) {
            throw new RegraNegocioException("O produto " + produto.getNome() + " não está mais à venda");
        }
        // Regra: o produtor não pode comprar o próprio produto.
        if (produto.getProdutor().getId().equals(consumidor.getId())) {
            throw new RegraNegocioException("O usuário " + consumidor.getNome()
                    + " é o produtor de " + produto.getNome() + " e não pode comprar o próprio produto");
        }
        validarEstoque(produto, quantidade);
    }

    /** Regra: não se vende mais do que o estoque disponível. */
    private void validarEstoque(Produto produto, Integer quantidade) {
        if (quantidade > produto.getEstoque()) {
            throw new RegraNegocioException("Estoque insuficiente para " + produto.getNome() + ": pedido "
                    + quantidade + ", disponível " + produto.getEstoque());
        }
    }

    private Usuario validarConsumidor(Integer consumidorId) {
        Usuario consumidor = usuarioServico.buscarEntidade(consumidorId);
        if (!consumidor.getAtivo()) {
            throw new RegraNegocioException("O usuário " + consumidor.getNome() + " está desativado");
        }
        return consumidor;
    }

    private PontoRetirada validarPonto(Integer pontoId) {
        PontoRetirada ponto = pontoRetiradaServico.buscarEntidade(pontoId);
        if (!ponto.getAtivo()) {
            throw new RegraNegocioException("O ponto " + ponto.getNome() + " não está recebendo pedidos");
        }
        return ponto;
    }

    private ItemPedido novoItem(Pedido pedido, Produto produto, Integer quantidade) {
        ItemPedido item = new ItemPedido();
        item.setPedido(pedido);
        item.setProduto(produto);
        item.setQuantidade(quantidade);
        item.setPrecoUnitario(produto.getPreco());
        return item;
    }

    private ItemPedido buscarItemEntidade(Pedido pedido, Integer itemId) {
        return pedido.getItens().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Item " + itemId + " não foi encontrado no pedido " + pedido.getId()));
    }

    private void recalcularValor(Pedido pedido) {
        BigDecimal total = pedido.getItens().stream()
                .map(ItemPedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        pedido.setValorProdutos(total);
    }

    private void exigirAguardandoAceitacao(Pedido pedido, String acao) {
        if (pedido.getStatus() != StatusPedido.AGUARDANDO_ACEITACAO) {
            throw new RegraNegocioException("O pedido " + pedido.getId() + " está " + pedido.getStatus()
                    + " e só pode " + acao + " enquanto aguarda aceitação");
        }
    }
}
