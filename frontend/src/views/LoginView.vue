<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'
import { auth } from '../auth'
import Logo from '../components/Logo.vue'

const router = useRouter()
const email = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

async function submit() {
  error.value = ''
  loading.value = true
  try {
    const res = await api('/auth/login', { method: 'POST', body: { email: email.value, password: password.value } })
    auth.set(res.token, res.user)
    router.push(auth.home)
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

function fill(e, p) {
  email.value = e
  password.value = p
}
</script>

<template>
  <div class="login">
    <section class="login-hero">
      <div class="row" style="gap: 10px">
        <Logo />
        <strong style="font-size: 1.4rem">Elo</strong>
      </div>
      <h1>A voz do cliente chegando até a gestão da Hitss</h1>
      <p>
        Quem trabalha alocado no cliente é visto todos os dias por quem contrata o serviço. O Elo leva essa percepção, de forma
        simples e recorrente, para quem cuida da carreira e do contrato.
      </p>
      <ul>
        <li>Feedback mensal do gestor do cliente em poucos minutos</li>
        <li>Visão da Hitss por profissional, projeto e cliente</li>
        <li>Alertas e reconhecimento no tempo certo</li>
      </ul>
    </section>
    <section class="login-panel">
      <div class="login-box">
        <h1>Entrar</h1>
        <p class="sub" style="margin-bottom: 22px">Use seu e-mail corporativo.</p>
        <div v-if="error" class="banner error">{{ error }}</div>
        <form @submit.prevent="submit">
          <div class="field">
            <label for="email">E-mail</label>
            <input id="email" v-model="email" type="email" autocomplete="username" required />
          </div>
          <div class="field">
            <label for="pwd">Senha</label>
            <input id="pwd" v-model="password" type="password" autocomplete="current-password" required />
          </div>
          <button class="btn primary" style="width: 100%" :disabled="loading">{{ loading ? 'Entrando...' : 'Entrar' }}</button>
        </form>
        <div class="demo">
          <div class="small muted">Acesso de demonstração (dados fictícios)</div>
          <div class="row">
            <button class="btn small" type="button" @click="fill('gestor@hitss.demo', 'hitss123')">Gestor Hitss</button>
            <button class="btn small" type="button" @click="fill('carlos@cliente.demo', 'cliente123')">Gestor do cliente</button>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>
