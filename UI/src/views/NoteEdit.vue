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
        <!-- 封面图上传区域 -->
        <el-form-item label="Cover image">
          <div
            class="cover-upload"
            :class="{ 'has-cover': form.coverImage }"
            @click="pickCover"
            @dragover.prevent
            @drop.prevent="onDropCover"
          >
            <img v-if="form.coverImage" :src="form.coverImage" class="cover-preview" alt="cover" />
            <div v-else class="cover-placeholder">
              <el-icon :size="32"><Picture /></el-icon>
              <span>Click or drag an image here to set cover</span>
            </div>
            <button
              v-if="form.coverImage"
              type="button"
              class="cover-remove"
              @click.stop="form.coverImage = ''"
            >
              <el-icon><Close /></el-icon>
            </button>
          </div>
          <input ref="coverInput" type="file" accept="image/*" hidden @change="onCoverSelected" />
        </el-form-item>

        <el-form-item label="Title" prop="title">
          <el-input v-model="form.title" placeholder="Enter title" maxlength="256" show-word-limit size="large" />
        </el-form-item>

        <el-form-item label="Content" prop="content">
          <div class="content-toolbar">
            <el-button size="small" @click="pickInlineImage" :loading="inlineUploading">
              <el-icon><Picture /></el-icon>
              Insert image
            </el-button>
            <div class="toolbar-right">
              <el-button-group size="small">
                <el-button :type="previewMode === 'write' ? 'primary' : 'default'" @click="previewMode = 'write'">Write</el-button>
                <el-button :type="previewMode === 'preview' ? 'primary' : 'default'" @click="previewMode = 'preview'">Preview</el-button>
              </el-button-group>
            </div>
          </div>
          <el-input
            v-show="previewMode === 'write'"
            ref="contentInput"
            v-model="form.content"
            type="textarea"
            :rows="16"
            placeholder="Supports Markdown syntax: **bold**, *italic*, # heading, ```code```, > quote, - list..."
            maxlength="10000"
            show-word-limit
          />
          <div v-show="previewMode === 'preview'" class="md-preview markdown-body" v-html="previewHtml"></div>
          <input ref="inlineInput" type="file" accept="image/*" hidden @change="onInlineSelected" />
        </el-form-item>

        <!-- 标签输入 -->
        <el-form-item label="Tags">
          <div class="tags-input-wrap">
            <el-tag
              v-for="tag in form.tags"
              :key="tag"
              closable
              size="default"
              @close="removeTag(tag)"
            >{{ tag }}</el-tag>
            <el-input
              v-if="form.tags.length < 5"
              v-model="tagInput"
              size="small"
              class="tag-add-input"
              placeholder="Add tag, press Enter"
              @keydown.enter.prevent="addTag"
            />
          </div>
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
import { getNote, createNote, updateNote, deleteNote, uploadImage } from '@/api/note'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture, Close } from '@element-plus/icons-vue'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark.css'

marked.setOptions({
  highlight(code, lang) {
    if (lang && hljs.getLanguage(lang)) return hljs.highlight(code, { language: lang }).value
    return hljs.highlightAuto(code).value
  },
  breaks: true,
  gfm: true,
})

const route = useRoute()
const router = useRouter()

const formRef = ref()
const loading = ref(false)
const inlineUploading = ref(false)
const coverInput = ref()
const inlineInput = ref()
const contentInput = ref()
const tagInput = ref('')

const previewMode = ref('write')
const form = reactive({ title: '', content: '', coverImage: '', tags: [] })

const previewHtml = computed(() => {
  if (!form.content) return '<p style="color:var(--color-text-muted)">Nothing to preview</p>'
  try { return marked(form.content) } catch { return form.content }
})

const noteId = computed(() => route.params.id)
const isEdit = computed(() => !!noteId.value)

const rules = {
  title: [{ required: true, message: 'Please enter title', trigger: 'blur' }],
}

function addTag() {
  const val = tagInput.value.trim()
  if (!val) return
  if (form.tags.includes(val)) {
    ElMessage.warning('Tag already exists')
    return
  }
  if (form.tags.length >= 5) return
  form.tags.push(val)
  tagInput.value = ''
}

