package com.taqueria.pos.dto;

import java.math.BigDecimal;

public record ProductoVendido(
        String producto,
        Long cantidad,
        BigDecimal ingresos
) {}