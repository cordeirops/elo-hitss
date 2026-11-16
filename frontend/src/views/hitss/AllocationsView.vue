<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { api } from '../../api'
import { dateBR } from '../../utils'

const allocations = ref([])
const devs = ref([])
const managers = ref([])
const error = ref('')
const ok = ref('')
const onlyActive = ref(true)
const form = reactive({ developerId: '', clientManagerId: '', project: '', startDate: new Date().toISOString().slice(0, 10) })

const visible = computed(() => (onlyActive.value ? allocations.value.filter((a) => a.active) : allocations.value))

async function load() {
  try {
    const [a, d, m] = await Promise.all([api('/hitss/allocations'), api('/hitss/developers'), api('/hitss/client-managers')])
    allocations.value = a
    devs.value = d.filter((x) => x.active)
    managers.value = m
  } catch (e) {
    error.value = e.message
  }
}

async function create() {
  error.value = ''
  ok.value = ''
  try {
    const created = await api('/hitss/allocations', {
      method: 'POST',
      body: {
        developerId: Number(form.developerId),
        clientManagerId: Number(form.clientManagerId),
        project: form.project,
        startDate: form.startDate
      }
    })
    ok.value = `${created.developerName} alocado(a) a ${created.clientManagerName}. O gestor já pode enviar feedback.`
    form.project = ''
    await load()
  } catch (e) {
    error.value = e.message
  }
}

async function end(a) {
  error.value = ''
  ok.value = ''
  try {
    await api(`/hitss/allocations/${a.id}/end`, { method: 'POST' })
    ok.value = `Alocação de ${a.developerName} encerrada.`
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
        <h1>Alocações</h1>
        <p class="sub">Vincule cada profissional ao gestor do cliente que vai avaliá-lo.</p>
      </div>
    </div>

    <div v-if="error" class="banner error">{{ error }}</div>
    <div v-if="ok" class="banner ok">{{ ok }}</div>

    <form class="card" style="margin-bottom: 16px" @submit.prevent="create">
      <h2>Nova alocação</h2>
      <div class="form-grid">
        <div class="field">
          <label>Profissional</label>
          <select v-model="form.developerId" required>
            <option value="" disabled>Selecione</option>
            <option v-for="d in devs" :key="d.id" :value="d.id">{{ d.name }}</option>
          </select>
        </div>
        <div class="field">
          <label>Gestor do cliente</label>
          <select v-model="form.clientManagerId" required>
            <option value="" disabled>Selecione</option>
            <option v-for="m in managers" :key="m.id" :value="m.id">{{ m.name }} ({{ m.organization }})</option>
          </select>
        </div>
        <div class="field"><label>Projeto ou squad</label><input v-model="form.project" required placeholder="Ex.: Portal do Cidadão" /></div>
        <div class="field"><label>Início</label><input v-model="form.startDate" type="date" required /></div>
      </div>
      <div class="row">
        <button class="btn primary">Alocar</button>
        <span class="muted small" v-if="!managers.length">
          Cadastre antes um gestor do cliente em <RouterLink to="/hitss/gestores">Gestores do cliente</RouterLink>.
        </span>
      </div>
    </form>

    <div class="filters">
      <label class="row small"><input v-model="onlyActive" type="checkbox" style="width: auto" /> Mostrar somente alocações ativas</label>
    </div>

    <div class="card flush table-wrap">
      <table>
        <thead>
          <tr><th>Profissional</th><th>Gestor do cliente</th><th>Projeto</th><th>Período</th><th>Status</th><th></th></tr>
        </thead>
        <tbody>
          <tr v-for="a in visible" :key="a.id">
            <td><RouterLink :to="`/hitss/devs/${a.developerId}`"><strong>{{ a.developerName }}</strong></RouterLink></td>
            <td>{{ a.clientManagerName }}<div class="muted small">{{ a.clientOrganization }}</div></td>
            <td>{{ a.project }}</td>
            <td class="small">{{ dateBR(a.startDate) }}<template v-if="a.endDate"> até {{ dateBR(a.endDate) }}</template></td>
            <td><span class="chip" :class="a.active ? 'great' : ''">{{ a.active ? 'Ativa' : 'Encerrada' }}</span></td>
            <td class="num"><button v-if="a.active" class="btn small" @click="end(a)">Encerrar</button></td>
          </tr>
          <tr v-if="!visible.length"><td colspan="6" class="empty">Nenhuma alocação para exibir.</td></tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
