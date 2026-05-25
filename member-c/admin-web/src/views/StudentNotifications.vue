<template>
  <div class="page">
    <h3>我的通知</h3>
    <el-card>
      <el-tabs v-model="activeTab" @tab-change="loadData">
        <el-tab-pane label="全部" name="" />
        <el-tab-pane label="通知" name="NOTICE" />
        <el-tab-pane label="待办" name="TODO" />
      </el-tabs>

      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="id" label="序号" width="80" />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <el-tag :type="row.notificationType === 'TODO' ? 'warning' : 'info'" size="small">
              {{ row.notificationType === 'TODO' ? '待办' : '通知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
        <el-table-column prop="readStatus" label="状态" width="80">
          <template #default="{ row }">
            <span :style="{ color: row.readStatus === 'UNREAD' ? '#f56c6c' : '#909399' }">
              {{ row.readStatus === 'UNREAD' ? '未读' : '已读' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" width="170" />
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button v-if="row.readStatus === 'UNREAD'" size="small" text type="primary" @click="markRead(row.id)">标为已读</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end"
        @change="loadData"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyNotificationList, readNotification, type NotificationItem } from '@/api/student'

const loading = ref(false)
const list = ref<NotificationItem[]>([])
const pagination = reactive({ page: 1, pageSize: 10, total: 0, pages: 0 })
const activeTab = ref('')

async function loadData() {
  loading.value = true
  try {
    const res = await getMyNotificationList({
      page: pagination.page, pageSize: pagination.pageSize,
      type: activeTab.value || undefined,
    })
    list.value = res.records
    pagination.total = res.total
    pagination.pages = res.pages
  } finally {
    loading.value = false
  }
}

async function markRead(id: number) {
  await readNotification(id)
  ElMessage.success('已标记为已读')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.page h3 { margin: 0 0 16px; }
</style>
