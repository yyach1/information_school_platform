import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
      },
      {
        path: 'students/progress',
        name: 'StudentProgress',
        component: () => import('@/views/StudentProgress.vue'),
        meta: { roles: ['TEACHER', 'ADMIN'] },
      },
      {
        path: 'processes',
        name: 'ProcessManagement',
        component: () => import('@/views/ProcessManagement.vue'),
        meta: { roles: ['TEACHER', 'ADMIN'] },
      },
      {
        path: 'materials',
        name: 'MaterialAudit',
        component: () => import('@/views/MaterialAudit.vue'),
        meta: { roles: ['TEACHER', 'ADMIN'] },
      },
      {
        path: 'certificates',
        name: 'CertificateManagement',
        component: () => import('@/views/CertificateManagement.vue'),
        meta: { roles: ['TEACHER', 'ADMIN'] },
      },
      {
        path: 'reports',
        name: 'ReportAnalysis',
        component: () => import('@/views/ReportAnalysis.vue'),
        meta: { roles: ['TEACHER', 'ADMIN'] },
      },
      {
        path: 'files',
        name: 'FileManagement',
        component: () => import('@/views/FileManagement.vue'),
        meta: { roles: ['TEACHER', 'ADMIN'] },
      },
      {
        path: 'logs',
        name: 'SystemLogs',
        component: () => import('@/views/SystemLogs.vue'),
        meta: { roles: ['ADMIN'] },
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('@/views/UserManagement.vue'),
        meta: { roles: ['ADMIN'] },
      },
      // 学生端页面
      {
        path: 'student',
        name: 'StudentDashboard',
        redirect: '/student/certificates',
      },
      {
        path: 'student/certificates',
        name: 'StudentMyCertificates',
        component: () => import('@/views/StudentMyCertificates.vue'),
        meta: { roles: ['STUDENT'] },
      },
      {
        path: 'student/notifications',
        name: 'StudentNotifications',
        component: () => import('@/views/StudentNotifications.vue'),
        meta: { roles: ['STUDENT'] },
      },
      {
        path: 'student/progress',
        name: 'StudentProgressPage',
        component: () => import('@/views/StudentProgressPage.vue'),
        meta: { roles: ['STUDENT'] },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  if (to.path === '/login') {
    if (token) {
      next('/dashboard')
    } else {
      next()
    }
    return
  }

  if (!token) {
    next('/login')
    return
  }

  const userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')
  const requiredRoles = to.meta.roles as string[] | undefined
  if (requiredRoles && userInfo) {
    if (!requiredRoles.includes(userInfo.role)) {
      next('/dashboard')
      return
    }
  }

  next()
})

export default router
