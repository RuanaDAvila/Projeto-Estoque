package com.adega.stock.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "produtos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nome;

    @Column
    private String unidadeEstoque;

    @NotNull
    @DecimalMin("0.0")
    @Column(nullable = false)
    private BigDecimal quantidadeAtual;

    @NotNull
    @DecimalMin("0.0")
    @Column(nullable = false)
    private BigDecimal valorCusto;

    @NotNull
    @DecimalMin("0.0")
    @Column(nullable = false)
    private BigDecimal valorVenda;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    // true = este produto pode ser vendido em frações (ex: garrafa pode virar doses)
    // false = só pode ser vendido inteiro
    @Column(nullable = false)
    private boolean fracionavel = false;

    // Nome da unidade fracionada. Ex: "dose", "cigarro"
    private String unidadeFracionada;

    // Quantas frações existem em 1 unidade inteira
    // Ex: 1 garrafa = 15 doses → fatorFracionamento = 15
    @DecimalMin("1.0")
    private BigDecimal fatorFracionamento;
}
