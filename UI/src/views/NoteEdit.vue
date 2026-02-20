<template>
  <div class="note-edit">
    <div class="edit-card">
      <h2>{{ isEdit ? 'Edit note' : 'Publish note' }}</h2>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="edit-form"
        @submit.prevent="onSubmit"
      >
        <el-form-item label="Title" prop="title">
          <el-input v-model="form.title" placeholder="Enter title" maxlength="256" show-word-limit size="large" />
        </el-form-item>
        <el-form-item label="Content" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="16" placeholder="Write your note..." maxlength="10000" show-word-limit />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" @click="onSubmit">
            {{ isEdit ? 'Save' : 'Publish' }}
          </el-button>
          <el-button v-if="isEdit" type="danger" size="large" @click="onDelete">Delete</el-button>
          <el-button size="large" @click="$router.back()">Cancel</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getNote, createNote, updateNote, deleteNote } from '@/api/note'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()

const formRef = ref()
const loading = ref(false)
const form = reactive({ title: '', content: '' })

const noteId = computed(() => route.params.id)
const isEdit = computed(() => !!noteId.value)

const rules = {
  title: [{ required: true, message: 'Please enter title', trigger: 'blur' }],
}

async function load() {
  if (!isEdit.value) return
  const n = await getNote(noteId.value)
  form.title = n.title || ''
  form.content = n.content || ''
}

async function onSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    if (isEdit.value) {
      await updateNote(noteId.value, form)
      ElMessage.success('Saved')
    } else {
      await createNote(form)
      ElMessage.success('Published, will show after approval')
    }
    router.push('/my/notes')
  } finally {
    loading.value = false
  }
}

async function onDelete() {
  await ElMessageBox.confirm('Delete this note?', 'Confirm', {
    type: 'warning',
  })
  await deleteNote(noteId.value)
  ElMessage.success('Deleted')
  router.push('/my/notes')
}

onMounted(load)
</script>

<style scoped>
.note-edit {
  max-width: 720px;
  margin: 0 auto;
}

.edit-card {
  background: var(--color-bg-card);
  border-radius: var(--radius);
  box-shadow: 0 2px 16px var(--color-shadow);
  padding: 32px 40px;
}

.edit-card h2 {
  margin-bottom: 24px;
}

.edit-form .el-form-item:last-child {
  margin-bottom: 0;
}
</style>
