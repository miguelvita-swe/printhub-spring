package br.edu.unifio.printhub.dtos;

import java.math.BigDecimal;

import br.edu.unifio.printhub.entidades.PontoRetirada;

public record PontoRetiradaResposta(
        Integer id,
        String nome,
        String endereco,
        String bairro,
        String cidade,
        BigDecimal custoEntrega,
        Boolean ativo) {

    public static PontoRetiradaResposta de(PontoRetirada ponto) {
        return new PontoRetiradaResposta(ponto.getId(), ponto.getNome(), ponto.getEndereco(), ponto.getBairro(),
                ponto.getCidade(), ponto.getCustoEntrega(), ponto.getAtivo());
    }
}
