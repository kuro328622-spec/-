<template>
  <div class="profile-container">
    <h2>个人资料</h2>
    <el-card>
      <!-- 头像上传区域：使用 previewAvatarUrl 进行预览 -->
      <div class="avatar-group">
        <el-avatar
          :size="100"
          :src="
            previewAvatarUrl || (userStore.userInfo?.avatarUrl
              ? `${baseUrl}${userStore.userInfo.avatarUrl}?t=${new Date().getTime()}`
              : 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png')
          "
        />
        <el-upload
          class="avatar-uploader"
          :action="uploadAction"
          :show-file-list="false"
          :on-success="handleAvatarSuccess"
          accept="image/*"
          :headers="uploadHeaders"
        >
          <el-button type="primary" size="small">更换头像</el-button>
        </el-upload>
      </div>

      <!-- 资料表单 -->
      <el-form :model="userForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="userForm.username" disabled />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="userForm.nickname" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="userForm.newPassword" type="password" placeholder="不修改请留空" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="userForm.confirmPassword" type="password" placeholder="不修改请留空" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveProfile">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { useUserStore } from '@/stores/userStore'

const baseUrl = 'http://localhost:8080'
const userStore = useUserStore()
const router = useRouter()

// 1. 用于本地预览的临时头像URL
const previewAvatarUrl = ref('')
// 2. 用于存储上传成功后从后端返回的相对路径，准备在保存时提交
const newAvatarUrlToSave = ref('')

const userForm = ref({
  username: '',
  nickname: '',
  newPassword: '',
  confirmPassword: ''
})
const uploadAction = ref(`${baseUrl}/api/users/upload-avatar`)

const uploadHeaders = computed(() => ({ 'Authorization': `Bearer ${userStore.token}` }))

onMounted(() => loadUserProfile())

const loadUserProfile = async () => {
  try {
    const response = await axios.get(`${baseUrl}/api/users/profile`, {
      headers: uploadHeaders.value
    })
    const data = response.data
    userForm.value.username = data.username
    userForm.value.nickname = data.nickname || ''
    // 更新全局状态，让右上角的头像同步
    userStore.updateProfile(data)
  } catch (error) {
    console.error("加载用户资料失败:", error)
    if (error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录。')
      userStore.logout()
      router.push('/login')
    } else {
      ElMessage.error('加载用户资料失败，请检查后端服务。')
    }
  }
}

// --- 核心修复 1: 修正 handleAvatarSuccess 的参数和逻辑 ---
const handleAvatarSuccess = (response) => {
  // el-upload 的 on-success 回调第一个参数就是后端返回的数据
  const res = response;
  if (res && res.success) {
    ElMessage.success(res.message || '头像上传成功，请点击“保存修改”以生效。');

    // 保存后端返回的相对路径，用于后续提交
    newAvatarUrlToSave.value = res.avatarUrl;

    // 更新本地预览
    previewAvatarUrl.value = `${baseUrl}${res.avatarUrl}?t=${new Date().getTime()}`;
  } else {
    // 兼容后端可能返回的不同错误格式
    const errorMessage = res?.message || '头像上传失败';
    ElMessage.error(errorMessage);
  }
}
// --- 修复 1 结束 ---

// --- 核心修复 2: 重构 saveProfile 方法，调用正确的接口 ---
const saveProfile = async () => {
  const isChangingPassword = !!userForm.value.newPassword;
  const isChangingNickname = userForm.value.nickname !== userStore.userInfo.nickname;
  const isChangingAvatar = !!newAvatarUrlToSave.value;

  if (isChangingPassword && userForm.value.newPassword !== userForm.value.confirmPassword) {
    ElMessage.error('两次输入的新密码不一致！')
    return
  }

  // 如果什么都没改，直接提示
  if (!isChangingPassword && !isChangingNickname && !isChangingAvatar) {
    ElMessage.info('没有任何信息需要修改。')
    return
  }

  try {
    // 如果修改了昵称或密码，调用 /profile 接口
    if (isChangingNickname || isChangingPassword) {
      const updateData = { nickname: userForm.value.nickname };
      if (isChangingPassword) updateData.password = userForm.value.newPassword;
      await axios.put(`${baseUrl}/api/users/profile`, updateData, { headers: uploadHeaders.value });
    }

    // 如果上传了新头像，调用专门的 /avatar 接口
    if (isChangingAvatar) {
      await axios.put(`${baseUrl}/api/users/avatar`, { avatarUrl: newAvatarUrlToSave.value }, { headers: uploadHeaders.value });
    }

    ElMessage.success('资料保存成功！');

    // 所有操作成功后，重新加载用户资料以同步全局状态（右上角头像会更新）
    await loadUserProfile();

    // 清空所有临时变量
    previewAvatarUrl.value = '';
    newAvatarUrlToSave.value = '';
    userForm.value.newPassword = '';
    userForm.value.confirmPassword = '';

    // 如果修改了密码，则强制退出
    if (isChangingPassword) {
      ElMessage.success('密码已修改，请使用新密码重新登录。')
      userStore.logout()
      router.push('/login')
    }

  } catch (error) {
    console.error("保存资料失败:", error)
    const errorMsg = error.response?.data?.message || '操作失败，请联系管理员'
    ElMessage.error(errorMsg)
  }
}
// --- 修复 2 结束 ---
</script>

<style scoped>
.profile-container {
  padding: 20px;
}
.avatar-group {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 20px;
}
</style>
