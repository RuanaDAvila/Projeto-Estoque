package com.adega.stock.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "venda_itens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venda_id", nullable = false)
    private Venda venda;

    // Produto vendido diretamente — será null se o item for um combo
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "produto_id")
    private Produto produto;

    // Combo vendido — será null se o item for um produto direto
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "combo_id")
    private Combo combo;

    @Column(nullable = false)
    private BigDecimal quantidade;

    @Column(nullable = false)
    private BigDecimal valorUnitarioVenda;

    @Column(nullable = false)
    private BigDecimal valorUnitarioCusto;

    // true = vendido como fração (ex: dose avulsa de garrafa)
    // false = vendido inteiro
    private boolean fracionado = false;

    // Quantas frações tem 1 unidade — só preenchido quando fracionado = true
    // Ex: 1 garrafa = 15 doses → fatorFracionamento = 15
    private BigDecimal fatorFracionamento;
}
