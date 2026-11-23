package br.edu.unifio.printhub.seguranca;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import br.edu.unifio.printhub.excecoes.ErroResposta;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.json.JsonMapper;

/**
 * Respostas JSON para os erros de segurança, no mesmo formato dos outros erros da API.
 *
 * O Spring Security barra a requisição antes de ela chegar no controller,
 * por isso o @RestControllerAdvice não trata esses casos e eles ficam aqui.
 */
@Component
@RequiredArgsConstructor
public class RespostasDeSeguranca implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final JsonMapper jsonMapper;

    /** 401 — não mandou usuário e senha, ou mandou credenciais erradas. */
    @Override
    public void commence(HttpServletRequest req, HttpServletResponse resp, AuthenticationException ex)
            throws IOException {
        resp.setHeader("WWW-Authenticate", "Basic realm=\"PrintHub\"");
        escrever(req, resp, HttpStatus.UNAUTHORIZED,
                "Autenticação necessária: envie usuário e senha válidos (HTTP Basic)");
    }

    /** 403 — está logado, mas o perfil não tem permissão para esta operação. */
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, AccessDeniedException ex)
            throws IOException {
        escrever(req, resp, HttpStatus.FORBIDDEN, "O perfil do usuário " + req.getRemoteUser()
                + " não tem permissão para " + req.getMethod() + " " + req.getRequestURI());
    }

    private void escrever(HttpServletRequest req, HttpServletResponse resp, HttpStatus status, String mensagem)
            throws IOException {
        ErroResposta corpo = new ErroResposta(LocalDateTime.now(), status.value(), status.getReasonPhrase(),
                mensagem, req.getRequestURI(), null);
        resp.setStatus(status.value());
        resp.setContentType(MediaType.APPLICATION_JSON_VALUE);
        resp.setCharacterEncoding("UTF-8");
        jsonMapper.writeValue(resp.getOutputStream(), corpo);
    }
}
