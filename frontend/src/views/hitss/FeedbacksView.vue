<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { api } from '../../api'
import { periodLabel } from '../../utils'
import FeedbackCard from '../../components/FeedbackCard.vue'

const feedbacks = ref([])
const devs = ref([])
const managers = ref([])
const error = ref('')
const filters = ref({ developerId: '', period: '', clientManagerId: '' })
const allPeriods = ref([])

async function load() {
  const qs = new URLSearchParams()
  if (filters.value.developerId) qs.set('developerId', filters.value.developerId)
  if (filters.value.period) qs.set('period', filters.value.period)
  if (filters.value.clientManagerId) qs.set('clientManagerId', filters.value.clientManagerId)
  try {
    feedbacks.value = await api(`/hitss/feedbacks${qs.toString() ? '?' + qs : ''}`)
  } catch (e) {
    error.value = e.message
  }
}

onMounted(async () => {
  try {
    const [d, m, all] = await Promise.all([api('/hitss/developers'), api('/hitss/client-managers'), api('/hitss/feedbacks')])
    devs.value = d
    managers.value = m
    allPeriods.value = [...new Set(all.map((f) => f.period))].sort().reverse()
    feedbacks.value = all
  } catch (e) {
    error.value = e.message
  }
})

watch(filters, load, { deep: true })
const count = computed(() => feedbacks.value.length)
</script>

<template>
  <div>
    <div class="page-head">
      <div>
        <h1>Feedbacks recebidos</h1>
        <p class="sub">Tudo o que os gestores do cliente registraram sobre os profissionais Hitss.</p>
      </div>
    </div>
    <div v-if="error" class="banner error">{{ error }}</div>

    <div class="filters">
      <select v-model="filters.developerId" aria-label="Profissional">
        <option value="">Todos os profissionais</option>
        <option v-for="d in devs" :key="d.id" :value="d.id">{{ d.name }}</option>
      </select>
      <select v-model="filters.clientManagerId" aria-label="Gestor do cliente">
        <option value="">Todos os gestores</option>
        <option v-for="m in managers" :key="m.id" :value="m.id">{{ m.name }}</option>
      </select>
      <select v-model="filters.period" aria-label="Competência">
        <option value="">Todas as competências</option>
        <option v-for="p in allPeriods" :key="p" :value="p">{{ periodLabel(p) }}</option>
      </select>
      <span class="muted small" style="align-self: center">{{ count }} feedback(s)</span>
    </div>

    <div class="card flush">
      <FeedbackCard v-for="f in feedbacks" :key="f.id" :fb="f" />
      <div v-if="!feedbacks.length" class="empty">Nenhum feedback encontrado com esses filtros.</div>
    </div>
  </div>
</template>
