<script setup>
import { computed } from 'vue'
import { SCALE_LABELS } from '../utils'

const props = defineProps({
  modelValue: { type: Number, default: null },
  min: { type: Number, default: 1 },
  max: { type: Number, default: 5 },
  caption: { type: Boolean, default: true },
  low: { type: String, default: '' },
  high: { type: String, default: '' }
})
const emit = defineEmits(['update:modelValue'])

const values = computed(() => Array.from({ length: props.max - props.min + 1 }, (_, i) => props.min + i))

function tone(v) {
  if (props.max === 10) return v >= 9 ? 't-great' : v >= 7 ? 't-warn' : 't-bad'
  return v >= 5 ? 't-great' : v >= 4 ? 't-good' : v >= 3 ? 't-warn' : 't-bad'
}

const caption = computed(() => {
  if (props.modelValue === null || props.modelValue === undefined) return ''
  if (props.max === 5) return SCALE_LABELS[props.modelValue]
  return props.modelValue >= 9 ? 'Promotor' : props.modelValue >= 7 ? 'Neutro' : 'Detrator'
})
</script>

<template>
  <div>
    <div class="rating" role="radiogroup">
      <button
        v-for="v in values"
        :key="v"
        type="button"
        role="radio"
        :aria-checked="modelValue === v"
        :class="[modelValue === v ? 'on ' + tone(v) : '']"
        @click="emit('update:modelValue', v)"
      >
        {{ v }}
      </button>
    </div>
    <div class="rating-cap small" v-if="caption || low">
      <template v-if="modelValue === null || modelValue === undefined">{{ low && high ? low + ' ... ' + high : '' }}</template>
      <template v-else>{{ caption }}</template>
    </div>
  </div>
</template>
