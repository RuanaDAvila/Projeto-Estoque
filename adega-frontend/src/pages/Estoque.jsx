import { useState, useEffect } from 'react'
import { api } from '../api'

export default function Estoque() {
  const [saldos, setSaldos] = useState([])
  const [modalEntrada, setModalEntrada] = useState(false)
  const [modalPerda, setModalPerda] = useState(false)
  const [formEntrada, setFormEntrada] = useState({ produtoId: '', quantidade: '', valorUnitarioCusto: '', observacao: '' })
  const [formPerda, setFormPerda] = useState({ produtoId: '', quantidade: '', motivo: '' })
  const [erro, setErro] = useState('')
  const [sucesso, setSucesso] = useState('')

  useEffect(() => { carregar() }, [])

  async function carregar() {
    const data = await api.estoque.saldos()
    setSaldos(data)
  }

  async function salvarEntrada() {
    try {
      await api.estoque.entrada({
        produtoId: Number(formEntrada.produtoId),
        quantidade: Number(formEntrada.quantidade),
        valorUnitarioCusto: Number(formEntrada.valorUnitarioCusto),
        observacao: formEntrada.observacao
      })
      setModalEntrada(false)
      setSucesso('Entrada registrada!')
      setTimeout(() => setSucesso(''), 3000)
      carregar()
    } catch (e) {
      setErro(e.message)
    }
  }

  async function salvarPerda() {
    try {
      await api.estoque.perda({
        produtoId: Number(formPerda.produtoId),
        quantidade: Number(formPerda.quantidade),
        motivo: formPerda.motivo
      })
      setModalPerda(false)
      setSucesso('Perda registrada!')
      setTimeout(() => setSucesso(''), 3000)
      carregar()
    } catch (e) {
      setErro(e.message)
    }
  }

  return (
    <>
      <h2>Estoque</h2>

      {sucesso && <div className="sucesso">{sucesso}</div>}

      <div className="actions">
        <button className="btn-success" onClick={() => { setFormEntrada({ produtoId: '', quantidade: '', valorUnitarioCusto: '', observacao: '' }); setErro(''); setModalEntrada(true) }}>
          + Entrada
        </button>
        <button className="btn-danger" onClick={() => { setFormPerda({ produtoId: '', quantidade: '', motivo: '' }); setErro(''); setModalPerda(true) }}>
          Registrar Perda
        </button>
      </div>

      <div className="card">
        <table>
          <thead>
            <tr>
              <th>Produto</th>
              <th>Categoria</th>
              <th>Quantidade</th>
              <th>Frações disponíveis</th>
              <th>Custo</th>
              <th>Venda</th>
            </tr>
          </thead>
          <tbody>
            {saldos.map(s => (
              <tr key={s.produtoId}>
                <td>{s.produtoNome}</td>
                <td>{s.categoriaNome}</td>
                <td style={{ fontWeight: 'bold', color: s.quantidadeAtual <= 0 ? '#e74c3c' : 'inherit' }}>
                  {s.quantidadeAtual}
                </td>
                <td>
                  {s.fracionavel
                    ? `${s.quantidadeEmFracoes} ${s.unidadeFracionada}s`
                    : '—'}
                </td>
                <td>R$ {Number(s.valorCusto).toFixed(2)}</td>
                <td>R$ {Number(s.valorVenda).toFixed(2)}</td>
              </tr>
            ))}
            {saldos.length === 0 && (
              <tr><td colSpan={6} style={{ textAlign: 'center', color: '#888' }}>Nenhum produto no estoque</td></tr>
            )}
          </tbody>
        </table>
      </div>

      {modalEntrada && (
        <div className="modal-overlay">
          <div className="modal">
            <h3>Registrar Entrada</h3>
            {erro && <div className="erro">{erro}</div>}
            <label>Produto *</label>
            <select value={formEntrada.produtoId} onChange={e => setFormEntrada({ ...formEntrada, produtoId: e.target.value })}>
              <option value="">Selecione...</option>
              {saldos.map(s => <option key={s.produtoId} value={s.produtoId}>{s.produtoNome}</option>)}
            </select>
            <div className="form-row">
              <div>
                <label>Quantidade *</label>
                <input type="number" value={formEntrada.quantidade} onChange={e => setFormEntrada({ ...formEntrada, quantidade: e.target.value })} />
              </div>
              <div>
                <label>Valor Unitário de Custo (R$) *</label>
                <input type="number" step="0.01" value={formEntrada.valorUnitarioCusto} onChange={e => setFormEntrada({ ...formEntrada, valorUnitarioCusto: e.target.value })} />
              </div>
            </div>
            <label>Observação</label>
            <input value={formEntrada.observacao} onChange={e => setFormEntrada({ ...formEntrada, observacao: e.target.value })} />
            <div className="modal-footer">
              <button className="btn-primary" onClick={() => setModalEntrada(false)}>Cancelar</button>
              <button className="btn-success" onClick={salvarEntrada}>Confirmar</button>
            </div>
          </div>
        </div>
      )}

      {modalPerda && (
        <div className="modal-overlay">
          <div className="modal">
            <h3>Registrar Perda</h3>
            {erro && <div className="erro">{erro}</div>}
            <label>Produto *</label>
            <select value={formPerda.produtoId} onChange={e => setFormPerda({ ...formPerda, produtoId: e.target.value })}>
              <option value="">Selecione...</option>
              {saldos.map(s => <option key={s.produtoId} value={s.produtoId}>{s.produtoNome}</option>)}
            </select>
            <label>Quantidade *</label>
            <input type="number" value={formPerda.quantidade} onChange={e => setFormPerda({ ...formPerda, quantidade: e.target.value })} />
            <label>Motivo * (obrigatório)</label>
            <input placeholder="Ex: Garrafa quebrada, produto vencido..." value={formPerda.motivo} onChange={e => setFormPerda({ ...formPerda, motivo: e.target.value })} />
            <div className="modal-footer">
              <button className="btn-primary" onClick={() => setModalPerda(false)}>Cancelar</button>
              <button className="btn-danger" onClick={salvarPerda}>Confirmar Perda</button>
            </div>
          </div>
        </div>
      )}
    </>
  )
}
