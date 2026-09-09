<template>
  <div class="shell">
    <header class="mast">
      <div class="brand">
        <svg class="mark" viewBox="0 0 32 32" aria-hidden="true">
          <circle cx="16" cy="16" r="10.4" fill="none" stroke="currentColor" stroke-width="1.4" opacity="0.55"/>
          <circle cx="5.6" cy="16" r="1.5" fill="currentColor"/>
          <circle cx="8.7" cy="23.5" r="1.5" fill="currentColor"/>
          <circle cx="26.4" cy="16" r="1.5" fill="currentColor"/>
          <circle cx="23.3" cy="8.5" r="1.6" fill="currentColor"/>
          <path fill="currentColor" d="M16.00,6.80 L17.28,12.91 L22.51,9.49 L19.09,14.72 L25.20,16.00 L19.09,17.28 L22.51,22.51 L17.28,19.09 L16.00,25.20 L14.72,19.09 L9.49,22.51 L12.91,17.28 L6.80,16.00 L12.91,14.72 L9.49,9.49 L14.72,12.91 Z"/>
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
      <input v-model="seedBox" placeholder="https://example.com/  (add seed)"
             title="Each seed’s host is allowed when same host is on. Add ucla.edu pages here to crawl those sites too."/>
      <button :disabled="!job" class="tip" data-tip="Add another start URL. With same host on, that URL’s site is included (e.g. paste a cio.ucla.edu link to crawl UCLA too)." @click="addSeed">Add seed</button>
      <label class="hint tip" data-tip="Stay on the seed site. www.mattmann.ai and mattmann.ai count as one. This is how you crawl a whole site. Uncheck to follow off-site links (Wired, UCLA, GitHub), limited by depth.">
        <input type="checkbox" v-model="sameHost"/> same host
      </label>
      <label class="hint tip" data-tip="Link hops from a seed. 0 = homepage only. 1 = homepage plus its links. 3 = two more hops, enough to walk into ucla.edu and similar sites. Blank or −1 = no cap.">
        depth
        <input v-model.number="maxDepth" type="number" min="-1" max="20" placeholder="∞" style="width:3.6rem"/>
      </label>
      <label class="hint tip" data-tip="One iteration fetches one batch (50 URLs) then stops — that’s why you saw 1 fetched and 30 waiting. Check this to keep going until the frontier is empty.">
        <input type="checkbox" v-model="untilDone"/> until done
      </label>
      <label class="hint tip" data-tip="Fetch what is already queued. Do not add new outlinks, so the frontier only shrinks. Same-host off + until-done + don't expand drains NASA/Viterbi/etc. without following their links.">
        <input type="checkbox" v-model="noExpand"/> don't expand
      </label>
      <input v-if="!untilDone" v-model.number="iterations" type="number" min="1" max="500" style="width:4.5rem"
             class="tip" data-tip="How many fetch batches to run. Each batch is up to 50 URLs."/>
      <button v-if="!running" :disabled="!job" @click="startCrawl">Crawl</button>
      <button v-else class="danger-fill" @click="stopCrawl">{{ run.state === 'stopping' ? 'Stopping…' : 'Stop' }}</button>
      <button class="ghost" :disabled="!job" @click="download">Export</button>
      <button class="ghost danger tip" :disabled="!job || running" data-tip="Delete this job’s seeds, labels, and Solr pages. Other jobs stay."
              @click="clearJob()">Clear job</button>
      <button class="ghost danger tip" :disabled="running" data-tip="Empty the whole CrawlDB and every job."
              @click="clearAll">Clear all</button>
      <span v-if="run.url" class="hint">{{ run.url }}</span>
      <span v-if="run.state === 'error' && run.error" class="err">{{ shortErr }}</span>
    </section>

    <section class="chips" v-if="filterChips.length">
      <span class="hint">Documents filter</span>
      <button v-for="c in filterChips" :key="c.key" class="chip" @click="clearOneFilter(c.key)">{{ c.label }} ×</button>
      <button class="ghost" @click="clearDocFilter">Clear filters</button>
    </section>

    <section class="stats" v-if="stats">
      <div><strong>{{ stats.total || 0 }}</strong> urls</div>
      <div><strong>{{ fetched }}</strong> fetched</div>
      <div><strong>{{ unfetched }}</strong> frontier</div>
      <div><strong>{{ filtered }}</strong> filtered</div>
      <div><strong>{{ errors }}</strong> errors</div>
      <div><strong>{{ (stats.seeds || []).length }}</strong> seeds</div>
      <div><strong>{{ (facets.hostname || []).length }}</strong> hosts</div>
      <div v-if="topMime"><strong>{{ topMime }}</strong> top MIME</div>
      <div v-if="labelN"><strong>{{ labelN }}</strong> labels</div>
      <div v-if="stats && stats.model"><strong>{{ modelHint }}</strong> scorer</div>
    </section>

    <main>
      <div v-if="error" class="err">{{ error }}</div>
      <JobsView v-if="view === 'jobs'" :jobs="jobs" @open="openJob" @clear="id => clearJob(id)"/>
      <SeedsView v-else-if="view === 'seeds'" :seeds="(stats && stats.seeds) || []" @open="openSeed"/>
      <DocumentsView v-else-if="view === 'docs'" :documents="documents" :num="numFound"
                     :job="job" :q="q" @search="onSearch" @label="onLabel" @more="moreDocs"/>
      <FrontierView v-else-if="view === 'frontier'" :documents="frontier" :num="frontierN"/>
      <StatsView v-else-if="view === 'stats'" :job="job" :facets="facets" :on-filter="onStatFilter"/>
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
const untilDone = ref(true)
const sameHost = ref(true)
const noExpand = ref(false)
const maxDepth = ref(-1)
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
const filtered = computed(() => countOf('FILTERED'))
const running = computed(() => run.value.state === 'running' || run.value.state === 'stopping')
const docFilter = ref({ status: 'FETCHED' })
const filterChips = computed(() => {
  const f = docFilter.value || {}
  const out = []
  if (q.value) out.push({ key: 'q', label: `q:${q.value}` })
  if (f.status) out.push({ key: 'status', label: f.status })
  if (f.hostname) out.push({ key: 'hostname', label: f.hostname })
  if (f.content_type) out.push({ key: 'content_type', label: f.content_type })
  if (f.depth) out.push({ key: 'depth', label: `depth ${f.depth}` })
  return out
})
const topMime = computed(() => {
  const list = [...(facets.value.content_type || [])].sort((a, b) => b.count - a.count)
  if (!list.length) return ''
  const v = list[0].value || ''
  return v.split(';')[0]
})
const labelN = computed(() => Object.keys((stats.value && stats.value.labels) || {}).length)
const modelHint = computed(() => {
  const m = (stats.value && stats.value.model) || {}
  if (m.ok) return `${m.relevant} relevant / ${m.not} not`
  return 'needs relevant + not'
})
const shortErr = computed(() => {
  const e = (run.value && run.value.error) || ''
  return e.length > 180 ? `${e.slice(0, 180)}…` : e
})

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

