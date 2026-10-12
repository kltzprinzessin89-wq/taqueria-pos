package com.taqueria.pos.service;

import com.taqueria.pos.dto.ProductoVendido;
import com.taqueria.pos.dto.ReporteDiario;
import com.taqueria.pos.model.EstadoOrden;
import com.taqueria.pos.repository.DetalleOrdenRepository;
import com.taqueria.pos.repository.OrdenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReporteService {

    private final OrdenRepository ordenRepository;
    private final DetalleOrdenRepository detalleOrdenRepository;

    public ReporteService(OrdenRepository ordenRepository,
                          DetalleOrdenRepository detalleOrdenRepository) {
        this.ordenRepository = ordenRepository;
        this.detalleOrdenRepository = detalleOrdenRepository;
    }

    @Transactional(readOnly = true)
    public ReporteDiario reporteDelDia(LocalDate fecha) {
        LocalDateTime desde = fecha.atStartOfDay();
        LocalDateTime hasta = fecha.plusDays(1).atStartOfDay();

        long ordenes = ordenRepository.contarPorEstadoEntre(EstadoOrden.PAGADO, desde, hasta);
        BigDecimal total = ordenRepository.sumarTotalPorEstadoEntre(EstadoOrden.PAGADO, desde, hasta);
        List<ProductoVendido> masVendidos = detalleOrdenRepository.masVendidos(desde, hasta);

        return new ReporteDiario(fecha, ordenes, total, masVendidos);
    }
}