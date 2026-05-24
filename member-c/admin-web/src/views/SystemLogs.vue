<template>
  <div class="page">
    <h3>系统日志</h3>
    <el-card>
      <el-form :inline="true" :model="filters">
        <el-form-item label="操作类型">
          <el-input v-model="filters.operationType" placeholder="如 LOGIN" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="结果">
          <el-select v-model="filters.result" placeholder="全部" clearable style="width: 120px">
            <el-option label="成功" value="SUCCESS" />
            <el-option label="失败" value="FAIL" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="filters.startTime"
            type="datetime"
            placeholder="开始时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 190px"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="filters.endTime"
            type="datetime"
            placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 190px"
          />
        </el-form-item>
        <el-form-item label="关键字">
          <el-input v-model="filters.keyword" placeholder="操作内容" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="realName" label="操作人" width="120" />
        <el-table-column prop="operationType" label="操作类型" width="160" />
        <el-table-column prop="operationContent" label="操作内容" min-width="180" show-overflow-tooltip />
        <el-table-column prop="result" label="结果" width="80">
          <template #default="{ row }">
            <el-tag :type="row.result === 'SUCCESS' ? 'success' : 'danger'" size="small">
              {{ row.result === 'SUCCESS' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="failReason" label="失败原因" width="150" show-overflow-tooltip />
        <el-table-column prop="ipAddress" label="IP" width="140" />
        <el-table-column prop="operationTime" label="操作时间" width="170" />
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
import { getLogs, type OperationLogItem } from '@/api/log'

const loading = ref(false)
const tableData = ref<OperationLogItem[]>([])
const pagination = reactive({ page: 1, pageSize: 10, total: 0, pages: 0 })

const filters = reactive({
  operationType: '',
  result: '',
  startTime: '',
  endTime: '',
  keyword: '',
})

async function loadData() {
  loading.value = true
  try {
    const res = await getLogs({
      page: pagination.page,
      pageSize: pagination.pageSize,
      operationType: filters.operationType || undefined,
      result: filters.result || undefined,
      startTime: filters.startTime || undefined,
      endTime: filters.endTime || undefined,
      keyword: filters.keyword || undefined,
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
  filters.operationType = ''
  filters.result = ''
  filters.startTime = ''
  filters.endTime = ''
  filters.keyword = ''
  pagination.page = 1
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.page h3 {
  margin: 0 0 16px;
}
</style>
