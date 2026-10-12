package com.taqueria.pos.exception;

import java.time.LocalDateTime;

public record ErrorRespuesta(
        int status,
        String mensaje,
        LocalDateTime fecha
) {}