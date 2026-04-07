package com.adega.stock.service;

import com.adega.stock.dto.MovimentacaoDTO;
import com.adega.stock.entity.*;
import com.adega.stock.repository.MovimentacaoEstoqueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovimentacaoServiceTest {

    @Mock
    private MovimentacaoEstoqueRepository repository;

    @Mock
    private EstoqueService estoqueService;

    @InjectMocks
    private MovimentacaoService service;

    private MovimentacaoEstoque movimentacao;

    @BeforeEach
    void setUp() {
        Categoria categoria = Categoria.builder().id(1L).nome("Bebida").build();
        Produto produto = Produto.builder()
                .id(1L).nome("Whisky").categoria(categoria)
                .valorCusto(new BigDecimal("80")).valorVenda(new BigDecimal("150"))
                .quantidadeAtual(new BigDecimal("10")).unidadeEstoque("garrafa")
                .build();

        movimentacao = MovimentacaoEstoque.builder()
                .id(1L).produto(produto)
                .quantidade(new BigDecimal("1"))
                .tipo(TipoMovimentacao.ENTRADA)
                .valorUnitarioCusto(new BigDecimal("80"))
                .valorTotal(new BigDecimal("80"))
                .dataHora(LocalDateTime.now())
                .build();
    }

    @Test
    void listarTodas_retornaLista() {
        MovimentacaoDTO dto = MovimentacaoDTO.builder().id(1L).tipo(TipoMovimentacao.ENTRADA).build();
        when(repository.findAll()).thenReturn(List.of(movimentacao));
        when(estoqueService.toDTO(movimentacao)).thenReturn(dto);

        List<MovimentacaoDTO> result = service.listarTodas();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTipo()).isEqualTo(TipoMovimentacao.ENTRADA);
    }

    @Test
    void filtrar_comParametros_retornaLista() {
        MovimentacaoDTO dto = MovimentacaoDTO.builder().id(1L).tipo(TipoMovimentacao.VENDA).build();
        LocalDateTime inicio = LocalDateTime.now().minusDays(1);
        LocalDateTime fim = LocalDateTime.now();

        when(repository.filtrar(1L, TipoMovimentacao.VENDA, inicio, fim))
                .thenReturn(List.of(movimentacao));
        when(estoqueService.toDTO(movimentacao)).thenReturn(dto);

        List<MovimentacaoDTO> result = service.filtrar(1L, TipoMovimentacao.VENDA, inicio, fim);
        assertThat(result).hasSize(1);
    }

    @Test
    void filtrar_semParametros_retornaLista() {
        MovimentacaoDTO dto = MovimentacaoDTO.builder().id(1L).build();
        when(repository.filtrar(null, null, null, null)).thenReturn(List.of(movimentacao));
        when(estoqueService.toDTO(movimentacao)).thenReturn(dto);

        List<MovimentacaoDTO> result = service.filtrar(null, null, null, null);
        assertThat(result).hasSize(1);
    }
}
