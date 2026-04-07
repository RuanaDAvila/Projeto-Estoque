// Arquivo central para todas as chamadas à API do backend
const BASE = '/api'

async function req(method, path, body) {
  const res = await fetch(BASE + path, {
    method,
    headers: { 'Content-Type': 'application/json' },
    body: body ? JSON.stringify(body) : undefined
  })
  if (!res.ok) {
    const erro = await res.json().catch(() => ({ erro: 'Erro desconhecido' }))
    throw new Error(erro.erro || 'Erro na requisição')
  }
  if (res.status === 204) return null
  return res.json()
}

// Categorias
export const api = {
  categorias: {
    listar: () => req('GET', '/categorias'),
    criar: (data) => req('POST', '/categorias', data),
    atualizar: (id, data) => req('PUT', `/categorias/${id}`, data),
    deletar: (id) => req('DELETE', `/categorias/${id}`)
  },

  produtos: {
    listar: () => req('GET', '/produtos'),
    criar: (data) => req('POST', '/produtos', data),
    atualizar: (id, data) => req('PUT', `/produtos/${id}`, data),
    deletar: (id) => req('DELETE', `/produtos/${id}`)
  },

  combos: {
    listar: () => req('GET', '/combos'),
    buscar: (id) => req('GET', `/combos/${id}`),
    criar: (data) => req('POST', '/combos', data),
    deletar: (id) => req('DELETE', `/combos/${id}`)
  },

  estoque: {
    saldos: () => req('GET', '/estoque/saldo'),
    saldoProduto: (id) => req('GET', `/estoque/saldo/${id}`),
    entrada: (data) => req('POST', '/estoque/entrada', data),
    perda: (data) => req('POST', '/estoque/perda', data)
  },

  vendas: {
    listar: () => req('GET', '/vendas'),
    buscar: (id) => req('GET', `/vendas/${id}`),
    criar: (data) => req('POST', '/vendas', data)
  },

  movimentacoes: {
    listar: (params) => {
      const q = new URLSearchParams()
      if (params?.produtoId) q.set('produtoId', params.produtoId)
      if (params?.tipo) q.set('tipo', params.tipo)
      if (params?.inicio) q.set('inicio', params.inicio)
      if (params?.fim) q.set('fim', params.fim)
      return req('GET', `/movimentacoes?${q}`)
    }
  }
}
