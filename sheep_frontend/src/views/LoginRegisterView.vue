<template>
  <div class="auth-container">
    <h2>欢迎回来</h2>
    <el-tabs v-model="activeTab" class="auth-tabs">
      <!-- 登录 Tab -->
      <el-tab-pane label="登录" name="login">
        <el-form :model="loginForm" label-width="80px" class="auth-form">
          <el-form-item label="用户名">
            <el-input v-model="loginForm.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleLogin" class="auth-button">登录</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 注册 Tab -->
      <el-tab-pane label="注册" name="register">
        <el-form :model="registerForm" label-width="80px" class="auth-form">
          <el-form-item label="用户名">
            <el-input v-model="registerForm.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" />
          </el-form-item>
          <el-form-item label="确认密码">
            <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码" />
          </el-form-item>
          <el-form-item>
            <el-button type="success" @click="handleRegister" class="auth-button">注册</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/userStore' // 1. 引入 Pinia 的 userStore

const activeTab = ref('login')

const loginForm = ref({
  username: '',
  password: ''
})

const registerForm = ref({
  username: '',
  password: '',
  confirmPassword: ''
})

const router = useRouter()
const userStore = useUserStore() // 2. 创建 userStore 实例

const handleLogin = async () => {
  try {
    const res = await axios.post('http://localhost:8080/api/users/login', loginForm.value)

    // 3. 修改：使用 Pinia 存储用户信息，而不是 localStorage
    // res.data.data 包含用户信息 (id, username, nickname, avatarUrl...)
    // res.data.token 包含 token
    userStore.setUserInfo({
      ...res.data.data,
      token: res.data.token
    })

    ElMessage.success('登录成功！')
    router.push('/')
  } catch (err) {
    console.error('登录失败:', err)
    // 尝试从错误响应中获取更具体的信息
    const errorMsg = err.response?.data?.message || '登录失败，请检查账号密码！'
    ElMessage.error(errorMsg)
  }
}

const handleRegister = async () => {
  if (registerForm.value.password !== registerForm.value.confirmPassword) {
    ElMessage.error('两次输入的密码不一致！')
    return
  }

  try {
    const res = await axios.post('http://localhost:8080/api/users/register', {
      username: registerForm.value.username,
      password: registerForm.value.password
    })
    console.log('注册成功，后端响应:', res)
    ElMessage.success('注册成功，请登录！')
    activeTab.value = 'login'
  } catch (err) {
    console.error('注册失败:', err)
    const errorMsg = err.response?.data?.message || '注册失败，请稍后再试！'
    ElMessage.error(errorMsg)
  }
}
</script>

<style scoped>
.auth-container {
  width: 400px;
  margin: 100px auto;
  padding: 30px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  background-color: #fff;
}
h2 {
  text-align: center;
  margin-bottom: 20px;
  color: #1989fa;
}
.auth-tabs {
  border-bottom: none;
}
.auth-form {
  margin-top: 20px;
}
.auth-button {
  width: 100%;
}
.el-form-item {
  margin-bottom: 20px;
}
</style>
