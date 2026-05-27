<template>
  <div class="page">
    <h3>用户管理</h3>
    <el-card>
      <div class="toolbar">
        <el-form :inline="true" :model="filters">
          <el-form-item label="角色">
            <el-select v-model="filters.role" placeholder="全部" clearable style="width: 130px">
              <el-option label="学生" value="STUDENT" />
              <el-option label="教师" value="TEACHER" />
              <el-option label="管理员" value="ADMIN" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="filters.status" placeholder="全部" clearable style="width: 120px">
              <el-option label="启用" value="ACTIVE" />
              <el-option label="禁用" value="DISABLED" />
            </el-select>
          </el-form-item>
          <el-form-item label="关键字">
            <el-input v-model="filters.keyword" placeholder="用户名/姓名" clearable style="width: 180px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button type="success" @click="showCreateDialog">新增用户</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="userId" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" width="130" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ roleMap[row.role] || row.role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'" size="small">
              {{ row.status === 'ACTIVE' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button size="small" @click="handleResetPwd(row.userId)">重置密码</el-button>
            <el-button
              size="small"
              :type="row.status === 'ACTIVE' ? 'danger' : 'success'"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 'ACTIVE' ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="showDeleteDialog(row)">删除</el-button>
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

    <!-- Create/Edit Dialog -->
    <el-dialog
      v-model="dialog.visible"
      :title="dialog.isEdit ? '编辑用户' : '新增用户'"
      width="560px"
      @close="resetDialog"
    >
      <el-form ref="dialogFormRef" :model="dialog.form" :rules="dialogRules" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="dialog.form.username" :disabled="dialog.isEdit" />
        </el-form-item>
        <el-form-item v-if="!dialog.isEdit" label="密码" prop="password">
          <el-input v-model="dialog.form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="dialog.form.realName" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="dialog.form.role" :disabled="dialog.isEdit" style="width: 100%">
            <el-option label="学生" value="STUDENT" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="dialog.form.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="dialog.form.email" />
        </el-form-item>

        <template v-if="dialog.form.role === 'STUDENT'">
          <el-divider content-position="left">学生信息</el-divider>
          <el-form-item label="学号">
            <el-input v-model="dialog.form.studentNo" />
          </el-form-item>
          <el-form-item label="班级">
            <el-input v-model="dialog.form.className" />
          </el-form-item>
          <el-form-item label="年级">
            <el-input v-model="dialog.form.grade" />
          </el-form-item>
          <el-form-item label="专业">
            <el-input v-model="dialog.form.major" />
          </el-form-item>
          <el-form-item label="政治面貌">
            <el-input v-model="dialog.form.politicalStatus" />
          </el-form-item>
        </template>

        <template v-if="dialog.form.role === 'TEACHER'">
          <el-divider content-position="left">教师信息</el-divider>
          <el-form-item label="工号">
            <el-input v-model="dialog.form.teacherNo" />
          </el-form-item>
          <el-form-item label="部门">
            <el-input v-model="dialog.form.department" />
          </el-form-item>
          <el-form-item label="职称">
            <el-input v-model="dialog.form.title" />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="dialog.submitting" @click="handleDialogSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 删除确认弹窗 -->
    <el-dialog v-model="deleteDialog.visible" title="删除用户" width="420px">
      <div style="margin-bottom: 12px">
        <el-alert type="warning" show-icon :closable="false">
          <template #title>
            确认删除用户 <b>{{ deleteDialog.targetName }}</b>？此操作不可恢复。
          </template>
        </el-alert>
      </div>
      <el-form label-width="80px">
        <el-form-item label="管理员密码">
          <el-input v-model="deleteDialog.adminPassword" type="password" show-password placeholder="请输入管理员密码以确认" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deleteDialog.visible = false">取消</el-button>
        <el-button type="danger" :loading="deleteDialog.submitting" @click="handleDelete">确认删除</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import {
  getUserList,
  createUser,
  updateUser,
  deleteUser,
  resetPassword,
  updateUserStatus,
  type UserInfo,
} from '@/api/user'

const roleMap: Record<string, string> = { STUDENT: '学生', TEACHER: '教师', ADMIN: '管理员' }
const loading = ref(false)
const tableData = ref<UserInfo[]>([])
const pagination = reactive({ page: 1, pageSize: 10, total: 0, pages: 0 })
const filters = reactive({ keyword: '', role: '', status: '' })

const dialogFormRef = ref<FormInstance>()
const dialog = reactive({
  visible: false,
  isEdit: false,
  submitting: false,
  form: {
    username: '', password: '', realName: '', role: '', phone: '', email: '',
    studentNo: '', className: '', grade: '', major: '', politicalStatus: '',
    teacherNo: '', department: '', title: '',
  },
  editId: null as number | null,
})

const dialogRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  studentNo: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: Function) => {
        if (dialog.form.role === 'STUDENT' && !value) callback(new Error('学生学号必填'))
        else callback()
      },
      trigger: 'blur',
    },
  ],
  teacherNo: [
    { required: true, message: '请输入工号', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: Function) => {
        if (dialog.form.role === 'TEACHER' && !value) callback(new Error('教师工号必填'))
        else callback()
      },
      trigger: 'blur',
    },
  ],
}

