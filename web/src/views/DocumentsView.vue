<template>
  <div>
    <section class="bar" style="padding:0 0 0.8rem;border:0;background:transparent">
      <input v-model="text" placeholder="Search text" @keyup.enter="$emit('search', text)"/>
      <button class="ghost" @click="$emit('search', text)">Search</button>
    </section>
    <p class="hint">Highest relevance score first. Labeling stays on this card; scores update on the next crawl.</p>
    <p v-if="!documents.length" class="empty">No documents. Inject seeds and crawl.</p>
    <div v-for="d in documents" :key="d.id" class="card" :class="{ open: openUrl === d.url }">
      <div class="row">
        <div class="click" @click="toggle(d)">
          <h3>{{ d.title || d.url }}</h3>
          <p class="meta">{{ d.status }} · d{{ d.discover_depth }} · {{ d.hostname }} · {{ d.content_type || '—' }}
            · {{ d.metadata_n || 0 }} Tika fields
            · <span class="score" :class="{ hi: Number(d.page_score) > 0, lo: Number(d.page_score) < 0 }">score {{ Number(d.page_score || 0).toFixed(2) }}</span></p>
          <p class="meta"><a :href="d.url" target="_blank" rel="noreferrer" @click.stop>{{ d.url }}</a></p>
          <p v-if="d.snippet" class="snippet">{{ d.snippet }}</p>
        </div>
        <div class="labels" @click.stop>
          <button :class="{ on: d.label === 'not' }" @click="$emit('label', { url: d.url, label: 'not' })">not</button>
          <button :class="{ on: d.label === 'relevant' }" @click="$emit('label', { url: d.url, label: 'relevant' })">relevant</button>
          <button :class="{ on: d.label === 'highly' }" @click="$emit('label', { url: d.url, label: 'highly' })">highly</button>
        </div>
      </div>
      <div v-if="openUrl === d.url" class="detail">
        <p v-if="detailErr" class="err">{{ detailErr }}</p>
        <p v-else-if="!detail" class="hint">Loading Tika metadata…</p>
        <template v-else>
          <h4>Tika metadata</h4>
          <p v-if="!metaRows.length" class="hint">No Tika metadata on this page. Re-crawl after the schema update.</p>
          <table v-else class="kv">
            <tbody>
              <tr v-for="row in metaRows" :key="row[0]">
                <th>{{ row[0] }}</th>
                <td>{{ row[1] }}</td>
              </tr>
            </tbody>
          </table>
          <h4>Extracted text</h4>
          <pre class="text">{{ detail.extracted_text || '—' }}</pre>
        </template>
      </div>
    </div>
    <button v-if="documents.length < num" class="ghost" @click="$emit('more')">Load more</button>
  </div>
</template>
<script setup>
import { computed, ref } from 'vue'
import { get } from '../api.js'

const props = defineProps({
  documents: { type: Array, default: () => [] },
  num: { type: Number, default: 0 },
  q: { type: String, default: '' },
  job: { type: String, default: '' }
})
defineEmits(['search', 'label', 'more'])
const text = ref('')
const openUrl = ref('')
const detail = ref(null)
const detailErr = ref('')

const metaRows = computed(() => {
  const md = (detail.value && detail.value.metadata) || {}
  return Object.keys(md).sort().map(k => {
    const v = md[k]
    return [k, Array.isArray(v) ? v.join(', ') : String(v)]
  })
})

async function toggle(d) {
  if (openUrl.value === d.url) {
    openUrl.value = ''
    detail.value = null
    return
  }
  openUrl.value = d.url
  detail.value = null
  detailErr.value = ''
  try {
    const q = new URLSearchParams({ url: d.url })
    detail.value = await get(`/api/jobs/${encodeURIComponent(props.job)}/page?${q}`)
  } catch (e) {
    detailErr.value = e.message
  }
}
</script>
