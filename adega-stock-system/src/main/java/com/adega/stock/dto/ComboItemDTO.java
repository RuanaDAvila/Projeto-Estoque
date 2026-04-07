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
@Schema(description = "Item de um combo")
public class ComboItemDTO {

    @Schema(description = "ID do item", example = "1")
    private Long id;

    @NotNull
    @Schema(description = "ID do produto", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long produtoId;

    @Schema(description = "Nome do produto (somente leitura)")
    private String produtoNome;

    @NotNull
    @DecimalMin("0.01")
    @Schema(description = "Quantidade deste produto no combo", example = "1.0", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal quantidade;
}
