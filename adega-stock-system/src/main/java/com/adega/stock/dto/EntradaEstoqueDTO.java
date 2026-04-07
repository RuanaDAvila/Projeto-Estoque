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
@Schema(description = "Dados para entrada de estoque (reposicao/compra)")
public class EntradaEstoqueDTO {

    @NotNull
    @Schema(description = "ID do produto", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long produtoId;

    @NotNull
    @DecimalMin("0.01")
    @Schema(description = "Quantidade a ser adicionada", example = "5.0", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal quantidade;

    @NotNull
    @DecimalMin("0.0")
    @Schema(description = "Valor unitario de custo desta entrada", example = "85.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal valorUnitarioCusto;

    @Schema(description = "Observacao sobre a entrada", example = "Reposicao de estoque mensal")
    private String observacao;
}
