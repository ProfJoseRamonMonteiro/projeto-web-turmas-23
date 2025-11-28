package com.projetoweb.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.projetoweb.models.CategoriaModel;


public interface CategoriaRepo extends JpaRepository<CategoriaModel, Long>{

    Optional<CategoriaModel> findByNomeIgnoreCase(String nome);
    boolean existsByNomeIgnoreCase(String nome);


}
