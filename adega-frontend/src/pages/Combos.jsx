import { useState, useEffect } from 'react'
import { api } from '../api'

export default function Combos() {
  const [combos, setCombos] = useState([])
  const [produtos, setProdutos] = useState([])
  const [modal, setModal] = useState(false)
  const [detalhe, setDetalhe] = useState(null)
  const [form, setForm] = useState({ nome: '', descricao: '', valorVenda: '', valorCusto: '' })
  const [itens, setItens] = useState([{ produtoId: '', quantidade: '' }])
  const [erro, setErro] = useState('')

  useEffect(() => { carregar() }, [])

  async function carregar() {
    const [c, p] = await Promise.all([api.combos.listar(), api.produtos.listar()])
    setCombos(c)
    setProdutos(p)
  }

  function abrirNovo() {
    setForm({ nome: '', descricao: '', valorVenda: '', valorCusto: '' })
    setItens([{ produtoId: '', quantidade: '' }])
    setErro('')
    setModal(true)
  }

  function addItem() {
    setItens([...itens, { produtoId: '', quantidade: '' }])
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
      await api.combos.criar({
        ...form,
        valorVenda: Number(form.valorVenda),
        valorCusto: Number(form.valorCusto),
        itens: itens.map(i => ({ produtoId: Number(i.produtoId), quantidade: Number(i.quantidade) }))
      })
      setModal(false)
      carregar()
    } catch (e) {
      setErro(e.message)
    }
  }

  async function deletar(id) {
    if (!confirm('Deletar este combo?')) return
    try {
      await api.combos.deletar(id)
      carregar()
    } catch (e) {
      alert(e.message)
    }
  }

  async function verDetalhe(id) {
    const data = await api.combos.buscar(id)
    setDetalhe(data)
  }

  return (
    <>
      <h2>Combos</h2>

      <div className="actions">
        <button className="btn-primary" onClick={abrirNovo}>+ Novo Combo</button>
      </div>

      <div className="card">
        <table>
          <thead>
            <tr>
              <th>Nome</th>
              <th>Descrição</th>
              <th>Custo</th>
              <th>Venda</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            {combos.map(c => (
              <tr key={c.id}>
                <td>{c.nome}</td>
                <td>{c.descricao || '—'}</td>
                <td>R$ {Number(c.valorCusto).toFixed(2)}</td>
                <td>R$ {Number(c.valorVenda).toFixed(2)}</td>
                <td>
                  <div style={{ display: 'flex', gap: 6 }}>
                    <button className="btn-warning btn-sm" onClick={() => verDetalhe(c.id)}>Ver Itens</button>
                    <button className="btn-danger btn-sm" onClick={() => deletar(c.id)}>Deletar</button>
                  </div>
                </td>
              </tr>
            ))}
            {combos.length === 0 && (
              <tr><td colSpan={5} style={{ textAlign: 'center', color: '#888' }}>Nenhum combo cadastrado</td></tr>
            )}
          </tbody>
        </table>
      </div>

      {modal && (
        <div className="modal-overlay">
          <div className="modal">
            <h3>Novo Combo</h3>
            {erro && <div className="erro">{erro}</div>}

            <label>Nome *</label>
            <input value={form.nome} onChange={e => setForm({ ...form, nome: e.target.value })} />

            <label>Descrição</label>
            <input value={form.descricao} onChange={e => setForm({ ...form, descricao: e.target.value })} />

            <div className="form-row">
              <div>
                <label>Valor de Custo (R$) *</label>
                <input type="number" step="0.01" value={form.valorCusto} onChange={e => setForm({ ...form, valorCusto: e.target.value })} />
              </div>
              <div>
                <label>Valor de Venda (R$) *</label>
                <input type="number" step="0.01" value={form.valorVenda} onChange={e => setForm({ ...form, valorVenda: e.target.value })} />
              </div>
            </div>

            <label>Itens do Combo</label>
            {itens.map((item, i) => (
              <div key={i} className="form-row" style={{ alignItems: 'center', marginBottom: 8 }}>
                <select value={item.produtoId} onChange={e => atualizarItem(i, 'produtoId', e.target.value)}>
                  <option value="">Produto...</option>
                  {produtos.map(p => <option key={p.id} value={p.id}>{p.nome}</option>)}
                </select>
                <input type="number" placeholder="Qtd" value={item.quantidade} onChange={e => atualizarItem(i, 'quantidade', e.target.value)} />
                {itens.length > 1 && (
                  <button className="btn-danger btn-sm" onClick={() => removeItem(i)} style={{ whiteSpace: 'nowrap' }}>✕</button>
                )}
              </div>
            ))}
            <button className="btn-warning btn-sm" onClick={addItem} style={{ marginBottom: 12 }}>+ Adicionar item</button>

            <div className="modal-footer">
              <button className="btn-primary" onClick={() => setModal(false)}>Cancelar</button>
              <button className="btn-success" onClick={salvar}>Salvar</button>
            </div>
          </div>
        </div>
      )}

      {detalhe && (
        <div className="modal-overlay">
          <div className="modal">
            <h3>Itens do Combo: {detalhe.nome}</h3>
            <table>
              <thead>
                <tr><th>Produto</th><th>Quantidade</th></tr>
              </thead>
              <tbody>
                {detalhe.itens.map(i => (
                  <tr key={i.id}>
                    <td>{i.produtoNome}</td>
                    <td>{i.quantidade}</td>
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
