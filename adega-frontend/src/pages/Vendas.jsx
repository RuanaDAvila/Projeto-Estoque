import { useState, useEffect } from 'react'
import { api } from '../api'

export default function Vendas() {
  const [vendas, setVendas] = useState([])
  const [produtos, setProdutos] = useState([])
  const [combos, setCombos] = useState([])
  const [modal, setModal] = useState(false)
  const [detalhe, setDetalhe] = useState(null)
  const [observacao, setObservacao] = useState('')
  const [itens, setItens] = useState([{ tipo: 'produto', produtoId: '', comboId: '', quantidade: '', fracionado: false }])
  const [erro, setErro] = useState('')

  useEffect(() => { carregar() }, [])

  async function carregar() {
    const [v, p, c] = await Promise.all([api.vendas.listar(), api.produtos.listar(), api.combos.listar()])
    setVendas(v)
    setProdutos(p)
    setCombos(c)
  }

  function addItem() {
    setItens([...itens, { tipo: 'produto', produtoId: '', comboId: '', quantidade: '', fracionado: false }])
  }

  function removeItem(i) {
    setItens(itens.filter((_, idx) => idx !== i))
  }

  function atualizarItem(i, campo, valor) {
    const novos = [...itens]
    novos[i][campo] = valor
    setItens(novos)
  }

  async function salvar() {
    try {
      const payload = {
        observacao,
        itens: itens.map(i => ({
          produtoId: i.tipo === 'produto' ? Number(i.produtoId) : undefined,
          comboId: i.tipo === 'combo' ? Number(i.comboId) : undefined,
          quantidade: Number(i.quantidade),
          fracionado: i.fracionado
        }))
      }
      await api.vendas.criar(payload)
      setModal(false)
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
        <button className="btn-success" onClick={() => { setItens([{ tipo: 'produto', produtoId: '', comboId: '', quantidade: '', fracionado: false }]); setObservacao(''); setErro(''); setModal(true) }}>
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
              <th>Observação</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            {vendas.map(v => (
              <tr key={v.id}>
                <td>{v.id}</td>
                <td>{new Date(v.dataHora).toLocaleString('pt-BR')}</td>
                <td>R$ {Number(v.valorTotal).toFixed(2)}</td>
                <td>{v.observacao || '—'}</td>
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

      {modal && (
        <div className="modal-overlay">
          <div className="modal">
            <h3>Nova Venda</h3>
            {erro && <div className="erro">{erro}</div>}

            <label>Observação (ex: Mesa 5)</label>
            <input value={observacao} onChange={e => setObservacao(e.target.value)} />

            <label>Itens da Venda</label>
            {itens.map((item, i) => (
              <div key={i} style={{ background: '#f9f9f9', padding: 12, borderRadius: 6, marginBottom: 10 }}>
                <div className="form-row" style={{ marginBottom: 8 }}>
                  <div>
                    <label>Tipo</label>
                    <select value={item.tipo} onChange={e => atualizarItem(i, 'tipo', e.target.value)}>
                      <option value="produto">Produto</option>
                      <option value="combo">Combo</option>
                    </select>
                  </div>
                  <div>
                    <label>Quantidade</label>
                    <input type="number" value={item.quantidade} onChange={e => atualizarItem(i, 'quantidade', e.target.value)} />
                  </div>
                </div>

                {item.tipo === 'produto' ? (
                  <>
                    <label>Produto</label>
                    <select value={item.produtoId} onChange={e => atualizarItem(i, 'produtoId', e.target.value)}>
                      <option value="">Selecione...</option>
                      {produtos.map(p => <option key={p.id} value={p.id}>{p.nome} {p.fracionavel ? `(fracionável: ${p.fatorFracionamento} ${p.unidadeFracionada}s)` : ''}</option>)}
                    </select>
                    <div className="checkbox-row">
                      <input type="checkbox" id={`frac-${i}`} checked={item.fracionado} onChange={e => atualizarItem(i, 'fracionado', e.target.checked)} />
                      <label htmlFor={`frac-${i}`} style={{ margin: 0 }}>Vender como fração? (ex: dose)</label>
                    </div>
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
                  <button className="btn-danger btn-sm" onClick={() => removeItem(i)}>Remover item</button>
                )}
              </div>
            ))}

            <button className="btn-warning btn-sm" onClick={addItem} style={{ marginBottom: 16 }}>+ Adicionar item</button>

            <div className="modal-footer">
              <button className="btn-primary" onClick={() => setModal(false)}>Cancelar</button>
              <button className="btn-success" onClick={salvar}>Finalizar Venda</button>
            </div>
          </div>
        </div>
      )}

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
