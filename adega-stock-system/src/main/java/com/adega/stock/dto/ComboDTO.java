package com.adega.stock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados de um combo de produtos")
public class ComboDTO {

    @Schema(description = "ID do combo", example = "1")
    private Long id;

    @NotBlank
    @Schema(description = "Nome do combo", example = "Combo Aperol", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nome;

    @Schema(description = "Descricao do combo", example = "Aperol + Prosecco + Laranja")
    private String descricao;

    @NotNull
    @DecimalMin("0.0")
    @Schema(description = "Valor de venda do combo", example = "45.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal valorVenda;

    @NotNull
    @DecimalMin("0.0")
    @Schema(description = "Valor de custo do combo", example = "22.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal valorCusto;

    @Schema(description = "Itens que compõem o combo")
    private List<ComboItemDTO> itens;
}
