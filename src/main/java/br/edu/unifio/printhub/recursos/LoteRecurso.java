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

import br.edu.unifio.printhub.dtos.LotePrazoRequisicao;
import br.edu.unifio.printhub.dtos.LoteRequisicao;
import br.edu.unifio.printhub.dtos.LoteResposta;
import br.edu.unifio.printhub.entidades.StatusLote;
import br.edu.unifio.printhub.servicos.LoteServico;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/lotes")
@RequiredArgsConstructor
public class LoteRecurso {

    private final LoteServico loteServico;

    @GetMapping
    public List<LoteResposta> listar() {
        return loteServico.listar();
    }

    @GetMapping("/{id}")
    public LoteResposta buscarPorId(@PathVariable Integer id) {
        return loteServico.buscarPorId(id);
    }

    /** Consulta personalizada: GET /lotes/ponto/1?status=EM_FORMACAO (status é opcional) */
    @GetMapping("/ponto/{pontoId}")
    public List<LoteResposta> listarPorPonto(@PathVariable Integer pontoId,
            @RequestParam(required = false) StatusLote status) {
        return loteServico.listarPorPonto(pontoId, status);
    }

    @PostMapping
    public ResponseEntity<LoteResposta> criar(@Valid @RequestBody LoteRequisicao dados) {
        LoteResposta criado = loteServico.criar(dados);
        return ResponseEntity.created(URI.create("/lotes/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    public LoteResposta atualizarPrazo(@PathVariable Integer id, @Valid @RequestBody LotePrazoRequisicao dados) {
        return loteServico.atualizarPrazo(id, dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        loteServico.excluir(id);
        return ResponseEntity.noContent().build();
    }

    /** Fecha o lote antes de completar 3 pedidos (ex.: venceu o prazo) e divide a entrega. */
    @PostMapping("/{id}/fechar")
    public LoteResposta fechar(@PathVariable Integer id) {
        return loteServico.fechar(id);
    }

    /** Confirma que o lote chegou ao ponto; os pedidos ficam disponíveis para retirada. */
    @PostMapping("/{id}/receber")
    public LoteResposta receberNoPonto(@PathVariable Integer id) {
        return loteServico.receberNoPonto(id);
    }
}
