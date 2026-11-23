package br.edu.unifio.printhub.excecoes;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import br.edu.unifio.printhub.excecoes.ErroResposta.CampoInvalido;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Tratamento centralizado de exceções.
 *
 * Qualquer exceção lançada por um recurso (controller) ou service chega aqui
 * e vira uma resposta JSON no formato {@link ErroResposta}, com o código HTTP certo:
 * 400 para dados inválidos, 404 para recurso inexistente e 409 para regra de negócio.
 */
@RestControllerAdvice
public class TratadorDeErros {

    private static final Logger log = LoggerFactory.getLogger(TratadorDeErros.class);

    /** 400 — o corpo da requisição não passou nas anotações de Bean Validation (@NotBlank, @Min...). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> dadosInvalidos(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<CampoInvalido> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> new CampoInvalido(erro.getField(), erro.getDefaultMessage()))
                .toList();
        return responder(HttpStatus.BAD_REQUEST, "Dados inválidos: corrija os campos indicados", req, campos);
    }

    /** 400 — parâmetro da URL fora das regras (ex.: ?limite=-1 com @Min). */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErroResposta> parametroInvalido(HandlerMethodValidationException ex, HttpServletRequest req) {
        List<CampoInvalido> campos = ex.getParameterValidationResults().stream()
                .flatMap(resultado -> resultado.getResolvableErrors().stream()
                        .map(erro -> new CampoInvalido(resultado.getMethodParameter().getParameterName(),
                                erro.getDefaultMessage())))
                .toList();
        return responder(HttpStatus.BAD_REQUEST, "Parâmetro inválido", req, campos);
    }

    /** 400 — JSON mal formatado ou com tipo errado (texto onde se espera número, status inexistente...). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> jsonInvalido(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return responder(HttpStatus.BAD_REQUEST,
                "Corpo da requisição inválido: confira a sintaxe do JSON e o tipo de cada campo", req, null);
    }

    /** 400 — valor da URL com tipo errado (ex.: /produtos/abc ou /pedidos/status/XYZ). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResposta> tipoInvalido(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        Class<?> tipo = ex.getRequiredType();
        String esperado;
        if (tipo != null && tipo.isEnum()) {
            esperado = "um destes valores: " + Arrays.toString(tipo.getEnumConstants());
        } else {
            esperado = "um valor do tipo " + (tipo == null ? "correto" : tipo.getSimpleName());
        }
        String mensagem = "O parâmetro '" + ex.getName() + "' recebeu '" + ex.getValue() + "', mas espera " + esperado;
        return responder(HttpStatus.BAD_REQUEST, mensagem, req, null);
    }

    /** 400 — faltou um parâmetro obrigatório na URL. */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroResposta> parametroAusente(MissingServletRequestParameterException ex,
            HttpServletRequest req) {
        return responder(HttpStatus.BAD_REQUEST,
                "O parâmetro '" + ex.getParameterName() + "' é obrigatório", req, null);
    }

    /** 404 — o registro procurado não existe. */
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> naoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest req) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage(), req, null);
    }

    /** 404 — a rota (URL) não existe na API. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResposta> rotaInexistente(NoResourceFoundException ex, HttpServletRequest req) {
        return responder(HttpStatus.NOT_FOUND, "A rota " + req.getRequestURI() + " não existe nesta API", req, null);
    }

    /** 405 — a rota existe, mas não aceita esse método HTTP. */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResposta> metodoNaoPermitido(HttpRequestMethodNotSupportedException ex,
            HttpServletRequest req) {
        return responder(HttpStatus.METHOD_NOT_ALLOWED,
                "O método " + ex.getMethod() + " não é aceito nesta rota", req, null);
    }

    /** 409 — a operação fere uma regra de negócio do PrintHub. */
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResposta> regraNegocio(RegraNegocioException ex, HttpServletRequest req) {
        return responder(HttpStatus.CONFLICT, ex.getMessage(), req, null);
    }

    /** 409 — o banco recusou a gravação (ex.: chave única repetida). Rede de segurança para as regras. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResposta> integridade(DataIntegrityViolationException ex, HttpServletRequest req) {
        return responder(HttpStatus.CONFLICT,
                "A operação conflita com dados já existentes ou ligados a este registro", req, null);
    }

    /** 500 — erro inesperado. Registra no log e não expõe detalhes internos ao cliente. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> erroInesperado(Exception ex, HttpServletRequest req) {
        log.error("Erro inesperado em {} {}", req.getMethod(), req.getRequestURI(), ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no servidor", req, null);
    }

    private ResponseEntity<ErroResposta> responder(HttpStatus status, String mensagem, HttpServletRequest req,
            List<CampoInvalido> campos) {
        ErroResposta corpo = new ErroResposta(LocalDateTime.now(), status.value(), status.getReasonPhrase(),
                mensagem, req.getRequestURI(), campos);
        return ResponseEntity.status(status).body(corpo);
    }
}
