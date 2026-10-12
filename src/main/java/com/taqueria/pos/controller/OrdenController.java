package com.taqueria.pos.controller;

import com.taqueria.pos.dto.CambioEstadoRequest;
import com.taqueria.pos.dto.OrdenRequest;
import com.taqueria.pos.dto.OrdenResponse;
import com.taqueria.pos.service.OrdenService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes")
public class OrdenController {

    private final OrdenService ordenService;

    public OrdenController(OrdenService ordenService) {
        this.ordenService = ordenService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrdenResponse crear(@RequestBody OrdenRequest request) {
        return ordenService.crearOrden(request);
    }

    @GetMapping("/activas")
    public List<OrdenResponse> activas() {
        return ordenService.listarActivas();
    }

    @PutMapping("/{id}/estado")
    public OrdenResponse cambiarEstado(@PathVariable Integer id,
                                       @RequestBody CambioEstadoRequest request) {
        return ordenService.cambiarEstado(id, request.estado());
    }
}
