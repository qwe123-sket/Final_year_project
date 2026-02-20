<template>
  <div class="admin">
    <h1 class="admin-title">Dashboard</h1>
    <div class="admin-stats" v-if="stats">
      <div class="stat-card" v-for="s in statCards" :key="s.label">
        <div class="stat-icon" :style="{ background: s.iconBg }">
          <el-icon><component :is="s.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ animatedStats[s.key] ?? 0 }}</span>
          <span class="stat-label">{{ s.label }}</span>
        </div>
      </div>
    </div>

    <div class="admin-chart-row" v-if="stats">
      <div class="chart-card">
        <h3>Note status</h3>
        <div class="donut-wrap">
          <svg class="donut" viewBox="0 0 120 120">
            <circle cx="60" cy="60" r="50" fill="none" stroke="var(--color-border)" stroke-width="16" />
            <circle
              v-for="(seg, i) in donutSegments"
              :key="i"
              cx="60" cy="60" r="50"
              fill="none"
              :stroke="seg.color"
              stroke-width="16"
              :stroke-dasharray="seg.dash"
              :stroke-dashoffset="seg.offset"
              stroke-linecap="round"
              class="donut-seg"
              :style="{ animationDelay: i * 0.15 + 's' }"
            />
          </svg>
          <div class="donut-center">
            <span class="donut-total">{{ stats.noteCount }}</span>
            <span class="donut-label">Total</span>
          </div>
        </div>
        <div class="donut-legend">
          <span v-for="(seg, i) in donutSegments" :key="i" class="legend-item">
            <span class="legend-dot" :style="{ background: seg.color }"></span>
            {{ seg.label }} ({{ seg.value }})
          </span>
        </div>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="admin-tabs">
      <el-tab-pane label="Pending notes" name="notes">
        <div class="admin-list" v-loading="notesLoading">
          <div v-for="n in pendingNotes" :key="n.id" class="admin-item animate-in">
            <div class="item-main">
              <h4>{{ n.title }}</h4>
              <p class="author">{{ n.authorName }} · {{ formatTime(n.createdAt) }}</p>
            </div>
            <div class="item-actions">
              <el-button type="success" size="small" @click="audit(n.id, 'APPROVED')">Approve</el-button>
              <el-button type="danger" size="small" @click="auditReject(n.id)">Reject</el-button>
            </div>
          </div>
          <el-empty v-if="!notesLoading && pendingNotes.length === 0" description="No pending notes" />
        </div>
      </el-tab-pane>
      <el-tab-pane label="User management" name="users">
        <div class="admin-list" v-loading="usersLoading">
          <div v-for="u in users" :key="u.id" class="admin-item user-item animate-in">
            <div class="item-main">
              <h4>{{ u.username }}</h4>
              <p>{{ u.nickname || '-' }} · {{ u.email || '-' }}</p>
              <div class="tags">
                <el-tag size="small">{{ u.role }}</el-tag>
                <el-tag :type="u.status === 'NORMAL' ? 'success' : 'danger'" size="small">{{ u.status }}</el-tag>
              </div>
            </div>
            <div class="item-actions">
              <el-select v-model="statusMap[u.id]" placeholder="Status" size="small" @change="(v) => updateStatus(u.id, v)" style="width: 100px">
                <el-option label="Normal" value="NORMAL" />
                <el-option label="Disabled" value="DISABLED" />
                <el-option label="Banned" value="BANNED" />
              </el-select>
              <el-select v-if="u.role !== 'ADMIN'" v-model="roleMap[u.id]" placeholder="Role" size="small" @change="(v) => updateRole(u.id, v)" style="width: 80px">
                <el-option label="User" value="USER" />
                <el-option label="Admin" value="ADMIN" />
              </el-select>
            </div>
          </div>
          <el-empty v-if="!usersLoading && users.length === 0" description="No users" />
        </div>
      </el-tab-pane>
    </el-tabs>
    <el-dialog v-model="rejectVisible" title="Reject reason" width="400px">
      <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="Reason (e.g. policy violation)" />
      <template #footer>
        <el-button @click="rejectVisible = false">Cancel</el-button>
        <el-button type="danger" @click="confirmReject">Reject</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { getStats as apiGetStats, listPendingNotes, auditNote, listUsers, updateUserStatus, updateUserRole } from '@/api/admin'
import { ElMessage } from 'element-plus'
import { User, Notebook, Clock, CircleCheck } from '@element-plus/icons-vue'

const activeTab = ref('notes')
const stats = ref(null)
const notesLoading = ref(false)
const usersLoading = ref(false)
const pendingNotes = ref([])
const users = ref([])
const statusMap = reactive({})
const roleMap = reactive({})
const animatedStats = reactive({ userCount: 0, noteCount: 0, pendingNoteCount: 0, approvedNoteCount: 0 })

const rejectVisible = ref(false)
const rejectNoteId = ref(null)
const rejectReason = ref('')

const statCards = [
  { key: 'userCount', label: 'Users', icon: User, iconBg: 'rgba(13,148,136,0.12)' },
  { key: 'noteCount', label: 'Notes', icon: Notebook, iconBg: 'rgba(59,130,246,0.12)' },
  { key: 'pendingNoteCount', label: 'Pending', icon: Clock, iconBg: 'rgba(234,88,12,0.12)' },
  { key: 'approvedNoteCount', label: 'Approved', icon: CircleCheck, iconBg: 'rgba(34,197,94,0.12)' },
]

