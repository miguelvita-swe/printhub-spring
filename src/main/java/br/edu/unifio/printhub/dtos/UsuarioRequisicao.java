package br.edu.unifio.printhub.dtos;

import br.edu.unifio.printhub.entidades.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Dados para criar ou atualizar um usuário do marketplace. */
public record UsuarioRequisicao(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Informe um e-mail válido")
        @Size(max = 120, message = "O e-mail deve ter no máximo 120 caracteres")
        String email,

        @Pattern(regexp = "\\d{10,11}", message = "O telefone deve ter DDD + número, só dígitos (10 ou 11)")
        String telefone,

        @NotNull(message = "O tipo é obrigatório: CONSUMIDOR ou PRODUTOR")
        TipoUsuario tipo,

        /** Opcional; quando não vier, o usuário fica ativo. */
        Boolean ativo) {
}
