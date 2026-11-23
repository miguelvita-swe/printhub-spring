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

import br.edu.unifio.printhub.dtos.CategoriaRequisicao;
import br.edu.unifio.printhub.dtos.CategoriaResposta;
import br.edu.unifio.printhub.servicos.CategoriaServico;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller REST de categorias. Só recebe a requisição e repassa ao service;
 * não acessa o repositório diretamente.
 */
@RestController
@RequestMapping("/categorias")
@RequiredArgsConstructor
public class CategoriaRecurso {

    private final CategoriaServico categoriaServico;

    @GetMapping
    public List<CategoriaResposta> listar() {
        return categoriaServico.listar();
    }

    @GetMapping("/{id}")
    public CategoriaResposta buscarPorId(@PathVariable Integer id) {
        return categoriaServico.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<CategoriaResposta> criar(@Valid @RequestBody CategoriaRequisicao dados) {
        CategoriaResposta criada = categoriaServico.criar(dados);
        return ResponseEntity.created(URI.create("/categorias/" + criada.id())).body(criada);
    }

    @PutMapping("/{id}")
    public CategoriaResposta atualizar(@PathVariable Integer id, @Valid @RequestBody CategoriaRequisicao dados) {
        return categoriaServico.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        categoriaServico.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
