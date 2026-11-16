import { reactive } from 'vue'

const KEY = 'elo.session'

function load() {
  try {
    const raw = sessionStorage.getItem(KEY)
    return raw ? JSON.parse(raw) : { token: null, user: null }
  } catch {
    return { token: null, user: null }
  }
}

export const auth = reactive({
  ...load(),
  set(token, user) {
    this.token = token
    this.user = user
    try {
      sessionStorage.setItem(KEY, JSON.stringify({ token, user }))
    } catch {
      /* sessionStorage indisponivel: a sessao vale so enquanto a aba estiver aberta */
    }
  },
  clear() {
    this.token = null
    this.user = null
    try {
      sessionStorage.removeItem(KEY)
    } catch {
      /* ignora */
    }
  },
  get isHitss() {
    return this.user?.role === 'HITSS_MANAGER'
  },
  get isClient() {
    return this.user?.role === 'CLIENT_MANAGER'
  },
  get home() {
    return this.isHitss ? '/hitss' : this.isClient ? '/cliente' : '/login'
  }
})
