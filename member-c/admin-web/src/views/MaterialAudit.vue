<template>
  <div class="page">
    <h3>材料审核工作台</h3>
    <el-card>
      <el-form :inline="true" :model="filters">
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部" clearable style="width: 160px">
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已退回" value="RETURNED" />
          </el-select>
        </el-form-item>
        <el-form-item label="年级">
          <el-input v-model="filters.grade" placeholder="如 2023" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="班级">
          <el-input v-model="filters.className" placeholder="班级名称" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="流程类型">
          <el-input v-model="filters.processType" placeholder="如 LEAGUE_JOIN" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="studentNo" label="学号" width="140" />
        <el-table-column prop="studentName" label="姓名" width="120" />
        <el-table-column prop="grade" label="年级" width="100" />
        <el-table-column prop="className" label="班级" width="140" />
        <el-table-column prop="processName" label="流程" min-width="160" />
        <el-table-column prop="nodeName" label="节点" min-width="140" />
        <el-table-column prop="materialType" label="材料类型" width="160" />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PENDING' ? 'warning' : row.status === 'APPROVED' ? 'success' : 'info'" size="small">
              {{ row.status === 'PENDING' ? '待审核' : row.status === 'APPROVED' ? '已通过' : '已退回' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="180" />
        <el-table-column label="操作" width="140" fixed="right">
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

    <el-dialog v-model="detailVisible" title="材料详情" width="780px">
      <div v-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="学生">{{ detail.studentName }}（{{ detail.studentNo }}）</el-descriptions-item>
          <el-descriptions-item label="班级">{{ detail.grade }} {{ detail.className }}</el-descriptions-item>
          <el-descriptions-item label="流程">{{ detail.processName }}（{{ detail.processType }}）</el-descriptions-item>
          <el-descriptions-item label="节点">{{ detail.nodeName }}</el-descriptions-item>
          <el-descriptions-item label="材料类型">{{ detail.materialType }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ detail.status }}</el-descriptions-item>
          <el-descriptions-item label="文件名" :span="2">{{ detail.fileName }}</el-descriptions-item>
          <el-descriptions-item label="附件" :span="2">
            <el-link :href="detail.fileUrl" target="_blank" type="primary">{{ detail.fileUrl }}</el-link>
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ detail.description || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div style="margin-top: 16px" v-if="detail.approvals && detail.approvals.length">
          <h4 style="margin: 0 0 8px">审核记录</h4>
          <el-table :data="detail.approvals" border stripe size="small">
            <el-table-column prop="approveTime" label="时间" width="180" />
            <el-table-column prop="approverName" label="审核人" width="140" />
            <el-table-column prop="result" label="结果" width="120" />
            <el-table-column prop="comment" label="意见" min-width="200" />
          </el-table>
        </div>
      </div>

      <div style="margin-top: 16px; display: flex; gap: 8px" v-if="detail && detail.status === 'PENDING'">
        <el-button type="success" @click="approve">通过</el-button>
        <el-button type="warning" @click="openReturn">退回</el-button>
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
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { auditMaterial, getMaterialDetail, listMaterials, type MaterialDetail, type MaterialListItem } from '@/api/material'

const loading = ref(false)
const tableData = ref<MaterialListItem[]>([])
const pagination = reactive({ page: 1, pageSize: 10, total: 0, pages: 0 })

const filters = reactive({ status: 'PENDING', grade: '', className: '', processType: '' })

const detailVisible = ref(false)
const detail = ref<MaterialDetail | null>(null)

const returnVisible = ref(false)
const returnComment = ref('')

async function loadData() {
  loading.value = true
  try {
    const res = await listMaterials({
      page: pagination.page,
      pageSize: pagination.pageSize,
      status: filters.status || undefined,
      grade: filters.grade || undefined,
      className: filters.className || undefined,
      processType: filters.processType || undefined,
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
}

function handleReset() {
  filters.status = 'PENDING'
  filters.grade = ''
  filters.className = ''
  filters.processType = ''
  pagination.page = 1
  loadData()
}

async function openDetail(id: number) {
  detail.value = await getMaterialDetail(id)
  detailVisible.value = true
}

async function approve() {
  if (!detail.value) return
  await auditMaterial(detail.value.id, { result: 'APPROVED', comment: '通过' })
  ElMessage.success('已通过')
  detailVisible.value = false
  loadData()
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
  await auditMaterial(detail.value.id, { result: 'RETURNED', comment: returnComment.value.trim() })
  ElMessage.success('已退回')
  returnVisible.value = false
  detailVisible.value = false
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.page h3 {
  margin: 0 0 16px;
}
</style>
