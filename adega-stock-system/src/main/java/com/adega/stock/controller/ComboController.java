package com.adega.stock.controller;

import com.adega.stock.dto.ComboDTO;
import com.adega.stock.service.ComboService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/combos")
@RequiredArgsConstructor
@Tag(name = "Combos", description = "Gestao de combos de produtos")
public class ComboController {

    private final ComboService service;

    @GetMapping
    @Operation(summary = "Listar todos os combos")
    public List<ComboDTO> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar combo por ID (exibe itens)")
    public ComboDTO buscar(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastrar novo combo",
               description = "Exemplo: { \"nome\": \"Combo Aperol\", \"descricao\": \"Aperol + Prosecco\", \"valorVenda\": 45.00, \"valorCusto\": 22.00, \"itens\": [ { \"produtoId\": 1, \"quantidade\": 1 }, { \"produtoId\": 2, \"quantidade\": 1 } ] }")
    public ComboDTO criar(@Valid @RequestBody ComboDTO dto) {
        return service.criar(dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deletar combo")
    public void deletar(@PathVariable Long id) {
        service.deletar(id);
    }
}
