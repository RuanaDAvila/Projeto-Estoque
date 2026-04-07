package com.adega.stock.controller;

import com.adega.stock.dto.CategoriaDTO;
import com.adega.stock.service.CategoriaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoriaController.class)
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoriaService service;

    @Test
    void listar_retornaLista() throws Exception {
        when(service.listarTodas()).thenReturn(List.of(
                CategoriaDTO.builder().id(1L).nome("Bebida").build()
        ));

        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Bebida"));
    }

    @Test
    void buscar_retornaCategoria() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(
                CategoriaDTO.builder().id(1L).nome("Bebida").build()
        );

        mockMvc.perform(get("/api/categorias/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Bebida"));
    }

    @Test
    void criar_retorna201() throws Exception {
        CategoriaDTO dto = CategoriaDTO.builder().nome("Bebida").descricao("Desc").build();
        CategoriaDTO response = CategoriaDTO.builder().id(1L).nome("Bebida").descricao("Desc").build();

        when(service.criar(any())).thenReturn(response);

        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void criar_nomeVazio_retorna400() throws Exception {
        CategoriaDTO dto = CategoriaDTO.builder().nome("").build();

        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void atualizar_retornaCategoria() throws Exception {
        CategoriaDTO dto = CategoriaDTO.builder().nome("Bebida").build();
        when(service.atualizar(eq(1L), any())).thenReturn(
                CategoriaDTO.builder().id(1L).nome("Bebida").build()
        );

        mockMvc.perform(put("/api/categorias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Bebida"));
    }

    @Test
    void deletar_retorna204() throws Exception {
        doNothing().when(service).deletar(1L);

        mockMvc.perform(delete("/api/categorias/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void buscar_naoEncontrado_retorna404() throws Exception {
        when(service.buscarPorId(99L)).thenThrow(new RuntimeException("Nao encontrado"));

        mockMvc.perform(get("/api/categorias/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void criar_nomeJaExiste_retorna400() throws Exception {
        CategoriaDTO dto = CategoriaDTO.builder().nome("Bebida").build();
        when(service.criar(any())).thenThrow(new IllegalArgumentException("Ja existe"));

        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}
