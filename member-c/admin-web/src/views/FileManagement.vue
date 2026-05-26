<template>
  <div class="file-page">
    <div class="page-header">
      <div>
        <h2>文件管理</h2>
        <p>集中管理材料与证明附件，支持上传、查询、预览、下载和逻辑删除。</p>
      </div>
      <el-button type="primary" @click="openUploadDialog">上传文件</el-button>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filters">
        <el-form-item label="关联类型">
          <el-select v-model="filters.relatedType" clearable placeholder="全部" style="width: 150px">
            <el-option label="材料" value="MATERIAL" />
            <el-option label="电子证明" value="CERTIFICATE" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="上传人ID">
          <el-input v-model="filters.ownerId" clearable placeholder="ownerId" style="width: 120px" />
        </el-form-item>
        <el-form-item label="关联ID">
          <el-input v-model="filters.relatedId" clearable placeholder="relatedId" style="width: 120px" />
        </el-form-item>
        <el-form-item label="文件名">
          <el-input v-model="filters.keyword" clearable placeholder="关键字" style="width: 180px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="tableData" border stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="originalName" label="文件名" min-width="220" show-overflow-tooltip />
        <el-table-column prop="relatedType" label="关联类型" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ typeMap[row.relatedType] || row.relatedType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="relatedId" label="关联ID" width="110" />
        <el-table-column prop="ownerId" label="上传人ID" width="110" />
        <el-table-column label="大小" width="110">
          <template #default="{ row }">{{ formatFileSize(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column prop="contentType" label="类型" min-width="160" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="上传时间" width="180" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handlePreview(row)">预览</el-button>
            <el-button size="small" type="primary" @click="handleDownload(row)">下载</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end"
        @change="loadData"
      />
    </el-card>

    <el-dialog v-model="uploadDialog.visible" title="上传文件" width="520px" @close="resetUploadDialog">
      <el-form label-width="90px">
        <el-form-item label="关联类型">
          <el-select v-model="uploadDialog.relatedType" placeholder="请选择" style="width: 100%">
            <el-option label="材料" value="MATERIAL" />
            <el-option label="电子证明" value="CERTIFICATE" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="上传人ID">
          <el-input v-model="uploadDialog.ownerId" placeholder="不填则使用当前登录用户" />
        </el-form-item>
        <el-form-item label="关联ID">
          <el-input v-model="uploadDialog.relatedId" placeholder="可选，如 materialId/certificateId" />
        </el-form-item>
        <el-form-item label="文件">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
          >
            <el-button>选择文件</el-button>
            <template #tip>
              <div class="upload-tip">支持 pdf、doc、docx、xls、xlsx、png、jpg、jpeg，大小限制以后端配置为准。</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="submitUpload">上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { UploadFile, UploadInstance } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  deleteFile,
  downloadFileBlob,
  listFiles,
  previewFileBlob,
  uploadFile,
  type FileRecord,
} from '@/api/file'
import { downloadBlob, formatFileSize } from '@/utils/file'

const typeMap: Record<string, string> = {
  MATERIAL: '材料',
  CERTIFICATE: '电子证明',
  OTHER: '其他',
}

const loading = ref(false)
const uploading = ref(false)
const tableData = ref<FileRecord[]>([])
const uploadRef = ref<UploadInstance>()
const selectedFile = ref<File | null>(null)

const filters = reactive({
  page: 1,
  pageSize: 10,
  ownerId: '',
  relatedType: '',
  relatedId: '',
  keyword: '',
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

const uploadDialog = reactive({
  visible: false,
  ownerId: '',
  relatedType: 'OTHER',
  relatedId: '',
})

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    const result = await listFiles({
      page: pagination.page,
      pageSize: pagination.pageSize,
      ownerId: filters.ownerId || undefined,
      relatedType: filters.relatedType || undefined,
      relatedId: filters.relatedId || undefined,
      keyword: filters.keyword || undefined,
    })
    tableData.value = result.records || []
    pagination.total = result.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  loadData()
}

function resetSearch() {
  filters.ownerId = ''
  filters.relatedType = ''
  filters.relatedId = ''
  filters.keyword = ''
  handleSearch()
}

function openUploadDialog() {
  uploadDialog.visible = true
}

function resetUploadDialog() {
  uploadDialog.ownerId = ''
  uploadDialog.relatedType = 'OTHER'
  uploadDialog.relatedId = ''
  selectedFile.value = null
  uploadRef.value?.clearFiles()
}

function handleFileChange(file: UploadFile) {
  selectedFile.value = file.raw || null
}

function handleFileRemove() {
  selectedFile.value = null
}

async function submitUpload() {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    if (uploadDialog.ownerId) formData.append('ownerId', uploadDialog.ownerId)
    if (uploadDialog.relatedType) formData.append('relatedType', uploadDialog.relatedType)
    if (uploadDialog.relatedId) formData.append('relatedId', uploadDialog.relatedId)
    await uploadFile(formData)
    ElMessage.success('上传成功')
    uploadDialog.visible = false
    loadData()
  } finally {
    uploading.value = false
  }
}

async function handlePreview(row: FileRecord) {
  try {
    const blob = await previewFileBlob(row.id)
    const url = URL.createObjectURL(blob)
    window.open(url, '_blank')
    setTimeout(() => URL.revokeObjectURL(url), 60_000)
  } catch (e: any) {
    ElMessage.error(e.message || '预览失败')
  }
}

async function handleDownload(row: FileRecord) {
  try {
    const blob = await downloadFileBlob(row.id)
    downloadBlob(blob, row.originalName || `file-${row.id}`)
  } catch (e: any) {
    ElMessage.error(e.message || '下载失败')
  }
}

async function handleDelete(row: FileRecord) {
  await ElMessageBox.confirm(`确认删除文件“${row.originalName}”？`, '提示', { type: 'warning' })
  await deleteFile(row.id)
  ElMessage.success('已删除')
  loadData()
}
</script>

<style scoped>
.file-page {
  padding: 4px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-header h2 {
  margin: 0 0 6px;
}

.page-header p {
  margin: 0;
  color: #666;
}

.filter-card {
  margin-bottom: 16px;
}

.upload-tip {
  color: #909399;
  font-size: 12px;
  line-height: 20px;
}
</style>
