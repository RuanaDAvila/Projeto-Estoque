package com.adega.stock.controller;

import com.adega.stock.dto.CategoriaDTO;
import com.adega.stock.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
@Tag(name = "Categorias", description = "Gestao de categorias de produtos")
public class CategoriaController {

    private final CategoriaService service;

    @GetMapping
    @Operation(summary = "Listar todas as categorias")
    public List<CategoriaDTO> listar() {
        return service.listarTodas();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar categoria por ID")
    public CategoriaDTO buscar(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar nova categoria",
               description = "Exemplo: { \"nome\": \"Bebida Alcoolica\", \"descricao\": \"Cervejas, vinhos, destilados\" }")
    public CategoriaDTO criar(@Valid @RequestBody CategoriaDTO dto) {
        return service.criar(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar categoria")
    public CategoriaDTO atualizar(@PathVariable Long id, @Valid @RequestBody CategoriaDTO dto) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deletar categoria")
    public void deletar(@PathVariable Long id) {
        service.deletar(id);
    }
}
