package br.edu.unifio.printhub.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.unifio.printhub.entidades.Categoria;

public interface CategoriaRepositorio extends JpaRepository<Categoria, Integer> {

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Integer id);
}
