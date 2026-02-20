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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { updateProfile as apiUpdate, changePassword as apiChange } from '@/api/user'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const activeTab = ref('info')
const profileFormRef = ref()
const pwdFormRef = ref()
const profileLoading = ref(false)
const pwdLoading = ref(false)

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

onMounted(load)
</script>

<style scoped>
.profile {
  max-width: 640px;
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
</style>
