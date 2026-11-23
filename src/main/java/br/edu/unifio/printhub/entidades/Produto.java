package br.edu.unifio.printhub.entidades;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

/** Peça impressa em 3D anunciada por um produtor. */
@Entity
@Getter
@Setter
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 500)
    private String descricao;

    /** Filamento ou resina usado na impressão (PLA, PETG, ABS, Resina...). */
    @Column(nullable = false, length = 40)
    private String material;

    @Column(length = 30)
    private String cor;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    /** Quantidade disponível para venda. */
    @Column(nullable = false)
    private Integer estoque;

    /** Dias que o produtor leva para imprimir a peça. */
    @Column(nullable = false)
    private Integer diasProducao;

    @Column(nullable = false)
    private Boolean ativo = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Categoria categoria;

    /** Usuário do tipo PRODUTOR dono do anúncio. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Usuario produtor;

    @Column(nullable = false)
    private LocalDateTime criadoEm;
}
