<template>
  <div class="app-shell">
    <header class="top-bar">
      <router-link to="/" class="logo">
        <span class="logo-icon">N</span>
        <span class="logo-text">Notes</span>
      </router-link>
      <div class="top-search">
        <el-input
          v-model="keyword"
          :placeholder="userStore.isLogin ? 'Search notes...' : 'Sign in to explore more'"
          clearable
          class="top-search-input"
          @keyup.enter="goSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
      <div class="top-actions">
        <button class="theme-toggle" @click="toggleTheme" :title="isDark ? 'Switch to light mode' : 'Switch to dark mode'">
          <el-icon v-if="isDark"><Sunny /></el-icon>
          <el-icon v-else><Moon /></el-icon>
        </button>
        <template v-if="userStore.isLogin">
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="user-btn">
              <el-avatar :size="36">{{ (userStore.userInfo?.nickname || userStore.userInfo?.username || '?').slice(0, 1) }}</el-avatar>
              <span class="user-btn-name">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">Profile</el-dropdown-item>
                <el-dropdown-item command="logout" divided>Log out</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <router-link to="/login" class="top-link">Log in</router-link>
          <router-link to="/register" class="top-link top-link-primary">Sign up</router-link>
        </template>
      </div>
    </header>

    <div class="body">
      <aside class="sidebar">
        <nav class="sidebar-nav">
          <router-link to="/" class="sidebar-item" :class="{ 'is-active': $route.path === '/' }">
            <el-icon><Compass /></el-icon>
            <span>Discover</span>
          </router-link>
          <template v-if="userStore.isLogin">
            <router-link to="/note/edit" class="sidebar-item">
              <el-icon><Plus /></el-icon>
              <span>Publish</span>
            </router-link>
            <router-link to="/my/notes" class="sidebar-item" active-class="is-active">
              <el-icon><Document /></el-icon>
              <span>My Notes</span>
            </router-link>
            <router-link to="/my/favorites" class="sidebar-item" active-class="is-active">
              <el-icon><Star /></el-icon>
              <span>Favorites</span>
            </router-link>
            <router-link v-if="userStore.isAdmin" to="/admin" class="sidebar-item" active-class="is-active">
              <el-icon><Setting /></el-icon>
              <span>Admin</span>
            </router-link>
          </template>
        </nav>

        <div v-if="!userStore.isLogin" class="sidebar-cta">
          <div class="sidebar-cta-inner">
            <p class="sidebar-cta-title">Sign in for more</p>
            <ul class="sidebar-cta-list">
              <li><el-icon><TrendCharts /></el-icon> Recommendations tailored for you</li>
              <li><el-icon><Search /></el-icon> Search & discover notes</li>
              <li><el-icon><Star /></el-icon> Save favorites & like notes</li>
              <li><el-icon><ChatDotRound /></el-icon> Interact with the community</li>
            </ul>
            <router-link to="/login" class="sidebar-cta-btn">Sign in</router-link>
          </div>
        </div>

        <div class="sidebar-footer">
          <p class="sidebar-credit">Built with Vue 3 + Spring Boot</p>
        </div>
      </aside>

      <main class="main">
        <router-view v-slot="{ Component }">
          <transition name="slide-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
    <BackToTop />
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useTheme } from '@/composables/useTheme'
import { Search, ArrowDown, Compass, Plus, Document, Star, Setting, MoreFilled, TrendCharts, ChatDotRound, Sunny, Moon } from '@element-plus/icons-vue'
import BackToTop from '@/components/BackToTop.vue'

const { isDark, toggle: toggleTheme } = useTheme()

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const keyword = ref(route.query.keyword || '')

watch(() => route.query.keyword, (v) => {
  keyword.value = v || ''
})

function goSearch() {
  if (keyword.value.trim()) {
    router.push({ path: '/', query: { keyword: keyword.value } })
  }
}

function handleCommand(cmd) {
  if (cmd === 'profile') router.push('/profile')
  else if (cmd === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  --font-heading: 'Fraunces', Georgia, serif;
}

.top-bar {
  height: 56px;
  padding: 0 24px;
  background: var(--color-bg-elevated);
  border-bottom: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  gap: 24px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--color-primary);
  font-family: var(--font-heading);
  font-size: 1.35rem;
  font-weight: 700;
  flex-shrink: 0;
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, var(--color-primary), #14b8a6);
  color: #fff;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.1rem;
  font-weight: 700;
}

