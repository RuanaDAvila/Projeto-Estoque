import { useState, useEffect } from 'react'
import { api } from '../api'
import Numpad from '../components/Numpad'

export default function PDV() {
  const [produtos, setProdutos] = useState([])
  const [categorias, setCategorias] = useState([])
  const [categoriaSelecionada, setCategoriaSelecionada] = useState(null)
  const [busca, setBusca] = useState('')
  const [carrinho, setCarrinho] = useState([])
  const [desconto, setDesconto] = useState('')
  const [tipoDesconto, setTipoDesconto] = useState('R$')
  const [modalPagamento, setModalPagamento] = useState(false)
  const [pagamentos, setPagamentos] = useState([{ metodo: 'Dinheiro', valor: '' }])
  const [numpadMap, setNumpadMap] = useState({})
  const [recebido, setRecebido] = useState('')
  const [sucesso, setSucesso] = useState('')
  const [erro, setErro] = useState('')

  useEffect(() => {
    carregar()
  }, [])

  async function carregar() {
    const [p, c] = await Promise.all([api.produtos.listar(), api.categorias.listar()])
    setProdutos(p)
    setCategorias(c)
  }

  const produtosFiltrados = produtos.filter(p => {
    const nomeOk = p.nome.toLowerCase().includes(busca.toLowerCase())
    const catOk = !categoriaSelecionada || p.categoriaId === categoriaSelecionada
    return nomeOk && catOk
  })

  function adicionarAoCarrinho(produto) {
    setCarrinho(prev => {
      const existe = prev.find(i => i.produtoId === produto.id)
      if (existe) {
        return prev.map(i => i.produtoId === produto.id
          ? { ...i, quantidade: i.quantidade + 1 }
          : i)
      }
      return [...prev, {
        produtoId: produto.id,
        nome: produto.nome,
        valorVenda: Number(produto.valorVenda),
        quantidade: 1,
        fracionado: false,
        tipo: produto.tipo || 'SIMPLES'
      }]
    })
  }

  function alterarQuantidade(produtoId, delta) {
    setCarrinho(prev => prev
      .map(i => i.produtoId === produtoId ? { ...i, quantidade: i.quantidade + delta } : i)
      .filter(i => i.quantidade > 0)
    )
  }

  function removerItem(produtoId) {
    setCarrinho(prev => prev.filter(i => i.produtoId !== produtoId))
  }

  function limparCarrinho() {
    setCarrinho([])
    setDesconto('')
    setPagamentos([{ metodo: 'Dinheiro', valor: '' }])
    setNumpadMap({})
    setRecebido('')
  }

  const subtotal = carrinho.reduce((acc, i) => acc + i.valorVenda * i.quantidade, 0)

  const valorDesconto = (() => {
    if (!desconto) return 0
    const v = Number(desconto)
    if (tipoDesconto === '%') return subtotal * (v / 100)
    return Math.min(v, subtotal)
  })()

  const total = Math.max(0, subtotal - valorDesconto)

  const totalPago = pagamentos.reduce((acc, p) => acc + (Number(p.valor) || 0), 0)
  const troco = Math.max(0, totalPago - total)
  const falta = total > 0 ? Math.max(0, total - totalPago) : 0

  function handleNumpadChange(idx, digits) {
    setNumpadMap(prev => ({ ...prev, [idx]: digits }))
    const valor = (Number(digits || '0') / 100).toFixed(2)
    setPagamentos(prev => {
      const novos = [...prev]
      novos[idx] = { ...novos[idx], valor }
      return novos
    })
  }

  function adicionarFormaPagamento() {
    const newIdx = pagamentos.length
    setPagamentos(prev => [...prev, { metodo: 'Dinheiro', valor: '' }])
    setNumpadMap(prev => ({ ...prev, [newIdx]: '' }))
  }

  function removerFormaPagamento(i) {
    setPagamentos(prev => prev.filter((_, idx) => idx !== i))
  }

  function atualizarPagamento(i, campo, valor) {
    setPagamentos(prev => {
      const novos = [...prev]
      novos[i] = { ...novos[i], [campo]: valor }
      return novos
    })
    if (campo === 'metodo' && valor === 'Dinheiro') {
      setNumpadMap(prev => ({ ...prev, [i]: '' }))
    }
  }

  function abrirPagamento() {
    if (carrinho.length === 0) { setErro('Adicione produtos ao carrinho.'); return }
    setErro('')
    const initialDigits = String(Math.round(total * 100))
    setPagamentos([{ metodo: 'Dinheiro', valor: total.toFixed(2) }])
    setNumpadMap({ 0: initialDigits })
    setRecebido(total.toFixed(2))
    setModalPagamento(true)
  }

  async function finalizarVenda() {
    if (totalPago < total) {
      setErro('Valor pago insuficiente. Faltam R$ ' + (total - totalPago).toFixed(2))
      return
    }
    try {
      const metodosTexto = pagamentos.map(p => `${p.metodo}: R$ ${Number(p.valor).toFixed(2)}`).join(' | ')
      await api.vendas.criar({
        observacao: `PDV | ${metodosTexto}${troco > 0 ? ` | Troco: R$ ${troco.toFixed(2)}` : ''}`,
        desconto: valorDesconto,
        itens: carrinho.map(i => ({
          produtoId: i.produtoId,
          quantidade: i.quantidade,
          fracionado: false
        }))
      })
      setModalPagamento(false)
      limparCarrinho()
      setSucesso('Venda registrada com sucesso!')
      setTimeout(() => setSucesso(''), 4000)
    } catch (e) {
      setErro(e.message)
    }
  }

  return (
    <div style={{ display: 'flex', gap: 16, height: 'calc(100vh - 64px)', overflow: 'hidden' }}>

      {/* PAINEL ESQUERDO — Produtos */}
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>

        {sucesso && <div className="sucesso" style={{ marginBottom: 8 }}>{sucesso}</div>}

        <input
          placeholder="🔍 Buscar produto..."
          value={busca}
          onChange={e => setBusca(e.target.value)}
          style={{ marginBottom: 10, fontSize: 16 }}
        />

        <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginBottom: 12 }}>
          <button
            onClick={() => setCategoriaSelecionada(null)}
            style={{
              padding: '6px 14px', borderRadius: 20, border: 'none', cursor: 'pointer',
              background: !categoriaSelecionada ? '#1a3a6b' : '#dce3f0',
              color: !categoriaSelecionada ? 'white' : '#1a3a6b',
              fontWeight: 600, fontSize: 14
            }}>
            Todos
          </button>
          {categorias.map(c => (
            <button key={c.id}
              onClick={() => setCategoriaSelecionada(categoriaSelecionada === c.id ? null : c.id)}
              style={{
                padding: '6px 14px', borderRadius: 20, border: 'none', cursor: 'pointer',
                background: categoriaSelecionada === c.id ? '#1a3a6b' : '#dce3f0',
                color: categoriaSelecionada === c.id ? 'white' : '#1a3a6b',
                fontWeight: 600, fontSize: 14
              }}>
              {c.nome}
            </button>
          ))}
        </div>

        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fill, minmax(150px, 1fr))',
          gap: 10,
          overflowY: 'auto',
          flex: 1,
          paddingRight: 4
        }}>
          {produtosFiltrados.map(p => (
            <button key={p.id} onClick={() => adicionarAoCarrinho(p)}
              style={{
                background: 'white', border: '2px solid #d0d8e8',
                borderRadius: 10, padding: '14px 10px', cursor: 'pointer',
                textAlign: 'center', transition: 'all 0.15s',
                display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 6
              }}
              onMouseEnter={e => { e.currentTarget.style.borderColor = '#1a3a6b'; e.currentTarget.style.background = '#f0f4ff' }}
              onMouseLeave={e => { e.currentTarget.style.borderColor = '#d0d8e8'; e.currentTarget.style.background = 'white' }}
            >
              <span style={{ fontSize: 28 }}>
                {(p.tipo === 'COMPOSTO') ? '📦' : '🍺'}
              </span>
              <span style={{ fontWeight: 600, fontSize: 14, color: '#1a1a2e', lineHeight: 1.2 }}>{p.nome}</span>
              <span style={{ color: '#1e7e34', fontWeight: 700, fontSize: 15 }}>
                R$ {Number(p.valorVenda).toFixed(2)}
              </span>
              {(p.tipo !== 'COMPOSTO') && p.quantidadeAtual <= 0 && (
                <span style={{ fontSize: 11, color: '#c0392b', fontWeight: 600 }}>SEM ESTOQUE</span>
              )}
            </button>
          ))}
          {produtosFiltrados.length === 0 && (
            <div style={{ gridColumn: '1/-1', textAlign: 'center', color: '#888', padding: 32 }}>
              Nenhum produto encontrado
            </div>
          )}
        </div>
      </div>

      {/* PAINEL DIREITO — Carrinho */}
      <div style={{
        width: 340, background: 'white', borderRadius: 12, border: '2px solid #d0d8e8',
        display: 'flex', flexDirection: 'column', overflow: 'hidden'
      }}>

        <div style={{ background: '#1a3a6b', color: 'white', padding: '14px 16px', fontWeight: 700, fontSize: 17 }}>
          🛒 Carrinho
          {carrinho.length > 0 && (
            <button onClick={limparCarrinho} style={{
              float: 'right', background: 'transparent', color: '#ffaaaa',
              border: 'none', cursor: 'pointer', fontSize: 13, fontWeight: 600
            }}>✕ Limpar</button>
          )}
        </div>

        <div style={{ flex: 1, overflowY: 'auto', padding: 12 }}>
          {carrinho.length === 0 && (
            <div style={{ textAlign: 'center', color: '#aaa', padding: 32, fontSize: 15 }}>
              Clique em um produto para adicionar
            </div>
          )}
          {carrinho.map(item => (
            <div key={item.produtoId} style={{
              borderBottom: '1px solid #eee', paddingBottom: 10, marginBottom: 10
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <span style={{ fontWeight: 600, fontSize: 14, flex: 1 }}>{item.nome}</span>
                <button onClick={() => removerItem(item.produtoId)} style={{
                  background: 'none', border: 'none', color: '#c0392b', cursor: 'pointer', fontSize: 16, padding: 0
                }}>✕</button>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: 6 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  <button onClick={() => alterarQuantidade(item.produtoId, -1)} style={{
                    width: 28, height: 28, borderRadius: 6, border: '2px solid #1a3a6b',
                    background: 'white', color: '#1a3a6b', fontWeight: 700, cursor: 'pointer', fontSize: 16
                  }}>−</button>
                  <span style={{ fontWeight: 700, fontSize: 16, minWidth: 24, textAlign: 'center' }}>{item.quantidade}</span>
                  <button onClick={() => alterarQuantidade(item.produtoId, 1)} style={{
                    width: 28, height: 28, borderRadius: 6, border: '2px solid #1a3a6b',
                    background: '#1a3a6b', color: 'white', fontWeight: 700, cursor: 'pointer', fontSize: 16
                  }}>+</button>
                </div>
                <span style={{ fontWeight: 700, color: '#1e7e34', fontSize: 15 }}>
                  R$ {(item.valorVenda * item.quantidade).toFixed(2)}
                </span>
              </div>
              <div style={{ color: '#888', fontSize: 12, marginTop: 2 }}>
                R$ {item.valorVenda.toFixed(2)} cada
              </div>
            </div>
          ))}
        </div>

        <div style={{ borderTop: '2px solid #eee', padding: 12 }}>

          <div style={{ display: 'flex', gap: 6, alignItems: 'center', marginBottom: 10 }}>
            <select value={tipoDesconto} onChange={e => setTipoDesconto(e.target.value)}
              style={{ width: 60, marginBottom: 0, padding: '6px 4px', fontSize: 14 }}>
              <option>R$</option>
              <option>%</option>
            </select>
            <input
              type="number" placeholder="Desconto"
              value={desconto} onChange={e => setDesconto(e.target.value)}
              style={{ flex: 1, marginBottom: 0, fontSize: 14 }}
            />
          </div>

          <div style={{ fontSize: 14, color: '#555', marginBottom: 4 }}>
            Subtotal: <strong>R$ {subtotal.toFixed(2)}</strong>
          </div>
          {valorDesconto > 0 && (
            <div style={{ fontSize: 14, color: '#c0392b', marginBottom: 4 }}>
              Desconto: <strong>− R$ {valorDesconto.toFixed(2)}</strong>
            </div>
          )}
          <div style={{ fontSize: 20, fontWeight: 700, color: '#1a3a6b', marginBottom: 12 }}>
            Total: R$ {total.toFixed(2)}
          </div>

          {erro && !modalPagamento && <div className="erro">{erro}</div>}

          <button className="btn-success" onClick={abrirPagamento}
            style={{ width: '100%', fontSize: 16, padding: 12 }}>
            💳 Finalizar Venda
          </button>
        </div>
      </div>

      {/* MODAL DE PAGAMENTO */}
      {modalPagamento && (
        <div className="modal-overlay">
          <div className="modal" style={{ width: 460, maxHeight: '90vh', overflowY: 'auto' }}>
            <h3>💳 Pagamento</h3>

            <div style={{ background: '#f0f4ff', borderRadius: 8, padding: 12, marginBottom: 16 }}>
              <div style={{ fontSize: 14, color: '#555' }}>Total a pagar</div>
              <div style={{ fontSize: 28, fontWeight: 700, color: '#1a3a6b' }}>R$ {total.toFixed(2)}</div>
            </div>

            {erro && <div className="erro">{erro}</div>}

            <label>Formas de Pagamento</label>
            {pagamentos.map((pag, i) => (
              <div key={i} style={{ marginBottom: 10 }}>
                <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                  <select value={pag.metodo} onChange={e => atualizarPagamento(i, 'metodo', e.target.value)}
                    style={{ flex: 1, marginBottom: 0 }}>
                    <option>Dinheiro</option>
                    <option>Cartão Débito</option>
                    <option>Cartão Crédito</option>
                    <option>Pix</option>
                  </select>
                  {pag.metodo !== 'Dinheiro' && (
                    <input type="number" step="0.01" placeholder="R$ 0,00"
                      value={pag.valor} onChange={e => atualizarPagamento(i, 'valor', e.target.value)}
                      style={{ flex: 1, marginBottom: 0 }} />
                  )}
                  {pagamentos.length > 1 && (
                    <button onClick={() => removerFormaPagamento(i)}
                      style={{ background: 'none', border: 'none', color: '#c0392b', cursor: 'pointer', fontSize: 18 }}>✕</button>
                  )}
                </div>
                {pag.metodo === 'Dinheiro' && (
                  <Numpad
                    digits={numpadMap[i] ?? ''}
                    onChange={digits => handleNumpadChange(i, digits)}
                  />
                )}
              </div>
            ))}

            <button onClick={adicionarFormaPagamento} style={{
              background: 'none', border: '2px dashed #b0bdd6', borderRadius: 6,
              width: '100%', padding: 8, cursor: 'pointer', color: '#1a3a6b',
              fontWeight: 600, fontSize: 14, marginBottom: 12
            }}>+ Dividir pagamento</button>

            <div style={{ background: '#f8f9ff', borderRadius: 8, padding: 12, marginBottom: 16 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4, fontSize: 15 }}>
                <span style={{ color: '#555' }}>Total da venda:</span>
                <strong>R$ {total.toFixed(2)}</strong>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4, fontSize: 15 }}>
                <span style={{ color: '#555' }}>Total pago:</span>
                <strong>R$ {totalPago.toFixed(2)}</strong>
              </div>
              {troco > 0 && (
                <div style={{ display: 'flex', justifyContent: 'space-between', color: '#1e7e34', fontWeight: 700, fontSize: 17 }}>
                  <span>Troco:</span>
                  <strong>R$ {troco.toFixed(2)}</strong>
                </div>
              )}
              {falta > 0 && totalPago > 0 && (
                <div style={{ display: 'flex', justifyContent: 'space-between', color: '#c0392b', fontWeight: 600 }}>
                  <span>Falta:</span>
                  <strong>R$ {falta.toFixed(2)}</strong>
                </div>
              )}
            </div>

            <div className="modal-footer">
              <button className="btn-primary" onClick={() => { setModalPagamento(false); setErro('') }}>Cancelar</button>
              <button className="btn-success" onClick={finalizarVenda}
                style={{ fontSize: 15, padding: '10px 24px' }}>
                ✓ Confirmar Venda
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
