<template>
  <div class="skeleton-card">
    <div class="skeleton-cover"></div>
    <div class="skeleton-body">
      <div class="skeleton-line title"></div>
      <div class="skeleton-line subtitle"></div>
      <div class="skeleton-line meta"></div>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.skeleton-card {
  background: var(--color-surface);
  border-radius: var(--radius);
  box-shadow: var(--color-shadow);
  border: 1px solid var(--color-border);
  overflow: hidden;
}

.skeleton-cover {
  height: 120px;
  background: var(--color-skeleton);
  animation: shimmer 1.4s ease-in-out infinite;
  background: linear-gradient(90deg, var(--color-skeleton) 25%, var(--color-skeleton-shine) 50%, var(--color-skeleton) 75%);
  background-size: 200% 100%;
}

.skeleton-body {
  padding: 16px;
}

.skeleton-line {
  height: 1rem;
  border-radius: 6px;
  background: linear-gradient(90deg, var(--color-skeleton) 25%, var(--color-skeleton-shine) 50%, var(--color-skeleton) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s ease-in-out infinite;
}

.skeleton-line.title {
  width: 85%;
  height: 1.1rem;
  margin-bottom: 12px;
}

.skeleton-line.subtitle {
  width: 60%;
  height: 0.85rem;
  margin-bottom: 16px;
}

.skeleton-line.meta {
  width: 35%;
  height: 0.75rem;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
</style>
