<template>
  <div class="page">
    <h3>流程模板管理</h3>
    <el-card>
      <el-form :inline="true" :model="filters">
        <el-form-item label="关键字">
          <el-input v-model="filters.keyword" placeholder="流程名称/类型" clearable style="width: 220px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="启用" value="ENABLED" />
            <el-option label="停用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="openCreate">新建流程</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="流程名称" min-width="180" />
        <el-table-column prop="type" label="类型" width="160" />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'" size="small">
              {{ row.status === 'ENABLED' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="primary" @click="openNodes(row)">配置节点</el-button>
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

    <el-dialog v-model="editVisible" :title="editMode === 'create' ? '新建流程' : '编辑流程'" width="520px">
      <el-form :model="editForm" label-width="90px">
        <el-form-item label="名称">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="类型">
          <el-input v-model="editForm.type" :disabled="editMode === 'edit'" />
        </el-form-item>
        <el-form-item label="状态" v-if="editMode === 'edit'">
          <el-select v-model="editForm.status" style="width: 160px">
            <el-option label="启用" value="ENABLED" />
            <el-option label="停用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="nodesVisible" title="配置流程节点" width="760px">
      <div style="margin-bottom: 12px; display: flex; gap: 8px">
        <el-button type="success" @click="addNode">新增节点</el-button>
        <el-button type="danger" :disabled="!selectedNodeIndexes.length" @click="removeSelectedNodes">
          删除选中
        </el-button>
      </div>
      <el-table
        :data="nodesForm.nodes"
        border
        stripe
        style="width: 100%"
        @selection-change="onNodeSelectionChange"
      >
        <el-table-column type="selection" width="48" />
        <el-table-column label="顺序" width="90">
          <template #default="{ row }">
            <el-input-number v-model="row.nodeOrder" :min="0" :controls="false" style="width: 80px" />
          </template>
        </el-table-column>
        <el-table-column label="节点名称" min-width="160">
          <template #default="{ row }">
            <el-input v-model="row.nodeName" />
          </template>
        </el-table-column>
        <el-table-column label="审核角色" width="150">
          <template #default="{ row }">
            <el-input v-model="row.approverRole" placeholder="TEACHER/ADMIN/STUDENT" />
          </template>
        </el-table-column>
        <el-table-column label="材料要求(JSON/逗号)" min-width="220">
          <template #default="{ row }">
            <el-input v-model="row.requiredMaterial" placeholder='例如 ["ID_CARD_FRONT"] 或 ID_CARD_FRONT,ID_CARD_BACK' />
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="nodesVisible = false">取消</el-button>
        <el-button type="primary" @click="submitNodes">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { createProcess, getProcessNodes, listProcesses, updateProcess, updateProcessNodes, type ProcessItem } from '@/api/process'

const loading = ref(false)
const tableData = ref<ProcessItem[]>([])
const pagination = reactive({ page: 1, pageSize: 10, total: 0, pages: 0 })

const filters = reactive({ keyword: '', status: '' })

const editVisible = ref(false)
const editMode = ref<'create' | 'edit'>('create')
const editForm = reactive<{ id?: number; name: string; type: string; description?: string; status: string }>({
  name: '',
  type: '',
  description: '',
  status: 'ENABLED',
})

const nodesVisible = ref(false)
const nodesProcessId = ref<number | null>(null)
const nodesForm = reactive<{ nodes: Array<{ nodeName: string; nodeOrder: number; approverRole: string; requiredMaterial?: string }> }>(
  { nodes: [] }
)
const selectedNodeIndexes = ref<number[]>([])

async function loadData() {
  loading.value = true
  try {
    const res = await listProcesses({
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: filters.keyword || undefined,
      status: filters.status || undefined,
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
  filters.keyword = ''
  filters.status = ''
  pagination.page = 1
  loadData()
}

function openCreate() {
  editMode.value = 'create'
  editForm.id = undefined
  editForm.name = ''
  editForm.type = ''
  editForm.description = ''
  editForm.status = 'ENABLED'
  editVisible.value = true
}

function openEdit(row: ProcessItem) {
  editMode.value = 'edit'
  editForm.id = row.id
  editForm.name = row.name
  editForm.type = row.type
  editForm.description = row.description || ''
  editForm.status = row.status
  editVisible.value = true
}

async function submitEdit() {
  if (!editForm.name || !editForm.type) {
    ElMessage.error('请填写名称与类型')
    return
  }
  if (editMode.value === 'create') {
    await createProcess({ name: editForm.name, type: editForm.type, description: editForm.description || undefined })
    ElMessage.success('创建成功')
  } else {
    await updateProcess(editForm.id as number, {
      name: editForm.name,
      description: editForm.description || undefined,
      status: editForm.status,
    })
    ElMessage.success('保存成功')
  }
  editVisible.value = false
  loadData()
}

async function openNodes(row: ProcessItem) {
  nodesProcessId.value = row.id
  try {
    const nodes = await getProcessNodes(row.id)
    nodesForm.nodes = (nodes || []).map((n) => ({
      nodeName: n.nodeName,
      nodeOrder: Number(n.nodeOrder),
      approverRole: n.approverRole,
      requiredMaterial: n.requiredMaterial || '',
    }))
    if (!nodesForm.nodes.length) {
      nodesForm.nodes = [
        { nodeName: '材料提交', nodeOrder: 0, approverRole: 'STUDENT', requiredMaterial: '' },
        { nodeName: '辅导员审核', nodeOrder: 1, approverRole: 'TEACHER', requiredMaterial: '' },
      ]
    }
  } catch {
    nodesForm.nodes = [
      { nodeName: '材料提交', nodeOrder: 0, approverRole: 'STUDENT', requiredMaterial: '' },
      { nodeName: '辅导员审核', nodeOrder: 1, approverRole: 'TEACHER', requiredMaterial: '' },
    ]
  }
  selectedNodeIndexes.value = []
  nodesVisible.value = true
}

function addNode() {
  nodesForm.nodes.push({ nodeName: '', nodeOrder: nodesForm.nodes.length, approverRole: 'TEACHER', requiredMaterial: '' })
}

function onNodeSelectionChange(rows: any[]) {
  const indexes: number[] = []
  rows.forEach((r) => {
    const idx = nodesForm.nodes.indexOf(r)
    if (idx >= 0) indexes.push(idx)
  })
  selectedNodeIndexes.value = indexes
}

function removeSelectedNodes() {
  nodesForm.nodes = nodesForm.nodes.filter((_n, idx) => !selectedNodeIndexes.value.includes(idx))
  selectedNodeIndexes.value = []
}

async function submitNodes() {
  if (!nodesProcessId.value) return
  const nodes = nodesForm.nodes
    .map((n) => ({
      nodeName: n.nodeName,
      nodeOrder: Number(n.nodeOrder),
      approverRole: n.approverRole,
      requiredMaterial: n.requiredMaterial || undefined,
    }))
    .sort((a, b) => a.nodeOrder - b.nodeOrder)
  await updateProcessNodes(nodesProcessId.value, { nodes })
  ElMessage.success('节点已保存')
  nodesVisible.value = false
}

onMounted(loadData)
</script>

<style scoped>
.page h3 {
  margin: 0 0 16px;
}
</style>

