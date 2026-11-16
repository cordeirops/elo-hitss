<script setup>
defineProps({
  label: String,
  value: [String, Number],
  hint: String,
  tone: { type: String, default: 'neutral' },
  progress: { type: Number, default: null }
})
</script>

<template>
  <div class="card stat" :class="tone">
    <div class="label">{{ label }}</div>
    <div class="value">{{ value }}</div>
    <div class="hint">{{ hint }}</div>
    <div class="meter" v-if="progress !== null"><span :style="{ width: Math.min(100, progress) + '%' }"></span></div>
  </div>
</template>
