package com.adega.stock.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProdutoComponenteDTO {
    private Long produtoFilhoId;
    private String produtoFilhoNome;
    private BigDecimal quantidade;
}
