<script setup>
import { computed } from 'vue'
import { fmt, scoreTone } from '../utils'

const props = defineProps({ value: { type: Number, default: null }, suffix: { type: String, default: '' } })
const tone = computed(() => scoreTone(props.value))
</script>

<template>
  <span class="chip" :class="tone">{{ fmt(value, 1) }}{{ suffix }}</span>
</template>
