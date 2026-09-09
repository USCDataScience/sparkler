<template>
  <div>
    <p v-if="err" class="err">{{ err }}</p>
    <p v-else-if="!charts" class="hint">Loading charts…</p>
    <template v-else>
      <div class="kpi-grid">
        <div class="card kpi"><strong>{{ span }}</strong><span>crawl span</span></div>
        <div class="card kpi"><strong>{{ tika.docs_with_tika || 0 }}</strong><span>pages with Tika metadata</span></div>
        <div class="card kpi"><strong>{{ fmtMs(tika.response_ms?.avg) }}</strong><span>avg fetch ms</span></div>
        <div class="card kpi"><strong>{{ fmtMs(tika.parse_ms?.avg) }}</strong><span>avg Tika parse ms</span></div>
        <div class="card kpi"><strong>{{ (tika.tika_keys || []).length }}</strong><span>Tika fields seen</span></div>
      </div>
      <div class="card">
        <h3>Crawl time × depth</h3>
        <p class="hint">Heatmap of when pages were fetched. Rows are depth bins (pagination no longer makes 100 rows). {{ stepHint }} Hover a cell.</p>
        <div ref="heatEl" class="chart heat"></div>
      </div>
      <div class="card">
        <h3>Fetch rate</h3>
        <p class="hint">Pages fetched per time bucket. Area + line.</p>
        <div ref="areaEl" class="chart area"></div>
      </div>
      <div class="grid2">
        <div class="card">
          <h3>Status</h3>
          <div ref="statusEl" class="chart donut"></div>
        </div>
        <div class="card">
          <h3>MIME</h3>
          <div ref="mimeEl" class="chart donut"></div>
        </div>
      </div>
      <div class="card">
        <h3>Hosts</h3>
        <p class="hint">Packed bubbles, area ∝ pages. Hover for the count.</p>
        <div ref="hostEl" class="chart pack"></div>
      </div>
      <div class="card">
        <h3>Tika metadata fields</h3>
        <p class="hint">Treemap: tile size is how many fetched pages carry that Tika key.</p>
        <div ref="keysEl" class="chart tree"></div>
      </div>
      <div class="grid2">
        <div class="card">
          <h3>Language</h3>
          <div ref="langEl" class="chart donut"></div>
        </div>
        <div class="card">
          <h3>Encoding</h3>
          <div ref="encEl" class="chart donut"></div>
        </div>
      </div>
      <div class="grid2">
        <div class="card">
          <h3>Tika parsers</h3>
          <div ref="parserEl" class="chart pack"></div>
        </div>
        <div class="card">
          <h3>Fetch latency</h3>
          <p class="hint">Histogram of response_time (ms). Hover a bin.</p>
          <div ref="rtEl" class="chart"></div>
        </div>
      </div>
    </template>
  </div>
</template>
<script setup>
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { get } from '../api.js'
import { areaChart, bubblePack, donut, heatMap, histogram, paint, treeMap } from '../charts.js'

const props = defineProps({
  job: { type: String, default: '' },
  facets: { type: Object, default: () => ({}) }
})
const charts = ref(null)
const err = ref('')
const statusEl = ref(null)
const hostEl = ref(null)
const heatEl = ref(null)
const areaEl = ref(null)
const mimeEl = ref(null)
const keysEl = ref(null)
const langEl = ref(null)
const encEl = ref(null)
const parserEl = ref(null)
const rtEl = ref(null)

const tika = computed(() => (charts.value && charts.value.tika) || {})
const span = computed(() => {
  const s = charts.value && charts.value.heatmap && charts.value.heatmap.span_s
  if (s == null) return '—'
  if (s < 90) return `${Math.round(s)}s`
  if (s < 3600) return `${Math.round(s / 60)} min`
  return `${(s / 3600).toFixed(1)} h`
})
const stepHint = computed(() => {
  const step = charts.value?.heatmap?.step_s
  if (!step) return ''
  if (step < 60) return `Columns are ${step}s.`
  if (step < 3600) return `Columns are ${step / 60} min.`
  return `Columns are ${step / 3600} h.`
})

function fmtMs(v) {
  if (v == null) return '—'
  return `${Math.round(v)} ms`
}

async function load() {
  if (!props.job) {
    charts.value = null
    return
  }
  err.value = ''
  try {
    charts.value = await get(`/api/jobs/${encodeURIComponent(props.job)}/charts`)
  } catch (e) {
    err.value = e.message
  }
}

function draw() {
  paint(() => {
    if (heatEl.value && charts.value?.heatmap) heatMap(heatEl.value, charts.value.heatmap)
    if (areaEl.value) areaChart(areaEl.value, charts.value?.heatmap?.series || [])
    if (statusEl.value) donut(statusEl.value, props.facets.status || [], { title: 'status' })
    if (mimeEl.value) donut(mimeEl.value, tika.value.tika_type || tika.value.mime || [], { title: 'MIME' })
    if (hostEl.value) bubblePack(hostEl.value, props.facets.hostname || [])
    if (keysEl.value) treeMap(keysEl.value, tika.value.tika_keys || [])
    if (langEl.value) donut(langEl.value, tika.value.language || [], { title: 'lang' })
    if (encEl.value) donut(encEl.value, tika.value.encoding || [], { title: 'enc' })
    if (parserEl.value) bubblePack(parserEl.value, tika.value.parsers || [])
    if (rtEl.value) histogram(rtEl.value, tika.value.response_hist || [])
  })
}

watch(() => props.job, load)
watch(charts, async () => {
  await nextTick()
  draw()
})
watch(() => props.facets, async () => {
  await nextTick()
  draw()
}, { deep: true })
onMounted(load)
</script>
