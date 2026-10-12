package com.taqueria.pos.service;

import com.taqueria.pos.dto.DetalleRequest;
import com.taqueria.pos.dto.OrdenRequest;
import com.taqueria.pos.dto.OrdenResponse;
import com.taqueria.pos.model.EstadoMesa;
import com.taqueria.pos.model.EstadoOrden;
import com.taqueria.pos.model.Mesa;
import com.taqueria.pos.model.Orden;
import com.taqueria.pos.model.Producto;
import com.taqueria.pos.repository.MesaRepository;
import com.taqueria.pos.repository.OrdenRepository;
import com.taqueria.pos.repository.ProductoRepository;
import com.taqueria.pos.websocket.OrdenEvento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdenServiceTest {

    @Mock
    private OrdenRepository ordenRepository;
    @Mock
    private MesaRepository mesaRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private ApplicationEventPublisher eventos;

    @InjectMocks
    private OrdenService servicio;

    // ---------- datos de apoyo ----------

    private Mesa mesa(int id) {
        Mesa m = new Mesa();
        m.setId(id);
        m.setNumeroMesa(id);
        return m;
    }

    private Producto producto(int id, String nombre, String precio) {
        Producto p = new Producto();
        p.setId(id);
        p.setNombre(nombre);
        p.setPrecio(new BigDecimal(precio));
        p.setDisponible(true);
        return p;
    }

    private Orden orden(int id, Mesa mesa, EstadoOrden estado) {
        Orden o = new Orden();
        o.setId(id);
        o.setMesa(mesa);
        o.setEstado(estado);
        return o;
    }

    // ---------- crear orden ----------

    @Test
    void crearOrden_calculaSubtotalesTotalYOcupaLaMesa() {
        Mesa mesa = mesa(1);
        when(mesaRepository.findById(1)).thenReturn(Optional.of(mesa));
        when(productoRepository.findById(1))
                .thenReturn(Optional.of(producto(1, "Taco al pastor", "25.00")));
        when(productoRepository.findById(2))
                .thenReturn(Optional.of(producto(2, "Refresco", "20.00")));
        when(ordenRepository.save(any(Orden.class))).thenAnswer(inv -> inv.getArgument(0));

        OrdenRequest request = new OrdenRequest(1, List.of(
                new DetalleRequest(1, 4, "sin cebolla"),
                new DetalleRequest(2, 2, null)));

        OrdenResponse respuesta = servicio.crearOrden(request);

        // 4 x 25.00 + 2 x 20.00 = 140.00
        assertEquals(0, new BigDecimal("140.00").compareTo(respuesta.total()));
        assertEquals(2, respuesta.detalles().size());
        assertEquals(0, new BigDecimal("100.00").compareTo(respuesta.detalles().get(0).subtotal()));
        assertEquals(0, new BigDecimal("25.00").compareTo(respuesta.detalles().get(0).precioUnitario()));
        assertEquals(EstadoOrden.PENDIENTE, respuesta.estado());
        assertEquals(EstadoMesa.OCUPADA, mesa.getEstado());
        verify(eventos).publishEvent(any(OrdenEvento.class));
    }

    @Test
    void crearOrden_conCantidadCero_respondeBadRequestYNoGuarda() {
        when(mesaRepository.findById(1)).thenReturn(Optional.of(mesa(1)));

        OrdenRequest request = new OrdenRequest(1, List.of(new DetalleRequest(1, 0, null)));

        ResponseStatusException error =
                assertThrows(ResponseStatusException.class, () -> servicio.crearOrden(request));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verify(ordenRepository, never()).save(any());
    }

    @Test
    void crearOrden_conMesaInexistente_respondeNotFound() {
        when(mesaRepository.findById(99)).thenReturn(Optional.empty());

        OrdenRequest request = new OrdenRequest(99, List.of(new DetalleRequest(1, 1, null)));

        ResponseStatusException error =
                assertThrows(ResponseStatusException.class, () -> servicio.crearOrden(request));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }

    @Test
    void crearOrden_conProductoNoDisponible_respondeBadRequest() {
        Producto agotado = producto(1, "Taco al pastor", "25.00");
        agotado.setDisponible(false);
        when(mesaRepository.findById(1)).thenReturn(Optional.of(mesa(1)));
        when(productoRepository.findById(1)).thenReturn(Optional.of(agotado));

        OrdenRequest request = new OrdenRequest(1, List.of(new DetalleRequest(1, 1, null)));

        ResponseStatusException error =
                assertThrows(ResponseStatusException.class, () -> servicio.crearOrden(request));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verify(ordenRepository, never()).save(any());
    }

    // ---------- cambiar estado ----------

    @Test
    void cambiarEstado_avanzaUnPaso() {
        Orden orden = orden(5, mesa(1), EstadoOrden.PENDIENTE);
        when(ordenRepository.findById(5)).thenReturn(Optional.of(orden));

        OrdenResponse respuesta = servicio.cambiarEstado(5, EstadoOrden.EN_PREPARACION);

        assertEquals(EstadoOrden.EN_PREPARACION, respuesta.estado());
        verify(eventos).publishEvent(any(OrdenEvento.class));
    }

    @Test
    void cambiarEstado_noPermiteSaltarPasos() {
        Orden orden = orden(5, mesa(1), EstadoOrden.PENDIENTE);
        when(ordenRepository.findById(5)).thenReturn(Optional.of(orden));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.cambiarEstado(5, EstadoOrden.PAGADO));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        assertEquals(EstadoOrden.PENDIENTE, orden.getEstado());
        verify(eventos, never()).publishEvent(any(OrdenEvento.class));
    }

    @Test
    void cambiarEstado_noPermiteRetroceder() {
        Orden orden = orden(5, mesa(1), EstadoOrden.LISTO);
        when(ordenRepository.findById(5)).thenReturn(Optional.of(orden));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.cambiarEstado(5, EstadoOrden.PENDIENTE));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
    }

    @Test
    void pagar_liberaLaMesaSiNoQuedanOtrasOrdenesActivas() {
        Mesa mesa = mesa(1);
        mesa.setEstado(EstadoMesa.OCUPADA);
        Orden orden = orden(5, mesa, EstadoOrden.LISTO);
        when(ordenRepository.findById(5)).thenReturn(Optional.of(orden));
        when(ordenRepository.existsByMesaIdAndEstadoNot(eq(1), eq(EstadoOrden.PAGADO)))
                .thenReturn(false);

        servicio.cambiarEstado(5, EstadoOrden.PAGADO);

        assertEquals(EstadoMesa.DISPONIBLE, mesa.getEstado());
    }

    @Test
    void pagar_noLiberaLaMesaSiTieneOtrasOrdenesActivas() {
        Mesa mesa = mesa(1);
        mesa.setEstado(EstadoMesa.OCUPADA);
        Orden orden = orden(5, mesa, EstadoOrden.LISTO);
        when(ordenRepository.findById(5)).thenReturn(Optional.of(orden));
        when(ordenRepository.existsByMesaIdAndEstadoNot(eq(1), eq(EstadoOrden.PAGADO)))
                .thenReturn(true);

        servicio.cambiarEstado(5, EstadoOrden.PAGADO);

        assertEquals(EstadoMesa.OCUPADA, mesa.getEstado());
    }
}