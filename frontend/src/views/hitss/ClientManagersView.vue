<script setup>
import { onMounted, reactive, ref } from 'vue'
import { api } from '../../api'
import { initials } from '../../utils'

const managers = ref([])
const error = ref('')
const ok = ref('')
const showForm = ref(false)
const form = reactive({ name: '', email: '', organization: '', password: '' })

async function load() {
  try {
    managers.value = await api('/hitss/client-managers')
  } catch (e) {
    error.value = e.message
  }
}

async function save() {
  error.value = ''
  ok.value = ''
  try {
    const created = await api('/hitss/client-managers', { method: 'POST', body: { ...form } })
    ok.value = `Acesso criado para ${created.name}. Envie a senha inicial por um canal seguro.`
    Object.assign(form, { name: '', email: '', organization: form.organization, password: '' })
    showForm.value = false
    await load()
  } catch (e) {
    error.value = e.message
  }
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-head">
      <div>
        <h1>Gestores do cliente</h1>
        <p class="sub">Quem recebe acesso para avaliar os profissionais alocados.</p>
      </div>
      <button class="btn primary" @click="showForm = !showForm">{{ showForm ? 'Fechar' : 'Cadastrar gestor' }}</button>
    </div>

    <div v-if="error" class="banner error">{{ error }}</div>
    <div v-if="ok" class="banner ok">{{ ok }}</div>

    <form v-if="showForm" class="card" style="margin-bottom: 16px" @submit.prevent="save">
      <h2>Novo gestor do cliente</h2>
      <div class="form-grid">
        <div class="field"><label>Nome</label><input v-model="form.name" required /></div>
        <div class="field"><label>E-mail</label><input v-model="form.email" type="email" required /></div>
        <div class="field"><label>Cliente (organização)</label><input v-model="form.organization" required /></div>
        <div class="field"><label>Senha inicial</label><input v-model="form.password" type="password" minlength="6" required /></div>
      </div>
      <p class="muted small" style="margin-bottom: 12px">
        No protótipo o acesso usa senha local. Na versão final o login seria pelo SSO corporativo, sem senha no Elo.
      </p>
      <button class="btn primary">Criar acesso</button>
    </form>

    <div class="card flush table-wrap">
      <table>
        <thead><tr><th>Gestor</th><th>Cliente</th><th class="num">Profissionais alocados</th></tr></thead>
        <tbody>
          <tr v-for="m in managers" :key="m.id">
            <td>
              <div class="row" style="flex-wrap: nowrap">
                <span class="avatar">{{ initials(m.name) }}</span>
                <div><strong>{{ m.name }}</strong><div class="muted small">{{ m.email }}</div></div>
              </div>
            </td>
            <td>{{ m.organization }}</td>
            <td class="num">{{ m.activeAllocations }}</td>
          </tr>
          <tr v-if="!managers.length"><td colspan="3" class="empty">Nenhum gestor de cliente cadastrado.</td></tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
