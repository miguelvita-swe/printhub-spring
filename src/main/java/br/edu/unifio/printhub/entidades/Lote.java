package br.edu.unifio.printhub.entidades;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
import jakarta.persistence.OrderBy;
import lombok.Getter;
import lombok.Setter;

/**
 * Lote coletivo: junta os pedidos aceitos de um mesmo ponto de retirada
 * para que a entrega seja feita de uma vez e o custo seja dividido.
 */
@Entity
@Getter
@Setter
public class Lote {

    /** Quantidade de pedidos que fecha o lote automaticamente. */
    public static final int LIMITE_PEDIDOS = 3;

    /** Dias que o lote fica aberto esperando novos pedidos. */
    public static final int DIAS_ABERTO = 7;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private PontoRetirada pontoRetirada;

    /** Copiado do ponto quando o lote abre; mudar o ponto depois não altera lotes abertos. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal custoEntrega;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusLote status = StatusLote.EM_FORMACAO;

    @Column(nullable = false)
    private LocalDateTime abertoEm;

    /** Data limite para fechar o lote, mesmo sem completar os 3 pedidos. */
    @Column(nullable = false)
    private LocalDateTime prazo;

    private LocalDateTime fechadoEm;

    @OneToMany(mappedBy = "lote")
    @OrderBy("posicaoLote")
    private List<Pedido> pedidos = new ArrayList<>();
}
