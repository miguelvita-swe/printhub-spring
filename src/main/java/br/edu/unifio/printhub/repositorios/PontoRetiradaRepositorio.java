package br.edu.unifio.printhub.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.unifio.printhub.entidades.PontoRetirada;

public interface PontoRetiradaRepositorio extends JpaRepository<PontoRetirada, Integer> {
}
