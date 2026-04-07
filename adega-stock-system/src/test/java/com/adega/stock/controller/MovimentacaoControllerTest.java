package com.adega.stock.controller;

import com.adega.stock.dto.MovimentacaoDTO;
import com.adega.stock.entity.TipoMovimentacao;
import com.adega.stock.service.MovimentacaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MovimentacaoController.class)
class MovimentacaoControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private MovimentacaoService service;

    private MovimentacaoDTO movDTO() {
        return MovimentacaoDTO.builder()
                .id(1L).tipo(TipoMovimentacao.ENTRADA)
                .quantidade(new BigDecimal("5"))
                .dataHora(LocalDateTime.now()).build();
    }

    @Test
    void listar_semFiltros_retornaListaCompleta() throws Exception {
        when(service.listarTodas()).thenReturn(List.of(movDTO()));

        mockMvc.perform(get("/api/movimentacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("ENTRADA"));

        verify(service).listarTodas();
        verify(service, never()).filtrar(any(), any(), any(), any());
    }

    @Test
    void listar_comFiltroProduto_usaFiltrar() throws Exception {
        when(service.filtrar(eq(1L), isNull(), isNull(), isNull()))
                .thenReturn(List.of(movDTO()));

        mockMvc.perform(get("/api/movimentacoes").param("produtoId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("ENTRADA"));

        verify(service).filtrar(1L, null, null, null);
    }

    @Test
    void listar_comFiltroTipo_usaFiltrar() throws Exception {
        when(service.filtrar(isNull(), eq(TipoMovimentacao.VENDA), isNull(), isNull()))
                .thenReturn(List.of(movDTO()));

        mockMvc.perform(get("/api/movimentacoes").param("tipo", "VENDA"))
                .andExpect(status().isOk());

        verify(service).filtrar(null, TipoMovimentacao.VENDA, null, null);
    }

    @Test
    void listar_comFiltroData_usaFiltrar() throws Exception {
        when(service.filtrar(isNull(), isNull(), any(), any()))
                .thenReturn(List.of(movDTO()));

        mockMvc.perform(get("/api/movimentacoes")
                        .param("inicio", "2024-01-01T00:00:00")
                        .param("fim", "2024-12-31T23:59:59"))
                .andExpect(status().isOk());

        verify(service).filtrar(isNull(), isNull(), any(), any());
    }

    @Test
    void listar_comTodosFiltros_usaFiltrar() throws Exception {
        when(service.filtrar(eq(1L), eq(TipoMovimentacao.PERDA), any(), any()))
                .thenReturn(List.of(movDTO()));

        mockMvc.perform(get("/api/movimentacoes")
                        .param("produtoId", "1")
                        .param("tipo", "PERDA")
                        .param("inicio", "2024-01-01T00:00:00")
                        .param("fim", "2024-12-31T23:59:59"))
                .andExpect(status().isOk());

        verify(service).filtrar(eq(1L), eq(TipoMovimentacao.PERDA), any(), any());
    }
}
