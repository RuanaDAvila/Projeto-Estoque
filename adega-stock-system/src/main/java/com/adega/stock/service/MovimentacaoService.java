package com.adega.stock.service;

import com.adega.stock.dto.MovimentacaoDTO;
import com.adega.stock.entity.TipoMovimentacao;
import com.adega.stock.repository.MovimentacaoEstoqueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovimentacaoService {

    private final MovimentacaoEstoqueRepository repository;
    private final EstoqueService estoqueService;

    public List<MovimentacaoDTO> filtrar(Long produtoId, TipoMovimentacao tipo,
                                         LocalDateTime inicio, LocalDateTime fim) {
        return repository.filtrar(produtoId, tipo, inicio, fim).stream()
                .map(estoqueService::toDTO)
                .collect(Collectors.toList());
    }

    public List<MovimentacaoDTO> listarTodas() {
        return repository.findAll().stream()
                .map(estoqueService::toDTO)
                .collect(Collectors.toList());
    }
}