const CIRC = 2 * Math.PI * 50
const donutSegments = computed(() => {
  if (!stats.value) return []
  const total = stats.value.noteCount || 1
  const pending = stats.value.pendingNoteCount || 0
  const approved = stats.value.approvedNoteCount || 0
  const rejected = Math.max(0, total - pending - approved)
  const items = [
    { label: 'Approved', value: approved, color: '#22c55e' },
    { label: 'Pending', value: pending, color: '#f59e0b' },
    { label: 'Rejected', value: rejected, color: '#ef4444' },
  ].filter(s => s.value > 0)
  let cumulative = 0
  return items.map(s => {
    const pct = s.value / total
    const dash = `${pct * CIRC} ${CIRC}`
    const offset = -cumulative * CIRC + CIRC * 0.25
    cumulative += pct
    return { ...s, dash, offset }
  })
})

function animateCount(key, target) {
  const duration = 800
  const start = animatedStats[key]
  const diff = target - start
  if (diff === 0) return
  const startTime = performance.now()
  function step(now) {
    const elapsed = now - startTime
    const progress = Math.min(elapsed / duration, 1)
    const eased = 1 - Math.pow(1 - progress, 3)
    animatedStats[key] = Math.round(start + diff * eased)
    if (progress < 1) requestAnimationFrame(step)
  }
  requestAnimationFrame(step)
}

function formatTime(t) {
  if (!t) return ''
  return new Date(t).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}

async function loadNotes() {
  notesLoading.value = true
  try {
    const res = await listPendingNotes({ page: 0, size: 50 })
    pendingNotes.value = res.list || []
  } finally {
    notesLoading.value = false
  }
}

async function loadUsers() {
  usersLoading.value = true
  try {
    const res = await listUsers({ page: 0, size: 100 })
    users.value = res.list || []
    users.value.forEach((u) => {
      statusMap[u.id] = u.status
      roleMap[u.id] = u.role
    })
  } finally {
    usersLoading.value = false
  }
}

async function audit(id, status) {
  await auditNote(id, { status })
  ElMessage.success('Done')
  loadNotes()
  loadStats()
}

function auditReject(id) {
  rejectNoteId.value = id
  rejectReason.value = ''
  rejectVisible.value = true
}

async function confirmReject() {
  await auditNote(rejectNoteId.value, { status: 'REJECTED', rejectReason: rejectReason.value })
  ElMessage.success('Rejected')
  rejectVisible.value = false
  loadNotes()
  loadStats()
}

async function updateStatus(id, status) {
  await updateUserStatus(id, { status })
  ElMessage.success('Updated')
}

async function updateRole(id, role) {
  await updateUserRole(id, role)
  ElMessage.success('Updated')
  loadUsers()
}

watch(activeTab, (v) => {
  if (v === 'notes') loadNotes()
  else loadUsers()
})

async function loadStats() {
  try {
    stats.value = await apiGetStats()
    for (const key of ['userCount', 'noteCount', 'pendingNoteCount', 'approvedNoteCount']) {
      animateCount(key, stats.value[key] || 0)
    }
  } catch {}
}

onMounted(() => {
  loadStats()
  loadNotes()
})
</script>

<style scoped>
.admin {
  max-width: 900px;
  margin: 0 auto;
}

.admin-title {
  font-size: 1.75rem;
  margin-bottom: 24px;
}

.admin-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

@media (max-width: 700px) {
  .admin-stats {
    grid-template-columns: repeat(2, 1fr);
  }
}

.stat-card {
  background: var(--color-surface);
  border-radius: var(--radius);
  padding: 20px;
  box-shadow: var(--color-shadow);
  border: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  gap: 16px;
  transition: transform var(--transition), box-shadow var(--transition);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--color-shadow-lg);
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.3rem;
  color: var(--color-primary);
  flex-shrink: 0;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-family: var(--font-heading);
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--color-text);
  line-height: 1.2;
}

.stat-label {
  font-size: 0.85rem;
  color: var(--color-text-secondary);
}

.admin-chart-row {
  margin-bottom: 28px;
}

.chart-card {
  background: var(--color-surface);
  border-radius: var(--radius);
  padding: 24px;
  box-shadow: var(--color-shadow);
  border: 1px solid var(--color-border);
}

.chart-card h3 {
  font-size: 1rem;
  margin-bottom: 16px;
  color: var(--color-text);
}

.donut-wrap {
  position: relative;
  width: 160px;
  height: 160px;
  margin: 0 auto 16px;
}

.donut {
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
}

.donut-seg {
  animation: donutDraw 0.8s ease-out both;
}

@keyframes donutDraw {
  from { stroke-dasharray: 0 314.16; }
}

.donut-center {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.donut-total {
  font-family: var(--font-heading);
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--color-text);
}

.donut-label {
  font-size: 0.8rem;
  color: var(--color-text-muted);
}

.donut-legend {
  display: flex;
  justify-content: center;
  gap: 20px;
  flex-wrap: wrap;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.85rem;
  color: var(--color-text-secondary);
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.admin-tabs {
  margin-top: 8px;
}

.admin-list {
  margin-top: 16px;
}

.admin-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: var(--color-bg-card);
  border-radius: var(--radius-sm);
  margin-bottom: 12px;
  border: 1px solid var(--color-border);
  transition: box-shadow var(--transition);
}

.admin-item:hover {
  box-shadow: var(--color-shadow-lg);
}

.item-main h4 {
  font-size: 1rem;
  margin-bottom: 4px;
}

.item-main p {
  font-size: 0.85rem;
  color: var(--color-text-secondary);
}

.item-main .author {
  margin-bottom: 8px;
}

.tags {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.item-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.user-item .item-actions {
  flex-wrap: wrap;
}
</style>
