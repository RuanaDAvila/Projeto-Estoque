package com.adega.stock.controller;

import com.adega.stock.dto.ComboDTO;
import com.adega.stock.dto.ComboItemDTO;
import com.adega.stock.service.ComboService;
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

@WebMvcTest(ComboController.class)
class ComboControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private ComboService service;

    private ComboDTO comboDTO() {
        return ComboDTO.builder()
                .id(1L).nome("Combo Whisky")
                .valorVenda(new BigDecimal("160")).valorCusto(new BigDecimal("80"))
                .itens(List.of(ComboItemDTO.builder().id(1L).produtoId(1L)
                        .produtoNome("Whisky").quantidade(new BigDecimal("1")).build()))
                .build();
    }

    @Test
    void listar_retornaLista() throws Exception {
        when(service.listarTodos()).thenReturn(List.of(comboDTO()));
        mockMvc.perform(get("/api/combos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Combo Whisky"));
    }

    @Test
    void buscar_retornaCombo() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(comboDTO());
        mockMvc.perform(get("/api/combos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Combo Whisky"))
                .andExpect(jsonPath("$.itens[0].produtoNome").value("Whisky"));
    }

    @Test
    void buscar_naoEncontrado_retorna404() throws Exception {
        when(service.buscarPorId(99L)).thenThrow(new RuntimeException("Nao encontrado"));
        mockMvc.perform(get("/api/combos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void criar_retorna201() throws Exception {
        ComboDTO dto = comboDTO();
        dto.setId(null);
        when(service.criar(any())).thenReturn(comboDTO());

        mockMvc.perform(post("/api/combos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void criar_nomeVazio_retorna400() throws Exception {
        ComboDTO dto = ComboDTO.builder().nome("").build();
        mockMvc.perform(post("/api/combos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deletar_retorna204() throws Exception {
        doNothing().when(service).deletar(1L);
        mockMvc.perform(delete("/api/combos/1"))
                .andExpect(status().isNoContent());
    }
}
