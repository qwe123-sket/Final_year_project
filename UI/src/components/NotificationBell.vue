<template>
  <div class="notif-bell" v-if="userStore.isLogin">
    <el-popover
      placement="bottom-end"
      :width="360"
      trigger="click"
      @show="onOpen"
    >
      <template #reference>
        <button class="bell-btn" :class="{ 'has-unread': unreadCount > 0 }">
          <el-icon :size="20"><Bell /></el-icon>
          <span v-if="unreadCount > 0" class="badge-dot">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
        </button>
      </template>
      <div class="notif-panel">
        <div class="notif-header">
          <span class="notif-title">Notifications</span>
          <el-button v-if="unreadCount > 0" link size="small" @click="onMarkAllRead">Mark all read</el-button>
        </div>
        <div class="notif-list" v-if="list.length > 0">
          <div
            v-for="n in list"
            :key="n.id"
            class="notif-item"
            :class="{ unread: !n.isRead }"
            @click="goToNote(n)"
          >
            <div class="notif-icon" :class="n.type?.toLowerCase()">
              <el-icon v-if="n.type === 'LIKE'"><Star /></el-icon>
              <el-icon v-else-if="n.type === 'FAVORITE'"><Collection /></el-icon>
              <el-icon v-else><ChatDotRound /></el-icon>
            </div>
            <div class="notif-body">
              <p class="notif-text">{{ n.content }}</p>
              <span class="notif-time">{{ formatTime(n.createdAt) }}</span>
            </div>
          </div>
        </div>
        <el-empty v-else description="No notifications yet" :image-size="60" />
      </div>
    </el-popover>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getNotifications, getUnreadCount, markAllRead } from '@/api/notification'
import { Bell, Star, Collection, ChatDotRound } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const unreadCount = ref(0)
const list = ref([])
let ws = null
let pollTimer = null

function formatTime (t) {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return 'just now'
  if (diff < 3600000) return Math.floor(diff / 60000) + 'm ago'
  if (diff < 86400000) return Math.floor(diff / 3600000) + 'h ago'
  return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
}

async function loadUnread() {
  if (!userStore.isLogin) return
  try {
    const data = await getUnreadCount()
    unreadCount.value = data.count || 0
  } catch {}
}

async function onOpen() {
  try {
    list.value = await getNotifications({ page: 0, size: 30 })
  } catch {}
}

async function onMarkAllRead() {
  try {
    await markAllRead()
    unreadCount.value = 0
    list.value = list.value.map(n => ({ ...n, isRead: true }))
  } catch {}
}

function goToNote(n) {
  if (n.relatedNoteId) {
    router.push('/note/' + n.relatedNoteId)
  }
}

function connectWs() {
  if (!userStore.isLogin || !userStore.token) return
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const wsUrl = `${protocol}//${window.location.host}/ws/notifications?token=${userStore.token}`
  try {
    ws = new WebSocket(wsUrl)
    ws.onmessage = () => {
      unreadCount.value++
    }
    ws.onclose = () => {
      ws = null
    }
  } catch {}
}

onMounted(() => {
  loadUnread()
  connectWs()
  pollTimer = setInterval(loadUnread, 60000)
})

onBeforeUnmount(() => {
  if (ws) { try { ws.close() } catch {} }
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<style scoped>
.bell-btn {
  position: relative;
  background: none;
  border: none;
  cursor: pointer;
  padding: 6px;
  border-radius: 50%;
  color: var(--color-text-secondary);
  transition: color var(--transition), background var(--transition);
  display: flex;
  align-items: center;
  justify-content: center;
}

.bell-btn:hover {
  color: var(--color-primary);
  background: var(--color-primary-muted);
}

.bell-btn.has-unread {
  color: var(--color-primary);
}

.badge-dot {
  position: absolute;
  top: 0;
  right: -2px;
  background: #ef4444;
  color: #fff;
  font-size: 0.65rem;
  font-weight: 600;
  min-width: 18px;
  height: 18px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
  line-height: 1;
}

.notif-panel {
  max-height: 400px;
  overflow-y: auto;
}

.notif-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--color-border);
  margin-bottom: 8px;
}

.notif-title {
  font-weight: 600;
  font-size: 0.95rem;
}

.notif-item {
  display: flex;
  gap: 10px;
  padding: 10px 4px;
  border-radius: 8px;
  cursor: pointer;
  transition: background var(--transition);
}

.notif-item:hover {
  background: var(--color-primary-muted);
}

.notif-item.unread {
  background: rgba(99, 102, 241, 0.06);
}

.notif-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 0.9rem;
}

.notif-icon.like {
  background: #fef2f2;
  color: #ef4444;
}

.notif-icon.favorite {
  background: #fefce8;
  color: #f59e0b;
}

.notif-icon.reply {
  background: #eff6ff;
  color: #3b82f6;
}

.notif-body {
  flex: 1;
  min-width: 0;
}

.notif-text {
  font-size: 0.88rem;
  color: var(--color-text);
  line-height: 1.4;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notif-time {
  font-size: 0.75rem;
  color: var(--color-text-muted);
  margin-top: 2px;
  display: block;
}
</style>
