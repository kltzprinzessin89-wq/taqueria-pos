package com.taqueria.pos.service;

import com.taqueria.pos.dto.DetalleRequest;
import com.taqueria.pos.dto.OrdenRequest;
import com.taqueria.pos.dto.OrdenResponse;
import com.taqueria.pos.model.*;
import com.taqueria.pos.repository.MesaRepository;
import com.taqueria.pos.repository.OrdenRepository;
import com.taqueria.pos.repository.ProductoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.taqueria.pos.websocket.OrdenEvento;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrdenService {

    private final OrdenRepository ordenRepository;
    private final MesaRepository mesaRepository;
    private final ProductoRepository productoRepository;
    private final ApplicationEventPublisher eventos;

    public OrdenService(OrdenRepository ordenRepository,
                        MesaRepository mesaRepository,
                        ProductoRepository productoRepository,
                        ApplicationEventPublisher eventos) {
        this.ordenRepository = ordenRepository;
        this.mesaRepository = mesaRepository;
        this.productoRepository = productoRepository;
        this.eventos = eventos;
    }

    @Transactional
    public OrdenResponse crearOrden(OrdenRequest request) {
        if (request.mesaId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta el id de la mesa");
        }
        if (request.detalles() == null || request.detalles().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La orden no tiene productos");
        }

        Mesa mesa = mesaRepository.findById(request.mesaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mesa no encontrada"));

        Orden orden = new Orden();
        orden.setMesa(mesa);

        BigDecimal total = BigDecimal.ZERO;

        for (DetalleRequest item : request.detalles()) {
            if (item.cantidad() == null || item.cantidad() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cantidad debe ser mayor a 0");
            }

            Producto producto = productoRepository.findById(item.productoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Producto no encontrado: " + item.productoId()));

            if (!Boolean.TRUE.equals(producto.getDisponible())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Producto no disponible: " + producto.getNombre());
            }

            BigDecimal subtotal = producto.getPrecio().multiply(BigDecimal.valueOf(item.cantidad()));

            DetalleOrden detalle = new DetalleOrden();
            detalle.setProducto(producto);
            detalle.setCantidad(item.cantidad());
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setSubtotal(subtotal);
            detalle.setNotas(item.notas());

            orden.agregarDetalle(detalle);
            total = total.add(subtotal);
        }

        orden.setTotal(total);
        mesa.setEstado(EstadoMesa.OCUPADA);

        Orden guardada = ordenRepository.save(orden);
        eventos.publishEvent(new OrdenEvento("NUEVA_ORDEN", guardada.getId()));
        return OrdenResponse.from(guardada);
    }

    @Transactional(readOnly = true)
    public List<OrdenResponse> listarActivas() {
        return ordenRepository.findByEstadoNotOrderByFechaCreacionAsc(EstadoOrden.PAGADO)
                .stream()
                .map(OrdenResponse::from)
                .toList();


    }
    @Transactional
    public OrdenResponse cambiarEstado(Integer id, EstadoOrden nuevoEstado) {
        if (nuevoEstado == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta el nuevo estado");
        }

        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada"));

        EstadoOrden actual = orden.getEstado();

        if (nuevoEstado.ordinal() != actual.ordinal() + 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede pasar de " + actual + " a " + nuevoEstado);
        }

        orden.setEstado(nuevoEstado);

        if (nuevoEstado == EstadoOrden.PAGADO) {
            Mesa mesa = orden.getMesa();
            boolean hayOtrasActivas = ordenRepository
                    .existsByMesaIdAndEstadoNot(mesa.getId(), EstadoOrden.PAGADO);
            if (!hayOtrasActivas) {
                mesa.setEstado(EstadoMesa.DISPONIBLE);
            }
        }

        eventos.publishEvent(new OrdenEvento("ESTADO_CAMBIADO", orden.getId()));
        return OrdenResponse.from(orden);
    }
}