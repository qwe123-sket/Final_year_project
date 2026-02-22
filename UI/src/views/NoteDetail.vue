<template>
  <div class="note-detail" v-loading="loading">
    <div v-if="note" class="detail-wrap">
      <div class="reading-progress" :style="{ width: scrollProgress + '%' }"></div>
      <div class="detail-card">
        <div v-if="note.coverImage" class="detail-cover">
          <img :src="note.coverImage" alt="cover" />
        </div>
        <div class="detail-header">
          <h1>{{ note.title }}</h1>
          <div class="meta-row">
            <span class="author-name">{{ note.authorName || 'Anonymous' }}</span>
            <span class="meta-sep">·</span>
            <span>{{ formatTime(note.createdAt) }}</span>
            <span class="meta-sep">·</span>
            <span class="meta-icon"><el-icon><View /></el-icon> {{ note.viewCount || 0 }}</span>
            <span class="meta-sep">·</span>
            <span class="meta-icon"><el-icon><Clock /></el-icon> {{ readingTime }} min read</span>
          </div>
          <div class="header-actions">
            <el-button
              v-if="userStore.isLogin"
              :type="note.favorited ? 'primary' : 'default'"
              size="small"
              @click="toggleFavorite"
              :loading="favLoading"
            >
              <el-icon><Star /></el-icon>
              {{ note.favorited ? 'Favorited' : 'Favorite' }}
            </el-button>
            <el-button size="small" @click="shareNote">
              <el-icon><Share /></el-icon>
              Share
            </el-button>
          </div>
        </div>
        <article class="detail-content markdown-body" ref="contentRef" v-html="renderedContent"></article>
      </div>

      <div class="replies-card">
        <h3>Replies ({{ replies.length }})</h3>
        <div v-if="userStore.isLogin" class="reply-form">
          <el-input
            v-model="replyContent"
            type="textarea"
            :rows="3"
            placeholder="Write your reply..."
          />
          <el-button type="primary" size="small" @click="submitReply" :loading="replyLoading">
            Post reply
          </el-button>
        </div>
        <div v-else class="reply-tip">
          <router-link to="/login">Sign in</router-link> to join the discussion.
        </div>
        <div class="reply-list">
          <div v-for="r in replies" :key="r.id" class="reply-item">
            <div class="reply-avatar">{{ (r.username || '?').slice(0, 1).toUpperCase() }}</div>
            <div class="reply-body">
              <div class="reply-head">
                <span class="reply-user">{{ r.username }}</span>
                <span class="reply-time">{{ formatTime(r.createdAt) }}</span>
              </div>
              <p class="reply-text">{{ r.content }}</p>
            </div>
          </div>
          <el-empty v-if="replies.length === 0" description="No replies yet. Be the first!" />
        </div>
      </div>
    </div>
    <el-empty v-else-if="!loading" description="Note not found" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getNote, recordView } from '@/api/note'
import { listReplies, createReply } from '@/api/reply'
import { addFavorite, removeFavorite } from '@/api/favorite'
import { recordBrowse } from '@/api/browse'
import { View, Star, Clock, Share } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark.css'

marked.setOptions({
  highlight(code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      return hljs.highlight(code, { language: lang }).value
    }
    return hljs.highlightAuto(code).value
  },
  breaks: true,
  gfm: true,
})

const route = useRoute()
const userStore = useUserStore()

const note = ref(null)
const loading = ref(true)
const replies = ref([])
const replyContent = ref('')
const replyLoading = ref(false)
const favLoading = ref(false)
const browseStart = ref(0)
const scrollProgress = ref(0)
const contentRef = ref(null)

const noteId = computed(() => Number(route.params.id))

const renderedContent = computed(() => {
  if (!note.value?.content) return '<p>(No content)</p>'
  try {
    return marked(note.value.content)
  } catch {
    return note.value.content
  }
})

const readingTime = computed(() => {
  if (!note.value?.content) return 1
  const words = note.value.content.replace(/\s+/g, ' ').trim().length
  return Math.max(1, Math.ceil(words / 500))
})

function onScroll() {
  const doc = document.documentElement
  const scrollTop = doc.scrollTop || document.body.scrollTop
  const scrollHeight = doc.scrollHeight - doc.clientHeight
  scrollProgress.value = scrollHeight > 0 ? (scrollTop / scrollHeight) * 100 : 0
}

function formatTime(t) {
  if (!t) return ''
  return new Date(t).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}

async function shareNote() {
  try {
    await navigator.clipboard.writeText(window.location.href)
    ElMessage.success('Link copied to clipboard')
  } catch {
    ElMessage.info('Copy this URL to share: ' + window.location.href)
  }
}

async function load() {
  loading.value = true
  try {
    note.value = await getNote(noteId.value)
    savedNoteId = noteId.value
    await recordView(noteId.value)
    browseStart.value = Date.now()
    const list = await listReplies(noteId.value, { page: 0, size: 50 })
    replies.value = list || []
  } finally {
    loading.value = false
  }
}

async function toggleFavorite() {
  if (!note.value || favLoading.value) return
  favLoading.value = true
  try {
    if (note.value.favorited) {
      await removeFavorite(noteId.value)
      note.value.favorited = false
    } else {
      await addFavorite(noteId.value)
      note.value.favorited = true
    }
  } finally {
    favLoading.value = false
  }
}

async function submitReply() {
  if (!replyContent.value.trim()) return
  replyLoading.value = true
  try {
    const r = await createReply(noteId.value, { content: replyContent.value.trim() })
    replies.value = [r, ...replies.value]
    replyContent.value = ''
  } finally {
    replyLoading.value = false
  }
}

