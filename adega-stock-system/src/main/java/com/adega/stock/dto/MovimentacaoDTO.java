package com.adega.stock.dto;

import com.adega.stock.entity.TipoMovimentacao;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Movimentacao de estoque")
public class MovimentacaoDTO {

    private Long id;
    private Long produtoId;
    private String produtoNome;
    private Long comboId;
    private String comboNome;
    private BigDecimal quantidade;
    private TipoMovimentacao tipo;
    private BigDecimal valorUnitarioCusto;
    private BigDecimal valorUnitarioVenda;
    private BigDecimal valorTotal;
    private LocalDateTime dataHora;
    private String motivo;
    private Long vendaId;
    private boolean fracionado;
    private BigDecimal fatorFracionamento;
}
