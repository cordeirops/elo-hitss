<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { api } from '../../api'
import { dateBR, fmt, initials, periodLabel, recommendTone, scoreTone } from '../../utils'
import StatCard from '../../components/StatCard.vue'
import TrendChart from '../../components/TrendChart.vue'
import DimensionBars from '../../components/DimensionBars.vue'
import FeedbackCard from '../../components/FeedbackCard.vue'

const route = useRoute()
const data = ref(null)
const error = ref('')

onMounted(async () => {
  try {
    data.value = await api(`/hitss/developers/${route.params.id}`)
  } catch (e) {
    error.value = e.message
  }
})

const dev = computed(() => data.value?.developer)
</script>

<template>
  <div>
    <RouterLink class="small" to="/hitss/devs">Voltar para profissionais</RouterLink>
    <div v-if="error" class="banner error" style="margin-top: 12px">{{ error }}</div>
    <template v-if="data">
      <div class="page-head" style="margin-top: 10px">
        <div class="row" style="flex-wrap: nowrap; gap: 14px">
          <span class="avatar" style="width: 52px; height: 52px; font-size: 1.1rem">{{ initials(dev.name) }}</span>
          <div>
            <h1>{{ dev.name }}</h1>
            <p class="sub">{{ dev.jobTitle }} · {{ dev.stack }} · {{ dev.email }}</p>
          </div>
        </div>
      </div>

      <section class="grid stats">
        <StatCard label="Nota média" :value="fmt(dev.averageScore, 2)" hint="Média dos 5 critérios" :tone="scoreTone(dev.averageScore)" />
        <StatCard
          label="Recomendação média"
          :value="fmt(dev.recommendAverage, 1)"
          hint="Escala de 0 a 10"
          :tone="recommendTone(dev.recommendAverage)"
        />
        <StatCard label="Feedbacks recebidos" :value="dev.feedbackCount" :hint="`Último: ${periodLabel(dev.lastFeedbackPeriod)}`" />
        <StatCard label="Alocações ativas" :value="dev.activeAllocations" :hint="dev.clientManagers.join(', ') || 'Nenhuma'" />
      </section>

      <section class="grid two">
        <div class="card">
          <div class="card-head"><h2>Evolução por competência</h2></div>
          <TrendChart :points="data.trend" />
        </div>
        <div class="card">
          <div class="card-head"><h2>Média por critério</h2></div>
          <DimensionBars :averages="data.dimensionAverages" />
        </div>
      </section>

      <section class="card" style="margin-bottom: 16px">
        <div class="card-head"><h2>Alocações</h2></div>
        <ul class="list">
          <li v-for="a in data.allocations" :key="a.id">
            <div class="grow">
              <div class="title">{{ a.project }}</div>
              <div class="muted small">{{ a.clientManagerName }} ({{ a.clientOrganization }}) · desde {{ dateBR(a.startDate) }}</div>
            </div>
            <span class="chip" :class="a.active ? 'great' : ''">{{ a.active ? 'Ativa' : 'Encerrada em ' + dateBR(a.endDate) }}</span>
          </li>
        </ul>
      </section>

      <section class="card flush">
        <div class="pad card-head" style="margin: 0"><h2>Feedbacks do cliente</h2></div>
        <FeedbackCard v-for="f in data.feedbacks" :key="f.id" :fb="f" :show-dev="false" />
        <div v-if="!data.feedbacks.length" class="empty">Este profissional ainda não recebeu feedbacks.</div>
      </section>
    </template>
  </div>
</template>
