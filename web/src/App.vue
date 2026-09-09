<template>
  <div class="shell">
    <header class="mast">
      <div class="brand">
        <svg class="mark" viewBox="0 0 32 32" aria-hidden="true">
          <path fill="currentColor" d="M16 2l3 10 10 3-10 3-3 10-3-10-10-3 10-3z"/>
        </svg>
        <div>
          <h1>Sparkler</h1>
          <p>view · control · crawl</p>
        </div>
      </div>
      <nav>
        <button v-for="v in views" :key="v.id" :class="{ active: view === v.id }" @click="view = v.id">{{ v.label }}</button>
      </nav>
    </header>

    <section class="bar">
      <select v-model="job" @change="reload">
        <option value="">Job</option>
        <option v-for="j in jobs" :key="j.id" :value="j.id">{{ j.id }}</option>
      </select>
      <input v-model="newJob" placeholder="new job id" @keyup.enter="createJob"/>
      <button @click="createJob">Create</button>
      <input v-model="seedBox" placeholder="https://example.com/  (add seed)"/>
      <button :disabled="!job" @click="addSeed">Add seed</button>
      <input v-model.number="iterations" type="number" min="1" max="20" style="width:4.5rem" title="iterations"/>
      <label class="hint"><input type="checkbox" v-model="sameHost"/> same host</label>
      <button :disabled="!job || running" @click="startCrawl">{{ running ? 'Crawling…' : 'Crawl' }}</button>
      <button class="ghost" :disabled="!job" @click="download">Export</button>
      <span v-if="run.url" class="hint">{{ run.url }}</span>
    </section>

    <section class="stats" v-if="stats">
      <div><strong>{{ stats.total || 0 }}</strong> urls</div>
      <div><strong>{{ fetched }}</strong> fetched</div>
      <div><strong>{{ unfetched }}</strong> frontier</div>
      <div><strong>{{ errors }}</strong> errors</div>
      <div><strong>{{ (stats.seeds || []).length }}</strong> seeds</div>
    </section>

    <main>
      <div v-if="error" class="err">{{ error }}</div>
      <JobsView v-if="view === 'jobs'" :jobs="jobs" @open="openJob"/>
      <SeedsView v-else-if="view === 'seeds'" :seeds="(stats && stats.seeds) || []"/>
      <DocumentsView v-else-if="view === 'docs'" :documents="documents" :num="numFound"
                     :q="q" @search="onSearch" @label="onLabel" @more="moreDocs"/>
      <FrontierView v-else-if="view === 'frontier'" :documents="frontier" :num="frontierN"/>
      <StatsView v-else-if="view === 'stats'" :facets="facets"/>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { exportUrl, get, send } from './api.js'
import JobsView from './views/JobsView.vue'
import SeedsView from './views/SeedsView.vue'
import DocumentsView from './views/DocumentsView.vue'
import FrontierView from './views/FrontierView.vue'
import StatsView from './views/StatsView.vue'

const views = [
  { id: 'jobs', label: 'Jobs' },
  { id: 'seeds', label: 'Seeds' },
  { id: 'docs', label: 'Documents' },
  { id: 'frontier', label: 'Frontier' },
  { id: 'stats', label: 'Stats' }
]
const view = ref('jobs')
const jobs = ref([])
const job = ref('')
const newJob = ref('')
const seedBox = ref('')
const iterations = ref(1)
const sameHost = ref(false)
const stats = ref(null)
const documents = ref([])
const numFound = ref(0)
const frontier = ref([])
const frontierN = ref(0)
const q = ref('')
const error = ref('')
const run = ref({})
const start = ref(0)
let timer = null

const facets = computed(() => (stats.value && stats.value.facets) || {})
const fetched = computed(() => countOf('FETCHED'))
const unfetched = computed(() => countOf('UNFETCHED'))
const errors = computed(() => countOf('ERROR'))
const running = computed(() => run.value.state === 'running')

function countOf(status) {
  const list = (facets.value.status || [])
  const hit = list.find(x => x.value === status)
  return hit ? hit.count : 0
}

async function loadJobs() {
  const data = await get('/api/jobs')
  jobs.value = data.jobs || []
  if (!job.value && jobs.value.length) job.value = jobs.value[0].id
}

async function reload() {
  error.value = ''
  try {
    await loadJobs()
    if (!job.value) {
      stats.value = null
      documents.value = []
      frontier.value = []
      return
    }
    stats.value = await get(`/api/jobs/${encodeURIComponent(job.value)}/stats`)
    run.value = stats.value.run || {}
    start.value = 0
    await loadDocs()
    const fr = await get(`/api/jobs/${encodeURIComponent(job.value)}/frontier?rows=50`)
    frontier.value = fr.documents || []
    frontierN.value = fr.numFound || 0
  } catch (e) {
    error.value = e.message
  }
}

async function loadDocs() {
  if (!job.value) return
  const params = new URLSearchParams({ start: String(start.value), rows: '25' })
  if (q.value) params.set('q', q.value)
  const data = await get(`/api/jobs/${encodeURIComponent(job.value)}/documents?${params}`)
  if (start.value === 0) documents.value = data.documents || []
  else documents.value = documents.value.concat(data.documents || [])
  numFound.value = data.numFound || 0
}

function moreDocs() {
  start.value += 25
  loadDocs()
}

function onSearch(text) {
  q.value = text
  start.value = 0
  loadDocs()
}

async function createJob() {
  const id = (newJob.value || '').trim()
  if (!id) return
  try {
    await send('/api/jobs', 'POST', { id })
    job.value = id
    newJob.value = ''
    await reload()
    view.value = 'seeds'
  } catch (e) { error.value = e.message }
}

async function addSeed() {
  const url = seedBox.value.trim()
  if (!url || !job.value) return
  try {
    await send(`/api/jobs/${encodeURIComponent(job.value)}/seeds`, 'POST', { urls: [url] })
    seedBox.value = ''
    await reload()
  } catch (e) { error.value = e.message }
}

async function startCrawl() {
  if (!job.value) return
  try {
    await send(`/api/jobs/${encodeURIComponent(job.value)}/crawl`, 'POST', {
      topn: 50,
      iterations: Number(iterations.value) || 1,
      same_host: sameHost.value
    })
    poll()
  } catch (e) { error.value = e.message }
}

async function onLabel({ url, label }) {
  await send(`/api/jobs/${encodeURIComponent(job.value)}/label`, 'POST', { url, label })
  await send(`/api/jobs/${encodeURIComponent(job.value)}/train`, 'POST')
  await reload()
}

function openJob(id) {
  job.value = id
  view.value = 'docs'
  reload()
}

function download() {
  if (!job.value) return
  window.location = exportUrl(job.value)
}

function poll() {
  if (timer) clearInterval(timer)
  timer = setInterval(async () => {
    if (!job.value) return
    try {
      run.value = await get(`/api/jobs/${encodeURIComponent(job.value)}/run`)
      if (run.value.state !== 'running') {
        clearInterval(timer)
        timer = null
        await reload()
      }
    } catch { /* ignore */ }
  }, 800)
}

watch(view, () => { if (job.value) reload() })

onMounted(() => {
  const u = new URL(location.href)
  if (u.searchParams.get('job')) job.value = u.searchParams.get('job')
  reload()
})
onUnmounted(() => { if (timer) clearInterval(timer) })
</script>
