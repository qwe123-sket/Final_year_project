<template>
  <div class="empty-state">
    <div class="empty-icon">
      <slot name="icon">
        <el-icon :size="64"><Document /></el-icon>
      </slot>
    </div>
    <p class="empty-title">{{ title }}</p>
    <p class="empty-desc">{{ description }}</p>
    <slot name="action"></slot>
  </div>
</template>

<script setup>
import { Document } from '@element-plus/icons-vue'

defineProps({
  title: { type: String, default: 'Nothing here yet' },
  description: { type: String, default: '' },
})
</script>

<style scoped>
.empty-state {
  text-align: center;
  padding: 48px 24px;
}

.empty-icon {
  color: var(--color-text-muted);
  margin-bottom: 16px;
  opacity: 0.7;
}

.empty-title {
  font-family: var(--font-heading);
  font-size: 1.25rem;
  color: var(--color-text);
  margin-bottom: 8px;
}

.empty-desc {
  font-size: 0.95rem;
  color: var(--color-text-secondary);
  max-width: 320px;
  margin: 0 auto 20px;
  line-height: 1.5;
}
</style>
