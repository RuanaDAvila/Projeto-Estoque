export default function Numpad({ digits, onChange }) {
  const formatted = (Number(digits || '0') / 100).toFixed(2)

  function press(k) {
    if (k === '⌫') onChange((digits || '').slice(0, -1))
    else if (k === 'C') onChange('')
    else onChange((digits || '') + k)
  }

  const keys = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '00', '0', '⌫']

  return (
    <div style={{
      background: '#f0f4ff', borderRadius: 10, padding: 12,
      marginTop: 8, border: '2px solid #d0d8e8'
    }}>
      <div style={{
        textAlign: 'center', fontSize: 26, fontWeight: 700, color: '#1a3a6b',
        marginBottom: 10, padding: '8px 12px', background: 'white',
        borderRadius: 8, border: '2px solid #d0d8e8', letterSpacing: 2
      }}>
        R$ {formatted}
      </div>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 6 }}>
        {keys.map(k => (
          <button key={k} type="button" onClick={() => press(k)} style={{
            padding: '14px 0', fontSize: 20, fontWeight: 700, borderRadius: 8,
            border: '2px solid #d0d8e8',
            background: k === '⌫' ? '#ffe0e0' : 'white',
            color: k === '⌫' ? '#c0392b' : '#1a1a2e',
            cursor: 'pointer'
          }}>
            {k}
          </button>
        ))}
      </div>
      <button type="button" onClick={() => press('C')} style={{
        marginTop: 6, width: '100%', padding: '10px 0', fontSize: 15,
        fontWeight: 700, borderRadius: 8, border: '2px solid #ffc107',
        background: '#fff3cd', cursor: 'pointer', color: '#856404'
      }}>
        C — Limpar
      </button>
    </div>
  )
}
