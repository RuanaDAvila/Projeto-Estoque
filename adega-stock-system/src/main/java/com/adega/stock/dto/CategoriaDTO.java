package com.adega.stock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados de uma categoria de produto")
public class CategoriaDTO {

    @Schema(description = "ID da categoria", example = "1")
    private Long id;

    @NotBlank
    @Schema(description = "Nome da categoria", example = "Bebida Alcoolica", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nome;

    @Schema(description = "Descricao da categoria", example = "Bebidas com teor alcoolico")
    private String descricao;
}