// 用变量存一下当前笔记 ID，防止路由切走后 noteId 变成 NaN
let savedNoteId = null

async function reportBrowse() {
  if (!userStore.isLogin || !savedNoteId) return
  const duration = Math.floor((Date.now() - browseStart.value) / 1000)
  if (duration < 1) return
  try {
    await recordBrowse({ noteId: savedNoteId, browseDurationSeconds: duration })
  } catch {}
}

onMounted(() => {
  load()
  window.addEventListener('scroll', onScroll, { passive: true })
})

onBeforeUnmount(() => {
  reportBrowse()
  window.removeEventListener('scroll', onScroll)
})
</script>

<style scoped>
.note-detail {
  max-width: 800px;
  margin: 0 auto;
}

.detail-wrap {
  position: relative;
}

.reading-progress {
  position: fixed;
  top: 56px;
  left: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--color-primary), #14b8a6);
  z-index: 101;
  transition: width 0.1s linear;
  border-radius: 0 2px 2px 0;
}

.detail-card {
  background: var(--color-bg-card);
  border-radius: var(--radius);
  box-shadow: var(--color-shadow);
  border: 1px solid var(--color-border);
  overflow: hidden;
  margin-bottom: 24px;
}

.detail-cover {
  width: 100%;
  max-height: 400px;
  overflow: hidden;
}

.detail-cover img {
  width: 100%;
  max-height: 400px;
  object-fit: cover;
  display: block;
}

.detail-header {
  padding: 36px 40px 28px;
  border-bottom: 1px solid var(--color-border);
}

.detail-header h1 {
  font-size: 1.85rem;
  margin-bottom: 14px;
  line-height: 1.35;
}

.meta-row {
  color: var(--color-text-secondary);
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.author-name {
  color: var(--color-primary);
  font-weight: 500;
}

.meta-sep {
  color: var(--color-text-muted);
}

.meta-icon {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.header-actions {
  display: flex;
  gap: 10px;
  margin-top: 18px;
}

.detail-content {
  padding: 36px 40px 40px;
  line-height: 1.9;
  font-size: 1.02rem;
  color: var(--color-text);
  max-width: 680px;
}

.detail-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 12px 0;
  display: block;
}

.detail-content :deep(h1),
.detail-content :deep(h2),
.detail-content :deep(h3),
.detail-content :deep(h4) {
  margin: 1.4em 0 0.6em;
  line-height: 1.4;
  color: var(--color-text);
}

.detail-content :deep(h1) { font-size: 1.6rem; }
.detail-content :deep(h2) { font-size: 1.35rem; border-bottom: 1px solid var(--color-border); padding-bottom: 0.3em; }
.detail-content :deep(h3) { font-size: 1.15rem; }

.detail-content :deep(p) {
  margin: 0.8em 0;
}

.detail-content :deep(blockquote) {
  border-left: 4px solid var(--color-primary);
  margin: 1em 0;
  padding: 0.6em 1em;
  background: var(--color-primary-muted);
  border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
  color: var(--color-text-secondary);
}

.detail-content :deep(pre) {
  background: #1e1e2e;
  border-radius: var(--radius-sm);
  padding: 16px 20px;
  overflow-x: auto;
  margin: 1em 0;
  font-size: 0.9rem;
  line-height: 1.6;
}

.detail-content :deep(pre code) {
  background: none;
  padding: 0;
  color: #cdd6f4;
  font-family: 'Fira Code', 'Consolas', 'Monaco', monospace;
}

.detail-content :deep(code) {
  background: var(--color-primary-muted);
  color: var(--color-primary);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.9em;
  font-family: 'Fira Code', 'Consolas', 'Monaco', monospace;
}

.detail-content :deep(ul),
.detail-content :deep(ol) {
  padding-left: 1.8em;
  margin: 0.8em 0;
}

.detail-content :deep(li) {
  margin: 0.3em 0;
}

.detail-content :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 1em 0;
}

.detail-content :deep(th),
.detail-content :deep(td) {
  border: 1px solid var(--color-border);
  padding: 8px 12px;
  text-align: left;
}

.detail-content :deep(th) {
  background: var(--color-primary-muted);
  font-weight: 600;
}

.detail-content :deep(hr) {
  border: none;
  border-top: 1px solid var(--color-border);
  margin: 1.5em 0;
}

.detail-content :deep(a) {
  color: var(--color-primary);
  text-decoration: none;
}

.detail-content :deep(a:hover) {
  text-decoration: underline;
}

.replies-card {
  background: var(--color-bg-card);
  border-radius: var(--radius);
  box-shadow: var(--color-shadow);
  border: 1px solid var(--color-border);
  padding: 28px 40px 36px;
}

.replies-card h3 {
  font-size: 1.1rem;
  margin-bottom: 20px;
}

.reply-form {
  margin-bottom: 24px;
}

.reply-form .el-textarea {
  margin-bottom: 10px;
}

.reply-tip {
  color: var(--color-text-secondary);
  font-size: 0.9rem;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: var(--color-primary-muted);
  border-radius: var(--radius-sm);
}

.reply-item {
  display: flex;
  gap: 14px;
  padding: 16px 0;
  border-bottom: 1px solid var(--color-border);
}

.reply-item:last-child {
  border-bottom: none;
}

.reply-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--color-primary-muted);
  color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.9rem;
  flex-shrink: 0;
}

.reply-body {
  flex: 1;
  min-width: 0;
}

.reply-head {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 6px;
}

.reply-user {
  font-weight: 500;
  color: var(--color-text);
}

.reply-time {
  color: var(--color-text-muted);
  font-size: 0.82rem;
}

.reply-text {
  font-size: 0.95rem;
  color: var(--color-text-secondary);
  line-height: 1.6;
}
</style>
