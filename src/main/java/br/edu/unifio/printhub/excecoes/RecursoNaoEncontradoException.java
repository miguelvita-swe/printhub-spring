package br.edu.unifio.printhub.excecoes;

/**
 * Lançada quando o registro pedido não existe no banco.
 * O {@link TratadorDeErros} transforma em 404 Not Found.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }

    /** Monta a mensagem padrão, por exemplo: "Produto com id 99 não foi encontrado". */
    public RecursoNaoEncontradoException(String recurso, Integer id) {
        super(recurso + " com id " + id + " não foi encontrado");
    }
}
