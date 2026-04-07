import { useState, useEffect } from 'react'
import { api } from '../api'

const formVazio = {
  nome: '', quantidadeAtual: '',
  valorCusto: '', valorVenda: '', categoriaId: '',
  tipo: 'SIMPLES',
  fracionavel: false, unidadeFracionada: '', fatorFracionamento: '',
  componentes: []
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
      quantidadeAtual: p.quantidadeAtual,
      valorCusto: p.valorCusto,
      valorVenda: p.valorVenda,
      categoriaId: p.categoriaId,
      tipo: p.tipo || 'SIMPLES',
      fracionavel: p.fracionavel,
      unidadeFracionada: p.unidadeFracionada || '',
      fatorFracionamento: p.fatorFracionamento || '',
      componentes: (p.componentes || []).map(c => ({
        produtoFilhoId: c.produtoFilhoId,
        quantidade: c.quantidade
      }))
    })
    setEditando(p.id)
    setErro('')
    setModal(true)
  }

  function addComponente() {
    setForm({ ...form, componentes: [...form.componentes, { produtoFilhoId: '', quantidade: '' }] })
  }

  function removeComponente(i) {
    setForm({ ...form, componentes: form.componentes.filter((_, idx) => idx !== i) })
  }

  function atualizarComponente(i, campo, valor) {
    const novos = [...form.componentes]
    novos[i][campo] = valor
    setForm({ ...form, componentes: novos })
  }

  async function salvar() {
    try {
      const isComposto = form.tipo === 'COMPOSTO'
      const payload = {
        ...form,
        unidadeEstoque: '',
        quantidadeAtual: isComposto ? 0 : Number(form.quantidadeAtual),
        valorCusto: Number(form.valorCusto),
        valorVenda: Number(form.valorVenda),
        categoriaId: Number(form.categoriaId),
        fatorFracionamento: form.fracionavel ? Number(form.fatorFracionamento) : null,
        componentes: isComposto
          ? form.componentes.map(c => ({
              produtoFilhoId: Number(c.produtoFilhoId),
              quantidade: Number(c.quantidade)
            }))
          : []
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

  // Apenas produtos simples podem ser componentes (inclui os sem tipo definido ainda)
  const produtosSimples = produtos.filter(p => !p.tipo || p.tipo === 'SIMPLES')

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
              <th>Tipo</th>
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
                <td>
                  <span style={{
                    background: p.tipo === 'COMPOSTO' ? '#d0e8ff' : '#d4edda',
                    color: p.tipo === 'COMPOSTO' ? '#003d82' : '#155724',
                    padding: '2px 10px', borderRadius: 10, fontSize: 13, fontWeight: 600
                  }}>
                    {p.tipo === 'COMPOSTO' ? 'Kit/Pack' : 'Simples'}
                  </span>
                </td>
                <td>{p.tipo === 'COMPOSTO' ? '—' : p.quantidadeAtual}</td>
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
              <tr><td colSpan={8} style={{ textAlign: 'center', color: '#888' }}>Nenhum produto cadastrado</td></tr>
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

            <label>Tipo *</label>
            <select value={form.tipo} onChange={e => setForm({
              ...form,
              tipo: e.target.value,
              componentes: e.target.value === 'COMPOSTO' ? [{ produtoFilhoId: '', quantidade: '' }] : []
            })}>
              <option value="SIMPLES">Simples (tem estoque próprio)</option>
              <option value="COMPOSTO">Kit / Pack (composto por outros produtos)</option>
            </select>

            <label>Categoria *</label>
            <select value={form.categoriaId} onChange={e => setForm({ ...form, categoriaId: e.target.value })}>
              <option value="">Selecione...</option>
              {categorias.map(c => <option key={c.id} value={c.id}>{c.nome}</option>)}
            </select>

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

            {form.tipo === 'SIMPLES' && (
              <>
                <label>Quantidade Inicial</label>
                <input type="number" min="0" value={form.quantidadeAtual} onChange={e => setForm({ ...form, quantidadeAtual: e.target.value })} />

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
              </>
            )}

            {form.tipo === 'COMPOSTO' && (
              <>
                <label style={{ marginTop: 8 }}>Componentes do Kit/Pack *</label>
                <small style={{ color: '#555', display: 'block', marginBottom: 10 }}>
                  Ao vender este kit, o estoque de cada componente será descontado automaticamente.
                </small>
                {form.componentes.map((comp, i) => (
                  <div key={i} style={{ display: 'flex', gap: 8, marginBottom: 8, alignItems: 'center' }}>
                    <select
                      style={{ flex: 2, marginBottom: 0 }}
                      value={comp.produtoFilhoId}
                      onChange={e => atualizarComponente(i, 'produtoFilhoId', e.target.value)}
                    >
                      <option value="">Produto...</option>
                      {produtosSimples.map(p => <option key={p.id} value={p.id}>{p.nome}</option>)}
                    </select>
                    <input
                      type="number"
                      placeholder="Qtd"
                      style={{ flex: 1, marginBottom: 0 }}
                      value={comp.quantidade}
                      onChange={e => atualizarComponente(i, 'quantidade', e.target.value)}
                    />
                    <button className="btn-danger btn-sm" onClick={() => removeComponente(i)}>✕</button>
                  </div>
                ))}
                <button className="btn-warning btn-sm" onClick={addComponente} style={{ marginBottom: 12 }}>
                  + Adicionar componente
                </button>
              </>
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
