import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import en from 'element-plus/es/locale/lang/en'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import './styles/global.css'
import { useTheme } from './composables/useTheme'

// 在 app 挂载前初始化主题，避免页面闪一下白屏
useTheme()

const app = createApp(App)
const pinia = createPinia()

// 全局注册 ElementPlus 图标（偷懒写法，后续有时间可以改成按需导入）
for (const [name, comp] of Object.entries(ElementPlusIconsVue)) {
  app.component(name, comp)
}

app.use(pinia).use(router).use(ElementPlus, { locale: en }).mount('#app')
