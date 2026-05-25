<template>
  <div class="dashboard">
    <h2>欢迎，{{ userStore.userInfo?.realName }}</h2>
    <p class="role-info">当前角色：{{ roleLabel }}</p>

    <el-row :gutter="20" style="margin-top: 24px">
      <template v-if="hasRole(['STUDENT'])">
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card" @click="$router.push('/student/progress')">
            <el-icon :size="36" color="#409eff"><DataAnalysis /></el-icon>
            <div class="stat-label">我的党团进度</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card" @click="$router.push('/student/certificates')">
            <el-icon :size="36" color="#67c23a"><Tickets /></el-icon>
            <div class="stat-label">我的证明申请</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card" @click="$router.push('/student/notifications')">
            <el-icon :size="36" color="#e6a23c"><Bell /></el-icon>
            <div class="stat-label">我的通知</div>
          </el-card>
        </el-col>
      </template>

      <template v-if="hasRole(['TEACHER', 'ADMIN'])">
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card" @click="$router.push('/students/progress')">
            <el-icon :size="36" color="#67c23a"><DataAnalysis /></el-icon>
            <div class="stat-label">学生进度总览</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card" @click="$router.push('/materials')">
            <el-icon :size="36" color="#409eff"><DocumentChecked /></el-icon>
            <div class="stat-label">材料审核</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card" @click="$router.push('/certificates')">
            <el-icon :size="36" color="#e6a23c"><Tickets /></el-icon>
            <div class="stat-label">电子证明管理</div>
          </el-card>
        </el-col>
        <el-col :span="8" style="margin-top: 20px">
          <el-card shadow="hover" class="stat-card" @click="$router.push('/reports')">
            <el-icon :size="36" color="#409eff"><TrendCharts /></el-icon>
            <div class="stat-label">统计分析报表</div>
          </el-card>
        </el-col>
        <el-col :span="8" style="margin-top: 20px">
          <el-card shadow="hover" class="stat-card" @click="$router.push('/files')">
            <el-icon :size="36" color="#67c23a"><FolderOpened /></el-icon>
            <div class="stat-label">文件管理</div>
          </el-card>
        </el-col>
      </template>

      <template v-if="hasRole(['ADMIN'])">
        <el-col :span="8" style="margin-top: 20px">
          <el-card shadow="hover" class="stat-card" @click="$router.push('/users')">
            <el-icon :size="36" color="#409eff"><UserFilled /></el-icon>
            <div class="stat-label">用户管理</div>
          </el-card>
        </el-col>
        <el-col :span="8" style="margin-top: 20px">
          <el-card shadow="hover" class="stat-card" @click="$router.push('/processes')">
            <el-icon :size="36" color="#909399"><Setting /></el-icon>
            <div class="stat-label">流程模板管理</div>
          </el-card>
        </el-col>
        <el-col :span="8" style="margin-top: 20px">
          <el-card shadow="hover" class="stat-card" @click="$router.push('/logs')">
            <el-icon :size="36" color="#e6a23c"><Document /></el-icon>
            <div class="stat-label">系统日志</div>
          </el-card>
        </el-col>
      </template>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const roleLabel = computed(() => {
  const map: Record<string, string> = {
    STUDENT: '学生',
    TEACHER: '教师',
    ADMIN: '管理员',
  }
  return map[userStore.userInfo?.role || ''] || ''
})

function hasRole(roles: string[]) {
  return userStore.userInfo && roles.includes(userStore.userInfo.role)
}
</script>

<style scoped>
.dashboard h2 {
  margin: 0 0 8px;
}
.role-info {
  color: #909399;
  margin: 0;
}
.stat-card {
  text-align: center;
  padding: 24px;
  cursor: pointer;
}
.stat-label {
  margin-top: 12px;
  font-size: 16px;
  font-weight: 500;
}
</style>
