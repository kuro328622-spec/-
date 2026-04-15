<template>
  <div class="home-container">
    <div class="welcome-header">
      <div class="greet">
        <h2>您好，欢迎使用养殖场管理系统 👋</h2>
        <p>今天是 {{ currentDate }}，请选择您要处理的业务模块。</p>
      </div>
    </div>

    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="operation-card">
          <template #header>
            <div class="card-header">
              <span style="font-weight: bold; font-size: 16px;">🚀 快速操作助手(注：数据注入AI采取了“按需注入： 只有在您点击需要使用的页面时，系统才会提取该页内容并同步给 AI。”)</span>
            </div>
          </template>

          <div class="quick-nav-wrapper">
            <div class="nav-section">
              <p class="section-title">💰 财务管理</p>
              <div class="btn-group">
                <el-button type="primary" size="large" icon="List" @click="$router.push('/finance/records')">
                  收支明细
                </el-button>
                <el-button type="primary" plain size="large" icon="DataAnalysis" @click="$router.push('/finance/summary')">
                  汇总看板
                </el-button>
              </div>
            </div>

            <div class="nav-section">
              <p class="section-title">📥 进货入库</p>
              <div class="btn-group">
                <el-button type="success" size="large" icon="Plus" @click="$router.push('/stock/feed/in')">
                  饲料入库
                </el-button>
                <el-button type="success" plain size="large" icon="Box" @click="$router.push('/stock/drug-vaccine/in')">
                  药品入库
                </el-button>
              </div>
            </div>

            <div class="nav-section">
              <p class="section-title">📤 领用出库</p>
              <div class="btn-group">
                <el-button type="warning" size="large" icon="Minus" @click="$router.push('/stock/drug-vaccine/out')">
                  药品领用
                </el-button>
                <el-button type="warning" plain size="large" icon="Sell" @click="$router.push('/stock/feed/out')">
                  饲料领用
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed } from 'vue' // 删掉了 ref，因为不再需要定义响应式变量

// 删掉了所有关于 username 的逻辑，彻底解决报错

const currentDate = computed(() => {
  const date = new Date()
  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`
})
</script>

<style scoped>
.home-container {
  padding: 30px;
  background-color: #f0f2f5;
  min-height: 85vh;
}

.welcome-header {
  margin-bottom: 30px;
}

.welcome-header h2 {
  font-size: 28px;
  color: #1f2f3d;
  margin: 0;
}

.operation-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.quick-nav-wrapper {
  display: flex;
  flex-direction: column;
  gap: 30px;
  padding: 10px;
}

.section-title {
  font-size: 15px;
  color: #606266;
  font-weight: bold;
  margin-bottom: 15px;
  padding-left: 12px;
  border-left: 5px solid #409eff;
}

.btn-group {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.btn-group .el-button {
  height: 70px;
  font-size: 17px;
  font-weight: bold;
  margin-left: 0 !important;
  border-radius: 8px;
  transition: all 0.2s;
}

.btn-group .el-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
</style>
