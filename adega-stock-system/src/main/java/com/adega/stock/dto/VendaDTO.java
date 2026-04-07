package com.adega.stock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados de uma venda")
public class VendaDTO {

    @Schema(description = "ID da venda", example = "1")
    private Long id;

    @Schema(description = "Data e hora da venda (somente leitura)")
    private LocalDateTime dataHora;

    @Schema(description = "Valor total da venda (somente leitura, calculado automaticamente)")
    private BigDecimal valorTotal;

    @Schema(description = "Observacao da venda", example = "Mesa 5")
    private String observacao;

    @Schema(description = "Valor de desconto aplicado na venda", example = "5.00")
    private BigDecimal desconto;

    @Valid
    @NotEmpty
    @Schema(description = "Itens vendidos", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<VendaItemDTO> itens;
}
