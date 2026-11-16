<script setup>
import { computed } from 'vue'
import { RouterLink, RouterView, useRouter } from 'vue-router'
import { auth } from '../auth'
import { api } from '../api'
import { initials } from '../utils'
import Logo from './Logo.vue'

const router = useRouter()

const links = computed(() =>
  auth.isHitss
    ? [
        { to: '/hitss', label: 'Visão geral' },
        { to: '/hitss/devs', label: 'Profissionais' },
        { to: '/hitss/alocacoes', label: 'Alocações' },
        { to: '/hitss/gestores', label: 'Gestores do cliente' },
        { to: '/hitss/feedbacks', label: 'Feedbacks recebidos' }
      ]
    : [
        { to: '/cliente', label: 'Meu time' },
        { to: '/cliente/historico', label: 'Histórico de feedbacks' }
      ]
)

async function logout() {
  try {
    await api('/auth/logout', { method: 'POST' })
  } catch {
    /* mesmo com erro, encerramos a sessao local */
  }
  auth.clear()
  router.push('/login')
}
</script>

<template>
  <div class="shell">
    <aside class="sidebar">
      <div class="brand">
        <Logo />
        <div>
          Elo
          <small>Hitss · Experiência do Cliente</small>
        </div>
      </div>
      <nav class="nav">
        <RouterLink v-for="l in links" :key="l.to" :to="l.to">{{ l.label }}</RouterLink>
      </nav>
      <div class="userbox" v-if="auth.user">
        <div class="who">
          <span class="avatar">{{ initials(auth.user.name) }}</span>
          <div>
            <div class="name">{{ auth.user.name }}</div>
            <div class="org">{{ auth.isHitss ? 'Gestor Hitss' : 'Gestor do cliente' }} · {{ auth.user.organization }}</div>
          </div>
        </div>
        <button @click="logout">Sair</button>
      </div>
    </aside>
    <main class="main">
      <RouterView :key="$route.fullPath" />
    </main>
  </div>
</template>
