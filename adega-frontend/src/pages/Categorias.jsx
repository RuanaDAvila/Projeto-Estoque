import { useState, useEffect } from 'react'
import { api } from '../api'

export default function Categorias() {
  const [categorias, setCategorias] = useState([])
  const [modal, setModal] = useState(false)
  const [form, setForm] = useState({ nome: '', descricao: '' })
  const [editando, setEditando] = useState(null)
  const [erro, setErro] = useState('')

  useEffect(() => { carregar() }, [])

  async function carregar() {
    const data = await api.categorias.listar()
    setCategorias(data)
  }

  function abrirNovo() {
    setForm({ nome: '', descricao: '' })
    setEditando(null)
    setErro('')
    setModal(true)
  }

  function abrirEditar(cat) {
    setForm({ nome: cat.nome, descricao: cat.descricao || '' })
    setEditando(cat.id)
    setErro('')
    setModal(true)
  }

  async function salvar() {
    try {
      if (editando) {
        await api.categorias.atualizar(editando, form)
      } else {
        await api.categorias.criar(form)
      }
      setModal(false)
      carregar()
    } catch (e) {
      setErro(e.message)
    }
  }

  async function deletar(id) {
    if (!confirm('Deletar esta categoria?')) return
    try {
      await api.categorias.deletar(id)
      carregar()
    } catch (e) {
      alert(e.message)
    }
  }

  return (
    <>
      <h2>Categorias</h2>

      <div className="actions">
        <button className="btn-primary" onClick={abrirNovo}>+ Nova Categoria</button>
      </div>

      <div className="card">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Nome</th>
              <th>Descrição</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            {categorias.map(c => (
              <tr key={c.id}>
                <td>{c.id}</td>
                <td>{c.nome}</td>
                <td>{c.descricao || '—'}</td>
                <td>
                  <div style={{ display: 'flex', gap: 6 }}>
                    <button className="btn-warning btn-sm" onClick={() => abrirEditar(c)}>Editar</button>
                    <button className="btn-danger btn-sm" onClick={() => deletar(c.id)}>Deletar</button>
                  </div>
                </td>
              </tr>
            ))}
            {categorias.length === 0 && (
              <tr><td colSpan={4} style={{ textAlign: 'center', color: '#888' }}>Nenhuma categoria cadastrada</td></tr>
            )}
          </tbody>
        </table>
      </div>

      {modal && (
        <div className="modal-overlay">
          <div className="modal">
            <h3>{editando ? 'Editar Categoria' : 'Nova Categoria'}</h3>
            {erro && <div className="erro">{erro}</div>}
            <label>Nome *</label>
            <input value={form.nome} onChange={e => setForm({ ...form, nome: e.target.value })} />
            <label>Descrição</label>
            <input value={form.descricao} onChange={e => setForm({ ...form, descricao: e.target.value })} />
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
