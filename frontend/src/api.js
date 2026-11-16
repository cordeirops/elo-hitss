import { auth } from './auth'
import router from './router'

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
    res = await fetch(`/api${path}`, {
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
