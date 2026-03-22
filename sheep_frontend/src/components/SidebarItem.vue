<!-- src/components/SidebarItem.vue -->
<template>
  <!-- 父菜单 -->
  <el-sub-menu v-if="isParentMenu" :index="route.path">
    <template #title>
      <el-icon v-if="iconComponent"><component :is="iconComponent" /></el-icon>
      <span>{{ route.meta.title }}</span>
    </template>
    <sidebar-item
      v-for="child in route.children"
      :key="child.path"
      :route="child"
    />
  </el-sub-menu>

  <!-- 普通菜单项：手动跳转路由 -->
  <el-menu-item v-else :index="route.path" @click="handleMenuClick">
    <el-icon v-if="iconComponent"><component :is="iconComponent" /></el-icon>
    <span>{{ route.meta.title }}</span>
  </el-menu-item>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import * as Icons from '@element-plus/icons-vue'

const router = useRouter()
const props = defineProps({
  route: { type: Object, required: true }
})

// 判断是否为父菜单
const isParentMenu = computed(() => {
  return (props.route.children?.length > 0) || props.route.meta?.isParent
})

// 图标组件
const iconComponent = computed(() => {
  return props.route.meta.icon ? Icons[props.route.meta.icon] : null
})

// 手动跳转路由（关键：使用路由name，避免路径拼接）
const handleMenuClick = () => {
  router.push({ name: props.route.name })
}
</script>
