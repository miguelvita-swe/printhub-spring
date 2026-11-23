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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.edu.unifio.printhub.dtos.PedidoRequisicao;
import br.edu.unifio.printhub.dtos.PedidoResposta;
import br.edu.unifio.printhub.dtos.RejeicaoRequisicao;
import br.edu.unifio.printhub.entidades.StatusPedido;
import br.edu.unifio.printhub.servicos.PedidoServico;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/pedidos")
@RequiredArgsConstructor
public class PedidoRecurso {

    private final PedidoServico pedidoServico;

    @GetMapping
    public List<PedidoResposta> listar() {
        return pedidoServico.listar();
    }

    @GetMapping("/{id}")
    public PedidoResposta buscarPorId(@PathVariable Integer id) {
        return pedidoServico.buscarPorId(id);
    }

    /** Consulta personalizada: GET /pedidos/status/AGUARDANDO_ACEITACAO */
    @GetMapping("/status/{status}")
    public List<PedidoResposta> listarPorStatus(@PathVariable StatusPedido status) {
        return pedidoServico.listarPorStatus(status);
    }

    /** Consulta personalizada (JPQL): GET /pedidos/consumidor/3?status=ACEITO (status é opcional) */
    @GetMapping("/consumidor/{consumidorId}")
    public List<PedidoResposta> listarDoConsumidor(@PathVariable Integer consumidorId,
            @RequestParam(required = false) StatusPedido status) {
        return pedidoServico.listarDoConsumidor(consumidorId, status);
    }

    @PostMapping
    public ResponseEntity<PedidoResposta> criar(@Valid @RequestBody PedidoRequisicao dados) {
        PedidoResposta criado = pedidoServico.criar(dados);
        return ResponseEntity.created(URI.create("/pedidos/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    public PedidoResposta atualizar(@PathVariable Integer id, @Valid @RequestBody PedidoRequisicao dados) {
        return pedidoServico.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        pedidoServico.excluir(id);
        return ResponseEntity.noContent().build();
    }

    // ----- Fluxo do pedido -----

    /** PRODUTOR aceita: baixa o estoque e coloca o pedido no lote coletivo do ponto. */
    @PostMapping("/{id}/aceitar")
    public PedidoResposta aceitar(@PathVariable Integer id) {
        return pedidoServico.aceitar(id);
    }

    /** PRODUTOR recusa, informando o motivo. */
    @PostMapping("/{id}/rejeitar")
    public PedidoResposta rejeitar(@PathVariable Integer id, @Valid @RequestBody RejeicaoRequisicao dados) {
        return pedidoServico.rejeitar(id, dados.motivo());
    }

    /** CONSUMIDOR desiste (até o lote fechar). */
    @PostMapping("/{id}/cancelar")
    public PedidoResposta cancelar(@PathVariable Integer id) {
        return pedidoServico.cancelar(id);
    }

    /** ADMIN (atendente do ponto) confirma que o consumidor retirou o pedido. */
    @PostMapping("/{id}/retirar")
    public PedidoResposta retirar(@PathVariable Integer id) {
        return pedidoServico.retirar(id);
    }
}
