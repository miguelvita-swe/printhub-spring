package br.edu.unifio.printhub.dtos;

import java.time.LocalDateTime;

import br.edu.unifio.printhub.entidades.TipoUsuario;
import br.edu.unifio.printhub.entidades.Usuario;

public record UsuarioResposta(
        Integer id,
        String nome,
        String email,
        String telefone,
        TipoUsuario tipo,
        Boolean ativo,
        LocalDateTime criadoEm) {

    public static UsuarioResposta de(Usuario usuario) {
        return new UsuarioResposta(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getTelefone(),
                usuario.getTipo(), usuario.getAtivo(), usuario.getCriadoEm());
    }
}
