import { useState, useEffect } from 'react'
import { api } from '../api'

export default function Movimentacoes() {
  const [movs, setMovs] = useState([])
  const [produtos, setProdutos] = useState([])

  function dataHojeInicio() {
    const d = new Date(); d.setHours(0, 0, 0, 0)
    return d.toISOString().slice(0, 16)
  }
  function dataHojeFim() {
    const d = new Date(); d.setHours(23, 59, 0, 0)
    return d.toISOString().slice(0, 16)
  }

  const [filtros, setFiltros] = useState({ produtoId: '', tipo: '', inicio: dataHojeInicio(), fim: dataHojeFim() })

  useEffect(() => {
    api.produtos.listar().then(setProdutos)
    buscar()
  }, [])

  async function buscar() {
    const params = {}
    if (filtros.produtoId) params.produtoId = filtros.produtoId
    if (filtros.tipo) params.tipo = filtros.tipo
    if (filtros.inicio) params.inicio = filtros.inicio + ':00'
    if (filtros.fim) params.fim = filtros.fim + ':00'
    const data = await api.movimentacoes.listar(params)
    setMovs(data)
  }

  function badgeTipo(tipo) {
    const classes = { ENTRADA: 'badge-entrada', VENDA: 'badge-venda', PERDA: 'badge-perda' }
    return <span className={`badge ${classes[tipo]}`}>{tipo}</span>
  }

  return (
    <>
      <h2>Histórico de Movimentações</h2>

      <div className="card">
        <div className="form-row-3">
          <div>
            <label>Produto</label>
            <select value={filtros.produtoId} onChange={e => setFiltros({ ...filtros, produtoId: e.target.value })}>
              <option value="">Todos</option>
              {produtos.map(p => <option key={p.id} value={p.id}>{p.nome}</option>)}
            </select>
          </div>
          <div>
            <label>Tipo</label>
            <select value={filtros.tipo} onChange={e => setFiltros({ ...filtros, tipo: e.target.value })}>
              <option value="">Todos</option>
              <option value="ENTRADA">Entrada</option>
              <option value="VENDA">Venda</option>
              <option value="PERDA">Perda</option>
            </select>
          </div>
          <div style={{ display: 'flex', alignItems: 'flex-end' }}>
            <button className="btn-primary" onClick={buscar} style={{ width: '100%', marginBottom: 12 }}>Filtrar</button>
          </div>
        </div>
        <div className="form-row">
          <div>
            <label>Data início</label>
            <input type="datetime-local" value={filtros.inicio} onChange={e => setFiltros({ ...filtros, inicio: e.target.value })} />
          </div>
          <div>
            <label>Data fim</label>
            <input type="datetime-local" value={filtros.fim} onChange={e => setFiltros({ ...filtros, fim: e.target.value })} />
          </div>
        </div>
      </div>

      <div className="card">
        <table>
          <thead>
            <tr>
              <th>Data/Hora</th>
              <th>Tipo</th>
              <th>Produto/Combo</th>
              <th>Qtd</th>
              <th>Unit. Custo</th>
              <th>Unit. Venda</th>
              <th>Total</th>
              <th>Fracionado</th>
              <th>Motivo</th>
            </tr>
          </thead>
          <tbody>
            {movs.map(m => (
              <tr key={m.id}>
                <td>{new Date(m.dataHora).toLocaleString('pt-BR')}</td>
                <td>{badgeTipo(m.tipo)}</td>
                <td>{m.produtoNome || m.comboNome || '—'}</td>
                <td>{m.quantidade}</td>
                <td>R$ {Number(m.valorUnitarioCusto).toFixed(2)}</td>
                <td>{m.valorUnitarioVenda ? `R$ ${Number(m.valorUnitarioVenda).toFixed(2)}` : '—'}</td>
                <td>R$ {Number(m.valorTotal).toFixed(2)}</td>
                <td>{m.fracionado ? `Sim (÷${m.fatorFracionamento})` : 'Não'}</td>
                <td>{m.motivo || '—'}</td>
              </tr>
            ))}
            {movs.length === 0 && (
              <tr><td colSpan={9} style={{ textAlign: 'center', color: '#888' }}>Nenhuma movimentação encontrada</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </>
  )
}
