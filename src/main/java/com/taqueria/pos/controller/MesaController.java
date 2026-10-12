package com.taqueria.pos.controller;

import com.taqueria.pos.model.Mesa;
import com.taqueria.pos.repository.MesaRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mesas")
public class MesaController {

    private final MesaRepository mesaRepository;

    public MesaController(MesaRepository mesaRepository) {
        this.mesaRepository = mesaRepository;
    }

    @GetMapping
    public List<Mesa> listar() {
        return mesaRepository.findAll();
    }

    @PostMapping
    public Mesa crear(@RequestBody Mesa mesa) {
        return mesaRepository.save(mesa);
    }
}