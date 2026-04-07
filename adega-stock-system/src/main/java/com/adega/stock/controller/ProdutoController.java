package com.adega.stock.controller;

import com.adega.stock.dto.ProdutoDTO;
import com.adega.stock.service.ProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
@RequiredArgsConstructor
@Tag(name = "Produtos", description = "Gestao de produtos da adega")
public class ProdutoController {

    private final ProdutoService service;

    @GetMapping
    @Operation(summary = "Listar todos os produtos")
    public List<ProdutoDTO> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar produto por ID")
    public ProdutoDTO buscar(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @GetMapping("/categoria/{categoriaId}")
    @Operation(summary = "Listar produtos por categoria")
    public List<ProdutoDTO> porCategoria(@PathVariable Long categoriaId) {
        return service.buscarPorCategoria(categoriaId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastrar novo produto",
               description = "Exemplo produto inteiro: { \"nome\": \"Whisky Jack Daniels 1L\", \"unidadeEstoque\": \"garrafa\", \"quantidadeAtual\": 10, \"valorCusto\": 85.00, \"valorVenda\": 140.00, \"categoriaId\": 1, \"fracionavel\": true, \"unidadeFracionada\": \"dose\", \"fatorFracionamento\": 15 }")
    public ProdutoDTO criar(@Valid @RequestBody ProdutoDTO dto) {
        return service.criar(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar produto")
    public ProdutoDTO atualizar(@PathVariable Long id, @Valid @RequestBody ProdutoDTO dto) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deletar produto")
    public void deletar(@PathVariable Long id) {
        service.deletar(id);
    }
}
