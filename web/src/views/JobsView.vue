<template>
  <div>
    <p v-if="!jobs.length" class="empty">Create a job, add seeds, then Crawl.</p>
    <div v-for="j in jobs" :key="j.id" class="card" @click="$emit('open', j.id)" style="cursor:pointer">
      <div class="row">
        <div>
          <h3>{{ j.id }}</h3>
          <p class="meta">{{ j.total }} urls · {{ j.fetched }} fetched · {{ j.unfetched }} frontier · {{ j.seeds }} seeds
            <span v-if="j.running"> · running</span>
          </p>
        </div>
        <button class="ghost danger" :disabled="j.running" @click.stop="$emit('clear', j.id)">Clear</button>
      </div>
    </div>
  </div>
</template>
<script setup>
defineProps({ jobs: { type: Array, default: () => [] } })
defineEmits(['open', 'clear'])
</script>
