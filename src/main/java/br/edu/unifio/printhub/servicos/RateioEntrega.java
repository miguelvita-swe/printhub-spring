package br.edu.unifio.printhub.servicos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Divide o custo de entrega de um lote entre os pedidos, sem perder nenhum centavo.
 *
 * A conta é feita em centavos inteiros. O que sobra da divisão vai, um centavo de cada vez,
 * para os primeiros pedidos que entraram no lote.
 *
 * Exemplo: R$ 20,00 entre 3 pedidos = 2000 centavos / 3 = 666 e sobram 2
 *          -> 6,67 + 6,67 + 6,66 = 20,00
 */
public final class RateioEntrega {

    private RateioEntrega() {
    }

    /**
     * @param custoTotal custo de entrega do lote, em reais
     * @param partes     quantidade de pedidos do lote (maior que zero)
     * @return a taxa de cada pedido, na ordem de entrada no lote
     */
    public static List<BigDecimal> dividir(BigDecimal custoTotal, int partes) {
        if (partes <= 0) {
            throw new IllegalArgumentException("O rateio precisa de pelo menos um pedido");
        }
        long centavos = custoTotal.movePointRight(2).longValueExact();
        long base = centavos / partes;
        long sobra = centavos % partes;

        List<BigDecimal> taxas = new ArrayList<>();
        for (int i = 0; i < partes; i++) {
            long taxa = base + (i < sobra ? 1 : 0);
            taxas.add(BigDecimal.valueOf(taxa, 2));
        }
        return taxas;
    }
}
