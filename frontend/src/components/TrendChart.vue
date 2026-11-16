<script setup>
import { computed } from 'vue'
import { fmt, periodLabel } from '../utils'

const props = defineProps({ points: { type: Array, default: () => [] } })

const W = 620
const H = 230
const pad = { l: 44, r: 40, t: 24, b: 30 }

const data = computed(() => props.points.filter((p) => p.averageScore !== null))

const coords = computed(() => {
  const n = data.value.length
  const innerW = W - pad.l - pad.r
  const innerH = H - pad.t - pad.b
  return data.value.map((p, i) => ({
    x: pad.l + (n === 1 ? innerW / 2 : (innerW * i) / (n - 1)),
    y: pad.t + innerH - ((p.averageScore - 1) / 4) * innerH,
    p
  }))
})

const path = computed(() => coords.value.map((c, i) => `${i ? 'L' : 'M'}${c.x.toFixed(1)},${c.y.toFixed(1)}`).join(' '))
const gridY = [1, 2, 3, 4, 5].map((v) => ({ v, y: pad.t + (H - pad.t - pad.b) - ((v - 1) / 4) * (H - pad.t - pad.b) }))
</script>

<template>
  <div class="trend">
    <div v-if="!data.length" class="empty">Ainda não há feedbacks suficientes para exibir a evolução.</div>
    <svg v-else :viewBox="`0 0 ${W} ${H}`" role="img" aria-label="Evolução da nota média por competência">
      <g v-for="g in gridY" :key="g.v">
        <line class="grid-line" :x1="pad.l" :x2="W - pad.r" :y1="g.y" :y2="g.y" />
        <text class="axis-text" :x="pad.l - 8" :y="g.y + 4" text-anchor="end">{{ g.v }}</text>
      </g>
      <path class="line" :d="path" />
      <g v-for="c in coords" :key="c.p.period">
        <circle class="dot" :cx="c.x" :cy="c.y" r="6" />
        <text class="val" :x="c.x" :y="c.y - 14">{{ fmt(c.p.averageScore, 1) }}</text>
        <text class="axis-text" :x="c.x" :y="H - 8" text-anchor="middle">{{ periodLabel(c.p.period) }}</text>
      </g>
    </svg>
  </div>
</template>
