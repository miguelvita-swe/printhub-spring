package br.edu.unifio.printhub;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import br.edu.unifio.printhub.repositorios.ProdutoRepositorio;

@SpringBootTest
class PrinthubApplicationTests {

	@Autowired
	private ProdutoRepositorio produtoRepositorio;

	@Test
	void deveSubirOContextoECarregarOsDadosDeExemplo() {
		assertThat(produtoRepositorio.count()).isEqualTo(6);
	}

	@Test
	void deveBuscarProdutosComEstoqueBaixo() {
		assertThat(produtoRepositorio.buscarComEstoqueAbaixoDe(5))
				.extracting("nome")
				.containsExactly("Miniatura de dragão", "Luminária lua");
	}
}
