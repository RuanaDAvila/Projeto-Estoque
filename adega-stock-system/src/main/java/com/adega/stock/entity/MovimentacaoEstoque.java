package com.adega.stock.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimentacoes_estoque")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Produto movimentado — null se for um combo
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "produto_id")
    private Produto produto;

    // Combo movimentado — null se for um produto direto
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "combo_id")
    private Combo combo;

    @Column(nullable = false)
    private BigDecimal quantidade;

    // Tipo: ENTRADA, VENDA ou PERDA
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimentacao tipo;

    @Column(nullable = false)
    private BigDecimal valorUnitarioCusto;

    // Valor cobrado do cliente — só preenchido em vendas
    private BigDecimal valorUnitarioVenda;

    @Column(nullable = false)
    private BigDecimal valorTotal;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    // Motivo da movimentação — obrigatório para PERDA
    private String motivo;

    // Referência à venda — só preenchido quando tipo = VENDA
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venda_id")
    private Venda venda;

    // true = foi uma venda fracionada (ex: dose de garrafa)
    // false = unidade inteira
    private boolean fracionado = false;

    // Quantas frações tem 1 unidade — só preenchido quando fracionado = true
    // Ex: 1 garrafa = 15 doses → fatorFracionamento = 15
    private BigDecimal fatorFracionamento;

    @PrePersist
    public void prePersist() {
        if (dataHora == null) {
            dataHora = LocalDateTime.now();
        }
    }
}
