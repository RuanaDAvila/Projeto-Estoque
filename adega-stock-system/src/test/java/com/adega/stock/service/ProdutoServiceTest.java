package com.adega.stock.service;

import com.adega.stock.dto.ProdutoDTO;
import com.adega.stock.entity.Categoria;
import com.adega.stock.entity.Produto;
import com.adega.stock.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository repository;

    @Mock
    private CategoriaService categoriaService;

    @InjectMocks
    private ProdutoService service;

    private Categoria categoria;
    private Produto produto;
    private ProdutoDTO dto;

    @BeforeEach
    void setUp() {
        categoria = Categoria.builder().id(1L).nome("Bebida").build();
        produto = Produto.builder()
                .id(1L).nome("Whisky").unidadeEstoque("garrafa")
                .quantidadeAtual(new BigDecimal("10"))
                .valorCusto(new BigDecimal("80")).valorVenda(new BigDecimal("150"))
                .categoria(categoria).fracionavel(true)
                .unidadeFracionada("dose").fatorFracionamento(new BigDecimal("15"))
                .build();
        dto = ProdutoDTO.builder()
                .nome("Whisky").unidadeEstoque("garrafa")
                .quantidadeAtual(new BigDecimal("10"))
                .valorCusto(new BigDecimal("80")).valorVenda(new BigDecimal("150"))
                .categoriaId(1L).fracionavel(true)
                .unidadeFracionada("dose").fatorFracionamento(new BigDecimal("15"))
                .build();
    }

    @Test
    void listarTodos_retornaLista() {
        when(repository.findAll()).thenReturn(List.of(produto));
        List<ProdutoDTO> result = service.listarTodos();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNome()).isEqualTo("Whisky");
    }

    @Test
    void buscarPorId_retornaProduto() {
        when(repository.findById(1L)).thenReturn(Optional.of(produto));
        ProdutoDTO result = service.buscarPorId(1L);
        assertThat(result.getNome()).isEqualTo("Whisky");
        assertThat(result.isFracionavel()).isTrue();
        assertThat(result.getFatorFracionamento()).isEqualByComparingTo("15");
    }

    @Test
    void buscarPorId_naoEncontrado_lancaExcecao() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void buscarPorCategoria_retornaLista() {
        when(repository.findByCategoriaId(1L)).thenReturn(List.of(produto));
        List<ProdutoDTO> result = service.buscarPorCategoria(1L);
        assertThat(result).hasSize(1);
    }

    @Test
    void criar_sucesso() {
        when(categoriaService.findById(1L)).thenReturn(categoria);
        when(repository.save(any())).thenReturn(produto);
        ProdutoDTO result = service.criar(dto);
        assertThat(result.getNome()).isEqualTo("Whisky");
    }

    @Test
    void atualizar_sucesso() {
        when(repository.findById(1L)).thenReturn(Optional.of(produto));
        when(categoriaService.findById(1L)).thenReturn(categoria);
        when(repository.save(any())).thenReturn(produto);
        ProdutoDTO result = service.atualizar(1L, dto);
        assertThat(result.getNome()).isEqualTo("Whisky");
    }

    @Test
    void atualizar_naoEncontrado_lancaExcecao() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.atualizar(99L, dto))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void deletar_sucesso() {
        when(repository.findById(1L)).thenReturn(Optional.of(produto));
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
        when(repository.findById(1L)).thenReturn(Optional.of(produto));
        Produto result = service.findById(1L);
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void toDTO_produtoSemFracionamento_retornaDTO() {
        produto.setFracionavel(false);
        produto.setUnidadeFracionada(null);
        produto.setFatorFracionamento(null);
        when(repository.findById(1L)).thenReturn(Optional.of(produto));
        ProdutoDTO result = service.buscarPorId(1L);
        assertThat(result.isFracionavel()).isFalse();
        assertThat(result.getFatorFracionamento()).isNull();
    }
}
