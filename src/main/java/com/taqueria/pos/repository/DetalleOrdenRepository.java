package com.taqueria.pos.repository;

import com.taqueria.pos.dto.ProductoVendido;
import com.taqueria.pos.model.DetalleOrden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DetalleOrdenRepository extends JpaRepository<DetalleOrden, Integer> {

    @Query("""
            select new com.taqueria.pos.dto.ProductoVendido(
                d.producto.nombre, sum(d.cantidad), sum(d.subtotal))
            from DetalleOrden d
            where d.orden.estado = com.taqueria.pos.model.EstadoOrden.PAGADO
              and d.orden.fechaCreacion >= :desde
              and d.orden.fechaCreacion < :hasta
            group by d.producto.id, d.producto.nombre
            order by sum(d.cantidad) desc
            """)
    List<ProductoVendido> masVendidos(@Param("desde") LocalDateTime desde,
                                      @Param("hasta") LocalDateTime hasta);
}