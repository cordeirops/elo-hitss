<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { api } from '../../api'
import { DIMENSIONS, initials, periodLabel } from '../../utils'
import RatingInput from '../../components/RatingInput.vue'

const route = useRoute()
const router = useRouter()

const editingId = computed(() => (route.params.id ? Number(route.params.id) : null))
const allocationId = ref(route.params.allocationId ? Number(route.params.allocationId) : null)
const person = ref(null)
const period = ref('')
const error = ref('')
const saving = ref(false)

const form = reactive({
  technical: null,
  communication: null,
  proactivity: null,
  commitment: null,
  teamwork: null,
  recommend: null,
  strengths: '',
  improvements: '',
  comment: ''
})

onMounted(async () => {
  try {
    if (editingId.value) {
      const fb = await api(`/client/feedbacks/${editingId.value}`)
      allocationId.value = fb.allocationId
      person.value = { name: fb.developerName, project: fb.project }
      period.value = fb.period
      Object.keys(form).forEach((k) => (form[k] = fb[k] ?? form[k]))
    } else {
      const team = await api('/client/team')
      period.value = team.period
      const m = team.members.find((x) => x.allocationId === allocationId.value)
      if (!m) throw new Error('Profissional não encontrado no seu time.')
      person.value = { name: m.developerName, project: m.project, jobTitle: m.jobTitle }
    }
  } catch (e) {
    error.value = e.message
  }
})

async function submit() {
  error.value = ''
  const missing = [...DIMENSIONS.map((d) => d.key), 'recommend'].some((k) => form[k] === null)
  if (missing) {
    error.value = 'Preencha as cinco notas e a recomendação antes de enviar.'
    return
  }
  saving.value = true
  try {
    const body = { allocationId: allocationId.value, ...form }
    if (editingId.value) await api(`/client/feedbacks/${editingId.value}`, { method: 'PUT', body })
    else await api('/client/feedbacks', { method: 'POST', body })
    router.push({ path: '/cliente', query: { saved: '1' } })
  } catch (e) {
    error.value = e.message
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div style="max-width: 820px">
    <RouterLink class="small" to="/cliente">Voltar para meu time</RouterLink>
    <div class="page-head" style="margin-top: 10px">
      <div class="row" style="flex-wrap: nowrap; gap: 14px" v-if="person">
        <span class="avatar" style="width: 48px; height: 48px">{{ initials(person.name) }}</span>
        <div>
          <h1>{{ editingId ? 'Editar feedback' : 'Feedback' }}: {{ person.name }}</h1>
          <p class="sub">{{ person.project }} · competência {{ periodLabel(period) }}</p>
        </div>
      </div>
    </div>

    <div v-if="error" class="banner error">{{ error }}</div>

    <form class="card" @submit.prevent="submit" v-if="person">
      <h2>Como foi o trabalho neste mês?</h2>
      <div class="crit" v-for="d in DIMENSIONS" :key="d.key">
        <strong>{{ d.label }}</strong>
        <div class="hint">{{ d.hint }}</div>
        <RatingInput v-model="form[d.key]" :min="1" :max="5" />
      </div>

      <div class="crit">
        <strong>Recomendação</strong>
        <div class="hint">De 0 a 10, o quanto você recomendaria este profissional para outro projeto ou equipe?</div>
        <RatingInput v-model="form.recommend" :min="0" :max="10" low="Nada provável" high="Muito provável" />
      </div>

      <div class="field" style="margin-top: 16px">
        <label>Pontos fortes</label>
        <textarea v-model="form.strengths" maxlength="2000" placeholder="O que esta pessoa faz bem?"></textarea>
      </div>
      <div class="field">
        <label>Pontos de melhoria</label>
        <textarea v-model="form.improvements" maxlength="2000" placeholder="O que ajudaria a evoluir nos próximos meses?"></textarea>
      </div>
      <div class="field">
        <label>Comentário adicional (opcional)</label>
        <textarea v-model="form.comment" maxlength="2000"></textarea>
      </div>

      <div class="row">
        <button class="btn primary" :disabled="saving">{{ saving ? 'Enviando...' : editingId ? 'Salvar alterações' : 'Enviar feedback' }}</button>
        <RouterLink class="btn ghost" to="/cliente">Cancelar</RouterLink>
      </div>
      <p class="muted small" style="margin-top: 12px">
        Seu feedback é lido pela gestão Hitss e usado para acompanhamento, reconhecimento e plano de desenvolvimento.
      </p>
    </form>
  </div>
</template>
