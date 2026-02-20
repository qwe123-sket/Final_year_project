<template>
  <div class="auth-page">
    <div class="auth-split">
      <div class="auth-showcase">
        <div class="showcase-blob blob-1"></div>
        <div class="showcase-blob blob-2"></div>
        <div class="showcase-blob blob-3"></div>
        <div class="showcase-content">
          <span class="showcase-icon">N</span>
          <h2>Welcome to Notes</h2>
          <p>A smart recommendation system for discovering and sharing knowledge.</p>
          <ul class="showcase-features">
            <li><el-icon><TrendCharts /></el-icon> Personalized recommendations</li>
            <li><el-icon><Star /></el-icon> Save and organize favorites</li>
            <li><el-icon><ChatDotRound /></el-icon> Engage with the community</li>
          </ul>
        </div>
      </div>
      <div class="auth-form-side">
        <div class="auth-card">
          <h1>Welcome back</h1>
          <p class="auth-sub">Log in to discover more notes</p>
          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-position="top"
            class="auth-form"
            @submit.prevent="onSubmit"
          >
            <el-form-item label="Username" prop="username">
              <el-input v-model="form.username" placeholder="Enter username" size="large" />
            </el-form-item>
            <el-form-item label="Password" prop="password">
              <el-input v-model="form.password" type="password" placeholder="Enter password" size="large" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" :loading="loading" class="submit-btn" native-type="submit">
                Log in
              </el-button>
            </el-form-item>
            <p class="auth-tip">
              No account? <router-link to="/register">Sign up</router-link>
            </p>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { TrendCharts, Star, ChatDotRound } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: 'Please enter username', trigger: 'blur' }],
  password: [{ required: true, message: 'Please enter password', trigger: 'blur' }],
}

async function onSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('Login successful')
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  background: var(--color-bg);
}

.auth-split {
  display: flex;
  width: 100%;
  min-height: 100vh;
}

.auth-showcase {
  flex: 1;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(160deg, var(--color-primary) 0%, #14b8a6 50%, #0f766e 100%);
  padding: 48px;
}

.showcase-blob {
  position: absolute;
  border-radius: 50%;
  opacity: 0.15;
  animation: blobFloat 8s ease-in-out infinite;
}

.blob-1 {
  width: 300px;
  height: 300px;
  top: -60px;
  right: -40px;
  background: #fff;
  animation-duration: 7s;
}

.blob-2 {
  width: 200px;
  height: 200px;
  bottom: 10%;
  left: -30px;
  background: #fff;
  animation-duration: 9s;
  animation-delay: 1s;
}

.blob-3 {
  width: 150px;
  height: 150px;
  top: 40%;
  right: 20%;
  background: #fff;
  animation-duration: 11s;
  animation-delay: 2s;
}

@keyframes blobFloat {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(15px, -20px) scale(1.05); }
  66% { transform: translate(-10px, 15px) scale(0.95); }
}

.showcase-content {
  position: relative;
  z-index: 1;
  color: #fff;
  max-width: 360px;
}

.showcase-icon {
  display: inline-flex;
  width: 64px;
  height: 64px;
  background: rgba(255,255,255,0.2);
  border-radius: 16px;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  font-weight: 700;
  font-family: var(--font-heading);
  margin-bottom: 24px;
  backdrop-filter: blur(8px);
}

.showcase-content h2 {
  font-size: 1.75rem;
  margin-bottom: 12px;
  color: #fff;
}

.showcase-content p {
  opacity: 0.85;
  line-height: 1.6;
  margin-bottom: 28px;
}

.showcase-features {
  list-style: none;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.showcase-features li {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 0.95rem;
  opacity: 0.9;
}

.showcase-features .el-icon {
  font-size: 1.2rem;
}

.auth-form-side {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
  background: var(--color-bg);
}

.auth-card {
  width: 100%;
  max-width: 400px;
}

.auth-card h1 {
  font-size: 1.75rem;
  margin-bottom: 8px;
}

.auth-sub {
  color: var(--color-text-secondary);
  margin-bottom: 32px;
}

.submit-btn {
  width: 100%;
  height: 44px;
}

.auth-tip {
  text-align: center;
  color: var(--color-text-secondary);
  font-size: 0.9rem;
  margin-top: 16px;
}

@media (max-width: 768px) {
  .auth-split {
    flex-direction: column;
  }

  .auth-showcase {
    min-height: 220px;
    padding: 32px;
  }

  .showcase-features {
    display: none;
  }
}
</style>
