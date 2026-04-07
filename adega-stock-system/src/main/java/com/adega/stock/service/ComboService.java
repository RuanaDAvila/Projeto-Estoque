package com.adega.stock.service;

import com.adega.stock.dto.ComboDTO;
import com.adega.stock.dto.ComboItemDTO;
import com.adega.stock.entity.Combo;
import com.adega.stock.entity.ComboItem;
import com.adega.stock.entity.Produto;
import com.adega.stock.repository.ComboRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComboService {

    private final ComboRepository repository;
    private final ProdutoService produtoService;

    public List<ComboDTO> listarTodos() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ComboDTO buscarPorId(Long id) {
        return toDTO(findById(id));
    }

    public ComboDTO criar(ComboDTO dto) {
        Combo combo = Combo.builder()
                .nome(dto.getNome())
                .descricao(dto.getDescricao())
                .valorVenda(dto.getValorVenda())
                .valorCusto(dto.getValorCusto())
                .build();

        if (dto.getItens() != null) {
            for (ComboItemDTO itemDTO : dto.getItens()) {
                Produto produto = produtoService.findById(itemDTO.getProdutoId());
                ComboItem item = ComboItem.builder()
                        .combo(combo)
                        .produto(produto)
                        .quantidade(itemDTO.getQuantidade())
                        .build();
                combo.getItens().add(item);
            }
        }

        return toDTO(repository.save(combo));
    }

    public void deletar(Long id) {
        findById(id);
        repository.deleteById(id);
    }

    public Combo findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Combo nao encontrado: " + id));
    }

    public ComboDTO toDTO(Combo c) {
        List<ComboItemDTO> itens = c.getItens().stream()
                .map(i -> ComboItemDTO.builder()
                        .id(i.getId())
                        .produtoId(i.getProduto().getId())
                        .produtoNome(i.getProduto().getNome())
                        .quantidade(i.getQuantidade())
                        .build())
                .collect(Collectors.toList());

        return ComboDTO.builder()
                .id(c.getId())
                .nome(c.getNome())
                .descricao(c.getDescricao())
                .valorVenda(c.getValorVenda())
                .valorCusto(c.getValorCusto())
                .itens(itens)
                .build();
    }
}
