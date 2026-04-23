import { useState, useEffect } from 'react'
import { api } from '../api'
import Numpad from '../components/Numpad'

function itemVazio() {
  return { tipo: 'produto', produtoId: '', comboId: '', quantidade: 1, fracionado: false, tipoFracao: '', qtdFracoes: '' }
}

export default function Vendas() {
  const [vendas, setVendas] = useState([])
  const [produtos, setProdutos] = useState([])
  const [combos, setCombos] = useState([])
  const [modal, setModal] = useState(false)
  const [detalhe, setDetalhe] = useState(null)
  const [itens, setItens] = useState([itemVazio()])
  const [erro, setErro] = useState('')

  // Modal de pagamento
  const [modalPagamento, setModalPagamento] = useState(false)
  const [pagamentos, setPagamentos] = useState([{ metodo: 'Dinheiro', valor: '' }])
  const [numpadMap, setNumpadMap] = useState({})

  useEffect(() => { carregar() }, [])

  async function carregar() {
    const [v, p, c] = await Promise.all([api.vendas.listar(), api.produtos.listar(), api.combos.listar()])
    setVendas(v)
    setProdutos(p)
    setCombos(c)
  }

  function addItem() {
    setItens(prev => [...prev, itemVazio()])
  }

  function removeItem(i) {
    setItens(prev => prev.filter((_, idx) => idx !== i))
  }

  function atualizarItem(i, campo, valor) {
    setItens(prev => {
      const novos = [...prev]
      novos[i] = { ...novos[i], [campo]: valor }
      return novos
    })
  }

  // Calcula total estimado com base nos preços dos produtos/combos
  const totalVenda = itens.reduce((acc, item) => {
    if (item.tipo === 'produto') {
      const prod = produtos.find(p => p.id === Number(item.produtoId))
      if (!prod) return acc
      if (item.fracionado && prod.fracionavel && prod.fatorFracionamento) {
        const precoPorFracao = Number(prod.valorVenda) / Number(prod.fatorFracionamento)
        return acc + precoPorFracao * Number(item.qtdFracoes || 0)
      }
      return acc + Number(prod.valorVenda) * Number(item.quantidade || 0)
    } else {
      const combo = combos.find(c => c.id === Number(item.comboId))
      if (!combo) return acc
      const preco = Number(combo.preco || combo.valorVenda || combo.valorTotal || 0)
      return acc + preco * Number(item.quantidade || 0)
    }
  }, 0)

  const totalPago = pagamentos.reduce((acc, p) => acc + (Number(p.valor) || 0), 0)
  const troco = Math.max(0, totalPago - totalVenda)
  const falta = totalVenda > 0 ? Math.max(0, totalVenda - totalPago) : 0

  function handleNumpad(idx, digits) {
    setNumpadMap(prev => ({ ...prev, [idx]: digits }))
    const valor = (Number(digits || '0') / 100).toFixed(2)
    setPagamentos(prev => {
      const novos = [...prev]
      novos[idx] = { ...novos[idx], valor }
      return novos
    })
  }

  function abrirPagamento() {
    for (const item of itens) {
      if (item.tipo === 'produto' && !item.produtoId) {
        setErro('Selecione um produto para todos os itens.')
        return
      }
      if (item.tipo === 'combo' && !item.comboId) {
        setErro('Selecione um combo para todos os itens.')
        return
      }
      if (item.tipo === 'produto' && item.fracionado) {
        if (!item.qtdFracoes || Number(item.qtdFracoes) < 1) {
          setErro('Informe a quantidade de frações para itens fracionados.')
          return
        }
      } else if (!item.fracionado && (!item.quantidade || Number(item.quantidade) < 1)) {
        setErro('Informe a quantidade para todos os itens.')
        return
      }
    }
    setErro('')
    const initialDigits = String(Math.round(totalVenda * 100))
    setPagamentos([{ metodo: 'Dinheiro', valor: totalVenda > 0 ? totalVenda.toFixed(2) : '' }])
    setNumpadMap({ 0: totalVenda > 0 ? initialDigits : '' })
    setModalPagamento(true)
  }

  async function confirmarVenda() {
    if (totalVenda > 0 && totalPago < totalVenda) {
      setErro('Valor pago insuficiente. Faltam R$ ' + falta.toFixed(2))
      return
    }
    try {
      const metodosTexto = pagamentos
        .map(p => `${p.metodo}: R$ ${Number(p.valor).toFixed(2)}`)
        .join(' | ')
      const payload = {
        observacao: `${metodosTexto}${troco > 0 ? ` | Troco: R$ ${troco.toFixed(2)}` : ''}`,
        itens: itens.map(item => ({
          produtoId: item.tipo === 'produto' ? Number(item.produtoId) : undefined,
          comboId: item.tipo === 'combo' ? Number(item.comboId) : undefined,
          quantidade: (item.fracionado && item.tipo === 'produto')
            ? Number(item.qtdFracoes)
            : Number(item.quantidade),
          fracionado: item.fracionado
        }))
      }
      await api.vendas.criar(payload)
      setModalPagamento(false)
      setModal(false)
      setItens([itemVazio()])
      carregar()
    } catch (e) {
      setErro(e.message)
    }
  }

  async function verDetalhe(id) {
    const data = await api.vendas.buscar(id)
    setDetalhe(data)
  }

  return (
    <>
      <h2>Vendas</h2>

      <div className="actions">
        <button className="btn-success" onClick={() => {
          setItens([itemVazio()])
          setErro('')
          setModal(true)
        }}>
          + Nova Venda
        </button>
      </div>

      <div className="card">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Data/Hora</th>
              <th>Valor Total</th>
              <th>Pagamento</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            {vendas.map(v => (
              <tr key={v.id}>
                <td>{v.id}</td>
                <td>{new Date(v.dataHora).toLocaleString('pt-BR')}</td>
                <td>R$ {Number(v.valorTotal).toFixed(2)}</td>
                <td style={{ fontSize: 12, color: '#555' }}>{v.observacao || '—'}</td>
                <td>
                  <button className="btn-warning btn-sm" onClick={() => verDetalhe(v.id)}>Ver Itens</button>
                </td>
              </tr>
            ))}
            {vendas.length === 0 && (
              <tr><td colSpan={5} style={{ textAlign: 'center', color: '#888' }}>Nenhuma venda registrada</td></tr>
            )}
          </tbody>
        </table>
      </div>

      {/* ── MODAL: NOVA VENDA ── */}
      {modal && (
        <div className="modal-overlay">
          <div className="modal" style={{ maxHeight: '90vh', overflowY: 'auto' }}>
            <h3>Nova Venda</h3>
            {erro && <div className="erro">{erro}</div>}

            <label>Itens da Venda</label>
            {itens.map((item, i) => {
              const prod = produtos.find(p => p.id === Number(item.produtoId))
              const eFracionavel = prod?.fracionavel === true
              const precoPorFracao = (eFracionavel && prod?.fatorFracionamento)
                ? Number(prod.valorVenda) / Number(prod.fatorFracionamento)
                : 0
              const totalFracao = precoPorFracao * Number(item.qtdFracoes || 0)

              return (
                <div key={i} style={{ background: '#f9f9f9', padding: 12, borderRadius: 6, marginBottom: 10 }}>
                  <div className="form-row" style={{ marginBottom: 8 }}>
                    <div>
                      <label>Tipo</label>
                      <select value={item.tipo} onChange={e => {
                        setItens(prev => {
                          const novos = [...prev]
                          novos[i] = { ...itemVazio(), tipo: e.target.value }
                          return novos
                        })
                      }}>
                        <option value="produto">Produto</option>
                        <option value="combo">Combo</option>
                      </select>
                    </div>
                    {!item.fracionado && (
                      <div>
                        <label>Quantidade</label>
                        <input type="number" min="1" value={item.quantidade}
                          onChange={e => atualizarItem(i, 'quantidade', e.target.value)} />
                      </div>
                    )}
                  </div>

                  {item.tipo === 'produto' ? (
                    <>
                      <label>Produto</label>
                      <select value={item.produtoId} onChange={e => {
                        setItens(prev => {
                          const novos = [...prev]
                          novos[i] = { ...novos[i], produtoId: e.target.value, fracionado: false, tipoFracao: '', qtdFracoes: '' }
                          return novos
                        })
                      }}>
                        <option value="">Selecione...</option>
                        {produtos.map(p => (
                          <option key={p.id} value={p.id}>
                            {p.nome}{p.fracionavel ? ` (${p.fatorFracionamento} ${p.unidadeFracionada}s)` : ''}
                          </option>
                        ))}
                      </select>

                      {item.produtoId && (
                        <div style={{ marginTop: 8 }}>
                          <div className="checkbox-row" style={{ flexWrap: 'wrap', gap: 4 }}>
                            <input
                              type="checkbox"
                              id={`frac-${i}`}
                              checked={item.fracionado}
                              disabled={!eFracionavel}
                              onChange={e => {
                                setItens(prev => {
                                  const novos = [...prev]
                                  novos[i] = {
                                    ...novos[i],
                                    fracionado: e.target.checked,
                                    tipoFracao: e.target.checked ? (prod?.unidadeFracionada || 'dose') : '',
                                    qtdFracoes: ''
                                  }
                                  return novos
                                })
                              }}
                            />
                            <label htmlFor={`frac-${i}`}
                              style={{ margin: 0, opacity: eFracionavel ? 1 : 0.5, cursor: eFracionavel ? 'pointer' : 'not-allowed' }}>
                              Vender como fração? (ex: dose)
                            </label>
                          </div>
                          {!eFracionavel && (
                            <div style={{ color: '#c0392b', fontSize: 12, fontWeight: 600, marginTop: 4, marginLeft: 22 }}>
                              Produto não é fracionável
                            </div>
                          )}
                        </div>
                      )}

                      {item.fracionado && eFracionavel && (
                        <div style={{ background: '#fff3cd', border: '1px solid #ffc107', borderRadius: 6, padding: 10, marginTop: 8 }}>
                          <div className="form-row" style={{ marginBottom: 8 }}>
                            <div>
                              <label>Tipo de fração</label>
                              <select
                                value={item.tipoFracao || prod?.unidadeFracionada || 'dose'}
                                onChange={e => atualizarItem(i, 'tipoFracao', e.target.value)}
                              >
                                {prod?.unidadeFracionada && (
                                  <option value={prod.unidadeFracionada}>{prod.unidadeFracionada}</option>
                                )}
                                {['dose', '1/2', '1/3', 'ml', 'copo', 'taça']
                                  .filter(t => t !== prod?.unidadeFracionada)
                                  .map(t => <option key={t} value={t}>{t}</option>)
                                }
                              </select>
                            </div>
                            <div>
                              <label>Quantidade de frações</label>
                              <input type="number" min="1" placeholder="Ex: 3"
                                value={item.qtdFracoes}
                                onChange={e => atualizarItem(i, 'qtdFracoes', e.target.value)} />
                            </div>
                          </div>
                          <div style={{ fontSize: 13, color: '#666', marginBottom: 4 }}>
                            Preço por {item.tipoFracao || prod?.unidadeFracionada}:{' '}
                            <strong>R$ {precoPorFracao.toFixed(2)}</strong>
                            <span style={{ color: '#999' }}>
                              {' '}({prod?.fatorFracionamento} frações = R$ {Number(prod?.valorVenda).toFixed(2)})
                            </span>
                          </div>
                          {Number(item.qtdFracoes) > 0 && (
                            <div style={{ fontSize: 15, fontWeight: 700, color: '#1a3a6b' }}>
                              Total: R$ {totalFracao.toFixed(2)}
                            </div>
                          )}
                        </div>
                      )}
                    </>
                  ) : (
                    <>
                      <label>Combo</label>
                      <select value={item.comboId} onChange={e => atualizarItem(i, 'comboId', e.target.value)}>
                        <option value="">Selecione...</option>
                        {combos.map(c => <option key={c.id} value={c.id}>{c.nome}</option>)}
                      </select>
                    </>
                  )}

                  {itens.length > 1 && (
                    <button className="btn-danger btn-sm" onClick={() => removeItem(i)} style={{ marginTop: 8 }}>
                      Remover item
                    </button>
                  )}
                </div>
              )
            })}

            <button className="btn-warning btn-sm" onClick={addItem} style={{ marginBottom: 12 }}>
              + Adicionar item
            </button>

            {totalVenda > 0 && (
              <div style={{ background: '#f0f4ff', borderRadius: 8, padding: 10, marginBottom: 12, textAlign: 'right' }}>
                <span style={{ fontSize: 17, fontWeight: 700, color: '#1a3a6b' }}>
                  Total estimado: R$ {totalVenda.toFixed(2)}
                </span>
              </div>
            )}

            <div className="modal-footer">
              <button className="btn-primary" onClick={() => { setModal(false); setErro('') }}>Cancelar</button>
              <button className="btn-success" onClick={abrirPagamento}>Finalizar Venda</button>
            </div>
          </div>
        </div>
      )}

      {/* ── MODAL: PAGAMENTO ── */}
      {modalPagamento && (
        <div className="modal-overlay">
          <div className="modal" style={{ width: 460, maxHeight: '90vh', overflowY: 'auto' }}>
            <h3>💳 Pagamento</h3>

            <div style={{ background: '#f0f4ff', borderRadius: 8, padding: 12, marginBottom: 16 }}>
              <div style={{ fontSize: 14, color: '#555' }}>Total a pagar</div>
              <div style={{ fontSize: 28, fontWeight: 700, color: '#1a3a6b' }}>R$ {totalVenda.toFixed(2)}</div>
            </div>

            {erro && <div className="erro">{erro}</div>}

            <label>Formas de Pagamento</label>
            {pagamentos.map((pag, i) => (
              <div key={i} style={{ marginBottom: 10 }}>
                <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                  <select value={pag.metodo} onChange={e => {
                    setPagamentos(prev => {
                      const novos = [...prev]
                      novos[i] = { ...novos[i], metodo: e.target.value }
                      return novos
                    })
                    if (e.target.value === 'Dinheiro') {
                      setNumpadMap(prev => ({ ...prev, [i]: '' }))
                    }
                  }} style={{ flex: 1, marginBottom: 0 }}>
                    <option>Dinheiro</option>
                    <option>Cartão Débito</option>
                    <option>Cartão Crédito</option>
                    <option>Pix</option>
                  </select>
                  {pag.metodo !== 'Dinheiro' && (
                    <input type="number" step="0.01" placeholder="R$ 0,00"
                      value={pag.valor}
                      onChange={e => setPagamentos(prev => {
                        const novos = [...prev]
                        novos[i] = { ...novos[i], valor: e.target.value }
                        return novos
                      })}
                      style={{ flex: 1, marginBottom: 0 }}
                    />
                  )}
                  {pagamentos.length > 1 && (
                    <button onClick={() => setPagamentos(prev => prev.filter((_, idx) => idx !== i))}
                      style={{ background: 'none', border: 'none', color: '#c0392b', cursor: 'pointer', fontSize: 18 }}>✕</button>
                  )}
                </div>
                {pag.metodo === 'Dinheiro' && (
                  <Numpad digits={numpadMap[i] ?? ''} onChange={digits => handleNumpad(i, digits)} />
                )}
              </div>
            ))}

            <button onClick={() => {
              const newIdx = pagamentos.length
              setPagamentos(prev => [...prev, { metodo: 'Dinheiro', valor: '' }])
              setNumpadMap(prev => ({ ...prev, [newIdx]: '' }))
            }} style={{
              background: 'none', border: '2px dashed #b0bdd6', borderRadius: 6,
              width: '100%', padding: 8, cursor: 'pointer', color: '#1a3a6b',
              fontWeight: 600, fontSize: 14, marginBottom: 12
            }}>+ Dividir pagamento</button>

            <div style={{ background: '#f8f9ff', borderRadius: 8, padding: 12, marginBottom: 16 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4, fontSize: 15 }}>
                <span style={{ color: '#555' }}>Total da venda:</span>
                <strong>R$ {totalVenda.toFixed(2)}</strong>
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
              <button className="btn-success" onClick={confirmarVenda}
                style={{ fontSize: 15, padding: '10px 24px' }}>
                ✓ Confirmar Venda
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── MODAL: DETALHE DA VENDA ── */}
      {detalhe && (
        <div className="modal-overlay">
          <div className="modal">
            <h3>Venda #{detalhe.id}</h3>
            <p style={{ marginBottom: 12, color: '#888', fontSize: 13 }}>
              {new Date(detalhe.dataHora).toLocaleString('pt-BR')} — Total: R$ {Number(detalhe.valorTotal).toFixed(2)}
            </p>
            <table>
              <thead>
                <tr><th>Item</th><th>Qtd</th><th>Unit. Venda</th><th>Fracionado</th></tr>
              </thead>
              <tbody>
                {detalhe.itens.map(i => (
                  <tr key={i.id}>
                    <td>{i.produtoNome || i.comboNome}</td>
                    <td>{i.quantidade}</td>
                    <td>R$ {Number(i.valorUnitarioVenda).toFixed(2)}</td>
                    <td>{i.fracionado ? `Sim (1/${i.fatorFracionamento})` : 'Não'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
            <div className="modal-footer">
              <button className="btn-primary" onClick={() => setDetalhe(null)}>Fechar</button>
            </div>
          </div>
        </div>
      )}
    </>
  )
}
