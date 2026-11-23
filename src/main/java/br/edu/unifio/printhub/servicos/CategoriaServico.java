package br.edu.unifio.printhub.servicos;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.unifio.printhub.dtos.CategoriaRequisicao;
import br.edu.unifio.printhub.dtos.CategoriaResposta;
import br.edu.unifio.printhub.entidades.Categoria;
import br.edu.unifio.printhub.excecoes.RecursoNaoEncontradoException;
import br.edu.unifio.printhub.excecoes.RegraNegocioException;
import br.edu.unifio.printhub.repositorios.CategoriaRepositorio;
import br.edu.unifio.printhub.repositorios.ProdutoRepositorio;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoriaServico {

    private final CategoriaRepositorio categoriaRepositorio;
    private final ProdutoRepositorio produtoRepositorio;

    @Transactional(readOnly = true)
    public List<CategoriaResposta> listar() {
        return categoriaRepositorio.findAll(Sort.by("nome")).stream().map(CategoriaResposta::de).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResposta buscarPorId(Integer id) {
        return CategoriaResposta.de(buscarEntidade(id));
    }

    @Transactional
    public CategoriaResposta criar(CategoriaRequisicao dados) {
        // Regra: não pode haver duas categorias com o mesmo nome.
        if (categoriaRepositorio.existsByNomeIgnoreCase(dados.nome())) {
            throw new RegraNegocioException("Já existe uma categoria com o nome '" + dados.nome() + "'");
        }
        Categoria categoria = new Categoria();
        copiar(dados, categoria);
        return CategoriaResposta.de(categoriaRepositorio.save(categoria));
    }

    @Transactional
    public CategoriaResposta atualizar(Integer id, CategoriaRequisicao dados) {
        Categoria categoria = buscarEntidade(id);
        if (categoriaRepositorio.existsByNomeIgnoreCaseAndIdNot(dados.nome(), id)) {
            throw new RegraNegocioException("Já existe outra categoria com o nome '" + dados.nome() + "'");
        }
        copiar(dados, categoria);
        return CategoriaResposta.de(categoria);
    }

    @Transactional
    public void excluir(Integer id) {
        Categoria categoria = buscarEntidade(id);
        // Regra: categoria com produtos não pode ser excluída, senão os produtos ficariam órfãos.
        if (produtoRepositorio.existsByCategoriaId(id)) {
            throw new RegraNegocioException(
                    "A categoria '" + categoria.getNome() + "' tem produtos associados e não pode ser excluída");
        }
        categoriaRepositorio.delete(categoria);
    }

    /** Usado também pelo ProdutoServico para validar a categoria informada. */
    @Transactional(readOnly = true)
    public Categoria buscarEntidade(Integer id) {
        return categoriaRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria", id));
    }

    private void copiar(CategoriaRequisicao dados, Categoria categoria) {
        categoria.setNome(dados.nome().trim());
        categoria.setDescricao(dados.descricao());
    }
}
