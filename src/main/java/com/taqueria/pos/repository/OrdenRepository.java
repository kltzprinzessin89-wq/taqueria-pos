package com.taqueria.pos.repository;

import com.taqueria.pos.model.EstadoOrden;
import com.taqueria.pos.model.Orden;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import java.util.List;

public interface OrdenRepository extends JpaRepository<Orden, Integer> {

    @EntityGraph(attributePaths = "detalles")
    List<Orden> findByEstadoNotOrderByFechaCreacionAsc(EstadoOrden estado);

    boolean existsByMesaIdAndEstadoNot(Integer mesaId, EstadoOrden estado);

    @Query("select count(o) from Orden o " +
            "where o.estado = :estado and o.fechaCreacion >= :desde and o.fechaCreacion < :hasta")
    long contarPorEstadoEntre(@Param("estado") EstadoOrden estado,
                              @Param("desde") LocalDateTime desde,
                              @Param("hasta") LocalDateTime hasta);

    @Query("select coalesce(sum(o.total), 0) from Orden o " +
            "where o.estado = :estado and o.fechaCreacion >= :desde and o.fechaCreacion < :hasta")
    BigDecimal sumarTotalPorEstadoEntre(@Param("estado") EstadoOrden estado,
                                        @Param("desde") LocalDateTime desde,
                                        @Param("hasta") LocalDateTime hasta);
}