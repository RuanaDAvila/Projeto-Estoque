package com.adega.stock.repository;

import com.adega.stock.entity.MovimentacaoEstoque;
import com.adega.stock.entity.TipoMovimentacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    List<MovimentacaoEstoque> findByProdutoId(Long produtoId);

    List<MovimentacaoEstoque> findByTipo(TipoMovimentacao tipo);

    @Query("SELECT m FROM MovimentacaoEstoque m WHERE " +
           "(:produtoId IS NULL OR m.produto.id = :produtoId) AND " +
           "(:tipo IS NULL OR m.tipo = :tipo) AND " +
           "(:inicio IS NULL OR m.dataHora >= :inicio) AND " +
           "(:fim IS NULL OR m.dataHora <= :fim) " +
           "ORDER BY m.dataHora DESC")
    List<MovimentacaoEstoque> filtrar(
            @Param("produtoId") Long produtoId,
            @Param("tipo") TipoMovimentacao tipo,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );
}
