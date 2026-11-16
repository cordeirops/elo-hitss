<script setup>
import { onMounted, ref } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { api } from '../../api'
import { initials, periodLabel } from '../../utils'
import ScoreBadge from '../../components/ScoreBadge.vue'

const route = useRoute()
const team = ref(null)
const error = ref('')

onMounted(async () => {
  try {
    team.value = await api('/client/team')
  } catch (e) {
    error.value = e.message
  }
})
</script>

<template>
  <div>
    <div v-if="route.query.saved" class="banner ok">Feedback enviado. Obrigado por ajudar a acompanhar o time.</div>
    <div v-if="error" class="banner error">{{ error }}</div>
    <template v-if="team">
      <div class="page-head">
        <div>
          <h1>Meu time</h1>
          <p class="sub">
            Competência {{ periodLabel(team.period) }} · {{ team.done }} de {{ team.total }} feedbacks enviados
          </p>
        </div>
      </div>

      <div class="banner info" v-if="team.total && team.done < team.total">
        Falta pouco: cada feedback leva cerca de 3 minutos e vai direto para a gestão Hitss.
      </div>

      <div class="team">
        <article class="card member" v-for="m in team.members" :key="m.allocationId">
          <div class="top">
            <span class="avatar">{{ initials(m.developerName) }}</span>
            <div>
              <div class="name">{{ m.developerName }}</div>
              <div class="muted small">{{ m.jobTitle }}</div>
            </div>
            <span class="spacer"></span>
            <span class="chip" :class="m.feedbackDone ? 'great' : 'warn'">{{ m.feedbackDone ? 'Enviado' : 'Pendente' }}</span>
          </div>
          <div class="muted small">{{ m.project }} · {{ m.stack }}</div>
          <div class="foot">
            <div class="small muted">
              Última nota:
              <ScoreBadge :value="m.lastScore" />
              <span v-if="m.lastPeriod"> em {{ periodLabel(m.lastPeriod) }}</span>
            </div>
            <RouterLink
              v-if="!m.feedbackDone"
              class="btn sun small"
              :to="`/cliente/feedback/novo/${m.allocationId}`"
            >Dar feedback</RouterLink>
            <RouterLink v-else class="btn small" :to="`/cliente/feedback/${m.feedbackId}/editar`">Editar</RouterLink>
          </div>
        </article>
      </div>
      <div v-if="!team.total" class="card empty">
        Nenhum profissional foi alocado a você ainda. A gestão Hitss faz esse vínculo.
      </div>
    </template>
  </div>
</template>
