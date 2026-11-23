package br.edu.unifio.printhub.servicos;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.unifio.printhub.dtos.UsuarioRequisicao;
import br.edu.unifio.printhub.dtos.UsuarioResposta;
import br.edu.unifio.printhub.entidades.TipoUsuario;
import br.edu.unifio.printhub.entidades.Usuario;
import br.edu.unifio.printhub.excecoes.RecursoNaoEncontradoException;
import br.edu.unifio.printhub.excecoes.RegraNegocioException;
import br.edu.unifio.printhub.repositorios.PedidoRepositorio;
import br.edu.unifio.printhub.repositorios.ProdutoRepositorio;
import br.edu.unifio.printhub.repositorios.UsuarioRepositorio;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioServico {

    private final UsuarioRepositorio usuarioRepositorio;
    private final ProdutoRepositorio produtoRepositorio;
    private final PedidoRepositorio pedidoRepositorio;

    @Transactional(readOnly = true)
    public List<UsuarioResposta> listar() {
        return usuarioRepositorio.findAll(Sort.by("nome")).stream().map(UsuarioResposta::de).toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioResposta> listarPorTipo(TipoUsuario tipo) {
        return usuarioRepositorio.findByTipoOrderByNome(tipo).stream().map(UsuarioResposta::de).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResposta buscarPorId(Integer id) {
        return UsuarioResposta.de(buscarEntidade(id));
    }

    @Transactional
    public UsuarioResposta criar(UsuarioRequisicao dados) {
        // Regra: o e-mail identifica a pessoa, então não pode repetir.
        if (usuarioRepositorio.existsByEmailIgnoreCase(dados.email())) {
            throw new RegraNegocioException("O e-mail " + dados.email() + " já está cadastrado");
        }
        Usuario usuario = new Usuario();
        usuario.setCriadoEm(LocalDateTime.now());
        copiar(dados, usuario);
        return UsuarioResposta.de(usuarioRepositorio.save(usuario));
    }

    @Transactional
    public UsuarioResposta atualizar(Integer id, UsuarioRequisicao dados) {
        Usuario usuario = buscarEntidade(id);
        if (usuarioRepositorio.existsByEmailIgnoreCaseAndIdNot(dados.email(), id)) {
            throw new RegraNegocioException("O e-mail " + dados.email() + " já está em uso por outro usuário");
        }
        // Regra: produtor com produtos anunciados não pode virar consumidor.
        if (usuario.getTipo() == TipoUsuario.PRODUTOR && dados.tipo() == TipoUsuario.CONSUMIDOR
                && produtoRepositorio.existsByProdutorId(id)) {
            throw new RegraNegocioException(
                    "O usuário tem produtos anunciados e não pode deixar de ser PRODUTOR");
        }
        copiar(dados, usuario);
        return UsuarioResposta.de(usuario);
    }

    @Transactional
    public void excluir(Integer id) {
        Usuario usuario = buscarEntidade(id);
        // Regra: quem já tem produtos ou pedidos fica no histórico; o caminho é desativar (ativo = false).
        if (produtoRepositorio.existsByProdutorId(id) || pedidoRepositorio.existsByConsumidorIdOrProdutorId(id, id)) {
            throw new RegraNegocioException("O usuário " + usuario.getNome()
                    + " tem produtos ou pedidos registrados e não pode ser excluído; desative-o com ativo = false");
        }
        usuarioRepositorio.delete(usuario);
    }

    /** Usado pelos outros services para validar produtor e consumidor. */
    @Transactional(readOnly = true)
    public Usuario buscarEntidade(Integer id) {
        return usuarioRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", id));
    }

    private void copiar(UsuarioRequisicao dados, Usuario usuario) {
        usuario.setNome(dados.nome().trim());
        usuario.setEmail(dados.email().trim().toLowerCase());
        usuario.setTelefone(dados.telefone());
        usuario.setTipo(dados.tipo());
        usuario.setAtivo(dados.ativo() == null ? Boolean.TRUE : dados.ativo());
    }
}
