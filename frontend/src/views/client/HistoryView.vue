<script setup>
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { api } from '../../api'
import FeedbackCard from '../../components/FeedbackCard.vue'

const feedbacks = ref([])
const currentPeriod = ref('')
const error = ref('')

onMounted(async () => {
  try {
    const [list, team] = await Promise.all([api('/client/feedbacks'), api('/client/team')])
    feedbacks.value = list
    currentPeriod.value = team.period
  } catch (e) {
    error.value = e.message
  }
})
</script>

<template>
  <div>
    <div class="page-head">
      <div>
        <h1>Histórico de feedbacks</h1>
        <p class="sub">O que você já enviou. Feedbacks do mês atual ainda podem ser editados.</p>
      </div>
    </div>
    <div v-if="error" class="banner error">{{ error }}</div>
    <div class="card flush">
      <FeedbackCard v-for="f in feedbacks" :key="f.id" :fb="f" :show-manager="false">
        <template #actions>
          <RouterLink v-if="f.period === currentPeriod" class="btn small" :to="`/cliente/feedback/${f.id}/editar`">Editar</RouterLink>
        </template>
      </FeedbackCard>
      <div v-if="!feedbacks.length" class="empty">Você ainda não enviou feedbacks.</div>
    </div>
  </div>
</template>
