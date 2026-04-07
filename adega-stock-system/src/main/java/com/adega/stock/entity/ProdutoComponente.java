package com.adega.stock.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "produto_componentes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProdutoComponente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_pai_id", nullable = false)
    private Produto produtoPai;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "produto_filho_id", nullable = false)
    private Produto produtoFilho;

    @Column(nullable = false)
    private BigDecimal quantidade;
}
