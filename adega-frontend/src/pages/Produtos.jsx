import { useState, useEffect } from 'react'
import { api } from '../api'

const formVazio = {
  nome: '', quantidadeAtual: '',
  valorCusto: '', valorVenda: '', categoriaId: '',
  fracionavel: false, unidadeFracionada: '', fatorFracionamento: ''
}

export default function Produtos() {
  const [produtos, setProdutos] = useState([])
  const [categorias, setCategorias] = useState([])
  const [modal, setModal] = useState(false)
  const [form, setForm] = useState(formVazio)
  const [editando, setEditando] = useState(null)
  const [erro, setErro] = useState('')

  useEffect(() => { carregar() }, [])

  async function carregar() {
    const [p, c] = await Promise.all([api.produtos.listar(), api.categorias.listar()])
    setProdutos(p)
    setCategorias(c)
  }

  function abrirNovo() {
    setForm(formVazio)
    setEditando(null)
    setErro('')
    setModal(true)
  }

  function abrirEditar(p) {
    setForm({
      nome: p.nome,
      quantidadeAtual: p.quantidadeAtual, valorCusto: p.valorCusto,
      valorVenda: p.valorVenda, categoriaId: p.categoriaId,
      fracionavel: p.fracionavel,
      unidadeFracionada: p.unidadeFracionada || '',
      fatorFracionamento: p.fatorFracionamento || ''
    })
    setEditando(p.id)
    setErro('')
    setModal(true)
  }

  async function salvar() {
    try {
      const payload = {
        ...form,
        unidadeEstoque: '',
        quantidadeAtual: Number(form.quantidadeAtual),
        valorCusto: Number(form.valorCusto),
        valorVenda: Number(form.valorVenda),
        categoriaId: Number(form.categoriaId),
        fatorFracionamento: form.fracionavel ? Number(form.fatorFracionamento) : null
      }
      if (editando) {
        await api.produtos.atualizar(editando, payload)
      } else {
        await api.produtos.criar(payload)
      }
      setModal(false)
      carregar()
    } catch (e) {
      setErro(e.message)
    }
  }

  async function deletar(id) {
    if (!confirm('Deletar este produto?')) return
    try {
      await api.produtos.deletar(id)
      carregar()
    } catch (e) {
      alert(e.message)
    }
  }

  return (
    <>
      <h2>Produtos</h2>

      <div className="actions">
        <button className="btn-primary" onClick={abrirNovo}>+ Novo Produto</button>
      </div>

      <div className="card">
        <table>
          <thead>
            <tr>
              <th>Nome</th>
              <th>Qtd. Atual</th>
              <th>Custo</th>
              <th>Venda</th>
              <th>Categoria</th>
              <th>Frac.</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            {produtos.map(p => (
              <tr key={p.id}>
                <td>{p.nome}</td>
                <td>{p.quantidadeAtual}</td>
                <td>R$ {Number(p.valorCusto).toFixed(2)}</td>
                <td>R$ {Number(p.valorVenda).toFixed(2)}</td>
                <td>{p.categoriaNome}</td>
                <td>{p.fracionavel ? `✓ (${p.fatorFracionamento} ${p.unidadeFracionada}s)` : '—'}</td>
                <td>
                  <div style={{ display: 'flex', gap: 6 }}>
                    <button className="btn-warning btn-sm" onClick={() => abrirEditar(p)}>Editar</button>
                    <button className="btn-danger btn-sm" onClick={() => deletar(p.id)}>Deletar</button>
                  </div>
                </td>
              </tr>
            ))}
            {produtos.length === 0 && (
              <tr><td colSpan={7} style={{ textAlign: 'center', color: '#888' }}>Nenhum produto cadastrado</td></tr>
            )}
          </tbody>
        </table>
      </div>

      {modal && (
        <div className="modal-overlay">
          <div className="modal">
            <h3>{editando ? 'Editar Produto' : 'Novo Produto'}</h3>
            {erro && <div className="erro">{erro}</div>}

            <label>Nome *</label>
            <input value={form.nome} onChange={e => setForm({ ...form, nome: e.target.value })} />

            <label>Quantidade Inicial *</label>
            <input type="number" min="0" value={form.quantidadeAtual} onChange={e => setForm({ ...form, quantidadeAtual: e.target.value })} />

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

            <label>Categoria *</label>
            <select value={form.categoriaId} onChange={e => setForm({ ...form, categoriaId: e.target.value })}>
              <option value="">Selecione...</option>
              {categorias.map(c => <option key={c.id} value={c.id}>{c.nome}</option>)}
            </select>

            <div className="checkbox-row">
              <input type="checkbox" id="frac" checked={form.fracionavel} onChange={e => setForm({ ...form, fracionavel: e.target.checked })} />
              <label htmlFor="frac" style={{ margin: 0 }}>Produto fracionável? (pode ser vendido em partes)</label>
            </div>

            {form.fracionavel && (
              <div className="form-row">
                <div>
                  <label>Unidade da Fração</label>
                  <input placeholder="dose, cigarro..." value={form.unidadeFracionada} onChange={e => setForm({ ...form, unidadeFracionada: e.target.value })} />
                </div>
                <div>
                  <label>Fator (ex: 1 garrafa = 15 doses)</label>
                  <input type="number" value={form.fatorFracionamento} onChange={e => setForm({ ...form, fatorFracionamento: e.target.value })} />
                </div>
              </div>
            )}

            <div className="modal-footer">
              <button className="btn-primary" onClick={() => setModal(false)}>Cancelar</button>
              <button className="btn-success" onClick={salvar}>Salvar</button>
            </div>
          </div>
        </div>
      )}
    </>
  )
}
