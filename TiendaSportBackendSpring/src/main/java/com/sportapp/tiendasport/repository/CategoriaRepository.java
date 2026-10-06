package com.sportapp.tiendasport.repository;

import com.sportapp.tiendasport.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
}
