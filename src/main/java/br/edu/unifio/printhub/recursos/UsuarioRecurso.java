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

import br.edu.unifio.printhub.dtos.UsuarioRequisicao;
import br.edu.unifio.printhub.dtos.UsuarioResposta;
import br.edu.unifio.printhub.entidades.TipoUsuario;
import br.edu.unifio.printhub.servicos.UsuarioServico;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioRecurso {

    private final UsuarioServico usuarioServico;

    @GetMapping
    public List<UsuarioResposta> listar() {
        return usuarioServico.listar();
    }

    @GetMapping("/{id}")
    public UsuarioResposta buscarPorId(@PathVariable Integer id) {
        return usuarioServico.buscarPorId(id);
    }

    /** Consulta personalizada: GET /usuarios/tipo/PRODUTOR */
    @GetMapping("/tipo/{tipo}")
    public List<UsuarioResposta> listarPorTipo(@PathVariable TipoUsuario tipo) {
        return usuarioServico.listarPorTipo(tipo);
    }

    @PostMapping
    public ResponseEntity<UsuarioResposta> criar(@Valid @RequestBody UsuarioRequisicao dados) {
        UsuarioResposta criado = usuarioServico.criar(dados);
        return ResponseEntity.created(URI.create("/usuarios/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    public UsuarioResposta atualizar(@PathVariable Integer id, @Valid @RequestBody UsuarioRequisicao dados) {
        return usuarioServico.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        usuarioServico.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
