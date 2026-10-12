package com.taqueria.pos.dto;

import java.math.BigDecimal;

public record DetalleResponse(
        String producto,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal,
        String notas
) {}