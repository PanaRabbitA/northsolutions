<template>
  <div class="status-card">
    <div class="icon"><i :class="icon"></i></div>
    <div>
      <span>{{ label }}</span>
      <strong>{{ value }}</strong>
      <small>{{ detail }}</small>
    </div>
  </div>
</template>

<script setup>
defineProps({
  icon: { type: String, required: true },
  label: { type: String, required: true },
  value: { type: [String, Number], required: true },
  detail: { type: String, default: '' }
})
</script>
