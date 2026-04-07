package com.adega.stock.service;

import com.adega.stock.dto.EntradaEstoqueDTO;
import com.adega.stock.dto.MovimentacaoDTO;
import com.adega.stock.dto.PerdaEstoqueDTO;
import com.adega.stock.dto.SaldoEstoqueDTO;
import com.adega.stock.entity.*;
import com.adega.stock.repository.MovimentacaoEstoqueRepository;
import com.adega.stock.repository.ProdutoRepository;
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
class EstoqueServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private MovimentacaoEstoqueRepository movimentacaoRepository;

    @Mock
    private ProdutoService produtoService;

    @InjectMocks
    private EstoqueService service;

    private Categoria categoria;
    private Produto produto;
    private Produto produtoFracionavel;

    @BeforeEach
    void setUp() {
        categoria = Categoria.builder().id(1L).nome("Bebida").build();

        produto = Produto.builder()
                .id(1L).nome("Whisky").unidadeEstoque("garrafa")
                .quantidadeAtual(new BigDecimal("10"))
                .valorCusto(new BigDecimal("80")).valorVenda(new BigDecimal("150"))
                .categoria(categoria).fracionavel(false)
                .build();

        produtoFracionavel = Produto.builder()
                .id(2L).nome("Cachaca").unidadeEstoque("garrafa")
                .quantidadeAtual(new BigDecimal("5"))
                .valorCusto(new BigDecimal("30")).valorVenda(new BigDecimal("60"))
                .categoria(categoria).fracionavel(true)
                .unidadeFracionada("dose").fatorFracionamento(new BigDecimal("20"))
                .build();
    }

    @Test
    void registrarEntrada_sucesso() {
        EntradaEstoqueDTO dto = EntradaEstoqueDTO.builder()
                .produtoId(1L).quantidade(new BigDecimal("5"))
                .valorUnitarioCusto(new BigDecimal("80"))
                .observacao("Reposicao")
                .build();

        MovimentacaoEstoque mov = MovimentacaoEstoque.builder()
                .id(1L).produto(produto).quantidade(new BigDecimal("5"))
                .tipo(TipoMovimentacao.ENTRADA)
                .valorUnitarioCusto(new BigDecimal("80"))
                .valorTotal(new BigDecimal("400"))
                .dataHora(LocalDateTime.now())
                .build();

        when(produtoService.findById(1L)).thenReturn(produto);
        when(produtoRepository.save(any())).thenReturn(produto);
        when(movimentacaoRepository.save(any())).thenReturn(mov);

        MovimentacaoDTO result = service.registrarEntrada(dto);
        assertThat(result.getTipo()).isEqualTo(TipoMovimentacao.ENTRADA);
        assertThat(result.getQuantidade()).isEqualByComparingTo("5");
    }

    @Test
    void registrarPerda_sucesso() {
        PerdaEstoqueDTO dto = PerdaEstoqueDTO.builder()
                .produtoId(1L).quantidade(new BigDecimal("2"))
                .motivo("Garrafa quebrada")
                .build();

        MovimentacaoEstoque mov = MovimentacaoEstoque.builder()
                .id(1L).produto(produto).quantidade(new BigDecimal("2"))
                .tipo(TipoMovimentacao.PERDA)
                .valorUnitarioCusto(new BigDecimal("80"))
                .valorTotal(new BigDecimal("160"))
                .motivo("Garrafa quebrada")
                .dataHora(LocalDateTime.now())
                .build();

        when(produtoService.findById(1L)).thenReturn(produto);
        when(produtoRepository.save(any())).thenReturn(produto);
        when(movimentacaoRepository.save(any())).thenReturn(mov);

        MovimentacaoDTO result = service.registrarPerda(dto);
        assertThat(result.getTipo()).isEqualTo(TipoMovimentacao.PERDA);
        assertThat(result.getMotivo()).isEqualTo("Garrafa quebrada");
    }

    @Test
    void registrarPerda_estoqueInsuficiente_lancaExcecao() {
        PerdaEstoqueDTO dto = PerdaEstoqueDTO.builder()
                .produtoId(1L).quantidade(new BigDecimal("99"))
                .motivo("Perda").build();

        when(produtoService.findById(1L)).thenReturn(produto);

        assertThatThrownBy(() -> service.registrarPerda(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Estoque insuficiente");
    }

    @Test
    void listarSaldos_retornaLista() {
        when(produtoRepository.findAll()).thenReturn(List.of(produto, produtoFracionavel));
        List<SaldoEstoqueDTO> result = service.listarSaldos();
        assertThat(result).hasSize(2);
    }

    @Test
    void listarSaldos_produtoFracionavel_calculaFracoes() {
        when(produtoRepository.findAll()).thenReturn(List.of(produtoFracionavel));
        List<SaldoEstoqueDTO> result = service.listarSaldos();
        assertThat(result.get(0).getQuantidadeEmFracoes()).isEqualByComparingTo("100"); // 5 * 20
    }

    @Test
    void listarSaldos_produtoNaoFracionavel_fracaoNula() {
        when(produtoRepository.findAll()).thenReturn(List.of(produto));
        List<SaldoEstoqueDTO> result = service.listarSaldos();
        assertThat(result.get(0).getQuantidadeEmFracoes()).isNull();
    }

    @Test
    void saldoPorProduto_retornaSaldo() {
        when(produtoService.findById(1L)).thenReturn(produto);
        SaldoEstoqueDTO result = service.saldoPorProduto(1L);
        assertThat(result.getProdutoId()).isEqualTo(1L);
        assertThat(result.getQuantidadeAtual()).isEqualByComparingTo("10");
    }

    @Test
    void toDTO_movimentacaoComVenda_retornaDTO() {
        Venda venda = Venda.builder().id(5L).build();
        Combo combo = Combo.builder().id(2L).nome("Combo").build();

        MovimentacaoEstoque mov = MovimentacaoEstoque.builder()
                .id(1L).produto(produto).combo(combo)
                .quantidade(new BigDecimal("1"))
                .tipo(TipoMovimentacao.VENDA)
                .valorUnitarioCusto(new BigDecimal("80"))
                .valorUnitarioVenda(new BigDecimal("150"))
                .valorTotal(new BigDecimal("150"))
                .dataHora(LocalDateTime.now())
                .venda(venda)
                .fracionado(true)
                .fatorFracionamento(new BigDecimal("15"))
                .build();

        MovimentacaoDTO result = service.toDTO(mov);
        assertThat(result.getVendaId()).isEqualTo(5L);
        assertThat(result.getComboId()).isEqualTo(2L);
        assertThat(result.isFracionado()).isTrue();
    }

    @Test
    void toDTO_movimentacaoSemProdutoSemCombo_retornaDTO() {
        MovimentacaoEstoque mov = MovimentacaoEstoque.builder()
                .id(1L)
                .quantidade(new BigDecimal("1"))
                .tipo(TipoMovimentacao.ENTRADA)
                .valorUnitarioCusto(new BigDecimal("80"))
                .valorTotal(new BigDecimal("80"))
                .dataHora(LocalDateTime.now())
                .build();

        MovimentacaoDTO result = service.toDTO(mov);
        assertThat(result.getProdutoId()).isNull();
        assertThat(result.getComboId()).isNull();
        assertThat(result.getVendaId()).isNull();
    }
}
