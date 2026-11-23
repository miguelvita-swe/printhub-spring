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

import br.edu.unifio.printhub.dtos.ProdutoRequisicao;
import br.edu.unifio.printhub.dtos.ProdutoResposta;
import br.edu.unifio.printhub.servicos.ProdutoServico;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/produtos")
@RequiredArgsConstructor
public class ProdutoRecurso {

    private final ProdutoServico produtoServico;

    @GetMapping
    public List<ProdutoResposta> listar() {
        return produtoServico.listar();
    }

    @GetMapping("/{id}")
    public ProdutoResposta buscarPorId(@PathVariable Integer id) {
        return produtoServico.buscarPorId(id);
    }

    /** Consulta personalizada: GET /produtos/categoria/1 */
    @GetMapping("/categoria/{categoriaId}")
    public List<ProdutoResposta> listarPorCategoria(@PathVariable Integer categoriaId) {
        return produtoServico.listarPorCategoria(categoriaId);
    }

    /** Consulta personalizada: GET /produtos/busca?nome=vaso */
    @GetMapping("/busca")
    public List<ProdutoResposta> buscarPorNome(
            @RequestParam @NotBlank(message = "Informe parte do nome para buscar") String nome) {
        return produtoServico.buscarPorNome(nome);
    }

    /** Consulta personalizada (JPQL): GET /produtos/estoque-baixo?limite=5 */
    @GetMapping("/estoque-baixo")
    public List<ProdutoResposta> listarComEstoqueBaixo(
            @RequestParam(defaultValue = "5") @Min(value = 1, message = "O limite deve ser pelo menos 1") Integer limite) {
        return produtoServico.listarComEstoqueBaixo(limite);
    }

    @PostMapping
    public ResponseEntity<ProdutoResposta> criar(@Valid @RequestBody ProdutoRequisicao dados) {
        ProdutoResposta criado = produtoServico.criar(dados);
        return ResponseEntity.created(URI.create("/produtos/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    public ProdutoResposta atualizar(@PathVariable Integer id, @Valid @RequestBody ProdutoRequisicao dados) {
        return produtoServico.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        produtoServico.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
