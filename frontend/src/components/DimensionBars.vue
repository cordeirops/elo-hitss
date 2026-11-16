<script setup>
import { DIMENSIONS, fmt } from '../utils'

defineProps({ averages: { type: Object, default: () => ({}) } })

function tone(v) {
  return v >= 4.5 ? 'great' : v >= 3.5 ? '' : v >= 3 ? 'warn' : 'bad'
}
</script>

<template>
  <div class="bars" v-if="Object.keys(averages).length">
    <div class="bar-row" v-for="d in DIMENSIONS" :key="d.key">
      <span>{{ d.label }}</span>
      <div class="bar-track">
        <div class="bar-fill" :class="tone(averages[d.key])" :style="{ width: (averages[d.key] / 5) * 100 + '%' }"></div>
      </div>
      <strong class="num">{{ fmt(averages[d.key], 1) }}</strong>
    </div>
  </div>
  <div v-else class="empty">Sem dados ainda.</div>
</template>
