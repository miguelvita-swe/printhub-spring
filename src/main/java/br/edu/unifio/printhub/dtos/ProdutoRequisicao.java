package br.edu.unifio.printhub.dtos;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Dados para anunciar ou atualizar um produto impresso em 3D. */
public record ProdutoRequisicao(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
        String nome,

        @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
        String descricao,

        @NotBlank(message = "O material é obrigatório (ex.: PLA, PETG, ABS, Resina)")
        @Size(max = 40, message = "O material deve ter no máximo 40 caracteres")
        String material,

        @Size(max = 30, message = "A cor deve ter no máximo 30 caracteres")
        String cor,

        @NotNull(message = "O preço é obrigatório")
        @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero")
        @Digits(integer = 8, fraction = 2, message = "O preço deve ter no máximo 2 casas decimais")
        BigDecimal preco,

        @NotNull(message = "O estoque é obrigatório")
        @Min(value = 0, message = "O estoque não pode ser negativo")
        Integer estoque,

        @NotNull(message = "Os dias de produção são obrigatórios")
        @Min(value = 1, message = "A produção leva pelo menos 1 dia")
        @Max(value = 60, message = "A produção pode levar no máximo 60 dias")
        Integer diasProducao,

        @NotNull(message = "A categoria é obrigatória")
        Integer categoriaId,

        @NotNull(message = "O produtor é obrigatório")
        Integer produtorId,

        Boolean ativo) {
}
