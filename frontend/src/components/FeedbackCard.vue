<script setup>
import { DIMENSIONS, periodLabel, recommendTone, scoreTone } from '../utils'
import ScoreBadge from './ScoreBadge.vue'

defineProps({
  fb: { type: Object, required: true },
  showDev: { type: Boolean, default: true },
  showManager: { type: Boolean, default: true }
})
</script>

<template>
  <article class="fb">
    <div class="fb-head">
      <strong v-if="showDev">{{ fb.developerName }}</strong>
      <span class="muted small">{{ fb.project }} · {{ periodLabel(fb.period) }}</span>
      <span class="muted small" v-if="showManager">por {{ fb.clientManagerName }} ({{ fb.clientOrganization }})</span>
      <span class="spacer"></span>
      <ScoreBadge :value="fb.score" suffix=" / 5" />
      <span class="chip" :class="recommendTone(fb.recommend)">Recomendação {{ fb.recommend }}/10</span>
      <slot name="actions" />
    </div>
    <div class="fb-dims">
      <span class="chip" :class="scoreTone(fb[d.key])" v-for="d in DIMENSIONS" :key="d.key">{{ d.label }}: {{ fb[d.key] }}</span>
    </div>
    <div class="fb-text" v-if="fb.strengths || fb.improvements || fb.comment">
      <div v-if="fb.strengths">
        <h4>Pontos fortes</h4>
        <p>{{ fb.strengths }}</p>
      </div>
      <div v-if="fb.improvements">
        <h4>Pontos de melhoria</h4>
        <p>{{ fb.improvements }}</p>
      </div>
      <div v-if="fb.comment">
        <h4>Comentário</h4>
        <p>{{ fb.comment }}</p>
      </div>
    </div>
  </article>
</template>
