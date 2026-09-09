<template>
  <div>
    <p class="hint">{{ num }} unfetched URLs, highest score first. Fetched-page scores live on Documents.</p>
    <p v-if="!documents.length" class="empty">Frontier is empty — the queue is drained. Open Documents to see scores on fetched pages.</p>
    <div v-for="d in documents" :key="d.id" class="card">
      <h3>{{ d.url }}</h3>
      <p class="meta">{{ d.hostname }} · depth {{ d.discover_depth }} · score {{ Number(d.page_score || 0).toFixed(2) }}</p>
    </div>
  </div>
</template>
<script setup>
defineProps({
  documents: { type: Array, default: () => [] },
  num: { type: Number, default: 0 }
})
</script>
