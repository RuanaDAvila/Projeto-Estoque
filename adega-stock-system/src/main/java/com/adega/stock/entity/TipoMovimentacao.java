package com.adega.stock.entity;

public enum TipoMovimentacao {
    ENTRADA,  // produto chegou no estoque (compra/reposição)
    VENDA,    // produto saiu por venda ao cliente
    PERDA     // produto saiu por quebra, vencimento ou extravio
}
