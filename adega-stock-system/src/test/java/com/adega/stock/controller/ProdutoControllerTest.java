package com.adega.stock.controller;

import com.adega.stock.dto.ProdutoDTO;
import com.adega.stock.service.ProdutoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProdutoController.class)
class ProdutoControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private ProdutoService service;

    private ProdutoDTO produtoDTO() {
        return ProdutoDTO.builder()
                .id(1L).nome("Whisky").unidadeEstoque("garrafa")
                .quantidadeAtual(new BigDecimal("10"))
                .valorCusto(new BigDecimal("80")).valorVenda(new BigDecimal("150"))
                .categoriaId(1L).categoriaNome("Bebida").build();
    }

    @Test
    void listar_retornaLista() throws Exception {
        when(service.listarTodos()).thenReturn(List.of(produtoDTO()));
        mockMvc.perform(get("/api/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Whisky"));
    }

    @Test
    void buscar_retornaProduto() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(produtoDTO());
        mockMvc.perform(get("/api/produtos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Whisky"));
    }

    @Test
    void buscar_naoEncontrado_retorna404() throws Exception {
        when(service.buscarPorId(99L)).thenThrow(new RuntimeException("Nao encontrado"));
        mockMvc.perform(get("/api/produtos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void porCategoria_retornaLista() throws Exception {
        when(service.buscarPorCategoria(1L)).thenReturn(List.of(produtoDTO()));
        mockMvc.perform(get("/api/produtos/categoria/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Whisky"));
    }

    @Test
    void criar_retorna201() throws Exception {
        ProdutoDTO dto = produtoDTO();
        dto.setId(null);
        when(service.criar(any())).thenReturn(produtoDTO());

        mockMvc.perform(post("/api/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void criar_camposObrigatoriosAusentes_retorna400() throws Exception {
        ProdutoDTO dto = ProdutoDTO.builder().nome("").build();
        mockMvc.perform(post("/api/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void atualizar_retornaProduto() throws Exception {
        ProdutoDTO dto = produtoDTO();
        when(service.atualizar(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(put("/api/produtos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Whisky"));
    }

    @Test
    void deletar_retorna204() throws Exception {
        doNothing().when(service).deletar(1L);
        mockMvc.perform(delete("/api/produtos/1"))
                .andExpect(status().isNoContent());
    }
}
