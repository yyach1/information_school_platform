<template>
  <div class="page">
    <h3>电子证明管理</h3>

    <el-row :gutter="16" style="margin-bottom: 16px">
      <el-col :span="6" v-for="s in statCards" :key="s.label">
        <el-card shadow="hover">
          <div style="text-align: center">
            <div style="font-size: 28px; font-weight: bold; color: s.color">{{ s.count }}</div>
            <div style="color: #909399; margin-top: 4px">{{ s.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card>
      <el-form :inline="true" :model="filters">
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已退回" value="RETURNED" />
            <el-option label="已发放" value="ISSUED" />
          </el-select>
        </el-form-item>
        <el-form-item label="证明类型">
          <el-select v-model="filters.certType" placeholder="全部" clearable style="width: 140px">
            <el-option label="在学证明" value="ENROLLMENT" />
            <el-option label="请假申请" value="LEAVE" />
            <el-option label="盖章申请" value="SEAL" />
            <el-option label="党员证明" value="PARTY" />
            <el-option label="成绩单证明" value="TRANSCRIPT" />
          </el-select>
        </el-form-item>
        <el-form-item label="年级">
          <el-input v-model="filters.grade" placeholder="如 2024" clearable style="width: 120px" />
        </el-form-item>
        <el-form-item label="班级">
          <el-input v-model="filters.className" placeholder="班级名称" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="certTypeLabel" label="证明类型" width="120" />
        <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        <el-table-column prop="studentNo" label="学号" width="140" />
        <el-table-column prop="className" label="班级" width="140" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 'PENDING' ? 'warning' : row.status === 'APPROVED' ? 'success' : row.status === 'RETURNED' ? 'danger' : 'info'"
              size="small"
            >
              {{ row.statusLabel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openDetail(row.id)">查看</el-button>
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

    <el-dialog v-model="detailVisible" title="证明详情" width="800px">
      <div v-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="学生">{{ detail.studentName }}（{{ detail.studentNo }}）</el-descriptions-item>
          <el-descriptions-item label="班级">{{ detail.className }}</el-descriptions-item>
          <el-descriptions-item label="证明类型">{{ detail.certTypeLabel }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag
              :type="detail.status === 'PENDING' ? 'warning' : detail.status === 'APPROVED' ? 'success' : detail.status === 'RETURNED' ? 'danger' : 'info'"
              size="small"
            >
              {{ detail.statusLabel }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="标题" :span="2">{{ detail.title }}</el-descriptions-item>
          <el-descriptions-item label="说明" :span="2">{{ detail.description || '-' }}</el-descriptions-item>
          <el-descriptions-item label="附件" :span="2">
            <el-link v-if="detail.attachmentUrl" :href="detail.attachmentUrl" target="_blank" type="primary">
              {{ detail.attachmentName || detail.attachmentUrl }}
            </el-link>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="审核人">{{ detail.approverName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审核意见">{{ detail.approveComment || '-' }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ detail.applyTime }}</el-descriptions-item>
          <el-descriptions-item label="审核时间">{{ detail.approveTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="发放时间">{{ detail.issueTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="PDF" :span="2">
            <el-link v-if="detail.pdfUrl" :href="detail.pdfUrl" target="_blank" type="primary">下载PDF</el-link>
            <span v-else>-</span>
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="detail.progressNodes && detail.progressNodes.length" style="margin-top: 16px">
          <h4 style="margin: 0 0 8px">办理进度</h4>
          <el-steps :active="detail.progressNodes.length - 1" align-center>
            <el-step
              v-for="node in detail.progressNodes"
              :key="node.type"
              :title="node.label"
              :description="node.message + (node.time ? ' (' + node.time + ')' : '')"
            />
          </el-steps>
        </div>
      </div>

      <div style="margin-top: 16px; display: flex; gap: 8px" v-if="detail">
        <el-button v-if="detail.status === 'PENDING'" type="success" @click="handleApprove">通过</el-button>
        <el-button v-if="detail.status === 'PENDING'" type="warning" @click="openReturn">退回</el-button>
        <el-button v-if="detail.status === 'APPROVED'" type="primary" @click="openIssue">办理发放</el-button>
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="returnVisible" title="退回原因" width="520px">
      <el-input v-model="returnComment" type="textarea" :rows="4" placeholder="请输入退回原因（必填）" />
      <template #footer>
        <el-button @click="returnVisible = false">取消</el-button>
        <el-button type="primary" @click="doReturn">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="issueVisible" title="办理发放" width="520px">
      <el-form label-width="80px">
        <el-form-item label="PDF地址">
          <el-input v-model="pdfUrl" placeholder="请输入PDF文件地址（必填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="issueVisible = false">取消</el-button>
        <el-button type="primary" @click="doIssue">确认发放</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getCertificateList,
  getCertificateDetail,
  auditCertificate,
  issueCertificate,
  getCertificateStats,
  type CertificateDetail,
} from '@/api/certificate'

const loading = ref(false)
const tableData = ref<CertificateDetail[]>([])
const pagination = reactive({ page: 1, pageSize: 10, total: 0, pages: 0 })
const filters = reactive({ status: 'PENDING', certType: '', grade: '', className: '' })

const statCards = ref([
  { label: '待审核', count: 0, color: '#e6a23c' },
  { label: '已通过', count: 0, color: '#67c23a' },
  { label: '已退回', count: 0, color: '#f56c6c' },
  { label: '已发放', count: 0, color: '#409eff' },
])

const detailVisible = ref(false)
const detail = ref<CertificateDetail | null>(null)

const returnVisible = ref(false)
const returnComment = ref('')

const issueVisible = ref(false)
const pdfUrl = ref('')

async function loadStats() {
  const stats = await getCertificateStats()
  statCards.value[0].count = stats.PENDING || 0
  statCards.value[1].count = stats.APPROVED || 0
  statCards.value[2].count = stats.RETURNED || 0
  statCards.value[3].count = stats.ISSUED || 0
}

async function loadData() {
  loading.value = true
  try {
    const res = await getCertificateList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      status: filters.status || undefined,
      certType: filters.certType || undefined,
      grade: filters.grade || undefined,
      className: filters.className || undefined,
    })
    tableData.value = res.records
    pagination.total = res.total
    pagination.pages = res.pages
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  loadData()
  loadStats()
}

function handleReset() {
  filters.status = 'PENDING'
  filters.certType = ''
  filters.grade = ''
  filters.className = ''
  pagination.page = 1
  loadData()
  loadStats()
}

async function openDetail(id: number) {
  detail.value = await getCertificateDetail(id)
  detailVisible.value = true
}

async function handleApprove() {
  if (!detail.value) return
  await auditCertificate(detail.value.id, { result: 'APPROVED', comment: '通过' })
  ElMessage.success('审核通过')
  detailVisible.value = false
  loadData()
  loadStats()
}

function openReturn() {
  returnComment.value = ''
  returnVisible.value = true
}

async function doReturn() {
  if (!detail.value) return
  if (!returnComment.value.trim()) {
    ElMessage.error('请填写退回原因')
    return
  }
  await auditCertificate(detail.value.id, { result: 'RETURNED', comment: returnComment.value.trim() })
  ElMessage.success('已退回')
  returnVisible.value = false
  detailVisible.value = false
  loadData()
  loadStats()
}

function openIssue() {
  pdfUrl.value = ''
  issueVisible.value = true
}

async function doIssue() {
  if (!detail.value) return
  if (!pdfUrl.value.trim()) {
    ElMessage.error('请填写PDF地址')
    return
  }
  await issueCertificate(detail.value.id, { pdfUrl: pdfUrl.value.trim() })
  ElMessage.success('已发放')
  issueVisible.value = false
  detailVisible.value = false
  loadData()
  loadStats()
}

onMounted(() => {
  loadData()
  loadStats()
})
</script>

<style scoped>
.page h3 {
  margin: 0 0 16px;
}
</style>
