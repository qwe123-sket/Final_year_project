import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { guest: true },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { guest: true },
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('@/views/Home.vue') },
      { path: 'note/:id', name: 'NoteDetail', component: () => import('@/views/NoteDetail.vue') },
      {
        path: 'note/edit/:id?',
        name: 'NoteEdit',
        component: () => import('@/views/NoteEdit.vue'),
        meta: { auth: true },
      },
      { path: 'my/notes', name: 'MyNotes', component: () => import('@/views/MyNotes.vue'), meta: { auth: true } },
      { path: 'my/favorites', name: 'MyFavorites', component: () => import('@/views/MyFavorites.vue'), meta: { auth: true } },
      { path: 'profile', name: 'Profile', component: () => import('@/views/Profile.vue'), meta: { auth: true } },
      { path: 'user/:id', name: 'UserPublicProfile', component: () => import('@/views/UserPublicProfile.vue') },
      { path: 'admin', name: 'Admin', component: () => import('@/views/Admin.vue'), meta: { auth: true, admin: true } },
    ],
  },
  // TODO: 404 页面
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫 —— 权限判断
router.beforeEach((to, _from, next) => {
  const store = useUserStore()

  if (to.meta.auth && !store.isLogin) return next('/login')
  if (to.meta.admin && !store.isAdmin) return next('/')
  // 已登录用户不需要再看登录页
  if (to.meta.guest && store.isLogin) return next('/')
  next()
})

export default router
