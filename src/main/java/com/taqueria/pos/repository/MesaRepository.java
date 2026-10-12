package com.taqueria.pos.repository;

import com.taqueria.pos.model.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MesaRepository extends JpaRepository<Mesa, Integer> {
}