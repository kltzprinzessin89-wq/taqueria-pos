package com.taqueria.pos.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReporteDiario(
        LocalDate fecha,
        long ordenesPagadas,
        BigDecimal totalVentas,
        List<ProductoVendido> masVendidos
) {}