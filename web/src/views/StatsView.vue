<template>
  <div>
    <div class="card">
      <h3>Status</h3>
      <div ref="statusEl" class="chart"></div>
    </div>
    <div class="card">
      <h3>Hosts</h3>
      <div ref="hostEl" class="chart" style="height:auto;min-height:12rem"></div>
    </div>
  </div>
</template>
<script setup>
import { onMounted, onUpdated, ref } from 'vue'
import { hostBars, statusBars } from '../charts.js'

const props = defineProps({ facets: { type: Object, default: () => ({}) } })
const statusEl = ref(null)
const hostEl = ref(null)

function draw() {
  if (statusEl.value) statusBars(statusEl.value, props.facets.status || [])
  if (hostEl.value) hostBars(hostEl.value, props.facets.hostname || [])
}
onMounted(draw)
onUpdated(draw)
</script>