.top-search {
  flex: 1;
  max-width: 400px;
  margin: 0 auto;
}

.top-search-input {
  --el-input-border-radius: var(--radius-full);
  --el-input-hover-border-color: var(--color-primary);
  --el-input-focus-border-color: var(--color-primary);
}

.top-search-input :deep(.el-input__wrapper) {
  border-radius: var(--radius-full);
}

.top-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.top-link {
  font-size: 0.95rem;
  color: var(--color-text-secondary);
  padding: 6px 12px;
  border-radius: var(--radius-sm);
  transition: color var(--transition), background var(--transition);
}

.top-link:hover {
  color: var(--color-primary);
  background: var(--color-primary-muted);
}

.top-link-primary {
  background: var(--color-primary);
  color: #fff !important;
}

.top-link-primary:hover {
  background: var(--color-primary-hover);
  color: #fff !important;
}

.theme-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-full);
  background: var(--color-surface);
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: color var(--transition), background var(--transition), border-color var(--transition);
  font-size: 1.1rem;
}

.theme-toggle:hover {
  color: var(--color-primary);
  border-color: var(--color-primary);
  background: var(--color-primary-muted);
}

.user-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: var(--radius-full);
  transition: background var(--transition);
}

.user-btn:hover {
  background: var(--color-bg);
}

.user-btn-name {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 0.95rem;
  color: var(--color-text);
}

.body {
  display: flex;
  flex: 1;
  min-height: 0;
}

.sidebar {
  width: 220px;
  flex-shrink: 0;
  background: var(--color-bg-elevated);
  border-right: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  padding: 16px 0;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 0 12px;
}

.sidebar-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: var(--radius-sm);
  color: var(--color-text-secondary);
  font-size: 0.95rem;
  transition: color var(--transition), background var(--transition);
}

.sidebar-item:hover {
  color: var(--color-primary);
  background: var(--color-primary-muted);
}

.sidebar-item.is-active {
  color: var(--color-primary);
  font-weight: 500;
  background: var(--color-primary-muted);
}

.sidebar-item .el-icon {
  font-size: 1.25rem;
  flex-shrink: 0;
}

.sidebar-cta {
  margin: 20px 12px 0;
  padding: 20px;
  background: linear-gradient(160deg, var(--color-primary-muted) 0%, rgba(255,255,255,0.6) 100%);
  border-radius: var(--radius);
  border: 1px solid rgba(13, 148, 136, 0.15);
}

.sidebar-cta-inner {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.sidebar-cta-title {
  font-family: var(--font-heading);
  font-weight: 600;
  font-size: 1.05rem;
  color: var(--color-text);
}

.sidebar-cta-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
  font-size: 0.85rem;
  color: var(--color-text-secondary);
}

.sidebar-cta-list li {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sidebar-cta-list .el-icon {
  color: var(--color-primary);
  font-size: 1rem;
  flex-shrink: 0;
}

.sidebar-cta-btn {
  display: block;
  text-align: center;
  padding: 10px 16px;
  background: var(--color-primary);
  color: #fff !important;
  border-radius: var(--radius-sm);
  font-weight: 500;
  font-size: 0.95rem;
  transition: background var(--transition);
}

.sidebar-cta-btn:hover {
  background: var(--color-primary-hover);
  color: #fff !important;
}

.sidebar-footer {
  margin-top: auto;
  padding: 12px 12px 0;
  border-top: 1px solid var(--color-border);
}

.sidebar-credit {
  font-size: 0.75rem;
  color: var(--color-text-muted);
  text-align: center;
  padding: 12px 8px;
  line-height: 1.4;
}

.main {
  flex: 1;
  min-width: 0;
  padding: 24px;
  background: var(--color-bg);
}

.slide-fade-enter-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.slide-fade-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}

.slide-fade-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.slide-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

@media (max-width: 900px) {
  .sidebar {
    width: 72px;
    padding: 16px 0;
    align-items: center;
  }

  .sidebar-item span,
  .sidebar-cta,
  .sidebar-item-more span {
    display: none;
  }

  .sidebar-nav,
  .sidebar-footer {
    padding: 0 8px;
    align-items: center;
  }

  .sidebar-item {
    justify-content: center;
    padding: 12px;
  }
}
</style>
