<script setup>
import { onMounted, reactive, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { api } from '../../api'
import { fmt, initials, periodLabel, recommendTone } from '../../utils'
import ScoreBadge from '../../components/ScoreBadge.vue'

const devs = ref([])
const error = ref('')
const ok = ref('')
const showForm = ref(false)
const form = reactive({ name: '', email: '', jobTitle: '', stack: '' })

async function load() {
  try {
    devs.value = await api('/hitss/developers')
  } catch (e) {
    error.value = e.message
  }
}

async function save() {
  error.value = ''
  ok.value = ''
  try {
    const created = await api('/hitss/developers', { method: 'POST', body: { ...form } })
    ok.value = `${created.name} foi cadastrado. Agora é só alocar a um gestor do cliente.`
    Object.assign(form, { name: '', email: '', jobTitle: '', stack: '' })
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
        <h1>Profissionais</h1>
        <p class="sub">Devs Hitss alocados em clientes e a percepção que o cliente tem de cada um.</p>
      </div>
      <button class="btn primary" @click="showForm = !showForm">{{ showForm ? 'Fechar' : 'Cadastrar profissional' }}</button>
    </div>

    <div v-if="error" class="banner error">{{ error }}</div>
    <div v-if="ok" class="banner ok">{{ ok }}</div>

    <form v-if="showForm" class="card" style="margin-bottom: 16px" @submit.prevent="save">
      <h2>Novo profissional</h2>
      <div class="form-grid">
        <div class="field"><label>Nome completo</label><input v-model="form.name" required /></div>
        <div class="field"><label>E-mail Hitss</label><input v-model="form.email" type="email" required /></div>
        <div class="field"><label>Cargo</label><input v-model="form.jobTitle" placeholder="Ex.: Dev Java Pleno" /></div>
        <div class="field"><label>Stack principal</label><input v-model="form.stack" placeholder="Ex.: Java 17, Quarkus, Kafka" /></div>
      </div>
      <button class="btn primary">Salvar</button>
    </form>

    <div class="card flush table-wrap">
      <table>
        <thead>
          <tr>
            <th>Profissional</th>
            <th>Gestor(es) no cliente</th>
            <th class="num">Feedbacks</th>
            <th class="num">Nota média</th>
            <th class="num">Recomendação</th>
            <th>Último</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="d in devs" :key="d.id">
            <td>
              <div class="row" style="flex-wrap: nowrap">
                <span class="avatar">{{ initials(d.name) }}</span>
                <div>
                  <RouterLink :to="`/hitss/devs/${d.id}`"><strong>{{ d.name }}</strong></RouterLink>
                  <div class="muted small">{{ d.jobTitle }} · {{ d.stack }}</div>
                </div>
              </div>
            </td>
            <td class="small">
              <template v-if="d.clientManagers.length">{{ d.clientManagers.join(', ') }}</template>
              <span v-else class="chip warn">Sem alocação</span>
            </td>
            <td class="num">{{ d.feedbackCount }}</td>
            <td class="num"><ScoreBadge :value="d.averageScore" /></td>
            <td class="num"><span class="chip" :class="recommendTone(d.recommendAverage)">{{ fmt(d.recommendAverage, 1) }}</span></td>
            <td class="small">{{ periodLabel(d.lastFeedbackPeriod) }}</td>
          </tr>
          <tr v-if="!devs.length"><td colspan="6" class="empty">Nenhum profissional cadastrado.</td></tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
