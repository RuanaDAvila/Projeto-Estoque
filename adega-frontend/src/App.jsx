import { Routes, Route, Navigate } from 'react-router-dom'
import Navbar from './components/Navbar'
import Categorias from './pages/Categorias'
import Produtos from './pages/Produtos'
import Estoque from './pages/Estoque'
import Vendas from './pages/Vendas'
import Movimentacoes from './pages/Movimentacoes'
import PDV from './pages/PDV'

export default function App() {
  return (
    <div className="layout">
      <Navbar />
      <main className="main">
        <Routes>
          <Route path="/" element={<Navigate to="/pdv" />} />
          <Route path="/pdv" element={<PDV />} />
          <Route path="/categorias" element={<Categorias />} />
          <Route path="/produtos" element={<Produtos />} />
          <Route path="/estoque" element={<Estoque />} />
          <Route path="/vendas" element={<Vendas />} />
          <Route path="/movimentacoes" element={<Movimentacoes />} />
        </Routes>
      </main>
    </div>
  )
}
