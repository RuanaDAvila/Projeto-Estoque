package com.adega.stock.controller;

import com.adega.stock.dto.EntradaEstoqueDTO;
import com.adega.stock.dto.MovimentacaoDTO;
import com.adega.stock.dto.PerdaEstoqueDTO;
import com.adega.stock.dto.SaldoEstoqueDTO;
import com.adega.stock.service.EstoqueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estoque")
@RequiredArgsConstructor
@Tag(name = "Estoque", description = "Controle de entradas, perdas e saldo de estoque")
public class EstoqueController {

    private final EstoqueService service;

    @GetMapping("/saldo")
    @Operation(summary = "Listar saldo atual de todos os produtos")
    public List<SaldoEstoqueDTO> listarSaldos() {
        return service.listarSaldos();
    }

    @GetMapping("/saldo/{produtoId}")
    @Operation(summary = "Saldo atual de um produto especifico")
    public SaldoEstoqueDTO saldoPorProduto(@PathVariable Long produtoId) {
        return service.saldoPorProduto(produtoId);
    }

    @PostMapping("/entrada")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar entrada de estoque (reposicao/compra)",
               description = "Exemplo: { \"produtoId\": 1, \"quantidade\": 10, \"valorUnitarioCusto\": 85.00, \"observacao\": \"Reposicao quinzenal\" }")
    public MovimentacaoDTO registrarEntrada(@Valid @RequestBody EntradaEstoqueDTO dto) {
        return service.registrarEntrada(dto);
    }

    @PostMapping("/perda")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar perda de estoque (quebra, vencimento, extravio)",
               description = "Motivo e obrigatorio. Exemplo: { \"produtoId\": 1, \"quantidade\": 1, \"motivo\": \"Garrafa quebrada durante limpeza\" }")
    public MovimentacaoDTO registrarPerda(@Valid @RequestBody PerdaEstoqueDTO dto) {
        return service.registrarPerda(dto);
    }
}
