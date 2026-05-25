<template>
  <div class="page">
    <h3>我的党团进度</h3>

    <el-card>
      <div v-if="loading" style="text-align: center; padding: 40px">
        <el-icon :size="32" class="is-loading"><Loading /></el-icon>
        <p style="color: #909399; margin-top: 8px">加载中...</p>
      </div>

      <el-empty v-else-if="processes.length === 0" description="暂无可用的党团流程" />

      <div v-else class="process-list">
        <el-card v-for="p in processes" :key="p.id" shadow="hover" class="process-item" @click="handleClick(p)">
          <div class="process-header">
            <span class="process-name">{{ p.name }}</span>
            <el-tag size="small" :type="p.type === 'PARTY' ? 'danger' : 'success'">
              {{ p.type === 'PARTY' ? '党建' : p.type === 'LEAGUE' ? '团建' : p.type }}
            </el-tag>
          </div>
          <div class="process-desc">{{ p.description || '暂无描述' }}</div>
          <div class="process-actions">
            <el-button size="small" type="primary">查看进度</el-button>
          </div>
        </el-card>
      </div>
    </el-card>

    <!-- 进度弹窗 -->
    <el-dialog v-model="detailVisible" :title="currentProcess?.name" width="560px">
      <div v-if="detailLoading" style="text-align: center; padding: 20px">
        <el-icon :size="28" class="is-loading"><Loading /></el-icon>
        <p style="color: #909399">获取进度...</p>
      </div>
      <template v-else-if="detailError">
        <el-result icon="error" title="获取失败" :sub-title="detailError" />
      </template>
      <template v-else>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="流程名称">{{ currentProcess?.name }}</el-descriptions-item>
          <el-descriptions-item label="流程类型">{{ currentProcess?.type }}</el-descriptions-item>
          <el-descriptions-item label="当前状态" v-if="detail">
            <el-tag :type="detail.status === 'COMPLETED' ? 'success' : 'warning'" size="small">
              {{ detail.status === 'COMPLETED' ? '已完成' : '进行中' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="开始时间" v-if="detail?.startTime">{{ detail.startTime }}</el-descriptions-item>
        </el-descriptions>
        <div style="margin-top: 12px; color: #909399; font-size: 13px">
          详细进度和材料提交，请在手机端「信息学院服务平台」小程序中查看
        </div>
      </template>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/api/request'

interface ProcessItem {
  id: number
  name: string
  type: string
  description: string
  status: string
}

const loading = ref(false)
const processes = ref<ProcessItem[]>([])

const detailVisible = ref(false)
const detailLoading = ref(false)
const detailError = ref('')
const currentProcess = ref<ProcessItem | null>(null)
const detail = ref<any>(null)

async function loadProcesses() {
  loading.value = true
  try {
    const res = await request.get<any, { items: ProcessItem[] }>('/student/processes')
    processes.value = (res as any).items || []
  } catch {
    processes.value = []
  } finally {
    loading.value = false
  }
}

async function handleClick(p: ProcessItem) {
  currentProcess.value = p
  detailVisible.value = true
  detailLoading.value = true
  detailError.value = ''
  detail.value = null
  try {
    const res = await request.post<any, any>('/student/student-processes', { processId: p.id })
    detail.value = res
  } catch (e: any) {
    detailError.value = e?.message || '请求失败，请确认已登录为学生账号'
  } finally {
    detailLoading.value = false
  }
}

onMounted(loadProcesses)
</script>

<style scoped>
.page h3 { margin: 0 0 16px; }
.process-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 16px; }
.process-item { cursor: pointer; transition: box-shadow .2s; }
.process-item:hover { box-shadow: 0 4px 12px rgba(0,0,0,.12); }
.process-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.process-name { font-size: 16px; font-weight: 500; }
.process-desc { color: #909399; font-size: 13px; margin-bottom: 12px; min-height: 20px; }
.process-actions { text-align: right; }
</style>
