package com.adega.stock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados para registro de perda de estoque")
public class PerdaEstoqueDTO {

    @NotNull
    @Schema(description = "ID do produto", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long produtoId;

    @NotNull
    @DecimalMin("0.01")
    @Schema(description = "Quantidade perdida", example = "1.0", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal quantidade;

    @NotBlank
    @Schema(description = "Motivo da perda (obrigatorio)", example = "Garrafa quebrada durante limpeza", requiredMode = Schema.RequiredMode.REQUIRED)
    private String motivo;
}
