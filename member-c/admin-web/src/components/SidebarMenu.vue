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

    <el-menu-item v-if="hasRole(['TEACHER', 'ADMIN'])" index="/students/progress">
      <el-icon><DataAnalysis /></el-icon>
      <span>学生进度总览</span>
    </el-menu-item>

    <el-menu-item v-if="hasRole(['TEACHER', 'ADMIN'])" index="/processes">
      <el-icon><Setting /></el-icon>
      <span>流程模板管理</span>
    </el-menu-item>

    <el-menu-item v-if="hasRole(['TEACHER', 'ADMIN'])" index="/materials">
      <el-icon><DocumentChecked /></el-icon>
      <span>材料审核</span>
    </el-menu-item>

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
