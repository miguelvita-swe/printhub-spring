package br.edu.unifio.printhub.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Motivo informado pelo produtor ao recusar um pedido. */
public record RejeicaoRequisicao(
        @NotBlank(message = "Informe o motivo da rejeição")
        @Size(max = 255, message = "O motivo deve ter no máximo 255 caracteres")
        String motivo) {
}
