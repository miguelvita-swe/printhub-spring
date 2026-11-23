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

import br.edu.unifio.printhub.dtos.PontoRetiradaRequisicao;
import br.edu.unifio.printhub.dtos.PontoRetiradaResposta;
import br.edu.unifio.printhub.servicos.PontoRetiradaServico;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/pontos-retirada")
@RequiredArgsConstructor
public class PontoRetiradaRecurso {

    private final PontoRetiradaServico pontoRetiradaServico;

    @GetMapping
    public List<PontoRetiradaResposta> listar() {
        return pontoRetiradaServico.listar();
    }

    @GetMapping("/{id}")
    public PontoRetiradaResposta buscarPorId(@PathVariable Integer id) {
        return pontoRetiradaServico.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<PontoRetiradaResposta> criar(@Valid @RequestBody PontoRetiradaRequisicao dados) {
        PontoRetiradaResposta criado = pontoRetiradaServico.criar(dados);
        return ResponseEntity.created(URI.create("/pontos-retirada/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    public PontoRetiradaResposta atualizar(@PathVariable Integer id,
            @Valid @RequestBody PontoRetiradaRequisicao dados) {
        return pontoRetiradaServico.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        pontoRetiradaServico.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
