package com.adega.stock.controller;

import com.adega.stock.dto.VendaDTO;
import com.adega.stock.dto.VendaItemDTO;
import com.adega.stock.service.VendaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VendaController.class)
class VendaControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private VendaService service;

    private VendaDTO vendaDTO() {
        return VendaDTO.builder()
                .id(1L).dataHora(LocalDateTime.now())
                .valorTotal(new BigDecimal("150"))
                .itens(List.of(VendaItemDTO.builder()
                        .produtoId(1L).quantidade(new BigDecimal("1"))
                        .valorUnitarioVenda(new BigDecimal("150")).build()))
                .build();
    }

    @Test
    void listar_retornaLista() throws Exception {
        when(service.listarTodas()).thenReturn(List.of(vendaDTO()));
        mockMvc.perform(get("/api/vendas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].valorTotal").value(150));
    }

    @Test
    void buscar_retornaVenda() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(vendaDTO());
        mockMvc.perform(get("/api/vendas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void buscar_naoEncontrado_retorna404() throws Exception {
        when(service.buscarPorId(99L)).thenThrow(new RuntimeException("Nao encontrada"));
        mockMvc.perform(get("/api/vendas/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void registrar_retorna201() throws Exception {
        VendaDTO dto = VendaDTO.builder()
                .itens(List.of(VendaItemDTO.builder()
                        .produtoId(1L).quantidade(new BigDecimal("1")).build()))
                .build();

        when(service.registrarVenda(any())).thenReturn(vendaDTO());

        mockMvc.perform(post("/api/vendas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valorTotal").value(150));
    }

    @Test
    void registrar_semItens_retorna400() throws Exception {
        VendaDTO dto = VendaDTO.builder().itens(List.of()).build();

        mockMvc.perform(post("/api/vendas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrar_estoqueInsuficiente_retorna400() throws Exception {
        VendaDTO dto = VendaDTO.builder()
                .itens(List.of(VendaItemDTO.builder()
                        .produtoId(1L).quantidade(new BigDecimal("99")).build()))
                .build();

        when(service.registrarVenda(any())).thenThrow(new IllegalArgumentException("Estoque insuficiente"));

        mockMvc.perform(post("/api/vendas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}
