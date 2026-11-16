export const DIMENSIONS = [
  { key: 'technical', label: 'Qualidade técnica', hint: 'Código, soluções e domínio da stack' },
  { key: 'communication', label: 'Comunicação', hint: 'Clareza, alinhamento e transparência' },
  { key: 'proactivity', label: 'Proatividade', hint: 'Antecipa riscos e propõe melhorias' },
  { key: 'commitment', label: 'Compromisso com prazos', hint: 'Cumpre combinados e avisa sobre bloqueios' },
  { key: 'teamwork', label: 'Trabalho em equipe', hint: 'Colabora com o time e com o negócio' }
]

export const SCALE_LABELS = {
  1: 'Muito abaixo do esperado',
  2: 'Abaixo do esperado',
  3: 'Atende ao esperado',
  4: 'Acima do esperado',
  5: 'Excelente'
}

export function fmt(n, digits = 1) {
  if (n === null || n === undefined || Number.isNaN(n)) return 'n/d'
  return Number(n).toFixed(digits).replace('.', ',')
}

const MONTHS = ['jan', 'fev', 'mar', 'abr', 'mai', 'jun', 'jul', 'ago', 'set', 'out', 'nov', 'dez']

export function periodLabel(period) {
  if (!period) return 'n/d'
  const [y, m] = period.split('-')
  return `${MONTHS[Number(m) - 1]}/${y}`
}

export function dateBR(iso) {
  if (!iso) return 'n/d'
  const [y, m, d] = iso.slice(0, 10).split('-')
  return `${d}/${m}/${y}`
}

export function scoreTone(v) {
  if (v === null || v === undefined) return 'neutral'
  if (v >= 4.5) return 'great'
  if (v >= 3.5) return 'good'
  if (v >= 3) return 'warn'
  return 'bad'
}

export function recommendTone(v) {
  if (v === null || v === undefined) return 'neutral'
  if (v >= 9) return 'great'
  if (v >= 7) return 'warn'
  return 'bad'
}

export function npsTone(v) {
  if (v === null || v === undefined) return 'neutral'
  if (v >= 50) return 'great'
  if (v >= 0) return 'good'
  return 'bad'
}

export function initials(name = '') {
  const parts = name.trim().split(/\s+/).filter(Boolean)
  if (!parts.length) return '?'
  return (parts[0][0] + (parts.length > 1 ? parts[parts.length - 1][0] : '')).toUpperCase()
}
