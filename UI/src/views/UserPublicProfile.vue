<template>
  <div class="user-public">
    <div v-if="profile" class="profile-header-card">
      <div class="profile-avatar" :style="avatarStyle">
        {{ avatarLetter }}
      </div>
      <div class="profile-identity">
        <h1>{{ profile.nickname || profile.username }}</h1>
        <div class="profile-badges">
          <span class="badge badge-role">{{ profile.role || 'USER' }}</span>
          <span class="badge badge-member" v-if="memberSince">Member since {{ memberSince }}</span>
        </div>
      </div>
    </div>

    <div class="notes-panel">
      <div class="notes-title-row">
        <h2>Published notes</h2>
      </div>

      <div v-if="notesLoading" class="masonry">
        <NoteCardSkeleton v-for="i in 9" :key="i" class="masonry-item animate-in" :class="`stagger-${(i % 6) + 1}`" />
      </div>

      <template v-else>
        <div v-if="notes.length > 0" class="masonry">
          <article
            v-for="(n, i) in notes"
            :key="n.id"
            class="note-card masonry-item animate-in"
            :class="`stagger-${(i % 6) + 1}`"
            @click="goNote(n.id)"
          >
            <div class="note-card-cover" :style="coverStyle(n)"></div>
            <div class="note-card-body">
              <h3 class="note-card-title">{{ n.title }}</h3>
              <p v-if="n.content" class="note-card-excerpt">{{ excerpt(n.content) }}</p>
              <div class="note-card-footer">
                <span class="note-card-author">{{ n.authorName || 'Anonymous' }}</span>
                <span class="note-card-meta">
                  <el-icon><View /></el-icon> {{ formatCount(n.viewCount) }}
                </span>
              </div>
            </div>
          </article>
        </div>

        <EmptyState
          v-else
          class="masonry-item masonry-full"
          title="No published notes"
          description="This user has not published notes yet."
        />
      </template>

      <div v-if="total > pageSize" class="pagination-wrap">
        <el-pagination
          v-model:current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="loadNotes"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { View } from '@element-plus/icons-vue'
import { getPublicProfile as apiGetPublicProfile, getPublicUserNotes as apiGetPublicUserNotes } from '@/api/user'
import NoteCardSkeleton from '@/components/NoteCardSkeleton.vue'
import EmptyState from '@/components/EmptyState.vue'

const route = useRoute()
const router = useRouter()

const pageSize = 12
const page = ref(1)
const total = ref(0)
const notes = ref([])
const notesLoading = ref(false)

const profile = ref(null)

const memberSince = computed(() => {
  const dt = profile.value?.createdAt
  if (!dt) return ''
  return new Date(dt).toLocaleDateString('en-US', { month: 'short', year: 'numeric' })
})

const avatarLetter = computed(() => {
  const name = profile.value?.nickname || profile.value?.username || '?'
  return String(name).slice(0, 1).toUpperCase()
})

const avatarStyle = computed(() => {
  const name = profile.value?.username || 'user'
  let hash = 0
  for (let i = 0; i < name.length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash)
  const hue = ((hash % 360) + 360) % 360
  return {
    background: `linear-gradient(135deg, hsl(${hue}, 55%, 55%), hsl(${(hue + 40) % 360}, 50%, 45%))`,
  }
})

function excerpt(text) {
  if (!text || typeof text !== 'string') return ''
  const cleaned = text.replace(/\s+/g, ' ').trim()
  return cleaned.length > 120 ? cleaned.slice(0, 120) + '…' : cleaned
}

function formatCount(val) {
  const num = Number(val) || 0
  if (num >= 10000) return (num / 10000).toFixed(1).replace(/\.0$/, '') + 'w'
  if (num >= 1000) return (num / 1000).toFixed(1).replace(/\.0$/, '') + 'k'
  return String(num)
}

function coverStyle(n) {
  const cover = n.coverImage
  if (cover) {
    return {
      backgroundImage: `url(${cover})`,
      backgroundSize: 'cover',
      backgroundPosition: 'center',
    }
  }
  const noteId = n.id
  const hue = (Number(noteId) * 137.508) % 360
  return {
    background: `linear-gradient(135deg, hsl(${hue}, 45%, 88%) 0%, hsl(${hue}, 35%, 78%) 100%)`,
  }
}

function goNote(id) {
  router.push('/note/' + id)
}

async function loadProfile() {
  const id = route.params.id
  profile.value = await apiGetPublicProfile(id)
}

async function loadNotes() {
  notesLoading.value = true
  try {
    const id = route.params.id
    const res = await apiGetPublicUserNotes(id, { page: page.value - 1, size: pageSize })
    notes.value = res.list || []
    total.value = res.total || 0
  } finally {
    notesLoading.value = false
  }
}

onMounted(async () => {
  await loadProfile()
  await loadNotes()
})

watch(() => route.params.id, async () => {
  page.value = 1
  await loadProfile()
  await loadNotes()
})
</script>

<style scoped>
.user-public {
  max-width: 900px;
  margin: 0 auto;
  padding-bottom: 80px;
}

.profile-header-card {
  display: flex;
  align-items: center;
  gap: 24px;
  background: var(--color-bg-card);
  border-radius: var(--radius);
  box-shadow: var(--color-shadow);
  border: 1px solid var(--color-border);
  padding: 28px 32px;
  margin-bottom: 24px;
}

.profile-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 2rem;
  font-weight: 700;
  font-family: var(--font-heading);
  flex-shrink: 0;
}

.profile-identity h1 {
  font-size: 1.5rem;
  margin-bottom: 8px;
}

.profile-badges {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: var(--radius-full);
  font-size: 0.78rem;
  font-weight: 500;
}

.badge-role {
  background: var(--color-primary-muted);
  color: var(--color-primary);
}

.badge-member {
  background: var(--color-border);
  color: var(--color-text-secondary);
}

.notes-panel {
  background: var(--color-bg-card);
  border-radius: var(--radius);
  box-shadow: var(--color-shadow);
  border: 1px solid var(--color-border);
  padding: 22px 24px;
}

.notes-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.notes-title-row h2 {
  margin: 0;
  font-size: 1.1rem;
  color: var(--color-text);
  font-weight: 700;
}

.masonry {
  column-count: 3;
  column-gap: 18px;
}

@media (max-width: 900px) {
  .masonry {
    column-count: 2;
  }
}

@media (max-width: 520px) {
  .masonry {
    column-count: 1;
  }
}

.masonry-item {
  break-inside: avoid;
  margin-bottom: 18px;
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
  border: 1px solid var(--color-border);
  transition: transform var(--transition), border-color var(--transition);
}

.note-card:hover {
  transform: translateY(-4px);
  border-color: var(--color-primary-muted);
}

.note-card-cover {
  height: 110px;
  width: 100%;
}

.note-card-body {
  padding: 14px 16px;
}

.note-card-title {
  font-size: 1.02rem;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.35;
}

.note-card-excerpt {
  font-size: 0.86rem;
  color: var(--color-text-secondary);
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 2.4em;
}

.note-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.8rem;
  color: var(--color-text-muted);
}

.note-card-author {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 70%;
}

.note-card-meta {
  display: flex;
  gap: 6px;
  align-items: center;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 18px;
}
</style>

