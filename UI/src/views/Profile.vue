<template>
  <div class="profile">
    <div class="profile-header-card" v-if="userStore.userInfo">
      <div class="profile-avatar" :style="avatarStyle">
        {{ avatarLetter }}
      </div>
      <div class="profile-identity">
        <h1>{{ userStore.userInfo.nickname || userStore.userInfo.username }}</h1>
        <div class="profile-badges">
          <span class="badge badge-role">{{ userStore.userInfo.role || 'USER' }}</span>
          <span class="badge badge-member" v-if="memberSince">Member since {{ memberSince }}</span>
        </div>
      </div>
    </div>

    <div class="profile-body">
      <el-tabs v-model="activeTab">
        <!-- 数据面板 -->
        <el-tab-pane label="Dashboard" name="dashboard">
          <div v-if="dashLoading" v-loading="true" style="min-height: 300px"></div>
          <template v-else-if="dash">
            <div class="stats-grid">
              <div class="stat-card">
                <div class="stat-value">{{ dash.stats.notesPublished }}</div>
                <div class="stat-label">Notes</div>
              </div>
              <div class="stat-card">
                <div class="stat-value">{{ formatNum(dash.stats.totalViews) }}</div>
                <div class="stat-label">Views</div>
              </div>
              <div class="stat-card">
                <div class="stat-value">{{ formatNum(dash.stats.totalLikes) }}</div>
                <div class="stat-label">Likes</div>
              </div>
              <div class="stat-card">
                <div class="stat-value">{{ formatNum(dash.stats.totalFavorited) }}</div>
                <div class="stat-label">Favorited</div>
              </div>
            </div>
            <div class="charts-row">
              <div class="chart-card">
                <h3>Publishing trend (last 30 days)</h3>
                <div ref="trendChartRef" class="chart-box"></div>
              </div>
              <div class="chart-card">
                <h3>Tag distribution</h3>
                <div ref="tagChartRef" class="chart-box"></div>
              </div>
            </div>
            <div class="chart-card chart-full">
              <h3>Views trend (last 30 days)</h3>
              <div ref="viewChartRef" class="chart-box"></div>
            </div>
          </template>
        </el-tab-pane>

        <el-tab-pane label="Basic info" name="info">
          <el-form ref="profileFormRef" :model="profile" label-position="top" class="profile-form">
            <el-form-item label="Username">
              <el-input :model-value="profile.username" disabled />
            </el-form-item>
            <el-form-item label="Nickname">
              <el-input v-model="profile.nickname" placeholder="Set a display name" />
            </el-form-item>
            <el-form-item label="Email">
              <el-input v-model="profile.email" placeholder="your@email.com" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="updateProfile" :loading="profileLoading">Save changes</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="Security" name="password">
          <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-position="top" class="profile-form">
            <el-form-item label="Current password" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="New password" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="changePassword" :loading="pwdLoading">Update password</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { updateProfile as apiUpdate, changePassword as apiChange, getUserDashboard } from '@/api/user'
import { useUserStore } from '@/stores/user'
import * as echarts from 'echarts'

const userStore = useUserStore()
const activeTab = ref('dashboard')
const profileFormRef = ref()
const pwdFormRef = ref()
const profileLoading = ref(false)
const pwdLoading = ref(false)
const dashLoading = ref(false)
const dash = ref(null)
const trendChartRef = ref(null)
const tagChartRef = ref(null)
const viewChartRef = ref(null)

const profile = reactive({ username: '', nickname: '', email: '' })
const pwdForm = reactive({ oldPassword: '', newPassword: '' })

const pwdRules = {
  oldPassword: [{ required: true, message: 'Please enter current password', trigger: 'blur' }],
  newPassword: [
    { required: true, message: 'Please enter new password', trigger: 'blur' },
    { min: 6, max: 32, message: '6-32 characters', trigger: 'blur' },
  ],
}

const avatarLetter = computed(() => {
  const name = userStore.userInfo?.nickname || userStore.userInfo?.username || '?'
  return name.slice(0, 1).toUpperCase()
})

const avatarStyle = computed(() => {
  const name = userStore.userInfo?.username || 'user'
  let hash = 0
  for (let i = 0; i < name.length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash)
  const hue = ((hash % 360) + 360) % 360
  return {
    background: `linear-gradient(135deg, hsl(${hue}, 55%, 55%), hsl(${(hue + 40) % 360}, 50%, 45%))`,
  }
})

const memberSince = computed(() => {
  const info = userStore.userInfo
  if (!info?.createdAt) return ''
  return new Date(info.createdAt).toLocaleDateString('en-US', { month: 'short', year: 'numeric' })
})