function removeTag(tag) {
  form.tags = form.tags.filter(t => t !== tag)
}

function pickCover() {
  coverInput.value?.click()
}

async function handleUpload(file) {
  if (!file) return null
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('Image must be smaller than 5MB')
    return null
  }
  try {
    const res = await uploadImage(file)
    return res.url
  } catch {
    ElMessage.error('Upload failed')
    return null
  }
}

async function onCoverSelected(e) {
  const file = e.target.files?.[0]
  if (!file) return
  const url = await handleUpload(file)
  if (url) form.coverImage = url
  coverInput.value.value = ''
}

async function onDropCover(e) {
  const file = e.dataTransfer.files?.[0]
  if (!file || !file.type.startsWith('image/')) return
  const url = await handleUpload(file)
  if (url) form.coverImage = url
}

function pickInlineImage() {
  inlineInput.value?.click()
}

async function onInlineSelected(e) {
  const file = e.target.files?.[0]
  if (!file) return
  inlineUploading.value = true
  try {
    const url = await handleUpload(file)
    if (url) {
      const mdImg = `![image](${url})`
      form.content = form.content + '\n' + mdImg + '\n'
    }
  } finally {
    inlineUploading.value = false
    inlineInput.value.value = ''
  }
}

async function load() {
  if (!isEdit.value) return
  const n = await getNote(noteId.value)
  form.title = n.title || ''
  form.content = n.content || ''
  form.coverImage = n.coverImage || ''
  form.tags = n.tags || []
}

async function onSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    const payload = {
      title: form.title,
      content: form.content,
      coverImage: form.coverImage || null,
      tags: form.tags,
    }
    if (isEdit.value) {
      await updateNote(noteId.value, payload)
      ElMessage.success('Saved')
    } else {
      await createNote(payload)
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

.cover-upload {
  width: 100%;
  min-height: 180px;
  border: 2px dashed var(--color-border);
  border-radius: var(--radius);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color var(--transition), background var(--transition);
  background: var(--color-bg-card);
}

.cover-upload:hover {
  border-color: var(--color-primary);
  background: var(--color-primary-muted);
}

.cover-upload.has-cover {
  border-style: solid;
}

.cover-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  color: var(--color-text-muted);
  font-size: 0.9rem;
}

.cover-preview {
  width: 100%;
  max-height: 300px;
  object-fit: cover;
  display: block;
}

.cover-remove {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: rgba(0,0,0,0.55);
  color: #fff;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background var(--transition);
}

.cover-remove:hover {
  background: rgba(220,38,38,0.85);
}

.content-toolbar {
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.md-preview {
  min-height: 380px;
  padding: 16px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-bg-card);
  line-height: 1.8;
  font-size: 0.98rem;
  color: var(--color-text);
  overflow-y: auto;
}

.md-preview :deep(pre) {
  background: #1e1e2e;
  border-radius: 6px;
  padding: 14px 18px;
  overflow-x: auto;
  margin: 0.8em 0;
}

.md-preview :deep(pre code) {
  background: none;
  color: #cdd6f4;
  font-family: 'Fira Code', 'Consolas', monospace;
  font-size: 0.88rem;
}

.md-preview :deep(code) {
  background: var(--color-primary-muted);
  color: var(--color-primary);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.9em;
}

.md-preview :deep(blockquote) {
  border-left: 4px solid var(--color-primary);
  margin: 0.8em 0;
  padding: 0.4em 1em;
  background: var(--color-primary-muted);
  border-radius: 0 4px 4px 0;
}

.md-preview :deep(img) {
  max-width: 100%;
  border-radius: 8px;
  margin: 8px 0;
}

.md-preview :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 0.8em 0;
}

.md-preview :deep(th),
.md-preview :deep(td) {
  border: 1px solid var(--color-border);
  padding: 6px 10px;
}

.md-preview :deep(th) {
  background: var(--color-primary-muted);
}

.tags-input-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.tag-add-input {
  width: 140px;
}
</style>
