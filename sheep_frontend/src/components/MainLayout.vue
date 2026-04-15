<template>
  <div class="layout-container">
    <aside class="sidebar">
      <div class="logo-container">
        <h1>羊场库存管理系统</h1>
      </div>
      <el-menu
        :default-active="$route.path"
        class="sidebar-menu"
      >
        <sidebar-item
          v-for="route in routes"
          :key="route.path"
          :route="route"
        />
      </el-menu>
    </aside>

    <div class="main-content">
      <header class="header">
        <div class="header-right">
          <template v-if="userStore.userInfo">
            <el-dropdown @command="handleDropdownCommand">
              <span class="user-info">
                <el-avatar
                  :size="36"
                  :src="
                    userStore.userInfo.avatarUrl
                      ? `http://localhost:8080${userStore.userInfo.avatarUrl}?t=${new Date().getTime()}`
                      : 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
                  "
                  class="avatar-margin"
                />
                <span>{{ userStore.userInfo.nickname || userStore.userInfo.username }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                  <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <router-link v-else to="/login" class="login-link">
            登录
          </router-link>
        </div>
      </header>
      <main class="content-wrapper">
        <router-view />
      </main>
    </div>

    <AIChatAssistant />
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/userStore'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { computed } from 'vue'
// 引入SidebarItem组件
import SidebarItem from '@/components/SidebarItem.vue'
// 引入 AI 助手组件
import AIChatAssistant from './AIChatAssistant.vue'

// 获取路由器实例
const router = useRouter()
const userStore = useUserStore()

// 从路由配置中计算出需要在侧边栏显示的顶层路由
const routes = computed(() => {
  const mainLayoutRoute = router.getRoutes().find(r => r.name === 'MainLayout')
  return mainLayoutRoute?.children?.filter(r => r.meta?.title) || []
})

// 下拉菜单命令处理
const handleDropdownCommand = (command) => {
  if (command === 'profile') {
    router.push({ name: 'profile' })
  } else if (command === 'logout') {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }
}
</script>

<style scoped>
.layout-container {
  display: flex;
  height: 100vh;
  position: relative; /* 确保悬浮组件定位正确 */
}

.sidebar {
  width: 200px;
  background-color: #273352;
  color: #fff;
  display: flex;
  flex-direction: column;
}

.logo-container {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid #4b5f8a;
}

.logo-container h1 {
  font-size: 16px;
  margin: 0;
  color: #fff;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 200px;
}
.sidebar-menu {
  border-right: none;
  flex-grow: 1;
  user-select: none;
  -webkit-user-select: none;
}
.el-menu-item.is-active {
  background-color: #1989fa !important;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  height: 60px;
  line-height: 60px;
  background-color: #fff;
  padding: 0 20px;
  text-align: right;
  border-bottom: 1px solid #e4e7ed;
}

.content-wrapper {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background-color: #f5f7fa;
}
.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
}
.avatar-margin {
  margin-right: 8px;
}
.login-link {
  text-decoration: none;
  color: #409EFF;
}
</style>
