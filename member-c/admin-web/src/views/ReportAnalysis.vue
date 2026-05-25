<template>
  <div class="report-page">
    <div class="page-header">
      <div>
        <h2>统计分析报表</h2>
        <p>按材料、流程、证明申请和文件上传情况生成统计结果，支持 CSV 导出。</p>
      </div>
      <el-button type="primary" :loading="loading" @click="loadAll">刷新数据</el-button>
    </div>

    <el-row :gutter="16" class="overview-row">
      <el-col v-for="card in overviewCards" :key="card.label" :xs="24" :sm="12" :md="6" :lg="6">
        <el-card shadow="hover" class="metric-card">
          <div class="metric-label">{{ card.label }}</div>
          <div class="metric-value">{{ card.value }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="8">
        <ReportBlock title="材料状态统计" export-type="material-status" :data="materialStatus" />
      </el-col>
      <el-col :xs="24" :lg="8">
        <ReportBlock title="流程状态统计" export-type="process-status" :data="processStatus" />
      </el-col>
      <el-col :xs="24" :lg="8">
        <ReportBlock title="证明申请统计" export-type="certificate-status" :data="certificateStatus" />
      </el-col>
    </el-row>

    <el-card shadow="never" class="trend-card">
      <template #header>
        <div class="card-header">
          <span>最近 7 天文件上传趋势</span>
          <el-button size="small" @click="handleExport('upload-trend')">导出</el-button>
        </div>
      </template>
      <el-table :data="uploadTrend" border stripe>
        <el-table-column prop="date" label="日期" min-width="120" />
        <el-table-column prop="count" label="上传数" min-width="100" />
        <el-table-column label="总大小" min-width="120">
          <template #default="{ row }">{{ formatBytes(row.totalSize) }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, defineComponent, h, onMounted, ref, type PropType } from 'vue'
import { ElButton, ElCard, ElMessage, ElTable, ElTableColumn } from 'element-plus'
import {
  downloadReport,
  getCertificateStatusReport,
  getMaterialStatusReport,
  getProcessStatusReport,
  getReportOverview,
  getUploadTrendReport,
  type ReportCountItem,
  type ReportOverview,
  type UploadTrendItem,
} from '@/api/report'

const loading = ref(false)
const overview = ref<ReportOverview | null>(null)
const materialStatus = ref<ReportCountItem[]>([])
const processStatus = ref<ReportCountItem[]>([])
const certificateStatus = ref<ReportCountItem[]>([])
const uploadTrend = ref<UploadTrendItem[]>([])

const overviewCards = computed(() => [
  { label: '学生总数', value: overview.value?.studentCount ?? 0 },
  { label: '材料总数', value: overview.value?.materialCount ?? 0 },
  { label: '待审核材料', value: overview.value?.pendingMaterialCount ?? 0 },
  { label: '证明申请', value: overview.value?.certificateCount ?? 0 },
  { label: '今日上传', value: overview.value?.todayUploadCount ?? 0 },
  { label: '文件总大小', value: formatBytes(overview.value?.totalFileSize ?? 0) },
  { label: '已通过材料', value: overview.value?.approvedMaterialCount ?? 0 },
  { label: '已退回材料', value: overview.value?.returnedMaterialCount ?? 0 },
])

async function loadAll() {
  loading.value = true
  try {
    const [o, m, p, c, u] = await Promise.all([
      getReportOverview(),
      getMaterialStatusReport(),
      getProcessStatusReport(),
      getCertificateStatusReport(),
      getUploadTrendReport(7),
    ])
    overview.value = o
    materialStatus.value = mergeByName(m)
    processStatus.value = mergeByName(p)
    certificateStatus.value = mergeByName(c)
    uploadTrend.value = u
  } finally {
    loading.value = false
  }
}

async function handleExport(type: string) {
  try {
    await downloadReport(type)
    ElMessage.success('报表已开始下载')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

function mergeByName(list: ReportCountItem[]): ReportCountItem[] {
  const map = new Map<string, number>()
  for (const item of list) {
    const key = item.name.trim()
    map.set(key, (map.get(key) || 0) + item.count)
  }
  return Array.from(map, ([name, count]) => ({ name, count } as ReportCountItem))
}

function formatBytes(size: number) {
  if (!size) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let value = size
  let idx = 0
  while (value >= 1024 && idx < units.length - 1) {
    value /= 1024
    idx += 1
  }
  return `${value.toFixed(idx === 0 ? 0 : 1)} ${units[idx]}`
}

const ReportBlock = defineComponent({
  name: 'ReportBlock',
  props: {
    title: { type: String, required: true },
    exportType: { type: String, required: true },
    data: { type: Array as PropType<ReportCountItem[]>, required: true },
  },
  setup(props) {
    return () => h(ElCard, { shadow: 'never', class: 'report-block' }, {
      header: () => h('div', { class: 'card-header' }, [
        h('span', props.title),
        h(ElButton, { size: 'small', onClick: () => handleExport(props.exportType) }, () => '导出'),
      ]),
      default: () => h('div', [
        h(ElTable, { data: props.data, border: true, stripe: true }, () => [
          h(ElTableColumn, { prop: 'name', label: '维度', minWidth: 120 }),
          h(ElTableColumn, { prop: 'count', label: '数量', width: 80 }),
        ]),
      ]),
    })
  },
})

onMounted(loadAll)
</script>

<style scoped>
.report-page {
  padding: 4px;
}
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.page-header h2 {
  margin: 0 0 6px;
}
.page-header p {
  margin: 0;
  color: #606266;
}
.overview-row {
  margin-bottom: 16px;
}
.metric-card {
  margin-bottom: 16px;
}
.metric-label {
  color: #909399;
  font-size: 13px;
}
.metric-value {
  margin-top: 8px;
  font-size: 28px;
  font-weight: 700;
}
.report-block,
.trend-card {
  margin-bottom: 16px;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
