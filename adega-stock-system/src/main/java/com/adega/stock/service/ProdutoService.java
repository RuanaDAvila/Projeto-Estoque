package com.adega.stock.service;

import com.adega.stock.dto.ProdutoComponenteDTO;
import com.adega.stock.dto.ProdutoDTO;
import com.adega.stock.entity.*;
import com.adega.stock.repository.MovimentacaoEstoqueRepository;
import com.adega.stock.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository repository;
    private final CategoriaService categoriaService;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;

    public List<ProdutoDTO> listarTodos() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ProdutoDTO buscarPorId(Long id) {
        return toDTO(findById(id));
    }

    public List<ProdutoDTO> buscarPorCategoria(Long categoriaId) {
        return repository.findByCategoriaId(categoriaId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProdutoDTO criar(ProdutoDTO dto) {
        validarFracionamento(dto);
        Categoria categoria = categoriaService.findById(dto.getCategoriaId());

        boolean isComposto = dto.getTipo() == TipoProduto.COMPOSTO;

        // Produto composto nunca tem estoque próprio
        BigDecimal quantidadeInicial = (!isComposto && dto.getQuantidadeAtual() != null)
                ? dto.getQuantidadeAtual() : BigDecimal.ZERO;
        dto.setQuantidadeAtual(BigDecimal.ZERO);

        Produto produto = toProduto(dto, categoria);
        produto = repository.save(produto);

        // Adiciona componentes se for composto
        if (isComposto && dto.getComponentes() != null) {
            for (ProdutoComponenteDTO c : dto.getComponentes()) {
                Produto filho = findById(c.getProdutoFilhoId());
                produto.getComponentes().add(ProdutoComponente.builder()
                        .produtoPai(produto)
                        .produtoFilho(filho)
                        .quantidade(c.getQuantidade())
                        .build());
            }
            produto = repository.save(produto);
        }

        // Gera ENTRADA automática para produto simples com quantidade inicial
        if (!isComposto && quantidadeInicial.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal custo = dto.getValorCusto() != null ? dto.getValorCusto() : BigDecimal.ZERO;
            movimentacaoRepository.save(MovimentacaoEstoque.builder()
                    .produto(produto)
                    .quantidade(quantidadeInicial)
                    .tipo(TipoMovimentacao.ENTRADA)
                    .valorUnitarioCusto(custo)
                    .valorTotal(custo.multiply(quantidadeInicial))
                    .motivo("Estoque inicial")
                    .build());
            produto.setQuantidadeAtual(quantidadeInicial);
            produto = repository.save(produto);
        }

        return toDTO(produto);
    }

    @Transactional
    public ProdutoDTO atualizar(Long id, ProdutoDTO dto) {
        validarFracionamento(dto);
        Produto produto = findById(id);
        Categoria categoria = categoriaService.findById(dto.getCategoriaId());

        produto.setNome(dto.getNome());
        produto.setUnidadeEstoque(dto.getUnidadeEstoque());
        produto.setValorCusto(dto.getValorCusto());
        produto.setValorVenda(dto.getValorVenda());
        produto.setCategoria(categoria);
        produto.setFracionavel(dto.isFracionavel());
        produto.setUnidadeFracionada(dto.getUnidadeFracionada());
        produto.setFatorFracionamento(dto.getFatorFracionamento());
        produto.setTipo(dto.getTipo() != null ? dto.getTipo() : TipoProduto.SIMPLES);

        // Atualiza componentes
        produto.getComponentes().clear();
        if (produto.getTipo() == TipoProduto.COMPOSTO && dto.getComponentes() != null) {
            for (ProdutoComponenteDTO c : dto.getComponentes()) {
                Produto filho = findById(c.getProdutoFilhoId());
                produto.getComponentes().add(ProdutoComponente.builder()
                        .produtoPai(produto)
                        .produtoFilho(filho)
                        .quantidade(c.getQuantidade())
                        .build());
            }
            produto.setQuantidadeAtual(BigDecimal.ZERO);
        } else if (dto.getQuantidadeAtual() != null) {
            produto.setQuantidadeAtual(dto.getQuantidadeAtual());
        }

        return toDTO(repository.save(produto));
    }

    private void validarFracionamento(ProdutoDTO dto) {
        if (!dto.isFracionavel()) return;
        boolean semUnidade = dto.getUnidadeFracionada() == null || dto.getUnidadeFracionada().isBlank();
        boolean semFator = dto.getFatorFracionamento() == null
                || dto.getFatorFracionamento().compareTo(BigDecimal.ONE) < 0;
        if (semUnidade || semFator) {
            throw new IllegalArgumentException(
                "Produto marcado como fracionável. Favor indicar a unidade fracionada e o fator de fracionamento (mínimo 1), ou desmarcar essa opção para continuar."
            );
        }
    }

    public void deletar(Long id) {
        findById(id);
        repository.deleteById(id);
    }

    public Produto findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto nao encontrado: " + id));
    }

    private Produto toProduto(ProdutoDTO dto, Categoria categoria) {
        return Produto.builder()
                .nome(dto.getNome())
                .unidadeEstoque(dto.getUnidadeEstoque())
                .quantidadeAtual(BigDecimal.ZERO)
                .valorCusto(dto.getValorCusto() != null ? dto.getValorCusto() : BigDecimal.ZERO)
                .valorVenda(dto.getValorVenda() != null ? dto.getValorVenda() : BigDecimal.ZERO)
                .categoria(categoria)
                .fracionavel(dto.isFracionavel())
                .unidadeFracionada(dto.getUnidadeFracionada())
                .fatorFracionamento(dto.getFatorFracionamento())
                .tipo(dto.getTipo() != null ? dto.getTipo() : TipoProduto.SIMPLES)
                .build();
    }

    public ProdutoDTO toDTO(Produto p) {
        List<ProdutoComponenteDTO> componentes = p.getComponentes().stream()
                .map(c -> ProdutoComponenteDTO.builder()
                        .produtoFilhoId(c.getProdutoFilho().getId())
                        .produtoFilhoNome(c.getProdutoFilho().getNome())
                        .quantidade(c.getQuantidade())
                        .build())
                .collect(Collectors.toList());

        return ProdutoDTO.builder()
                .id(p.getId())
                .nome(p.getNome())
                .unidadeEstoque(p.getUnidadeEstoque())
                .quantidadeAtual(p.getQuantidadeAtual())
                .valorCusto(p.getValorCusto())
                .valorVenda(p.getValorVenda())
                .categoriaId(p.getCategoria().getId())
                .categoriaNome(p.getCategoria().getNome())
                .fracionavel(p.isFracionavel())
                .unidadeFracionada(p.getUnidadeFracionada())
                .fatorFracionamento(p.getFatorFracionamento())
                .tipo(p.getTipo())
                .componentes(componentes)
                .build();
    }
}
