package br.edu.unifio.printhub.excecoes;

/**
 * Lançada pelos services quando a operação fere uma regra do PrintHub
 * (estoque insuficiente, lote já fechado, categoria com produtos...).
 * O {@link TratadorDeErros} transforma em 409 Conflict.
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
