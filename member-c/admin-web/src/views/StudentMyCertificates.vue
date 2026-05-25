<template>
  <div class="page">
    <h3>我的证明申请</h3>

    <el-row :gutter="16" style="margin-bottom: 16px">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card" :class="{ active: filters.status === '' }" @click="setFilter('')">
          <div class="stat-num">{{ stats.total }}</div>
          <div class="stat-text">全部</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card" :class="{ active: filters.status === 'PENDING' }" @click="setFilter('PENDING')">
          <div class="stat-num" style="color: #e6a23c">{{ stats.pending }}</div>
          <div class="stat-text">待审核</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card" :class="{ active: filters.status === 'APPROVED' }" @click="setFilter('APPROVED')">
          <div class="stat-num" style="color: #67c23a">{{ stats.approved }}</div>
          <div class="stat-text">已通过</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card" :class="{ active: filters.status === 'ISSUED' }" @click="setFilter('ISSUED')">
          <div class="stat-num" style="color: #409eff">{{ stats.issued }}</div>
          <div class="stat-text">已发放</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card>
      <div style="margin-bottom: 16px; display: flex; justify-content: space-between; align-items: center">
        <el-radio-group v-model="filters.status" size="small" @change="loadData">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button value="PENDING">待审核</el-radio-button>
          <el-radio-button value="APPROVED">已通过</el-radio-button>
          <el-radio-button value="RETURNED">已退回</el-radio-button>
          <el-radio-button value="ISSUED">已发放</el-radio-button>
        </el-radio-group>
        <el-button type="primary" @click="openApply">申请新证明</el-button>
      </div>

      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="id" label="编号" width="80" />
        <el-table-column prop="certTypeLabel" label="类型" width="120" />
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="statusLabel" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="170" />
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openDetail(row.id)">查看</el-button>
            <el-button v-if="row.status === 'RETURNED'" size="small" type="warning" @click="openResubmit(row)">修改</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20]"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end"
        @change="loadData"
      />
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="申请详情" width="700px">
      <div v-if="detail">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="类型">{{ detail.certTypeLabel }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(detail.status)" size="small">{{ detail.statusLabel }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="标题" :span="2">{{ detail.title }}</el-descriptions-item>
          <el-descriptions-item label="说明" :span="2">{{ detail.description || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审核人">{{ detail.approverName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审核意见">{{ detail.approveComment || '-' }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ detail.applyTime }}</el-descriptions-item>
          <el-descriptions-item label="审核时间">{{ detail.approveTime || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="detail.progressNodes && detail.progressNodes.length" style="margin-top: 16px">
          <h4 style="margin: 0 0 8px">办理进度</h4>
          <el-steps :active="detail.progressNodes.length - 1" align-center>
            <el-step v-for="node in detail.progressNodes" :key="node.type" :title="node.label" :description="node.message" />
          </el-steps>
        </div>

        <div v-if="detail.status === 'ISSUED' && detail.pdfUrl" style="margin-top: 12px">
          <el-button type="success" @click="downloadPdf(detail.id)">下载证明PDF</el-button>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 申请弹窗 -->
    <el-dialog v-model="applyVisible" title="申请电子证明" width="520px" @closed="resetApplyForm">
      <el-form ref="applyFormRef" :model="applyForm" :rules="applyRules" label-width="80px">
        <el-form-item label="证明类型" prop="certType">
          <el-select v-model="applyForm.certType" placeholder="请选择" style="width: 100%" @change="onTypeChange">
            <el-option v-for="t in certTypes" :key="t.certType" :label="t.label" :value="t.certType">
              <span>{{ t.label }}</span>
              <span style="float: right; color: #909399; font-size: 12px">{{ t.requireAttachment ? '需附件' : '无需附件' }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="applyForm.title" placeholder="简要描述申请内容" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="applyForm.description" type="textarea" :rows="3" placeholder="补充说明（选填）" />
        </el-form-item>
        <el-form-item label="附件地址" v-if="needAttachment">
          <el-input v-model="applyForm.attachmentUrl" placeholder="请先上传至文件服务，粘贴URL" />
        </el-form-item>
        <el-form-item label="附件名称" v-if="needAttachment">
          <el-input v-model="applyForm.attachmentName" placeholder="附件文件名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="doApply">提交</el-button>
      </template>
    </el-dialog>

    <!-- 重新提交弹窗 -->
    <el-dialog v-model="resubmitVisible" title="修改申请" width="520px">
      <el-form :model="resubmitForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="resubmitForm.title" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="resubmitForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resubmitVisible = false">取消</el-button>
        <el-button type="primary" @click="doResubmit">重新提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getMyCertificates, getMyCertificateDetail, getCertTypes,
  applyCert, resubmitCert, downloadCertPdf,
  type CertListItem, type CertDetail, type CertTypeItem,
} from '@/api/student'

const loading = ref(false)
const list = ref<CertListItem[]>([])
const pagination = reactive({ page: 1, pageSize: 10, total: 0, pages: 0 })
const filters = reactive({ status: '' })

const stats = reactive({ total: 0, pending: 0, approved: 0, issued: 0 })

const detailVisible = ref(false)
const detail = ref<CertDetail | null>(null)

const applyVisible = ref(false)
const applyFormRef = ref<FormInstance>()
const applyingId = ref<number>(0)
const certTypes = ref<CertTypeItem[]>([])
const applyForm = reactive({ certType: '', title: '', description: '', attachmentUrl: '', attachmentName: '' })
const applyRules: FormRules = {
  certType: [{ required: true, message: '请选择证明类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
}
const submitting = ref(false)

const resubmitVisible = ref(false)
const resubmitId = ref(0)
const resubmitForm = reactive({ title: '', description: '' })

const needAttachment = computed(() => {
  const t = certTypes.value.find(c => c.certType === applyForm.certType)
  return t?.requireAttachment ?? false
})

async function loadData() {
  loading.value = true
  try {
    const res = await getMyCertificates({
      page: pagination.page, pageSize: pagination.pageSize,
      status: filters.status || undefined,
    })
    list.value = res.records
    pagination.total = res.total
    pagination.pages = res.pages
  } finally {
    loading.value = false
  }
}

async function loadStats() {
  const [all, pending, approved, issued] = await Promise.allSettled([
    getMyCertificates({ page: 1, pageSize: 1 }),
    getMyCertificates({ page: 1, pageSize: 1, status: 'PENDING' }),
    getMyCertificates({ page: 1, pageSize: 1, status: 'APPROVED' }),
    getMyCertificates({ page: 1, pageSize: 1, status: 'ISSUED' }),
  ])
  stats.total = all.status === 'fulfilled' ? all.value.total : 0
  stats.pending = pending.status === 'fulfilled' ? pending.value.total : 0
  stats.approved = approved.status === 'fulfilled' ? approved.value.total : 0
  stats.issued = issued.status === 'fulfilled' ? issued.value.total : 0
}

function setFilter(s: string) {
  filters.status = s
  loadData()
}

function statusType(s: string) {
  return { PENDING: 'warning', APPROVED: 'success', RETURNED: 'danger', ISSUED: '' }[s] || 'info'
}

async function openDetail(id: number) {
  detail.value = await getMyCertificateDetail(id)
  detailVisible.value = true
}

async function downloadPdf(id: number) {
  const res = await downloadCertPdf(id)
  if (res.pdfUrl) window.open(res.pdfUrl, '_blank')
}

async function openApply() {
  certTypes.value = []
  const res = await getCertTypes()
  certTypes.value = res.items || []
  applyVisible.value = true
}
function onTypeChange() { /* no-op */ }
function resetApplyForm() {
  applyForm.certType = ''
  applyForm.title = ''
  applyForm.description = ''
  applyForm.attachmentUrl = ''
  applyForm.attachmentName = ''
}
async function doApply() {
  const valid = await applyFormRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await applyCert({
      certType: applyForm.certType, title: applyForm.title,
      description: applyForm.description || undefined,
      attachmentUrl: applyForm.attachmentUrl || undefined,
      attachmentName: applyForm.attachmentName || undefined,
    })
    ElMessage.success('提交成功')
    applyVisible.value = false
    loadData()
    loadStats()
  } finally {
    submitting.value = false
  }
}

function openResubmit(row: CertListItem) {
  resubmitId.value = row.id
  resubmitForm.title = row.title
  resubmitForm.description = ''
  resubmitVisible.value = true
}
async function doResubmit() {
  await resubmitCert(resubmitId.value, { title: resubmitForm.title, description: resubmitForm.description || undefined })
  ElMessage.success('已重新提交')
  resubmitVisible.value = false
  loadData()
}

onMounted(() => { loadData(); loadStats() })
</script>

<style scoped>
.page h3 { margin: 0 0 16px; }
.stat-card { cursor: pointer; text-align: center; padding: 12px; border: 2px solid transparent; transition: .2s; }
.stat-card.active { border-color: #409eff; }
.stat-num { font-size: 28px; font-weight: bold; }
.stat-text { color: #909399; margin-top: 4px; font-size: 13px; }
</style>
