package com.taqueria.pos.dto;

import java.util.List;

public record OrdenRequest(
        Integer mesaId,
        List<DetalleRequest> detalles
) {}