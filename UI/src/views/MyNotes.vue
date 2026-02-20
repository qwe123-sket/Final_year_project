<template>
  <div class="my-notes">
    <div class="page-header">
      <h1>My notes</h1>
      <el-button type="primary" @click="$router.push('/note/edit')">
        <el-icon><Plus /></el-icon>
        Publish note
      </el-button>
    </div>

    <div class="controls-bar">
      <div class="filter-group">
        <button
          v-for="f in statusFilters"
          :key="f.value"
          class="filter-btn"
          :class="{ active: statusFilter === f.value }"
          @click="statusFilter = f.value; load()"
        >{{ f.label }}</button>
      </div>
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
        @click="goNote(n.id)"
      >
        <div class="note-card-cover" :style="coverStyle(n)">
          <el-tag v-if="n.status === 'PENDING'" type="warning" size="small" class="status-tag">Pending</el-tag>
          <el-tag v-else-if="n.status === 'REJECTED'" type="danger" size="small" class="status-tag">Rejected</el-tag>
          <el-tag v-else type="success" size="small" class="status-tag">Published</el-tag>
        </div>
        <div class="note-card-body">
          <h3 class="note-card-title">{{ n.title }}</h3>
          <div class="note-card-footer">
            <span class="note-card-meta"><el-icon><View /></el-icon> {{ formatCount(n.viewCount) }}</span>
            <el-button link size="small" class="edit-link" @click.stop="$router.push(`/note/edit/${n.id}`)">
              <el-icon><Edit /></el-icon> Edit
            </el-button>
          </div>
        </div>
      </article>
      <EmptyState
        v-if="sortedList.length === 0"
        class="masonry-item masonry-full"
        title="No notes yet"
        description="Create your first note and share your knowledge."
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
import { useRouter } from 'vue-router'
import { myNotes } from '@/api/note'
import { View, Plus, Edit } from '@element-plus/icons-vue'
import NoteCardSkeleton from '@/components/NoteCardSkeleton.vue'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()
const list = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const statusFilter = ref('all')
const sortBy = ref('newest')

const statusFilters = [
  { label: 'All', value: 'all' },
  { label: 'Published', value: 'APPROVED' },
  { label: 'Pending', value: 'PENDING' },
  { label: 'Rejected', value: 'REJECTED' },
]

const sortedList = computed(() => {
  let items = [...list.value]
  if (statusFilter.value !== 'all') {
    items = items.filter(n => n.status === statusFilter.value)
  }
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
  const hue = (Number(n.id) * 137.508) % 360
  return {
    background: `linear-gradient(135deg, hsl(${hue}, 45%, 88%) 0%, hsl(${hue}, 35%, 78%) 100%)`,
  }
}

async function load() {
  loading.value = true
  try {
    const res = await myNotes({ page: page.value - 1, size: 12 })
    list.value = res.list || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function goNote(id) {
  router.push(`/note/${id}`)
}

onMounted(load)
</script>

<style scoped>
.my-notes {
  max-width: 1200px;
  margin: 0 auto;
  padding-bottom: 60px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
  font-size: 1.5rem;
}

.controls-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 12px;
}

.filter-group {
  display: flex;
  gap: 6px;
}

.filter-btn {
  padding: 6px 14px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-full);
  background: transparent;
  color: var(--color-text-secondary);
  font-size: 0.85rem;
  font-family: inherit;
  cursor: pointer;
  transition: all var(--transition);
}

.filter-btn:hover {
  color: var(--color-primary);
  border-color: var(--color-primary);
}

.filter-btn.active {
  background: var(--color-primary);
  color: #fff;
  border-color: var(--color-primary);
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
  height: 100px;
  width: 100%;
  position: relative;
}

.status-tag {
  position: absolute;
  top: 10px;
  right: 10px;
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

.note-card-meta {
  display: flex;
  align-items: center;
  gap: 4px;
}

.edit-link {
  font-size: 0.8rem;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}
</style>
