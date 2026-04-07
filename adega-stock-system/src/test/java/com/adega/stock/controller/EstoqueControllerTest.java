package com.adega.stock.controller;

import com.adega.stock.dto.*;
import com.adega.stock.entity.TipoMovimentacao;
import com.adega.stock.service.EstoqueService;
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

@WebMvcTest(EstoqueController.class)
class EstoqueControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private EstoqueService service;

    @Test
    void listarSaldos_retornaLista() throws Exception {
        SaldoEstoqueDTO saldo = SaldoEstoqueDTO.builder()
                .produtoId(1L).produtoNome("Whisky")
                .quantidadeAtual(new BigDecimal("10")).build();

        when(service.listarSaldos()).thenReturn(List.of(saldo));

        mockMvc.perform(get("/api/estoque/saldo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].produtoNome").value("Whisky"));
    }

    @Test
    void saldoPorProduto_retornaSaldo() throws Exception {
        SaldoEstoqueDTO saldo = SaldoEstoqueDTO.builder()
                .produtoId(1L).produtoNome("Whisky")
                .quantidadeAtual(new BigDecimal("10")).build();

        when(service.saldoPorProduto(1L)).thenReturn(saldo);

        mockMvc.perform(get("/api/estoque/saldo/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.produtoNome").value("Whisky"));
    }

    @Test
    void registrarEntrada_retorna201() throws Exception {
        EntradaEstoqueDTO dto = EntradaEstoqueDTO.builder()
                .produtoId(1L).quantidade(new BigDecimal("5"))
                .valorUnitarioCusto(new BigDecimal("80")).build();

        MovimentacaoDTO response = MovimentacaoDTO.builder()
                .id(1L).tipo(TipoMovimentacao.ENTRADA)
                .quantidade(new BigDecimal("5"))
                .dataHora(LocalDateTime.now()).build();

        when(service.registrarEntrada(any())).thenReturn(response);

        mockMvc.perform(post("/api/estoque/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("ENTRADA"));
    }

    @Test
    void registrarEntrada_camposObrigatoriosAusentes_retorna400() throws Exception {
        EntradaEstoqueDTO dto = EntradaEstoqueDTO.builder().build();
        mockMvc.perform(post("/api/estoque/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrarPerda_retorna201() throws Exception {
        PerdaEstoqueDTO dto = PerdaEstoqueDTO.builder()
                .produtoId(1L).quantidade(new BigDecimal("1"))
                .motivo("Quebrada").build();

        MovimentacaoDTO response = MovimentacaoDTO.builder()
                .id(1L).tipo(TipoMovimentacao.PERDA)
                .motivo("Quebrada")
                .dataHora(LocalDateTime.now()).build();

        when(service.registrarPerda(any())).thenReturn(response);

        mockMvc.perform(post("/api/estoque/perda")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("PERDA"));
    }

    @Test
    void registrarPerda_semMotivo_retorna400() throws Exception {
        PerdaEstoqueDTO dto = PerdaEstoqueDTO.builder()
                .produtoId(1L).quantidade(new BigDecimal("1")).build();

        mockMvc.perform(post("/api/estoque/perda")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrarPerda_estoqueInsuficiente_retorna400() throws Exception {
        PerdaEstoqueDTO dto = PerdaEstoqueDTO.builder()
                .produtoId(1L).quantidade(new BigDecimal("99"))
                .motivo("Perda").build();

        when(service.registrarPerda(any())).thenThrow(new IllegalArgumentException("Estoque insuficiente"));

        mockMvc.perform(post("/api/estoque/perda")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}
