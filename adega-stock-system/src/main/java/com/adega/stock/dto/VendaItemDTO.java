package com.adega.stock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Item de uma venda")
public class VendaItemDTO {

    @Schema(description = "ID do item de venda")
    private Long id;

    @Schema(description = "ID do produto vendido (usar este OU comboId)")
    private Long produtoId;

    @Schema(description = "Nome do produto (somente leitura)")
    private String produtoNome;

    @Schema(description = "ID do combo vendido (usar este OU produtoId)")
    private Long comboId;

    @Schema(description = "Nome do combo (somente leitura)")
    private String comboNome;

    @NotNull
    @DecimalMin("0.01")
    @Schema(description = "Quantidade vendida", example = "2.0", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal quantidade;

    @Schema(description = "Valor unitario de venda (somente leitura, calculado automaticamente)")
    private BigDecimal valorUnitarioVenda;

    @Schema(description = "Valor unitario de custo (somente leitura)")
    private BigDecimal valorUnitarioCusto;

    @Schema(description = "Se foi vendido como fracao (ex: dose)", example = "false")
    private boolean fracionado;

    @Schema(description = "Fator de fracionamento (somente leitura)")
    private BigDecimal fatorFracionamento;
}
