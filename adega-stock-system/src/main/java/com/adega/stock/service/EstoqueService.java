package com.adega.stock.service;

import com.adega.stock.dto.EntradaEstoqueDTO;
import com.adega.stock.dto.MovimentacaoDTO;
import com.adega.stock.dto.PerdaEstoqueDTO;
import com.adega.stock.dto.SaldoEstoqueDTO;
import com.adega.stock.entity.MovimentacaoEstoque;
import com.adega.stock.entity.Produto;
import com.adega.stock.entity.TipoMovimentacao;
import com.adega.stock.repository.MovimentacaoEstoqueRepository;
import com.adega.stock.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EstoqueService {

    private final ProdutoRepository produtoRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;
    private final ProdutoService produtoService;

    @Transactional
    public MovimentacaoDTO registrarEntrada(EntradaEstoqueDTO dto) {
        Produto produto = produtoService.findById(dto.getProdutoId());

        produto.setQuantidadeAtual(produto.getQuantidadeAtual().add(dto.getQuantidade()));
        produtoRepository.save(produto);

        BigDecimal valorTotal = dto.getValorUnitarioCusto().multiply(dto.getQuantidade());

        MovimentacaoEstoque mov = MovimentacaoEstoque.builder()
                .produto(produto)
                .quantidade(dto.getQuantidade())
                .tipo(TipoMovimentacao.ENTRADA)
                .valorUnitarioCusto(dto.getValorUnitarioCusto())
                .valorTotal(valorTotal)
                .motivo(dto.getObservacao())
                .build();

        return toDTO(movimentacaoRepository.save(mov));
    }

    @Transactional
    public MovimentacaoDTO registrarPerda(PerdaEstoqueDTO dto) {
        Produto produto = produtoService.findById(dto.getProdutoId());

        if (produto.getQuantidadeAtual().compareTo(dto.getQuantidade()) < 0) {
            throw new IllegalArgumentException("Estoque insuficiente. Disponivel: " + produto.getQuantidadeAtual());
        }

        produto.setQuantidadeAtual(produto.getQuantidadeAtual().subtract(dto.getQuantidade()));
        produtoRepository.save(produto);

        BigDecimal valorTotal = produto.getValorCusto().multiply(dto.getQuantidade());

        MovimentacaoEstoque mov = MovimentacaoEstoque.builder()
                .produto(produto)
                .quantidade(dto.getQuantidade())
                .tipo(TipoMovimentacao.PERDA)
                .valorUnitarioCusto(produto.getValorCusto())
                .valorTotal(valorTotal)
                .motivo(dto.getMotivo())
                .build();

        return toDTO(movimentacaoRepository.save(mov));
    }

    public List<SaldoEstoqueDTO> listarSaldos() {
        return produtoRepository.findAll().stream()
                .filter(p -> p.getTipo() == com.adega.stock.entity.TipoProduto.SIMPLES)
                .map(this::toSaldoDTO)
                .collect(Collectors.toList());
    }

    public SaldoEstoqueDTO saldoPorProduto(Long produtoId) {
        return toSaldoDTO(produtoService.findById(produtoId));
    }

    private SaldoEstoqueDTO toSaldoDTO(Produto p) {
        BigDecimal quantidadeEmFracoes = null;

        // Se o produto é fracionável, calcula quantas frações tem disponível
        // Ex: 5 garrafas × 15 doses/garrafa = 75 doses disponíveis
        if (p.isFracionavel() && p.getFatorFracionamento() != null) {
            quantidadeEmFracoes = p.getQuantidadeAtual().multiply(p.getFatorFracionamento());
        }

        return SaldoEstoqueDTO.builder()
                .produtoId(p.getId())
                .produtoNome(p.getNome())
                .unidadeEstoque(p.getUnidadeEstoque())
                .quantidadeAtual(p.getQuantidadeAtual())
                .valorCusto(p.getValorCusto())
                .valorVenda(p.getValorVenda())
                .categoriaNome(p.getCategoria().getNome())
                .fracionavel(p.isFracionavel())
                .unidadeFracionada(p.getUnidadeFracionada())
                .fatorFracionamento(p.getFatorFracionamento())
                .quantidadeEmFracoes(quantidadeEmFracoes)
                .build();
    }

    public MovimentacaoDTO toDTO(MovimentacaoEstoque m) {
        return MovimentacaoDTO.builder()
                .id(m.getId())
                .produtoId(m.getProduto() != null ? m.getProduto().getId() : null)
                .produtoNome(m.getProduto() != null ? m.getProduto().getNome() : null)
                .comboId(m.getCombo() != null ? m.getCombo().getId() : null)
                .comboNome(m.getCombo() != null ? m.getCombo().getNome() : null)
                .quantidade(m.getQuantidade())
                .tipo(m.getTipo())
                .valorUnitarioCusto(m.getValorUnitarioCusto())
                .valorUnitarioVenda(m.getValorUnitarioVenda())
                .valorTotal(m.getValorTotal())
                .dataHora(m.getDataHora())
                .motivo(m.getMotivo())
                .vendaId(m.getVenda() != null ? m.getVenda().getId() : null)
                .fracionado(m.isFracionado())
                .fatorFracionamento(m.getFatorFracionamento())
                .build();
    }
}
