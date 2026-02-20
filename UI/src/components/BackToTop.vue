<template>
  <transition name="btt">
    <button v-if="visible" class="back-to-top" @click="scrollToTop" title="Back to top">
      <el-icon><ArrowUp /></el-icon>
    </button>
  </transition>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ArrowUp } from '@element-plus/icons-vue'

const visible = ref(false)

function onScroll() {
  visible.value = (document.documentElement.scrollTop || document.body.scrollTop) > 300
}

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => window.addEventListener('scroll', onScroll, { passive: true }))
onBeforeUnmount(() => window.removeEventListener('scroll', onScroll))
</script>

<style scoped>
.back-to-top {
  position: fixed;
  bottom: 96px;
  right: 28px;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: 1px solid var(--color-border);
  background: var(--color-bg-elevated);
  color: var(--color-text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: var(--color-shadow-lg);
  transition: color var(--transition), background var(--transition), transform var(--transition);
  font-size: 1.2rem;
  z-index: 90;
}

.back-to-top:hover {
  color: var(--color-primary);
  border-color: var(--color-primary);
  transform: translateY(-2px);
}

.btt-enter-active,
.btt-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.btt-enter-from,
.btt-leave-to {
  opacity: 0;
  transform: translateY(12px);
}
</style>
