package br.edu.unifio.printhub.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.unifio.printhub.entidades.TipoUsuario;
import br.edu.unifio.printhub.entidades.Usuario;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Integer> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Integer id);

    /** Consulta personalizada (método derivado): lista os usuários de um tipo. */
    List<Usuario> findByTipoOrderByNome(TipoUsuario tipo);
}
