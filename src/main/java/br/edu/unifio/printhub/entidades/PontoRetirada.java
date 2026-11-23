package br.edu.unifio.printhub.entidades;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

/**
 * Local parceiro onde os consumidores retiram os pedidos.
 * O custo de entrega é o valor de levar um lote inteiro até o ponto;
 * ele é dividido entre os pedidos do lote.
 */
@Entity
@Getter
@Setter
public class PontoRetirada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 200)
    private String endereco;

    @Column(nullable = false, length = 80)
    private String bairro;

    @Column(nullable = false, length = 80)
    private String cidade;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal custoEntrega;

    @Column(nullable = false)
    private Boolean ativo = true;
}
