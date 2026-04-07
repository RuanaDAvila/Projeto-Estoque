package com.adega.stock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Saldo atual de estoque de um produto")
public class SaldoEstoqueDTO {

    private Long produtoId;
    private String produtoNome;
    private String unidadeEstoque;
    private BigDecimal quantidadeAtual;
    private BigDecimal valorCusto;
    private BigDecimal valorVenda;
    private String categoriaNome;
    private boolean fracionavel;
    private String unidadeFracionada;
    private BigDecimal fatorFracionamento;

    @Schema(description = "Quantidade disponivel em fracoes (se fracionavel)")
    private BigDecimal quantidadeEmFracoes;
}
