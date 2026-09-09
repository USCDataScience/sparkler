<template>
  <div>
    <section class="bar" style="padding:0 0 0.8rem;border:0;background:transparent">
      <input v-model="text" placeholder="Search text" @keyup.enter="$emit('search', text)"/>
      <button class="ghost" @click="$emit('search', text)">Search</button>
    </section>
    <p v-if="!documents.length" class="empty">No documents. Inject seeds and crawl.</p>
    <div v-for="d in documents" :key="d.id" class="card">
      <div class="row">
        <div>
          <h3>{{ d.title || d.url }}</h3>
          <p class="meta">{{ d.status }} · d{{ d.discover_depth }} · {{ d.hostname }} · score {{ Number(d.page_score || 0).toFixed(2) }}</p>
          <p class="meta"><a :href="d.url" target="_blank" rel="noreferrer">{{ d.url }}</a></p>
          <p v-if="d.snippet" class="snippet">{{ d.snippet }}</p>
        </div>
        <div class="labels">
          <button :class="{ on: d.label === 'not' }" @click="$emit('label', { url: d.url, label: 'not' })">not</button>
          <button :class="{ on: d.label === 'relevant' }" @click="$emit('label', { url: d.url, label: 'relevant' })">relevant</button>
          <button :class="{ on: d.label === 'highly' }" @click="$emit('label', { url: d.url, label: 'highly' })">highly</button>
        </div>
      </div>
    </div>
    <button v-if="documents.length < num" class="ghost" @click="$emit('more')">Load more</button>
  </div>
</template>
<script setup>
import { ref } from 'vue'
defineProps({
  documents: { type: Array, default: () => [] },
  num: { type: Number, default: 0 },
  q: { type: String, default: '' }
})
defineEmits(['search', 'label', 'more'])
const text = ref('')
</script>
