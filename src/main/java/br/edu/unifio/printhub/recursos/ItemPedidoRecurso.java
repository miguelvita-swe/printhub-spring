package br.edu.unifio.printhub.recursos;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.unifio.printhub.dtos.ItemPedidoRequisicao;
import br.edu.unifio.printhub.dtos.ItemPedidoResposta;
import br.edu.unifio.printhub.dtos.PedidoResposta;
import br.edu.unifio.printhub.dtos.QuantidadeRequisicao;
import br.edu.unifio.printhub.servicos.PedidoServico;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * CRUD dos itens de um pedido. Os itens só existem dentro de um pedido,
 * por isso a rota fica aninhada: /pedidos/{pedidoId}/itens.
 * As alterações devolvem o pedido inteiro, já com o valor recalculado.
 */
@RestController
@RequestMapping("/pedidos/{pedidoId}/itens")
@RequiredArgsConstructor
public class ItemPedidoRecurso {

    private final PedidoServico pedidoServico;

    @GetMapping
    public List<ItemPedidoResposta> listar(@PathVariable Integer pedidoId) {
        return pedidoServico.listarItens(pedidoId);
    }

    @GetMapping("/{itemId}")
    public ItemPedidoResposta buscarPorId(@PathVariable Integer pedidoId, @PathVariable Integer itemId) {
        return pedidoServico.buscarItem(pedidoId, itemId);
    }

    @PostMapping
    public ResponseEntity<PedidoResposta> adicionar(@PathVariable Integer pedidoId,
            @Valid @RequestBody ItemPedidoRequisicao dados) {
        PedidoResposta pedido = pedidoServico.adicionarItem(pedidoId, dados);
        return ResponseEntity.created(URI.create("/pedidos/" + pedidoId + "/itens")).body(pedido);
    }

    @PutMapping("/{itemId}")
    public PedidoResposta alterarQuantidade(@PathVariable Integer pedidoId, @PathVariable Integer itemId,
            @Valid @RequestBody QuantidadeRequisicao dados) {
        return pedidoServico.alterarQuantidade(pedidoId, itemId, dados.quantidade());
    }

    @DeleteMapping("/{itemId}")
    public PedidoResposta remover(@PathVariable Integer pedidoId, @PathVariable Integer itemId) {
        return pedidoServico.removerItem(pedidoId, itemId);
    }
}
