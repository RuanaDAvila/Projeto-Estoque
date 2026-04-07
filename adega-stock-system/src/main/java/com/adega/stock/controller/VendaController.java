package com.adega.stock.controller;

import com.adega.stock.dto.VendaDTO;
import com.adega.stock.service.VendaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendas")
@RequiredArgsConstructor
@Tag(name = "Vendas", description = "Registro de vendas com baixa automatica de estoque")
public class VendaController {

    private final VendaService service;

    @GetMapping
    @Operation(summary = "Listar todas as vendas")
    public List<VendaDTO> listar() {
        return service.listarTodas();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar venda por ID")
    public VendaDTO buscar(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar nova venda (baixa automatica de estoque)",
               description = """
                   Para venda de produto inteiro:
                   { "observacao": "Mesa 3", "itens": [ { "produtoId": 1, "quantidade": 2, "fracionado": false } ] }

                   Para venda fracionada (dose):
                   { "itens": [ { "produtoId": 1, "quantidade": 3, "fracionado": true } ] }

                   Para venda de combo:
                   { "itens": [ { "comboId": 1, "quantidade": 1 } ] }
                   """)
    public VendaDTO registrar(@Valid @RequestBody VendaDTO dto) {
        return service.registrarVenda(dto);
    }
}
