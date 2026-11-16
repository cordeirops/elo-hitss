import { auth } from './auth'
import router from './router'

// Em producao na Vercel, defina VITE_API_URL (ex: https://elo-hitss-9uqv.vercel.app).
// Vazio = mesma origem (Quarkus servindo o front, ou proxy do Vite em dev).
const BASE = (import.meta.env.VITE_API_URL || '').replace(/\/$/, '')

export class ApiError extends Error {
  constructor(message, status) {
    super(message)
    this.status = status
  }
}

export async function api(path, { method = 'GET', body } = {}) {
  const headers = { 'Content-Type': 'application/json' }
  if (auth.token) headers.Authorization = `Bearer ${auth.token}`

  let res
  try {
    res = await fetch(`${BASE}/api${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body)
    })
  } catch {
    throw new ApiError('Não foi possível falar com o servidor. Verifique se o backend está no ar.', 0)
  }

  if (res.status === 204) return null
  const data = await res.json().catch(() => null)

  if (!res.ok) {
    if (res.status === 401 && auth.token) {
      auth.clear()
      router.push('/login')
    }
    throw new ApiError(data?.message || 'Erro inesperado. Tente novamente.', res.status)
  }
  return data
}
