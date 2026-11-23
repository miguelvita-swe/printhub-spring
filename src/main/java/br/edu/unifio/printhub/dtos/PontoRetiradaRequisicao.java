package br.edu.unifio.printhub.dtos;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Dados para criar ou atualizar um ponto de retirada. */
public record PontoRetiradaRequisicao(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String nome,

        @NotBlank(message = "O endereço é obrigatório")
        @Size(max = 200, message = "O endereço deve ter no máximo 200 caracteres")
        String endereco,

        @NotBlank(message = "O bairro é obrigatório")
        @Size(max = 80, message = "O bairro deve ter no máximo 80 caracteres")
        String bairro,

        @NotBlank(message = "A cidade é obrigatória")
        @Size(max = 80, message = "A cidade deve ter no máximo 80 caracteres")
        String cidade,

        @NotNull(message = "O custo de entrega é obrigatório")
        @DecimalMin(value = "0.00", message = "O custo de entrega não pode ser negativo")
        @Digits(integer = 8, fraction = 2, message = "O custo de entrega deve ter no máximo 2 casas decimais")
        BigDecimal custoEntrega,

        Boolean ativo) {
}
