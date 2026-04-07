import { NavLink } from 'react-router-dom'

export default function Navbar() {
  return (
    <aside className="sidebar">
      <h1>🍷 Adega Stock</h1>
      <nav>
        <NavLink to="/categorias">Categorias</NavLink>
        <NavLink to="/produtos">Produtos</NavLink>
        <NavLink to="/combos">Combos</NavLink>
        <NavLink to="/estoque">Estoque</NavLink>
        <NavLink to="/vendas">Vendas</NavLink>
        <NavLink to="/movimentacoes">Histórico</NavLink>
      </nav>
    </aside>
  )
}
