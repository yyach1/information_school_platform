<template>
  <div class="profile-page">
    <el-row :gutter="24">
      <!-- 左侧：个人信息卡片 -->
      <el-col :span="8">
        <el-card class="info-card" shadow="hover">
          <div class="avatar-area">
            <el-avatar v-if="avatarSrc" :size="88" :src="avatarSrc" />
            <el-avatar v-else :size="88" class="avatar-text">
              {{ (userStore.userInfo?.realName || 'U').charAt(0) }}
            </el-avatar>
            <div class="avatar-name">{{ userStore.userInfo?.realName }}</div>
            <el-tag :type="roleType" size="small">{{ roleLabel }}</el-tag>
          </div>

          <el-divider />

          <div class="info-list">
            <div class="info-item">
              <span class="info-key">用户名</span>
              <span class="info-val">{{ userStore.userInfo?.username || '-' }}</span>
            </div>
            <div class="info-item" v-if="userStore.userInfo?.studentNo">
              <span class="info-key">学号</span>
              <span class="info-val">{{ userStore.userInfo.studentNo }}</span>
            </div>
            <div class="info-item" v-if="userStore.userInfo?.className">
              <span class="info-key">班级</span>
              <span class="info-val">{{ userStore.userInfo.className }}</span>
            </div>
            <div class="info-item" v-if="userStore.userInfo?.grade">
              <span class="info-key">年级</span>
              <span class="info-val">{{ userStore.userInfo.grade }}</span>
            </div>
            <div class="info-item" v-if="userStore.userInfo?.major">
              <span class="info-key">专业</span>
              <span class="info-val">{{ userStore.userInfo.major }}</span>
            </div>
            <div class="info-item" v-if="userStore.userInfo?.politicalStatus">
              <span class="info-key">政治面貌</span>
              <span class="info-val">{{ userStore.userInfo.politicalStatus }}</span>
            </div>
            <div class="info-item" v-if="userStore.userInfo?.phone">
              <span class="info-key">手机</span>
              <span class="info-val">{{ userStore.userInfo.phone }}</span>
            </div>
            <div class="info-item" v-if="userStore.userInfo?.email">
              <span class="info-key">邮箱</span>
              <span class="info-val">{{ userStore.userInfo.email }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：修改密码 -->
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">修改密码</span>
            </div>
          </template>

          <el-form
            ref="pwdFormRef"
            :model="pwdForm"
            :rules="pwdRules"
            label-width="0"
            size="large"
            class="pwd-form"
          >
            <el-form-item prop="oldPassword">
              <el-input
                v-model="pwdForm.oldPassword"
                type="password"
                placeholder="输入旧密码"
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>

            <el-form-item prop="newPassword">
              <el-input
                v-model="pwdForm.newPassword"
                type="password"
                placeholder="输入新密码（至少 6 位）"
                prefix-icon="Key"
                show-password
              />
            </el-form-item>

            <el-form-item prop="confirmPassword">
              <el-input
                v-model="pwdForm.confirmPassword"
                type="password"
                placeholder="再次输入新密码"
                prefix-icon="Key"
                show-password
              />
            </el-form-item>

            <el-form-item>
              <div class="pwd-strength" v-if="pwdForm.newPassword">
                <div class="strength-bar">
                  <div class="strength-fill" :class="strengthClass" :style="{ width: strengthPercent + '%' }" />
                </div>
                <span class="strength-text" :class="strengthClass">{{ strengthLabel }}</span>
              </div>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="changing" style="width: 100%" @click="changePassword">
                确认修改
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request from '@/api/request'

const userStore = useUserStore()
const pwdFormRef = ref<FormInstance>()
const changing = ref(false)

const roleLabel = computed(() => {
  const map: Record<string, string> = { STUDENT: '学生', TEACHER: '教师', ADMIN: '管理员' }
  return map[userStore.userInfo?.role || ''] || ''
})

const roleType = computed(() => {
  const map: Record<string, string> = { STUDENT: 'success', TEACHER: 'warning', ADMIN: 'danger' }
  return map[userStore.userInfo?.role || ''] || 'info'
})

const avatarSrc = computed(() => {
  const url = userStore.userInfo?.avatarUrl
  if (!url) return ''
  if (url.startsWith('http')) return url
  return url // relative path like /api/files/1 works from same origin
})

const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const validateConfirm = (_rule: any, value: string, callback: Function) => {
  if (value !== pwdForm.newPassword) callback(new Error('两次密码不一致'))
  else callback()
}

const pwdRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '至少 6 位', trigger: 'blur' },
  ],
  confirmPassword: [{ required: true, validator: validateConfirm, trigger: 'blur' }],
}

const strengthPercent = computed(() => {
  const p = pwdForm.newPassword
  if (!p) return 0
  let score = 0
  if (p.length >= 8) score += 25
  if (/[A-Z]/.test(p)) score += 25
  if (/[0-9]/.test(p)) score += 25
  if (/[^A-Za-z0-9]/.test(p)) score += 25
  return Math.min(100, score)
})

const strengthClass = computed(() => {
  const p = strengthPercent.value
  if (p <= 25) return 'weak'
  if (p <= 50) return 'fair'
  if (p <= 75) return 'good'
  return 'strong'
})

const strengthLabel = computed(() => {
  const map: Record<string, string> = { weak: '弱', fair: '一般', good: '良好', strong: '强' }
  return map[strengthClass.value] || ''
})

async function changePassword() {
  const valid = await pwdFormRef.value?.validate().catch(() => false)
  if (!valid) return
  changing.value = true
  try {
    await request.put('/auth/password', {
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword,
    })
    ElMessage.success('密码修改成功')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
  } finally {
    changing.value = false
  }
}

onMounted(async () => {
  await userStore.fetchUserInfo()
})
</script>

<style scoped>
.profile-page { padding: 4px; }

.info-card { text-align: center; }
.avatar-area { display: flex; flex-direction: column; align-items: center; gap: 10px; padding: 8px 0; }
.avatar-text { background: #409eff; color: #fff; font-size: 36px; font-weight: 600; line-height: 88px; }
.avatar-name { font-size: 20px; font-weight: 600; color: #303133; }

.info-list { text-align: left; }
.info-item { display: flex; justify-content: space-between; padding: 8px 0; }
.info-key { color: #909399; font-size: 14px; }
.info-val { color: #303133; font-size: 14px; font-weight: 500; }

.card-header { display: flex; align-items: center; justify-content: space-between; }
.card-title { font-size: 16px; font-weight: 600; }

.pwd-form { max-width: 440px; margin: 0 auto; padding-top: 12px; }

.pwd-strength { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.strength-bar { flex: 1; height: 4px; background: #ebeef5; border-radius: 2px; overflow: hidden; }
.strength-fill { height: 100%; border-radius: 2px; transition: width 0.3s; }
.strength-fill.weak { background: #f56c6c; }
.strength-fill.fair { background: #e6a23c; }
.strength-fill.good { background: #409eff; }
.strength-fill.strong { background: #67c23a; }
.strength-text { font-size: 12px; }
.strength-text.weak { color: #f56c6c; }
.strength-text.fair { color: #e6a23c; }
.strength-text.good { color: #409eff; }
.strength-text.strong { color: #67c23a; }
</style>