function formatNum(n) {
  if (!n) return '0'
  if (n >= 10000) return (n / 10000).toFixed(1).replace(/\.0$/, '') + 'w'
  if (n >= 1000) return (n / 1000).toFixed(1).replace(/\.0$/, '') + 'k'
  return String(n)
}

async function loadDashboard() {
  dashLoading.value = true
  try {
    dash.value = await getUserDashboard()
    await nextTick()
    // 等 DOM 布局完成再画图，避免容器宽高为 0
    setTimeout(() => renderCharts(), 150)
  } finally {
    dashLoading.value = false
  }
}

function renderCharts() {
  if (!dash.value) return

  const dailyNotes = dash.value.dailyNotes || []
  const dailyViews = dash.value.dailyViews || []
  const tagDist = dash.value.tagDistribution || []

  if (trendChartRef.value) {
    const chart = echarts.init(trendChartRef.value)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 40, right: 20, top: 20, bottom: 30 },
      xAxis: {
        type: 'category',
        data: dailyNotes.map(d => d.date),
        axisLabel: { fontSize: 11, interval: 4 },
      },
      yAxis: { type: 'value', minInterval: 1 },
      series: [{
        type: 'line',
        data: dailyNotes.map(d => d.count),
        smooth: true,
        areaStyle: { opacity: 0.15 },
        lineStyle: { width: 2.5 },
        itemStyle: { color: '#6366f1' },
      }],
    })
    chart.resize()
    window.addEventListener('resize', () => chart.resize())
  }

  if (viewChartRef.value) {
    const chart = echarts.init(viewChartRef.value)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 50, right: 20, top: 20, bottom: 30 },
      xAxis: {
        type: 'category',
        data: dailyViews.map(d => d.date),
        axisLabel: { fontSize: 11, interval: 4 },
      },
      yAxis: { type: 'value' },
      series: [{
        type: 'bar',
        data: dailyViews.map(d => d.count),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#14b8a6' },
            { offset: 1, color: '#6366f1' },
          ]),
          borderRadius: [4, 4, 0, 0],
        },
      }],
    })
    chart.resize()
    window.addEventListener('resize', () => chart.resize())
  }

  if (tagChartRef.value) {
    const chart = echarts.init(tagChartRef.value)
    const tagData = tagDist.map(t => ({ name: t.name, value: t.count }))
    chart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '50%'],
        data: tagData.length > 0 ? tagData : [{ name: 'No tags', value: 1 }],
        label: { fontSize: 12 },
        emphasis: {
          itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.2)' },
        },
      }],
    })
    chart.resize()
    window.addEventListener('resize', () => chart.resize())
  }
}

async function load() {
  await userStore.fetchProfile()
  Object.assign(profile, userStore.userInfo)
}

async function updateProfile() {
  profileLoading.value = true
  try {
    await apiUpdate({ nickname: profile.nickname, email: profile.email })
    await load()
    ElMessage.success('Saved')
  } finally {
    profileLoading.value = false
  }
}

async function changePassword() {
  await pwdFormRef.value.validate()
  pwdLoading.value = true
  try {
    await apiChange(pwdForm)
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    ElMessage.success('Password updated')
  } finally {
    pwdLoading.value = false
  }
}

watch(activeTab, (tab) => {
  if (tab === 'dashboard' && !dash.value) loadDashboard()
})

onMounted(() => {
  load()
  loadDashboard()
})
</script>

<style scoped>
.profile {
  max-width: 900px;
  margin: 0 auto;
}

.profile-header-card {
  display: flex;
  align-items: center;
  gap: 24px;
  background: var(--color-bg-card);
  border-radius: var(--radius);
  box-shadow: var(--color-shadow);
  border: 1px solid var(--color-border);
  padding: 32px;
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

.profile-body {
  background: var(--color-bg-card);
  border-radius: var(--radius);
  box-shadow: var(--color-shadow);
  border: 1px solid var(--color-border);
  padding: 28px 32px;
}

.profile-form {
  max-width: 420px;
  margin-top: 8px;
}

/* Dashboard styles */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  padding: 20px 16px;
  text-align: center;
  transition: transform var(--transition), box-shadow var(--transition);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
}

.stat-value {
  font-size: 1.8rem;
  font-weight: 700;
  color: var(--color-primary);
  font-family: var(--font-heading);
}

.stat-label {
  font-size: 0.85rem;
  color: var(--color-text-muted);
  margin-top: 4px;
}

.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.chart-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  padding: 20px;
}

.chart-card h3 {
  font-size: 0.95rem;
  margin-bottom: 12px;
  color: var(--color-text-secondary);
  font-weight: 500;
}

.chart-full {
  width: 100%;
}

.chart-box {
  width: 100%;
  height: 260px;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .charts-row {
    grid-template-columns: 1fr;
  }
}
</style>