async function refreshCounts() {
  await loadJobs()
  if (!job.value) {
    stats.value = null
    return
  }
  stats.value = await get(`/api/jobs/${encodeURIComponent(job.value)}/stats`)
  run.value = stats.value.run || {}
}

async function reload() {
  error.value = ''
  try {
    await refreshCounts()
    if (!job.value) {
      documents.value = []
      frontier.value = []
      return
    }
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
  const f = docFilter.value || {}
  if (f.status) params.set('status', f.status)
  if (f.hostname) params.set('hostname', f.hostname)
  if (f.content_type) params.set('content_type', f.content_type)
  if (f.depth) params.set('depth', f.depth)
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

function openSeed(url) {
  q.value = url
  docFilter.value = {}
  view.value = 'docs'
  start.value = 0
  loadDocs()
}

function onStatFilter(f) {
  hideTipSafe()
  q.value = f.q || ''
  docFilter.value = {
    status: f.status || '',
    hostname: f.hostname || '',
    content_type: f.content_type || '',
    depth: f.depth || ''
  }
  view.value = 'docs'
  start.value = 0
  loadDocs()
}

function hideTipSafe() {
  document.querySelectorAll('.spark-tip').forEach(n => { n.style.display = 'none' })
}

function clearDocFilter() {
  q.value = ''
  docFilter.value = {}
  start.value = 0
  loadDocs()
}

function clearOneFilter(key) {
  if (key === 'q') q.value = ''
  else docFilter.value = { ...docFilter.value, [key]: '' }
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
    const depth = maxDepth.value
    await send(`/api/jobs/${encodeURIComponent(job.value)}/crawl`, 'POST', {
      topn: 50,
      iterations: untilDone.value ? -1 : (Number(iterations.value) || 1),
      same_host: sameHost.value,
      expand: !noExpand.value,
      max_depth: depth === '' || depth == null ? -1 : Number(depth)
    })
    poll()
  } catch (e) { error.value = e.message }
}

async function stopCrawl() {
  if (!job.value) return
  try {
    await send(`/api/jobs/${encodeURIComponent(job.value)}/stop`, 'POST')
    poll()
  } catch (e) { error.value = e.message }
}

async function onLabel({ url, label }) {
  try {
    await send(`/api/jobs/${encodeURIComponent(job.value)}/label`, 'POST', { url, label })
    documents.value = documents.value.map(d => d.url === url ? { ...d, label } : d)
    await refreshCounts()
  } catch (e) { error.value = e.message }
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

async function clearJob(id) {
  const target = typeof id === 'string' && id ? id : job.value
  if (!target || running.value) return
  if (!confirm(`Delete job “${target}” and its catalog pages?`)) return
  try {
    await send(`/api/jobs/${encodeURIComponent(target)}`, 'DELETE')
    if (job.value === target) job.value = ''
    await reload()
  } catch (e) { error.value = e.message }
}

async function clearAll() {
  if (running.value) return
  if (!confirm('Delete every job and empty the Solr catalog?')) return
  try {
    await send('/api/catalog', 'DELETE')
    job.value = ''
    await reload()
  } catch (e) { error.value = e.message }
}

function poll() {
  if (timer) clearInterval(timer)
  timer = setInterval(async () => {
    if (!job.value) return
    try {
      await refreshCounts()
      if (run.value.state !== 'running' && run.value.state !== 'stopping') {
        clearInterval(timer)
        timer = null
        await reload()
      }
    } catch { /* ignore */ }
  }, 800)
}

watch(view, () => { if (job.value) reload() })

onMounted(async () => {
  const u = new URL(location.href)
  if (u.searchParams.get('job')) job.value = u.searchParams.get('job')
  await reload()
  if (running.value) poll()
})
onUnmounted(() => { if (timer) clearInterval(timer) })
</script>
