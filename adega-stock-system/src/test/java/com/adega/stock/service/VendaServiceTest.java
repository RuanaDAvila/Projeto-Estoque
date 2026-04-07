package com.adega.stock.service;

import com.adega.stock.dto.VendaDTO;
import com.adega.stock.dto.VendaItemDTO;
import com.adega.stock.entity.*;
import com.adega.stock.repository.MovimentacaoEstoqueRepository;
import com.adega.stock.repository.ProdutoRepository;
import com.adega.stock.repository.VendaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VendaServiceTest {

    @Mock private VendaRepository vendaRepository;
    @Mock private ProdutoRepository produtoRepository;
    @Mock private MovimentacaoEstoqueRepository movimentacaoRepository;
    @Mock private ProdutoService produtoService;
    @Mock private ComboService comboService;

    @InjectMocks
    private VendaService service;

    private Categoria categoria;
    private Produto produto;
    private Produto produtoFracionavel;
    private Combo combo;

    @BeforeEach
    void setUp() {
        categoria = Categoria.builder().id(1L).nome("Bebida").build();

        produto = Produto.builder()
                .id(1L).nome("Whisky").unidadeEstoque("garrafa")
                .quantidadeAtual(new BigDecimal("10"))
                .valorCusto(new BigDecimal("80")).valorVenda(new BigDecimal("150"))
                .categoria(categoria).fracionavel(false).build();

        produtoFracionavel = Produto.builder()
                .id(2L).nome("Cachaca").unidadeEstoque("garrafa")
                .quantidadeAtual(new BigDecimal("5"))
                .valorCusto(new BigDecimal("30")).valorVenda(new BigDecimal("60"))
                .categoria(categoria).fracionavel(true)
                .unidadeFracionada("dose").fatorFracionamento(new BigDecimal("15"))
                .build();

        ComboItem comboItem = ComboItem.builder()
                .produto(produto).quantidade(new BigDecimal("1")).build();

        combo = Combo.builder()
                .id(1L).nome("Combo Whisky")
                .valorVenda(new BigDecimal("160")).valorCusto(new BigDecimal("80"))
                .itens(new ArrayList<>(List.of(comboItem)))
                .build();
        comboItem.setCombo(combo);
    }

    private Venda vendaSalva(List<VendaItem> itens, BigDecimal total) {
        Venda v = Venda.builder().id(1L).dataHora(LocalDateTime.now())
                .valorTotal(total).itens(new ArrayList<>(itens)).build();
        return v;
    }

    @Test
    void registrarVenda_produtoInteiro_sucesso() {
        VendaItemDTO itemDTO = VendaItemDTO.builder()
                .produtoId(1L).quantidade(new BigDecimal("2")).fracionado(false).build();
        VendaDTO dto = VendaDTO.builder().itens(List.of(itemDTO)).build();

        VendaItem item = VendaItem.builder().produto(produto)
                .quantidade(new BigDecimal("2"))
                .valorUnitarioVenda(new BigDecimal("150"))
                .valorUnitarioCusto(new BigDecimal("80"))
                .fracionado(false).build();

        when(produtoService.findById(1L)).thenReturn(produto);
        when(produtoRepository.save(any())).thenReturn(produto);
        when(vendaRepository.save(any())).thenAnswer(inv -> {
            Venda v = inv.getArgument(0);
            v.setId(1L);
            if (v.getDataHora() == null) v.setDataHora(LocalDateTime.now());
            return v;
        });
        when(movimentacaoRepository.save(any())).thenReturn(new MovimentacaoEstoque());

        VendaDTO result = service.registrarVenda(dto);
        assertThat(result.getValorTotal()).isEqualByComparingTo("300");
    }

    @Test
    void registrarVenda_produtoInteiro_estoqueInsuficiente_lancaExcecao() {
        VendaItemDTO itemDTO = VendaItemDTO.builder()
                .produtoId(1L).quantidade(new BigDecimal("99")).fracionado(false).build();
        VendaDTO dto = VendaDTO.builder().itens(List.of(itemDTO)).build();

        when(produtoService.findById(1L)).thenReturn(produto);

        assertThatThrownBy(() -> service.registrarVenda(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Estoque insuficiente");
    }

    @Test
    void registrarVenda_fracionado_sucesso() {
        VendaItemDTO itemDTO = VendaItemDTO.builder()
                .produtoId(2L).quantidade(new BigDecimal("3")).fracionado(true).build();
        VendaDTO dto = VendaDTO.builder().itens(List.of(itemDTO)).build();

        when(produtoService.findById(2L)).thenReturn(produtoFracionavel);
        when(produtoRepository.save(any())).thenReturn(produtoFracionavel);
        when(vendaRepository.save(any())).thenAnswer(inv -> {
            Venda v = inv.getArgument(0);
            v.setId(1L);
            if (v.getDataHora() == null) v.setDataHora(LocalDateTime.now());
            return v;
        });
        when(movimentacaoRepository.save(any())).thenReturn(new MovimentacaoEstoque());

        VendaDTO result = service.registrarVenda(dto);
        assertThat(result).isNotNull();
    }

    @Test
    void registrarVenda_fracionado_produtoNaoFracionavel_lancaExcecao() {
        VendaItemDTO itemDTO = VendaItemDTO.builder()
                .produtoId(1L).quantidade(new BigDecimal("1")).fracionado(true).build();
        VendaDTO dto = VendaDTO.builder().itens(List.of(itemDTO)).build();

        when(produtoService.findById(1L)).thenReturn(produto);

        assertThatThrownBy(() -> service.registrarVenda(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fracionavel");
    }

    @Test
    void registrarVenda_fracionado_estoqueInsuficiente_lancaExcecao() {
        produtoFracionavel.setQuantidadeAtual(new BigDecimal("0.01"));
        VendaItemDTO itemDTO = VendaItemDTO.builder()
                .produtoId(2L).quantidade(new BigDecimal("100")).fracionado(true).build();
        VendaDTO dto = VendaDTO.builder().itens(List.of(itemDTO)).build();

        when(produtoService.findById(2L)).thenReturn(produtoFracionavel);

        assertThatThrownBy(() -> service.registrarVenda(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Estoque insuficiente");
    }

    @Test
    void registrarVenda_combo_sucesso() {
        VendaItemDTO itemDTO = VendaItemDTO.builder()
                .comboId(1L).quantidade(new BigDecimal("1")).build();
        VendaDTO dto = VendaDTO.builder().itens(List.of(itemDTO)).build();

        when(comboService.findById(1L)).thenReturn(combo);
        when(produtoRepository.save(any())).thenReturn(produto);
        when(vendaRepository.save(any())).thenAnswer(inv -> {
            Venda v = inv.getArgument(0);
            v.setId(1L);
            if (v.getDataHora() == null) v.setDataHora(LocalDateTime.now());
            return v;
        });
        when(movimentacaoRepository.save(any())).thenReturn(new MovimentacaoEstoque());

        VendaDTO result = service.registrarVenda(dto);
        assertThat(result.getValorTotal()).isEqualByComparingTo("160");
    }

    @Test
    void registrarVenda_combo_estoqueInsuficiente_lancaExcecao() {
        produto.setQuantidadeAtual(new BigDecimal("0"));
        VendaItemDTO itemDTO = VendaItemDTO.builder()
                .comboId(1L).quantidade(new BigDecimal("1")).build();
        VendaDTO dto = VendaDTO.builder().itens(List.of(itemDTO)).build();

        when(comboService.findById(1L)).thenReturn(combo);

        assertThatThrownBy(() -> service.registrarVenda(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Estoque insuficiente");
    }

    @Test
    void registrarVenda_semProdutoNemCombo_lancaExcecao() {
        VendaItemDTO itemDTO = VendaItemDTO.builder()
                .quantidade(new BigDecimal("1")).build();
        VendaDTO dto = VendaDTO.builder().itens(List.of(itemDTO)).build();

        assertThatThrownBy(() -> service.registrarVenda(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("produtoId ou comboId");
    }

    @Test
    void listarTodas_retornaLista() {
        Venda venda = Venda.builder().id(1L).dataHora(LocalDateTime.now())
                .valorTotal(new BigDecimal("150")).itens(new ArrayList<>()).build();
        when(vendaRepository.findAll()).thenReturn(List.of(venda));
        List<VendaDTO> result = service.listarTodas();
        assertThat(result).hasSize(1);
    }

    @Test
    void buscarPorId_retornaVenda() {
        Venda venda = Venda.builder().id(1L).dataHora(LocalDateTime.now())
                .valorTotal(new BigDecimal("150")).itens(new ArrayList<>()).build();
        when(vendaRepository.findById(1L)).thenReturn(Optional.of(venda));
        VendaDTO result = service.buscarPorId(1L);
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void buscarPorId_naoEncontrado_lancaExcecao() {
        when(vendaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void toDTO_itemComCombo_retornaDTO() {
        Combo comboItem = Combo.builder().id(1L).nome("Combo").build();
        VendaItem item = VendaItem.builder()
                .id(1L).combo(comboItem)
                .quantidade(new BigDecimal("1"))
                .valorUnitarioVenda(new BigDecimal("160"))
                .valorUnitarioCusto(new BigDecimal("80"))
                .fracionado(false).build();

        Venda venda = Venda.builder().id(1L).dataHora(LocalDateTime.now())
                .valorTotal(new BigDecimal("160"))
                .itens(new ArrayList<>(List.of(item))).build();
        item.setVenda(venda);

        when(vendaRepository.findById(1L)).thenReturn(Optional.of(venda));
        VendaDTO result = service.buscarPorId(1L);
        assertThat(result.getItens().get(0).getComboNome()).isEqualTo("Combo");
        assertThat(result.getItens().get(0).getProdutoId()).isNull();
    }
}
