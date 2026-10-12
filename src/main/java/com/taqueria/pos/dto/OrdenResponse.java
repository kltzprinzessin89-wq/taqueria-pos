package com.taqueria.pos.dto;

import com.taqueria.pos.model.EstadoOrden;
import com.taqueria.pos.model.Orden;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrdenResponse(
        Integer id,
        Integer numeroMesa,
        EstadoOrden estado,
        LocalDateTime fechaCreacion,
        BigDecimal total,
        List<DetalleResponse> detalles
) {
    public static OrdenResponse from(Orden orden) {
        List<DetalleResponse> lineas = orden.getDetalles().stream()
                .map(d -> new DetalleResponse(
                        d.getProducto().getNombre(),
                        d.getCantidad(),
                        d.getPrecioUnitario(),
                        d.getSubtotal(),
                        d.getNotas()))
                .toList();

        return new OrdenResponse(
                orden.getId(),
                orden.getMesa().getNumeroMesa(),
                orden.getEstado(),
                orden.getFechaCreacion(),
                orden.getTotal(),
                lineas);
    }
}