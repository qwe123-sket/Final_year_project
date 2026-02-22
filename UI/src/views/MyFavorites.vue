<template>
  <div class="my-favorites">
    <div class="page-header">
      <h1>My favorites</h1>
      <div class="sort-group">
        <el-select v-model="sortBy" size="small" style="width: 130px" @change="load">
          <el-option label="Newest" value="newest" />
          <el-option label="Most viewed" value="views" />
        </el-select>
      </div>
    </div>

    <div v-if="loading" class="masonry">
      <NoteCardSkeleton v-for="i in 6" :key="i" class="masonry-item animate-in" :class="`stagger-${(i % 6) + 1}`" />
    </div>

    <div v-else class="masonry">
      <article
        v-for="(n, i) in sortedList"
        :key="n.id"
        class="note-card masonry-item animate-in"
        :class="`stagger-${(i % 6) + 1}`"
        @click="$router.push(`/note/${n.id}`)"
      >
        <div class="note-card-cover" :style="coverStyle(n)"></div>
        <div class="note-card-body">
          <h3 class="note-card-title">{{ n.title }}</h3>
          <div class="note-card-footer">
            <span class="note-card-author">{{ n.authorName || 'Anonymous' }}</span>
            <span class="note-card-meta"><el-icon><View /></el-icon> {{ formatCount(n.viewCount) }}</span>
          </div>
        </div>
      </article>
      <EmptyState
        v-if="sortedList.length === 0"
        class="masonry-item masonry-full"
        title="No favorites yet"
        description="Browse notes and save your favorites here."
      />
    </div>

    <div v-if="total > 12" class="pagination-wrap">
      <el-pagination
        v-model:current-page="page"
        :page-size="12"
        :total="total"
        layout="prev, pager, next"
        @current-change="load"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { myFavorites } from '@/api/favorite'
import { View } from '@element-plus/icons-vue'
import NoteCardSkeleton from '@/components/NoteCardSkeleton.vue'
import EmptyState from '@/components/EmptyState.vue'

const list = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const sortBy = ref('newest')

const sortedList = computed(() => {
  const items = [...list.value]
  if (sortBy.value === 'views') {
    items.sort((a, b) => (b.viewCount || 0) - (a.viewCount || 0))
  }
  return items
})

function formatCount(v) {
  const n = Number(v) || 0
  if (n >= 10000) return (n / 10000).toFixed(1).replace(/\.0$/, '') + 'w'
  if (n >= 1000) return (n / 1000).toFixed(1).replace(/\.0$/, '') + 'k'
  return String(n)
}

function coverStyle(n) {
  if (n.coverImage) {
    return {
      backgroundImage: `url(${n.coverImage})`,
      backgroundSize: 'cover',
      backgroundPosition: 'center',
    }
  }
  const hue = (Number(n.id) * 137.508) % 360
  return {
    background: `linear-gradient(135deg, hsl(${hue}, 45%, 88%) 0%, hsl(${hue}, 35%, 78%) 100%)`,
  }
}

async function load() {
  loading.value = true
  try {
    const res = await myFavorites({ page: page.value - 1, size: 12 })
    list.value = res.list || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.my-favorites {
  max-width: 1200px;
  margin: 0 auto;
  padding-bottom: 60px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: 1.5rem;
}

.masonry {
  column-count: 3;
  column-gap: 20px;
}

@media (max-width: 900px) { .masonry { column-count: 2; } }
@media (max-width: 520px) { .masonry { column-count: 1; } }

.masonry-item {
  break-inside: avoid;
  margin-bottom: 20px;
}

.masonry-full {
  column-span: all;
}

.note-card {
  background: var(--color-surface);
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: var(--color-shadow);
  cursor: pointer;
  transition: transform var(--transition-slow), box-shadow var(--transition-slow);
  border: 1px solid var(--color-border);
}

.note-card:hover {
  transform: translateY(-6px) scale(1.015);
  box-shadow: 0 16px 48px -12px rgba(0,0,0,0.15);
}

.note-card-cover {
  height: 120px;
  width: 100%;
  transition: filter var(--transition-slow);
}

.note-card:hover .note-card-cover {
  filter: brightness(1.05);
}

.note-card-body {
  padding: 16px;
}

.note-card-title {
  font-size: 1.02rem;
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.note-card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 0.8rem;
  color: var(--color-text-muted);
}

.note-card-author {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 60%;
}

.note-card-meta {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}
</style>
