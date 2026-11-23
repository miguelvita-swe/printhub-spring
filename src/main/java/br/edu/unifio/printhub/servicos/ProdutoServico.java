package br.edu.unifio.printhub.servicos;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.unifio.printhub.dtos.ProdutoRequisicao;
import br.edu.unifio.printhub.dtos.ProdutoResposta;
import br.edu.unifio.printhub.entidades.Produto;
import br.edu.unifio.printhub.entidades.TipoUsuario;
import br.edu.unifio.printhub.entidades.Usuario;
import br.edu.unifio.printhub.excecoes.RecursoNaoEncontradoException;
import br.edu.unifio.printhub.excecoes.RegraNegocioException;
import br.edu.unifio.printhub.repositorios.ItemPedidoRepositorio;
import br.edu.unifio.printhub.repositorios.ProdutoRepositorio;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProdutoServico {

    private final ProdutoRepositorio produtoRepositorio;
    private final ItemPedidoRepositorio itemPedidoRepositorio;
    private final CategoriaServico categoriaServico;
    private final UsuarioServico usuarioServico;

    @Transactional(readOnly = true)
    public List<ProdutoResposta> listar() {
        return produtoRepositorio.findAll(Sort.by("nome")).stream().map(ProdutoResposta::de).toList();
    }

    @Transactional(readOnly = true)
    public ProdutoResposta buscarPorId(Integer id) {
        return ProdutoResposta.de(buscarEntidade(id));
    }

    /** Consulta personalizada: produtos de uma categoria. */
    @Transactional(readOnly = true)
    public List<ProdutoResposta> listarPorCategoria(Integer categoriaId) {
        categoriaServico.buscarEntidade(categoriaId); // 404 se a categoria não existir
        return produtoRepositorio.findByCategoriaIdOrderByNome(categoriaId).stream().map(ProdutoResposta::de).toList();
    }

    /** Consulta personalizada: busca por parte do nome. */
    @Transactional(readOnly = true)
    public List<ProdutoResposta> buscarPorNome(String nome) {
        return produtoRepositorio.findByNomeContainingIgnoreCaseOrderByNome(nome.trim()).stream()
                .map(ProdutoResposta::de).toList();
    }

    /** Consulta personalizada (JPQL): produtos ativos com estoque abaixo do limite. */
    @Transactional(readOnly = true)
    public List<ProdutoResposta> listarComEstoqueBaixo(Integer limite) {
        return produtoRepositorio.buscarComEstoqueAbaixoDe(limite).stream().map(ProdutoResposta::de).toList();
    }

    @Transactional
    public ProdutoResposta criar(ProdutoRequisicao dados) {
        Produto produto = new Produto();
        produto.setCriadoEm(LocalDateTime.now());
        copiar(dados, produto);
        return ProdutoResposta.de(produtoRepositorio.save(produto));
    }

    @Transactional
    public ProdutoResposta atualizar(Integer id, ProdutoRequisicao dados) {
        Produto produto = buscarEntidade(id);
        copiar(dados, produto);
        return ProdutoResposta.de(produto);
    }

    @Transactional
    public void excluir(Integer id) {
        Produto produto = buscarEntidade(id);
        // Regra: produto que já foi vendido aparece em pedidos; excluir apagaria o histórico.
        if (itemPedidoRepositorio.existsByProdutoId(id)) {
            throw new RegraNegocioException("O produto " + produto.getNome()
                    + " já aparece em pedidos e não pode ser excluído; desative-o com ativo = false");
        }
        produtoRepositorio.delete(produto);
    }

    @Transactional(readOnly = true)
    public Produto buscarEntidade(Integer id) {
        return produtoRepositorio.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Produto", id));
    }

    private void copiar(ProdutoRequisicao dados, Produto produto) {
        Usuario produtor = usuarioServico.buscarEntidade(dados.produtorId());
        // Regra: só um usuário do tipo PRODUTOR, e ativo, pode anunciar produtos.
        if (produtor.getTipo() != TipoUsuario.PRODUTOR) {
            throw new RegraNegocioException("O usuário " + produtor.getNome()
                    + " é " + produtor.getTipo() + " e só produtores podem anunciar produtos");
        }
        if (!produtor.getAtivo()) {
            throw new RegraNegocioException("O produtor " + produtor.getNome() + " está desativado");
        }
        produto.setProdutor(produtor);
        produto.setCategoria(categoriaServico.buscarEntidade(dados.categoriaId()));
        produto.setNome(dados.nome().trim());
        produto.setDescricao(dados.descricao());
        produto.setMaterial(dados.material().trim());
        produto.setCor(dados.cor());
        produto.setPreco(dados.preco());
        produto.setEstoque(dados.estoque());
        produto.setDiasProducao(dados.diasProducao());
        produto.setAtivo(dados.ativo() == null ? Boolean.TRUE : dados.ativo());
    }
}
