package com.taqueria.pos.dto;

public record DetalleRequest(
        Integer productoId,
        Integer cantidad,
        String notas
) {}