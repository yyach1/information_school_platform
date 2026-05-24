<template>
  <div class="page">
    <h3>学生进度总览</h3>
    <el-card>
      <el-form :inline="true" :model="filters">
        <el-form-item label="年级">
          <el-input v-model="filters.grade" placeholder="如 2023" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="班级">
          <el-input v-model="filters.className" placeholder="班级名称" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="政治面貌">
          <el-select v-model="filters.politicalStatus" placeholder="全部" clearable style="width: 140px">
            <el-option label="共青团员" value="共青团员" />
            <el-option label="中共党员" value="中共党员" />
            <el-option label="群众" value="群众" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input v-model="filters.keyword" placeholder="姓名/学号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="studentNo" label="学号" width="140" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="grade" label="年级" width="100" />
        <el-table-column prop="className" label="班级" width="140" />
        <el-table-column prop="major" label="专业" min-width="160" />
        <el-table-column prop="politicalStatus" label="政治面貌" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'" size="small">
              {{ row.status === 'ACTIVE' ? '启用' : '禁用' }}
            </el-tag>
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
import { getStudentProgress, type StudentProgressItem } from '@/api/student'
import type { PageResult } from '@/api/user'

const loading = ref(false)
const tableData = ref<StudentProgressItem[]>([])
const pagination = reactive({ page: 1, pageSize: 10, total: 0, pages: 0 })

const filters = reactive({
  grade: '',
  className: '',
  politicalStatus: '',
  keyword: '',
})

async function loadData() {
  loading.value = true
  try {
    const res = await getStudentProgress({
      page: pagination.page,
      pageSize: pagination.pageSize,
      grade: filters.grade || undefined,
      className: filters.className || undefined,
      politicalStatus: filters.politicalStatus || undefined,
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
  filters.grade = ''
  filters.className = ''
  filters.politicalStatus = ''
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
