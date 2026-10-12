package com.taqueria.pos.dto;

import com.taqueria.pos.model.EstadoOrden;

public record CambioEstadoRequest(EstadoOrden estado) {}