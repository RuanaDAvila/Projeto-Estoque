package com.adega.stock.service;

import com.adega.stock.dto.VendaDTO;
import com.adega.stock.dto.VendaItemDTO;
import com.adega.stock.entity.*;
import com.adega.stock.repository.MovimentacaoEstoqueRepository;
import com.adega.stock.repository.ProdutoRepository;
import com.adega.stock.repository.VendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VendaService {

    private final VendaRepository vendaRepository;
    private final ProdutoRepository produtoRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;
    private final ProdutoService produtoService;
    private final ComboService comboService;

    @Transactional
    public VendaDTO registrarVenda(VendaDTO dto) {
        Venda venda = Venda.builder()
                .observacao(dto.getObservacao())
                .valorTotal(BigDecimal.ZERO)
                .build();

        BigDecimal valorTotal = BigDecimal.ZERO;
        List<VendaItem> itens = new ArrayList<>();

        for (VendaItemDTO itemDTO : dto.getItens()) {
            if (itemDTO.getComboId() != null) {
                VendaItem item = processarVendaCombo(itemDTO, venda);
                itens.add(item);
                valorTotal = valorTotal.add(item.getValorUnitarioVenda().multiply(item.getQuantidade()));
            } else if (itemDTO.getProdutoId() != null) {
                VendaItem item = processarVendaProduto(itemDTO, venda);
                itens.add(item);
                valorTotal = valorTotal.add(item.getValorUnitarioVenda().multiply(item.getQuantidade()));
            } else {
                throw new IllegalArgumentException("Cada item deve ter produtoId ou comboId");
            }
        }

        venda.setValorTotal(valorTotal);
        venda.setItens(itens);
        Venda vendaSalva = vendaRepository.save(venda);

        for (VendaItem item : vendaSalva.getItens()) {
            registrarMovimentacaoVenda(item, vendaSalva);
        }

        return toDTO(vendaSalva);
    }

    private VendaItem processarVendaProduto(VendaItemDTO itemDTO, Venda venda) {
        Produto produto = produtoService.findById(itemDTO.getProdutoId());
        // fracionado = true significa que o cliente está comprando uma fração do produto
        // Ex: comprar 3 doses de uma garrafa, em vez de comprar a garrafa inteira
        boolean fracionado = itemDTO.isFracionado();

        if (fracionado) {
            if (!produto.isFracionavel() || produto.getFatorFracionamento() == null) {
                throw new IllegalArgumentException("Produto nao e fracionavel: " + produto.getNome());
            }

            // fator = quantas frações tem 1 unidade (ex: 15 doses por garrafa)
            BigDecimal fator = produto.getFatorFracionamento();

            // Calcula quanto baixar do estoque em unidades inteiras
            // Ex: vender 3 doses de garrafa de 15 → baixa 3/15 = 0.2 garrafa
            BigDecimal quantidadeBaixar = itemDTO.getQuantidade()
                    .divide(fator, 10, RoundingMode.HALF_UP);

            if (produto.getQuantidadeAtual().compareTo(quantidadeBaixar) < 0) {
                throw new IllegalArgumentException("Estoque insuficiente para " + produto.getNome());
            }

            produto.setQuantidadeAtual(produto.getQuantidadeAtual().subtract(quantidadeBaixar));

            // Preço da fração = preço da unidade ÷ fator
            // Ex: garrafa R$150 ÷ 15 doses = R$10 por dose
            BigDecimal valorUnitarioFracao = produto.getValorVenda()
                    .divide(fator, 2, RoundingMode.HALF_UP);
            BigDecimal valorUnitarioCustoFracao = produto.getValorCusto()
                    .divide(fator, 4, RoundingMode.HALF_UP);

            produtoRepository.save(produto);

            return VendaItem.builder()
                    .venda(venda).produto(produto)
                    .quantidade(itemDTO.getQuantidade())
                    .valorUnitarioVenda(valorUnitarioFracao)
                    .valorUnitarioCusto(valorUnitarioCustoFracao)
                    .fracionado(true)
                    .fatorFracionamento(fator)
                    .build();

        } else {
            if (produto.getQuantidadeAtual().compareTo(itemDTO.getQuantidade()) < 0) {
                throw new IllegalArgumentException("Estoque insuficiente para " + produto.getNome());
            }
            produto.setQuantidadeAtual(produto.getQuantidadeAtual().subtract(itemDTO.getQuantidade()));
            produtoRepository.save(produto);

            return VendaItem.builder()
                    .venda(venda).produto(produto)
                    .quantidade(itemDTO.getQuantidade())
                    .valorUnitarioVenda(produto.getValorVenda())
                    .valorUnitarioCusto(produto.getValorCusto())
                    .fracionado(false)
                    .build();
        }
    }

    private VendaItem processarVendaCombo(VendaItemDTO itemDTO, Venda venda) {
        Combo combo = comboService.findById(itemDTO.getComboId());

        for (ComboItem comboItem : combo.getItens()) {
            Produto produto = comboItem.getProduto();
            BigDecimal quantidadeBaixar = comboItem.getQuantidade().multiply(itemDTO.getQuantidade());

            if (produto.getQuantidadeAtual().compareTo(quantidadeBaixar) < 0) {
                throw new IllegalArgumentException(
                        "Estoque insuficiente para o produto " + produto.getNome() + " no combo " + combo.getNome());
            }
            produto.setQuantidadeAtual(produto.getQuantidadeAtual().subtract(quantidadeBaixar));
            produtoRepository.save(produto);
        }

        return VendaItem.builder()
                .venda(venda).combo(combo)
                .quantidade(itemDTO.getQuantidade())
                .valorUnitarioVenda(combo.getValorVenda())
                .valorUnitarioCusto(combo.getValorCusto())
                .fracionado(false)
                .build();
    }

    private void registrarMovimentacaoVenda(VendaItem item, Venda venda) {
        BigDecimal valorTotal = item.getValorUnitarioVenda().multiply(item.getQuantidade());

        MovimentacaoEstoque mov = MovimentacaoEstoque.builder()
                .produto(item.getProduto())
                .combo(item.getCombo())
                .quantidade(item.getQuantidade())
                .tipo(TipoMovimentacao.VENDA)
                .valorUnitarioCusto(item.getValorUnitarioCusto())
                .valorUnitarioVenda(item.getValorUnitarioVenda())
                .valorTotal(valorTotal)
                .venda(venda)
                .fracionado(item.isFracionado())
                .fatorFracionamento(item.getFatorFracionamento())
                .build();

        movimentacaoRepository.save(mov);
    }

    public List<VendaDTO> listarTodas() {
        return vendaRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public VendaDTO buscarPorId(Long id) {
        Venda venda = vendaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venda nao encontrada: " + id));
        return toDTO(venda);
    }

    private VendaDTO toDTO(Venda v) {
        List<VendaItemDTO> itens = v.getItens().stream()
                .map(i -> VendaItemDTO.builder()
                        .id(i.getId())
                        .produtoId(i.getProduto() != null ? i.getProduto().getId() : null)
                        .produtoNome(i.getProduto() != null ? i.getProduto().getNome() : null)
                        .comboId(i.getCombo() != null ? i.getCombo().getId() : null)
                        .comboNome(i.getCombo() != null ? i.getCombo().getNome() : null)
                        .quantidade(i.getQuantidade())
                        .valorUnitarioVenda(i.getValorUnitarioVenda())
                        .valorUnitarioCusto(i.getValorUnitarioCusto())
                        .fracionado(i.isFracionado())
                        .fatorFracionamento(i.getFatorFracionamento())
                        .build())
                .collect(Collectors.toList());

        return VendaDTO.builder()
                .id(v.getId())
                .dataHora(v.getDataHora())
                .valorTotal(v.getValorTotal())
                .observacao(v.getObservacao())
                .itens(itens)
                .build();
    }
}
