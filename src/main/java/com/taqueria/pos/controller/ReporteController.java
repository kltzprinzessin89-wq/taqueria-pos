package com.taqueria.pos.controller;

import com.taqueria.pos.dto.ReporteDiario;
import com.taqueria.pos.service.ReporteService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/diario")
    public ReporteDiario diario(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return reporteService.reporteDelDia(fecha != null ? fecha : LocalDate.now());
    }
}