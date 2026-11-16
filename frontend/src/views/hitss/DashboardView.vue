<script setup>
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { api } from '../../api'
import { fmt, npsTone, periodLabel, scoreTone } from '../../utils'
import StatCard from '../../components/StatCard.vue'
import TrendChart from '../../components/TrendChart.vue'
import DimensionBars from '../../components/DimensionBars.vue'
import FeedbackCard from '../../components/FeedbackCard.vue'
import ScoreBadge from '../../components/ScoreBadge.vue'

const d = ref(null)
const error = ref('')

onMounted(async () => {
  try {
    d.value = await api('/hitss/dashboard')
  } catch (e) {
    error.value = e.message
  }
})
</script>

<template>
  <div>
    <div v-if="error" class="banner error">{{ error }}</div>
    <template v-if="d">
      <div class="page-head">
        <div>
          <h1>Visão geral</h1>
          <p class="sub">
            Competência {{ periodLabel(d.period) }} · {{ d.activeDevelopers }} profissionais alocados em
            {{ d.activeClientManagers }} gestores de cliente
          </p>
        </div>
        <RouterLink class="btn primary" to="/hitss/alocacoes">Gerenciar alocações</RouterLink>
      </div>

      <section class="grid stats">
        <StatCard
          label="Cobertura de feedback"
          :value="d.coveragePercent + '%'"
          :hint="`${d.feedbacksThisPeriod} de ${d.activeAllocations} alocações avaliadas no mês`"
          :progress="d.coveragePercent"
        />
        <StatCard
          label="Nota média"
          :value="fmt(d.averageScore, 2)"
          hint="Média dos 5 critérios (1 a 5), todo o histórico"
          :tone="scoreTone(d.averageScore)"
        />
        <StatCard
          label="NPS dos profissionais"
          :value="d.nps === null ? 'n/d' : d.nps"
          hint="Promotores menos detratores (recomendação 0 a 10)"
          :tone="npsTone(d.nps)"
        />
        <StatCard
          label="Feedbacks pendentes"
          :value="d.pendingThisPeriod"
          hint="Alocações sem feedback na competência atual"
          :tone="d.pendingThisPeriod ? 'warn' : 'great'"
        />
      </section>

      <section class="grid two">
        <div class="card">
          <div class="card-head"><h2>Evolução da nota média</h2></div>
          <TrendChart :points="d.trend" />
        </div>
        <div class="card">
          <div class="card-head"><h2>Média por critério</h2></div>
          <DimensionBars :averages="d.dimensionAverages" />
        </div>
      </section>

      <section class="grid two">
        <div class="card">
          <div class="card-head">
            <h2>Pede atenção</h2>
            <span class="chip bad" v-if="d.attention.length">{{ d.attention.length }}</span>
          </div>
          <ul class="list" v-if="d.attention.length">
            <li v-for="s in d.attention" :key="s.developerId">
              <div class="grow">
                <RouterLink class="title" :to="`/hitss/devs/${s.developerId}`">{{ s.developerName }}</RouterLink>
                <div class="muted small">{{ s.note }}</div>
              </div>
              <ScoreBadge :value="s.averageScore" />
            </li>
          </ul>
          <div v-else class="empty">Nenhum profissional com sinal de alerta.</div>
        </div>
        <div class="card">
          <div class="card-head">
            <h2>Destaques para reconhecimento</h2>
            <span class="chip great" v-if="d.highlights.length">{{ d.highlights.length }}</span>
          </div>
          <ul class="list" v-if="d.highlights.length">
            <li v-for="s in d.highlights" :key="s.developerId">
              <div class="grow">
                <RouterLink class="title" :to="`/hitss/devs/${s.developerId}`">{{ s.developerName }}</RouterLink>
                <div class="muted small">{{ s.note }}</div>
              </div>
              <ScoreBadge :value="s.averageScore" />
            </li>
          </ul>
          <div v-else class="empty">Ainda sem destaques neste ciclo.</div>
        </div>
      </section>

      <section class="card flush">
        <div class="pad card-head" style="margin: 0">
          <h2>Últimos feedbacks recebidos</h2>
          <RouterLink to="/hitss/feedbacks">Ver todos</RouterLink>
        </div>
        <FeedbackCard v-for="f in d.recentFeedbacks" :key="f.id" :fb="f" />
      </section>
    </template>
  </div>
</template>
