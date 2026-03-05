<template>
  <div class="home">
    <section class="home-head">
      <h1 class="home-head-title">Discover & share notes</h1>
      <p class="home-head-sub">Recommendations for you, or browse the latest from the community.</p>
    </section>

    <div class="category-bar">
      <button
        v-for="tab in tabs"
        :key="tab.name"
        type="button"
        class="category-tab"
        :class="{ active: activeTab === tab.name }"
        @click="activeTab = tab.name"
      >
        {{ tab.label }}
      </button>
    </div>

    <div v-if="recommendLoading || latestLoading" class="masonry">
      <NoteCardSkeleton v-for="i in 9" :key="i" class="masonry-item animate-in" :class="`stagger-${(i % 6) + 1}`" />
    </div>

    <template v-else>
      <div v-if="activeTab === 'recommend'" class="masonry">
        <article
          v-for="(n, i) in recommendList"
          :key="n.noteId"
          class="note-card masonry-item animate-in"
          :class="`stagger-${(i % 6) + 1}`"
          @click="goNote(n.noteId)"
        >
          <div class="note-card-cover" :style="coverStyle(n)"></div>
          <div class="note-card-body">
            <div v-if="n.recallSource && n.recallSource !== 'default'" class="recommend-badges">
              <span class="recommend-tag" :class="n.recallSource">
                {{ n.recallSource === 'vector' ? 'Guess You Like' : (n.recallSource === 'popular' ? 'Trending' : 'Recommend') }}
              </span>
            </div>
            <h3 class="note-card-title">{{ n.title }}</h3>
            <p v-if="n.content" class="note-card-excerpt">{{ excerpt(n.content) }}</p>
            <div class="note-card-footer">
              <span class="note-card-author">{{ n.authorName || 'Anonymous' }}</span>
              <span class="note-card-meta"><el-icon><View /></el-icon> {{ formatCount(n.viewCount) }}</span>
            </div>
          </div>
        </article>
        <EmptyState
          v-if="recommendList.length === 0"
          class="masonry-item masonry-full"
          title="No recommendations yet"
          description="Start reading and liking notes to get personalized picks."
        />
      </div>

      <div v-else class="masonry">
        <article
          v-for="(n, i) in latestList"
          :key="n.id"
          class="note-card masonry-item animate-in"
          :class="`stagger-${(i % 6) + 1}`"
          @click="goNote(n.id)"
        >
          <div class="note-card-cover" :style="coverStyle(n)"></div>
          <div class="note-card-body">
            <h3 class="note-card-title">{{ n.title }}</h3>
            <p class="note-card-excerpt">{{ excerpt(n.content) }}</p>
            <div class="note-card-footer">
              <span class="note-card-author">{{ n.authorName || 'Anonymous' }}</span>
              <span class="note-card-meta"><el-icon><View /></el-icon> {{ formatCount(n.viewCount) }}</span>
            </div>
          </div>
        </article>
        <EmptyState
          v-if="latestList.length === 0"
          class="masonry-item masonry-full"
          :title="keyword ? 'No matches' : 'No notes yet'"
          :description="keyword ? 'Try a different keyword.' : 'Be the first to publish one.'"
        />
      </div>

      <div v-if="activeTab === 'latest' && latestTotal > 12" class="pagination-wrap">
        <el-pagination
          v-model:current-page="latestPage"
          :page-size="12"
          :total="latestTotal"
          layout="prev, pager, next"
          @current-change="loadLatest"
        />
      </div>
    </template>

    <div v-if="userStore.isLogin" class="fab-wrap">
      <el-tooltip content="New note" placement="left">
        <el-button type="primary" circle size="large" class="fab-btn" @click="$router.push('/note/edit')">
          <el-icon><Plus /></el-icon>
        </el-button>
      </el-tooltip>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { listNotes, searchNotes } from '@/api/note'
import { getRecommendList } from '@/api/recommend'
import { View, Plus } from '@element-plus/icons-vue'
import NoteCardSkeleton from '@/components/NoteCardSkeleton.vue'
import EmptyState from '@/components/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const tabs = [
  { name: 'recommend', label: 'For you' },
  { name: 'latest', label: 'Latest' },
]

const activeTab = ref('recommend')
const recommendList = ref([])
const recommendLoading = ref(false)
const latestList = ref([])
const latestLoading = ref(false)
const latestPage = ref(1)
const latestTotal = ref(0)

const keyword = computed(() => route.query.keyword || '')

// 截取摘要文本
function excerpt(text) {
  if (!text || typeof text !== 'string') return ''
  const cleaned = text.replace(/\s+/g, ' ').trim()
  return cleaned.length > 100 ? cleaned.slice(0, 100) + '…' : cleaned
}

