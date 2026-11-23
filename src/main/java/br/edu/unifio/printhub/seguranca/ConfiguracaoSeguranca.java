package br.edu.unifio.printhub.seguranca;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Autenticação HTTP Basic e autorização por perfil.
 *
 * Perfis:
 * - ADMIN: administra o marketplace (categorias, usuários, pontos, lotes) e confirma a retirada no ponto.
 * - PRODUTOR: anuncia produtos e aceita ou rejeita pedidos.
 * - CONSUMIDOR: faz, altera e cancela pedidos.
 *
 * Sem login (ou com senha errada) a resposta é 401 Unauthorized.
 * Logado, mas sem o perfil exigido, a resposta é 403 Forbidden.
 */
@Configuration
@EnableWebSecurity
public class ConfiguracaoSeguranca {

    public static final String ADMIN = "ADMIN";
    public static final String PRODUTOR = "PRODUTOR";
    public static final String CONSUMIDOR = "CONSUMIDOR";

    @Bean
    public SecurityFilterChain filtroDeSeguranca(HttpSecurity http, RespostasDeSeguranca respostas) throws Exception {
        http
                // API REST sem sessão nem formulário: o CSRF não se aplica.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(basic -> basic.authenticationEntryPoint(respostas))
                .exceptionHandling(erros -> erros
                        .authenticationEntryPoint(respostas) // 401
                        .accessDeniedHandler(respostas)) // 403
                .authorizeHttpRequests(regras -> regras
                        .requestMatchers("/error").permitAll()

                        // Usuários têm e-mail e telefone (dados pessoais): só o ADMIN vê e gerencia.
                        .requestMatchers("/usuarios/**").hasRole(ADMIN)

                        // Demais consultas: qualquer usuário autenticado.
                        .requestMatchers(HttpMethod.GET, "/**").authenticated()

                        // Só o produtor decide se aceita ou rejeita o pedido que recebeu.
                        .requestMatchers(HttpMethod.POST, "/pedidos/*/aceitar", "/pedidos/*/rejeitar").hasRole(PRODUTOR)

                        // Quem confirma a retirada é o administrador (atendente do ponto).
                        .requestMatchers(HttpMethod.POST, "/pedidos/*/retirar").hasRole(ADMIN)

                        // Criar, alterar, cancelar e excluir pedidos e seus itens.
                        .requestMatchers("/pedidos/**").hasAnyRole(CONSUMIDOR, ADMIN)

                        // Anunciar, alterar e excluir produtos.
                        .requestMatchers("/produtos/**").hasAnyRole(PRODUTOR, ADMIN)

                        // Cadastros administrativos e operação dos lotes.
                        .requestMatchers("/categorias/**", "/pontos-retirada/**", "/lotes/**")
                        .hasRole(ADMIN)

                        .anyRequest().authenticated());
        return http.build();
    }

    /**
     * Usuários de teste em memória (o enunciado dispensa cadastro de login no banco).
     * As senhas são guardadas com BCrypt, nunca em texto puro.
     */
    @Bean
    public UserDetailsService usuariosDoSistema(PasswordEncoder codificador) {
        return new InMemoryUserDetailsManager(
                User.withUsername("admin").password(codificador.encode("admin123")).roles(ADMIN).build(),
                User.withUsername("produtor").password(codificador.encode("produtor123")).roles(PRODUTOR).build(),
                User.withUsername("consumidor").password(codificador.encode("consumidor123")).roles(CONSUMIDOR)
                        .build());
    }

    @Bean
    public PasswordEncoder codificadorDeSenha() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
