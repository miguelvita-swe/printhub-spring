package br.edu.unifio.printhub.entidades;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

/** Compra de um consumidor, com produtos de um único produtor, para retirar num ponto. */
@Entity
@Getter
@Setter
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Usuario consumidor;

    /** Preenchido a partir dos produtos: todos os itens são do mesmo produtor. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Usuario produtor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private PontoRetirada pontoRetirada;

    /** Nulo até o produtor aceitar o pedido. */
    @ManyToOne(fetch = FetchType.LAZY)
    private Lote lote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private StatusPedido status = StatusPedido.AGUARDANDO_ACEITACAO;

    /** Soma dos itens (quantidade x preço unitário). */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorProdutos;

    /** Parte do custo de entrega do lote. Nula até o lote fechar. */
    @Column(precision = 10, scale = 2)
    private BigDecimal taxaEntrega;

    /** Ordem de entrada no lote; usada para distribuir a sobra de centavos do rateio. */
    private Integer posicaoLote;

    @Column(length = 255)
    private String motivoRejeicao;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    private LocalDateTime aceitoEm;

    private LocalDateTime retiradoEm;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

    /** Valor que o consumidor paga: produtos + taxa de entrega (quando já definida). */
    public BigDecimal getValorTotal() {
        return taxaEntrega == null ? valorProdutos : valorProdutos.add(taxaEntrega);
    }
}
