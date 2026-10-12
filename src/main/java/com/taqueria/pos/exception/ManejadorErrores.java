package com.taqueria.pos.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorRespuesta> alLanzarEstado(ResponseStatusException ex) {
        String mensaje = ex.getReason() != null ? ex.getReason() : "Error en la petición";
        return construir(ex.getStatusCode(), mensaje);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorRespuesta> alViolarIntegridad(DataIntegrityViolationException ex) {
        return construir(HttpStatus.CONFLICT,
                "Ya existe un registro con esos datos, o hace referencia a algo que no existe");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuesta> alLeerMalElJson(HttpMessageNotReadableException ex) {
        return construir(HttpStatus.BAD_REQUEST,
                "El cuerpo de la petición no es válido. Revisa el JSON y los valores permitidos");
    }

    private ResponseEntity<ErrorRespuesta> construir(HttpStatusCode status, String mensaje) {
        ErrorRespuesta cuerpo = new ErrorRespuesta(status.value(), mensaje, LocalDateTime.now());
        return ResponseEntity.status(status).body(cuerpo);
    }
}