package com.adega.stock.service;

import com.adega.stock.dto.CategoriaDTO;
import com.adega.stock.entity.Categoria;
import com.adega.stock.repository.CategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository repository;

    @InjectMocks
    private CategoriaService service;

    private Categoria categoria;
    private CategoriaDTO dto;

    @BeforeEach
    void setUp() {
        categoria = Categoria.builder().id(1L).nome("Bebida").descricao("Desc").build();
        dto = CategoriaDTO.builder().nome("Bebida").descricao("Desc").build();
    }

    @Test
    void listarTodas_retornaLista() {
        when(repository.findAll()).thenReturn(List.of(categoria));
        List<CategoriaDTO> result = service.listarTodas();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNome()).isEqualTo("Bebida");
    }

    @Test
    void buscarPorId_retornaCategoria() {
        when(repository.findById(1L)).thenReturn(Optional.of(categoria));
        CategoriaDTO result = service.buscarPorId(1L);
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNome()).isEqualTo("Bebida");
    }

    @Test
    void buscarPorId_naoEncontrado_lancaExcecao() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("99");
    }

    @Test
    void criar_sucesso() {
        when(repository.existsByNome("Bebida")).thenReturn(false);
        when(repository.save(any())).thenReturn(categoria);
        CategoriaDTO result = service.criar(dto);
        assertThat(result.getNome()).isEqualTo("Bebida");
        verify(repository).save(any());
    }

    @Test
    void criar_nomeJaExiste_lancaExcecao() {
        when(repository.existsByNome("Bebida")).thenReturn(true);
        assertThatThrownBy(() -> service.criar(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bebida");
    }

    @Test
    void atualizar_sucesso() {
        when(repository.findById(1L)).thenReturn(Optional.of(categoria));
        when(repository.save(any())).thenReturn(categoria);
        CategoriaDTO result = service.atualizar(1L, dto);
        assertThat(result.getNome()).isEqualTo("Bebida");
    }

    @Test
    void atualizar_naoEncontrado_lancaExcecao() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.atualizar(99L, dto))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void deletar_sucesso() {
        when(repository.findById(1L)).thenReturn(Optional.of(categoria));
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
        when(repository.findById(1L)).thenReturn(Optional.of(categoria));
        Categoria result = service.findById(1L);
        assertThat(result.getId()).isEqualTo(1L);
    }
}
