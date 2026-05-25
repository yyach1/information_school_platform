<template>
  <div class="login-page">
    <div class="login-banner">
      <div class="banner-content">
        <h1 class="banner-title">信息学院综合服务平台</h1>
        <p class="banner-desc">一站式党团管理、证明办理与学生服务</p>
        <div class="banner-features">
          <div class="feature-item">
            <el-icon :size="28"><Checked /></el-icon>
            <span>党团进度随时查看</span>
          </div>
          <div class="feature-item">
            <el-icon :size="28"><Checked /></el-icon>
            <span>电子证明在线办理</span>
          </div>
          <div class="feature-item">
            <el-icon :size="28"><Checked /></el-icon>
            <span>材料提交与审核反馈</span>
          </div>
        </div>
      </div>
    </div>
    <div class="login-form-side">
      <div class="form-wrapper">
        <h2 class="form-title">用户登录</h2>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="0" size="large">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码"
              prefix-icon="Lock"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" style="width: 100%" @click="handleLogin">
              登 录
            </el-button>
          </el-form-item>
        </el-form>
        <div class="form-tips">
          <p>学生请使用学号登录</p>
          <p>教师/管理员请联系系统管理员</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.login(form)
    const role = userStore.userInfo?.role
    if (role === 'STUDENT') {
      router.push('/student')
    } else {
      router.push('/dashboard')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  height: 100vh;
}
.login-banner {
  flex: 1;
  background: linear-gradient(135deg, #1a6fb5 0%, #2b8fd4 50%, #1a6fb5 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 0;
}
.banner-content {
  color: #fff;
  padding: 60px;
  max-width: 480px;
}
.banner-title {
  font-size: 32px;
  margin: 0 0 12px;
  font-weight: 700;
}
.banner-desc {
  font-size: 16px;
  opacity: 0.85;
  margin: 0 0 40px;
}
.banner-features {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
  opacity: 0.9;
}
.login-form-side {
  width: 440px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
}
.form-wrapper {
  width: 340px;
}
.form-title {
  text-align: center;
  margin: 0 0 32px;
  font-size: 22px;
  color: #303133;
}
.form-tips {
  text-align: center;
  color: #c0c4cc;
  font-size: 12px;
  margin-top: 24px;
}
.form-tips p {
  margin: 4px 0;
}
@media (max-width: 768px) {
  .login-banner {
    display: none;
  }
  .login-form-side {
    width: 100%;
  }
}
</style>
