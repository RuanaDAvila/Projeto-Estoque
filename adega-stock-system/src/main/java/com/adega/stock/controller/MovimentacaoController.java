package com.adega.stock.controller;

import com.adega.stock.dto.MovimentacaoDTO;
import com.adega.stock.entity.TipoMovimentacao;
import com.adega.stock.service.MovimentacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/movimentacoes")
@RequiredArgsConstructor
@Tag(name = "Movimentacoes", description = "Historico completo de movimentacoes de estoque")
public class MovimentacaoController {

    private final MovimentacaoService service;

    @GetMapping
    @Operation(summary = "Listar todas as movimentacoes (com filtros opcionais)",
               description = "Todos os parametros sao opcionais. Tipo: ENTRADA, VENDA ou PERDA. Datas no formato: 2024-01-15T10:30:00")
    public List<MovimentacaoDTO> listar(
            @Parameter(description = "Filtrar por ID do produto") @RequestParam(required = false) Long produtoId,
            @Parameter(description = "Filtrar por tipo: ENTRADA, VENDA, PERDA") @RequestParam(required = false) TipoMovimentacao tipo,
            @Parameter(description = "Data/hora inicio (ex: 2024-01-01T00:00:00)") @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @Parameter(description = "Data/hora fim (ex: 2024-12-31T23:59:59)") @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim
    ) {
        if (produtoId == null && tipo == null && inicio == null && fim == null) {
            return service.listarTodas();
        }
        return service.filtrar(produtoId, tipo, inicio, fim);
    }
}
