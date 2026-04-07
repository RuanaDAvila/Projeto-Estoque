package com.adega.stock.dto;

import com.adega.stock.entity.TipoProduto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados de um produto")
public class ProdutoDTO {

    @Schema(description = "ID do produto", example = "1")
    private Long id;

    @NotBlank
    @Schema(description = "Nome do produto", example = "Whisky Jack Daniels 1L", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nome;

    @Schema(description = "Tipo do produto: SIMPLES ou COMPOSTO")
    @Builder.Default
    private TipoProduto tipo = TipoProduto.SIMPLES;

    @Schema(description = "Componentes (somente para produto COMPOSTO)")
    private List<ProdutoComponenteDTO> componentes;

    @Schema(description = "Unidade de estoque (garrafa, dose, maco, etc)", example = "garrafa")
    private String unidadeEstoque;

    @NotNull
    @DecimalMin("0.0")
    @Schema(description = "Quantidade atual em estoque", example = "10.0", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal quantidadeAtual;

    @NotNull
    @DecimalMin("0.0")
    @Schema(description = "Valor de custo (preco de compra)", example = "85.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal valorCusto;

    @NotNull
    @DecimalMin("0.0")
    @Schema(description = "Valor de venda ao cliente", example = "140.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal valorVenda;

    @NotNull
    @Schema(description = "ID da categoria do produto", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long categoriaId;

    @Schema(description = "Nome da categoria (somente leitura)")
    private String categoriaNome;

    @Schema(description = "Se o produto pode ser fracionado", example = "true")
    private boolean fracionavel;

    @Schema(description = "Unidade da fracao (ex: dose, cigarro)", example = "dose")
    private String unidadeFracionada;

    @Schema(description = "Quantas fracoes tem 1 unidade (ex: 1 garrafa = 15 doses)", example = "15")
    private BigDecimal fatorFracionamento;
}
