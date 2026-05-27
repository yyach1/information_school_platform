<template>
  <el-menu
    :default-active="route.path"
    router
    background-color="#304156"
    text-color="#bfcbd9"
    active-text-color="#409eff"
  >
    <el-menu-item index="/dashboard">
      <el-icon><HomeFilled /></el-icon>
      <span>首页</span>
    </el-menu-item>

    <template v-if="hasRole(['STUDENT'])">
      <el-menu-item index="/student/progress">
        <el-icon><DataAnalysis /></el-icon>
        <span>我的党团进度</span>
      </el-menu-item>
      <el-menu-item index="/student/certificates">
        <el-icon><Tickets /></el-icon>
        <span>我的证明申请</span>
      </el-menu-item>
      <el-menu-item index="/student/notifications">
        <el-icon><Bell /></el-icon>
        <span>我的通知</span>
      </el-menu-item>
      <el-menu-item index="/student/profile">
        <el-icon><UserFilled /></el-icon>
        <span>个人中心</span>
      </el-menu-item>
    </template>

    <template v-if="hasRole(['TEACHER', 'ADMIN'])">
      <el-menu-item index="/students/progress">
        <el-icon><DataAnalysis /></el-icon>
        <span>学生进度总览</span>
      </el-menu-item>
      <el-menu-item index="/processes">
        <el-icon><Setting /></el-icon>
        <span>流程模板管理</span>
      </el-menu-item>
      <el-menu-item index="/materials">
        <el-icon><DocumentChecked /></el-icon>
        <span>材料审核</span>
      </el-menu-item>
      <el-menu-item index="/certificates">
        <el-icon><Tickets /></el-icon>
        <span>电子证明管理</span>
      </el-menu-item>
      <el-menu-item index="/reports">
        <el-icon><TrendCharts /></el-icon>
        <span>统计分析报表</span>
      </el-menu-item>
      <el-menu-item index="/files">
        <el-icon><FolderOpened /></el-icon>
        <span>文件管理</span>
      </el-menu-item>
    </template>

    <el-menu-item v-if="hasRole(['ADMIN'])" index="/users">
      <el-icon><UserFilled /></el-icon>
      <span>用户管理</span>
    </el-menu-item>

    <el-menu-item v-if="hasRole(['ADMIN'])" index="/logs">
      <el-icon><Document /></el-icon>
      <span>系统日志</span>
    </el-menu-item>
  </el-menu>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()

function hasRole(roles: string[]) {
  return userStore.userInfo && roles.includes(userStore.userInfo.role)
}
</script>
