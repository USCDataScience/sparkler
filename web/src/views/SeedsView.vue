<template>
  <div>
    <p v-if="!seeds.length" class="empty">No seeds yet. Paste a URL in the bar and Add seed.</p>
    <div v-for="s in seeds" :key="s" class="card">
      <p class="meta">{{ s }}</p>
    </div>
  </div>
</template>
<script setup>
defineProps({ seeds: { type: Array, default: () => [] } })
</script>
