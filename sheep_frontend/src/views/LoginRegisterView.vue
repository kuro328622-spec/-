<template>
  <div class="login-page">
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
    </div>

    <div class="auth-container">
      <div class="brand-section">
        <el-icon class="brand-logo" :size="40"><Pointer /></el-icon>
        <h2>智慧养殖管理系统</h2>
        <p class="subtitle">Efficient · Professional · Intelligent</p>
      </div>

      <el-tabs v-model="activeTab" class="auth-tabs" stretch>
        <el-tab-pane label="账号登录" name="login">
          <el-form :model="loginForm" class="auth-form" @keyup.enter="handleLogin">
            <el-form-item>
              <el-input
                v-model="loginForm.username"
                placeholder="用户名"
                :prefix-icon="User"
              />
            </el-form-item>
            <el-form-item>
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="密码"
                :prefix-icon="Lock"
                show-password
              />
            </el-form-item>
            <div class="spacer"></div>
            <el-button type="primary" :loading="loading" @click="handleLogin" class="auth-button">
              立即登录
            </el-button>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="新用户注册" name="register">
          <el-form :model="registerForm" class="auth-form" @keyup.enter="handleRegister">
            <el-form-item>
              <el-input v-model="registerForm.username" placeholder="设置用户名" :prefix-icon="User" />
            </el-form-item>
            <el-form-item>
              <el-input v-model="registerForm.password" type="password" placeholder="设置密码" :prefix-icon="Lock" show-password />
            </el-form-item>
            <el-form-item>
              <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码" :prefix-icon="CircleCheck" show-password />
            </el-form-item>
            <el-button type="success" :loading="loading" @click="handleRegister" class="auth-button register-btn">
              提交注册
            </el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <div class="auth-footer">
        <p>© 2026 SmartFarm Tech. All Rights Reserved.</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, CircleCheck, Pointer } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/userStore'

const activeTab = ref('login')
const loading = ref(false)

const loginForm = ref({ username: '', password: '' })
const registerForm = ref({ username: '', password: '', confirmPassword: '' })

const router = useRouter()
const userStore = useUserStore()

const handleLogin = async () => {
  if (!loginForm.value.username || !loginForm.value.password) return ElMessage.warning('请填写完整信息')
  loading.value = true
  try {
    const res = await axios.post('http://localhost:8080/api/users/login', loginForm.value)
    userStore.setUserInfo({ ...res.data.data, token: res.data.token })
    ElMessage.success('欢迎回来！')
    router.push('/')
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '登录失败，请检查账号密码')
  } finally { loading.value = false }
}

const handleRegister = async () => {
  if (registerForm.value.password !== registerForm.value.confirmPassword) return ElMessage.error('两次密码不一致')
  loading.value = true
  try {
    await axios.post('http://localhost:8080/api/users/register', {
      username: registerForm.value.username,
      password: registerForm.value.password
    })
    ElMessage.success('注册成功！')
    activeTab.value = 'login'
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '注册失败')
  } finally { loading.value = false }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(-45deg, #f3f4f7, #e3e9ff, #f1f0ff, #e8f5ff);
  background-size: 400% 400%;
  animation: gradientBG 15s ease infinite;
  position: relative;
  overflow: hidden;
}

@keyframes gradientBG {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.bg-decoration .circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  z-index: 0;
}
.circle-1 { width: 400px; height: 400px; background: rgba(64, 158, 255, 0.2); top: -100px; right: -100px; }
.circle-2 { width: 300px; height: 300px; background: rgba(103, 194, 58, 0.15); bottom: -50px; left: -50px; }

.auth-container {
  position: relative;
  z-index: 10;
  width: 400px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.6);
}

.brand-section { text-align: center; margin-bottom: 30px; }
.brand-logo { color: #409eff; margin-bottom: 10px; }
h2 { margin: 0; font-size: 22px; color: #303133; }
.subtitle { font-size: 12px; color: #909399; margin-top: 5px; letter-spacing: 1px; }

.spacer { height: 20px; }

.auth-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s;
}

:deep(.el-input__wrapper) {
  padding: 8px 12px;
  border-radius: 8px;
  background-color: #f5f7fa;
  box-shadow: none !important;
}

.auth-footer {
  margin-top: 25px;
  text-align: center;
  font-size: 12px;
  color: #c0c4cc;
}
</style>
