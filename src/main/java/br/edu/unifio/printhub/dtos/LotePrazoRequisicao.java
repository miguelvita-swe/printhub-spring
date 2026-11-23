package br.edu.unifio.printhub.dtos;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

/** Novo prazo de um lote em formação. */
public record LotePrazoRequisicao(
        @NotNull(message = "O prazo é obrigatório")
        @Future(message = "O prazo precisa ser uma data futura")
        LocalDateTime prazo) {
}