// 数字格式化（万 / 千）
function formatCount(val) {
  const num = Number(val) || 0
  if (num >= 10000) return (num / 10000).toFixed(1).replace(/\.0$/, '') + 'w'
  if (num >= 1000) return (num / 1000).toFixed(1).replace(/\.0$/, '') + 'k'
  return String(num)
}

// 有封面图就显示图片，否则用 ID 生成渐变色
function coverStyle(n) {
  const cover = n.coverImage
  if (cover) {
    return {
      backgroundImage: `url(${cover})`,
      backgroundSize: 'cover',
      backgroundPosition: 'center',
    }
  }
  const noteId = n.noteId ?? n.id
  const hue = (Number(noteId) * 137.508) % 360
  return {
    background: `linear-gradient(135deg, hsl(${hue}, 45%, 88%) 0%, hsl(${hue}, 35%, 78%) 100%)`,
  }
}

async function loadRecommend() {
  recommendLoading.value = true
  try {
    const data = await getRecommendList({ page: 0, size: 12 })
    recommendList.value = data
  } catch (e) {
    // console.error('loadRecommend failed:', e)
    recommendList.value = []
  } finally {
    recommendLoading.value = false
  }
}

async function loadLatest() {
  latestLoading.value = true
  try {
    const pageIdx = latestPage.value - 1
    let res
    if (keyword.value) {
      res = await searchNotes({ page: pageIdx, size: 12, keyword: keyword.value })
    } else {
      res = await listNotes({ page: pageIdx, size: 12 })
    }
    latestList.value = res.list || []
    latestTotal.value = res.total || 0
  } finally {
    latestLoading.value = false
  }
}

function goNote(id) {
  router.push('/note/' + id)
}

watch(activeTab, (tab) => {
  if (tab === 'recommend') loadRecommend()
  else loadLatest()
})

watch(() => route.query.keyword, () => {
  latestPage.value = 1
  if (activeTab.value === 'latest') loadLatest()
})

onMounted(() => {
  loadRecommend()
  // 如果 URL 带了搜索关键词就自动切到 Latest 标签
  if (keyword.value) activeTab.value = 'latest'
  loadLatest()
})
</script>

<style scoped>
.home {
  padding-bottom: 100px;
  max-width: 1200px;
  margin: 0 auto;
}

.home-head {
  margin-bottom: 24px;
}

.home-head-title {
  font-size: clamp(1.5rem, 3vw, 2rem);
  color: var(--color-text);
  margin-bottom: 6px;
}

.home-head-sub {
  font-size: 0.95rem;
  color: var(--color-text-secondary);
}

.category-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--color-border);
  overflow-x: auto;
  scrollbar-width: none;
}

.category-bar::-webkit-scrollbar {
  display: none;
}

.category-tab {
  flex-shrink: 0;
  padding: 8px 18px;
  border: none;
  border-radius: var(--radius-full);
  background: transparent;
  color: var(--color-text-secondary);
  font-size: 0.95rem;
  font-family: inherit;
  cursor: pointer;
  transition: color var(--transition), background var(--transition);
}

.category-tab:hover {
  color: var(--color-primary);
  background: var(--color-primary-muted);
}

.category-tab.active {
  color: #fff;
  background: var(--color-primary);
  font-weight: 500;
}

.masonry {
  column-count: 3;
  column-gap: 20px;
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
  transition: transform var(--transition-slow), box-shadow var(--transition-slow), border-color var(--transition);
  border: 1px solid var(--color-border);
}

.note-card:hover {
  transform: translateY(-6px) scale(1.015);
  box-shadow: 0 16px 48px -12px rgba(0,0,0,0.15);
  border-color: var(--color-primary-muted);
}

.note-card:hover .note-card-cover {
  filter: brightness(1.05);
}

.note-card-cover {
  height: 120px;
  width: 100%;
  transition: filter var(--transition-slow);
}

.note-card-body {
  padding: 16px;
}

.note-card-title {
  font-size: 1.05rem;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.note-card-excerpt {
  font-size: 0.875rem;
  color: var(--color-text-secondary);
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 2.5em;
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

.fab-wrap {
  position: fixed;
  bottom: 28px;
  right: 28px;
}

.fab-btn {
  width: 56px;
  height: 56px;
  box-shadow: var(--color-shadow-lg);
}

.fab-btn:hover {
  transform: scale(1.05);
}

.recommend-badges {
  margin-bottom: 6px;
}

.recommend-tag {
  display: inline-block;
  font-size: 0.7rem;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 600;
  line-height: 1;
}

.recommend-tag.vector {
  background-color: var(--color-primary-muted); /* Fallback or use specific color */
  background-color: rgba(24, 144, 255, 0.1);
  color: #1890ff;
}

.recommend-tag.popular {
  background-color: rgba(250, 140, 22, 0.1);
  color: #fa8c16;
}

.recommend-tag.random {
  background-color: rgba(0, 0, 0, 0.05);
  color: var(--color-text-secondary);
}
</style>
