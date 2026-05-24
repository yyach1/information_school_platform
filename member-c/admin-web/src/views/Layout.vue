<template>
  <el-container class="layout">
    <el-aside width="220px">
      <div class="logo">信息学院服务平台</div>
      <SidebarMenu />
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-right">
          <span class="user-name">{{ userStore.userInfo?.realName }}</span>
          <span class="user-role">{{ roleLabel }}</span>
          <el-button text @click="handleLogout">退出</el-button>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import SidebarMenu from '@/components/SidebarMenu.vue'

const router = useRouter()
const userStore = useUserStore()

const roleLabel = computed(() => {
  const map: Record<string, string> = {
    STUDENT: '学生',
    TEACHER: '教师',
    ADMIN: '管理员',
  }
  return map[userStore.userInfo?.role || ''] || ''
})

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  height: 100vh;
}
.el-aside {
  background: #304156;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  border-bottom: 1px solid #4a5568;
  overflow: hidden;
}
.header {
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.user-name {
  font-weight: 500;
}
.user-role {
  color: #909399;
  font-size: 13px;
}
.el-main {
  background: #f0f2f5;
}
</style>
