package br.edu.unifio.printhub.dtos;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

/**
 * Abertura manual de um lote. Normalmente o lote abre sozinho no primeiro aceite,
 * mas o administrador pode abrir antes para um ponto.
 */
public record LoteRequisicao(
        @NotNull(message = "O ponto de retirada é obrigatório")
        Integer pontoRetiradaId,

        /** Opcional: quando não vier, o prazo é de 7 dias a partir de agora. */
        @Future(message = "O prazo precisa ser uma data futura")
        LocalDateTime prazo) {
}