async function loadData() {
  loading.value = true
  try {
    const res = await getUserList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: filters.keyword || undefined,
      role: filters.role || undefined,
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

function showCreateDialog() {
  dialog.isEdit = false
  dialog.editId = null
  dialog.visible = true
}

function showEditDialog(row: UserInfo) {
  dialog.isEdit = true
  dialog.editId = row.userId
  dialog.form.username = row.username
  dialog.form.realName = row.realName
  dialog.form.role = row.role
  dialog.form.phone = row.phone || ''
  dialog.form.email = row.email || ''
  dialog.form.studentNo = row.studentNo || ''
  dialog.form.className = row.className || ''
  dialog.form.grade = row.grade || ''
  dialog.form.major = row.major || ''
  dialog.form.politicalStatus = row.politicalStatus || ''
  dialog.form.teacherNo = row.teacherNo || ''
  dialog.form.department = row.department || ''
  dialog.form.title = row.title || ''
  dialog.visible = true
}

function resetDialog() {
  dialog.form = {
    username: '', password: '', realName: '', role: '', phone: '', email: '',
    studentNo: '', className: '', grade: '', major: '', politicalStatus: '',
    teacherNo: '', department: '', title: '',
  }
  dialog.editId = null
  dialogFormRef.value?.resetFields()
}

async function handleDialogSubmit() {
  const valid = await dialogFormRef.value?.validate().catch(() => false)
  if (!valid) return
  dialog.submitting = true
  try {
    if (dialog.isEdit && dialog.editId) {
      await updateUser(dialog.editId, {
        realName: dialog.form.realName,
        phone: dialog.form.phone,
        email: dialog.form.email,
        studentNo: dialog.form.studentNo,
        className: dialog.form.className,
        grade: dialog.form.grade,
        major: dialog.form.major,
        politicalStatus: dialog.form.politicalStatus,
        teacherNo: dialog.form.teacherNo,
        department: dialog.form.department,
        title: dialog.form.title,
      })
      ElMessage.success('编辑成功')
    } else {
      await createUser(dialog.form as any)
      ElMessage.success('创建成功')
    }
    dialog.visible = false
    loadData()
  } finally {
    dialog.submitting = false
  }
}

async function handleResetPwd(id: number) {
  try {
    const { value } = await ElMessageBox.prompt('请输入新密码（至少6位）', '重置密码', {
      inputType: 'password',
      inputValidator: (val) => val && val.length >= 6 ? true : '密码至少6位',
    })
    await resetPassword(id, value)
    ElMessage.success('密码重置成功')
  } catch {
    // user cancelled
  }
}

const deleteDialog = reactive({
  visible: false,
  targetId: null as number | null,
  targetName: '',
  adminPassword: '',
  submitting: false,
})

function showDeleteDialog(row: UserInfo) {
  deleteDialog.targetId = row.userId
  deleteDialog.targetName = row.realName || row.username
  deleteDialog.adminPassword = ''
  deleteDialog.visible = true
}

async function handleDelete() {
  if (!deleteDialog.adminPassword) {
    ElMessage.error('请输入管理员密码')
    return
  }
  const id = deleteDialog.targetId
  if (id == null) return
  deleteDialog.submitting = true
  try {
    await deleteUser(id, deleteDialog.adminPassword)
    ElMessage.success('删除成功')
    deleteDialog.visible = false
    loadData()
  } finally {
    deleteDialog.submitting = false
  }
}

async function handleToggleStatus(row: UserInfo) {
  const newStatus = row.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
  const action = newStatus === 'ACTIVE' ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定${action}用户「${row.realName}」吗？`, `${action}确认`)
    await updateUserStatus(row.userId, newStatus)
    ElMessage.success(`${action}成功`)
    loadData()
  } catch {
    // user cancelled
  }
}

onMounted(loadData)
</script>

<style scoped>
.page h3 {
  margin: 0 0 16px;
}
.toolbar {
  margin-bottom: 8px;
}
</style>
