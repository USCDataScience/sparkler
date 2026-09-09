<template>
  <div>
    <section class="bar" style="padding:0 0 0.8rem;border:0;background:transparent">
      <input v-model="q" placeholder="Search seeds" />
      <span class="hint">{{ shown.length }} of {{ seeds.length }}</span>
    </section>
    <p v-if="!seeds.length" class="empty">No seeds yet. Paste a URL in the bar and Add seed.</p>
    <p v-else-if="!shown.length" class="empty">No seeds match “{{ q }}”.</p>
    <div v-for="s in shown" :key="s" class="card click" @click="$emit('open', s)">
      <p class="meta" v-html="highlight(s)"></p>
    </div>
  </div>
</template>
<script setup>
import { computed, ref } from 'vue'

const props = defineProps({ seeds: { type: Array, default: () => [] } })
defineEmits(['open'])
const q = ref('')
const shown = computed(() => {
  const needle = q.value.trim().toLowerCase()
  if (!needle) return props.seeds
  return props.seeds.filter(s => s.toLowerCase().includes(needle))
})

function highlight(s) {
  const needle = q.value.trim()
  if (!needle) return escapeHtml(s)
  const re = new RegExp(`(${escapeRe(needle)})`, 'ig')
  return escapeHtml(s).replace(re, '<mark>$1</mark>')
}
function escapeHtml(s) {
  return s.replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]))
}
function escapeRe(s) {
  return s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}
</script>
