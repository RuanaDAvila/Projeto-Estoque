package com.adega.stock.service;

import com.adega.stock.dto.ComboDTO;
import com.adega.stock.dto.ComboItemDTO;
import com.adega.stock.entity.*;
import com.adega.stock.repository.ComboRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComboServiceTest {

    @Mock
    private ComboRepository repository;

    @Mock
    private ProdutoService produtoService;

    @InjectMocks
    private ComboService service;

    private Categoria categoria;
    private Produto produto;
    private Combo combo;
    private ComboDTO dto;

    @BeforeEach
    void setUp() {
        categoria = Categoria.builder().id(1L).nome("Bebida").build();
        produto = Produto.builder()
                .id(1L).nome("Whisky").unidadeEstoque("garrafa")
                .quantidadeAtual(new BigDecimal("10"))
                .valorCusto(new BigDecimal("80")).valorVenda(new BigDecimal("150"))
                .categoria(categoria).build();

        combo = Combo.builder()
                .id(1L).nome("Combo Whisky").descricao("Desc")
                .valorVenda(new BigDecimal("160")).valorCusto(new BigDecimal("80"))
                .itens(new ArrayList<>())
                .build();

        ComboItem item = ComboItem.builder()
                .id(1L).combo(combo).produto(produto)
                .quantidade(new BigDecimal("1")).build();
        combo.getItens().add(item);

        dto = ComboDTO.builder()
                .nome("Combo Whisky").descricao("Desc")
                .valorVenda(new BigDecimal("160")).valorCusto(new BigDecimal("80"))
                .itens(List.of(ComboItemDTO.builder().produtoId(1L).quantidade(new BigDecimal("1")).build()))
                .build();
    }

    @Test
    void listarTodos_retornaLista() {
        when(repository.findAll()).thenReturn(List.of(combo));
        List<ComboDTO> result = service.listarTodos();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNome()).isEqualTo("Combo Whisky");
        assertThat(result.get(0).getItens()).hasSize(1);
    }

    @Test
    void buscarPorId_retornaCombo() {
        when(repository.findById(1L)).thenReturn(Optional.of(combo));
        ComboDTO result = service.buscarPorId(1L);
        assertThat(result.getNome()).isEqualTo("Combo Whisky");
        assertThat(result.getItens().get(0).getProdutoNome()).isEqualTo("Whisky");
    }

    @Test
    void buscarPorId_naoEncontrado_lancaExcecao() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void criar_comItens_sucesso() {
        when(produtoService.findById(1L)).thenReturn(produto);
        when(repository.save(any())).thenReturn(combo);
        ComboDTO result = service.criar(dto);
        assertThat(result.getNome()).isEqualTo("Combo Whisky");
    }

    @Test
    void criar_semItens_sucesso() {
        dto.setItens(null);
        combo.setItens(new ArrayList<>());
        when(repository.save(any())).thenReturn(combo);
        ComboDTO result = service.criar(dto);
        assertThat(result.getNome()).isEqualTo("Combo Whisky");
    }

    @Test
    void deletar_sucesso() {
        when(repository.findById(1L)).thenReturn(Optional.of(combo));
        service.deletar(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    void deletar_naoEncontrado_lancaExcecao() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.deletar(99L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void findById_retornaEntidade() {
        when(repository.findById(1L)).thenReturn(Optional.of(combo));
        Combo result = service.findById(1L);
        assertThat(result.getId()).isEqualTo(1L);
    }
}
