package com.adega.stock.service;

import com.adega.stock.dto.CategoriaDTO;
import com.adega.stock.entity.Categoria;
import com.adega.stock.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository repository;

    public List<CategoriaDTO> listarTodas() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public CategoriaDTO buscarPorId(Long id) {
        return toDTO(findById(id));
    }

    public CategoriaDTO criar(CategoriaDTO dto) {
        if (repository.existsByNome(dto.getNome())) {
            throw new IllegalArgumentException("Ja existe uma categoria com o nome: " + dto.getNome());
        }
        Categoria categoria = Categoria.builder()
                .nome(dto.getNome())
                .descricao(dto.getDescricao())
                .build();
        return toDTO(repository.save(categoria));
    }

    public CategoriaDTO atualizar(Long id, CategoriaDTO dto) {
        Categoria categoria = findById(id);
        categoria.setNome(dto.getNome());
        categoria.setDescricao(dto.getDescricao());
        return toDTO(repository.save(categoria));
    }

    public void deletar(Long id) {
        findById(id);
        repository.deleteById(id);
    }

    public Categoria findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria nao encontrada: " + id));
    }

    private CategoriaDTO toDTO(Categoria c) {
        return CategoriaDTO.builder()
                .id(c.getId())
                .nome(c.getNome())
                .descricao(c.getDescricao())
                .build();
    }
}
