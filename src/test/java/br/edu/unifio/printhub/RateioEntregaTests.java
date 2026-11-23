package br.edu.unifio.printhub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import br.edu.unifio.printhub.servicos.RateioEntrega;

class RateioEntregaTests {

    @Test
    void sobraDeCentavosVaiParaOsPrimeiros() {
        assertThat(RateioEntrega.dividir(new BigDecimal("20.00"), 3))
                .containsExactly(new BigDecimal("6.67"), new BigDecimal("6.67"), new BigDecimal("6.66"));
        assertThat(RateioEntrega.dividir(new BigDecimal("10.00"), 3))
                .containsExactly(new BigDecimal("3.34"), new BigDecimal("3.33"), new BigDecimal("3.33"));
    }

    @Test
    void divisaoExataEPedidoUnico() {
        assertThat(RateioEntrega.dividir(new BigDecimal("15.00"), 2))
                .containsExactly(new BigDecimal("7.50"), new BigDecimal("7.50"));
        assertThat(RateioEntrega.dividir(new BigDecimal("12.00"), 1)).containsExactly(new BigDecimal("12.00"));
    }

    @Test
    void somaDasTaxasSempreFechaOCusto() {
        BigDecimal soma = RateioEntrega.dividir(new BigDecimal("0.05"), 3).stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(soma).isEqualByComparingTo("0.05");
    }

    @Test
    void naoDivideEntreZeroPedidos() {
        assertThatThrownBy(() -> RateioEntrega.dividir(BigDecimal.TEN, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
