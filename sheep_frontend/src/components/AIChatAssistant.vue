<template>
  <div class="ai-assistant-wrapper">
    <div class="chat-trigger" @click="isOpen = !isOpen">
      <el-icon v-if="!isOpen" :size="28"><ChatDotRound /></el-icon>
      <el-icon v-else :size="28"><ArrowDown /></el-icon>
    </div>

    <transition name="el-zoom-in-bottom">
      <div v-show="isOpen" class="chat-window">
        <div class="chat-header">
          <span>🐏 养殖智能助手</span>
          <el-button link @click="isOpen = false">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>

        <div class="chat-body" ref="chatBox">
          <div v-for="(msg, index) in messages" :key="index" :class="['message', msg.role]">
            <div class="avatar">{{ msg.role === 'user' ? '我' : 'AI' }}</div>
            <div class="content">{{ msg.content }}</div>
          </div>
          <div v-if="loading" class="message assistant">
            <div class="avatar">AI</div>
            <div class="content loading-dots">正在思考中...</div>
          </div>
        </div>

        <div class="chat-footer">
          <el-input
            v-model="userInput"
            placeholder="输入养殖问题，按回车发送..."
            @keyup.enter="handleSend"
            :disabled="loading"
          >
            <template #append>
              <el-button @click="handleSend" :loading="loading">发送</el-button>
            </template>
          </el-input>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import { ChatDotRound, ArrowDown, Close } from '@element-plus/icons-vue';

const isOpen = ref(false);
const loading = ref(false);
const userInput = ref('');
const chatBox = ref(null);
const messages = ref([
  { role: 'assistant', content: '你好！我是你的养殖场智能助手，你可以问我任何关于饲料、药品或管理的问题。' }
]);

const scrollToBottom = async () => {
  await nextTick();
  if (chatBox.value) {
    chatBox.value.scrollTop = chatBox.value.scrollHeight;
  }
};

const handleSend = async () => {
  if (!userInput.value.trim() || loading.value) return;

  const content = userInput.value;
  messages.value.push({ role: 'user', content });
  userInput.value = '';
  loading.value = true;
  await scrollToBottom();

  try {
    // 1. 发送请求
    const response = await axios.post('http://localhost:8080/api/ai/chat', {
      message: content
    });

    // 2. 获取后端返回的数据
    const aiData = response.data;
    console.log('后端响应原文:', aiData); // 可以在浏览器F12控制台查看原文

    // 3. 核心修复：更严谨的解析逻辑
    let finalContent = '';

    // 情况 A: 正常的 DeepSeek 响应格式
    if (aiData && aiData.choices && aiData.choices[0] && aiData.choices[0].message) {
      finalContent = aiData.choices[0].message.content;
    }
    // 情况 B: 后端 catch 到的错误信息 (字符串或带 error 字段的对象)
    else if (aiData && aiData.error) {
      finalContent = '系统提醒：' + (typeof aiData.error === 'object' ? JSON.stringify(aiData.error) : aiData.error);
    }
    // 情况 C: 后端可能返回了纯文本错误
    else if (typeof aiData === 'string' && aiData.includes('error')) {
      finalContent = aiData;
    }
    // 情况 D: 实在解析不出来，把原文转成文字展示（防止出现 [object Object]）
    else {
      finalContent = '收到非预期格式数据：' + JSON.stringify(aiData);
    }

    messages.value.push({ role: 'assistant', content: finalContent });

  } catch (error) {
    console.error('AI接口请求失败:', error);
    ElMessage.error('无法连接到后端服务器，请检查后端是否正常启动');
  } finally {
    loading.value = false;
    await scrollToBottom();
  }
};
</script>

<style scoped>
/* 样式保持不变 */
.ai-assistant-wrapper { position: fixed; right: 30px; bottom: 30px; z-index: 9999; }
.chat-trigger {
  width: 56px; height: 56px; background: #409EFF; border-radius: 50%;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15); display: flex; align-items: center;
  justify-content: center; color: white; cursor: pointer; transition: all 0.3s;
}
.chat-trigger:hover { transform: scale(1.1); background: #66b1ff; }
.chat-window {
  position: absolute; bottom: 70px; right: 0; width: 360px; height: 500px;
  background: white; border-radius: 12px; box-shadow: 0 8px 24px rgba(0,0,0,0.2);
  display: flex; flex-direction: column; overflow: hidden;
}
.chat-header {
  padding: 15px; background: #409EFF; color: white; display: flex;
  justify-content: space-between; align-items: center; font-weight: bold;
}
.chat-body { flex: 1; padding: 15px; overflow-y: auto; background: #f5f7fa; }
.message { display: flex; margin-bottom: 15px; align-items: flex-start; }
.message.user { flex-direction: row-reverse; }
.avatar {
  width: 35px; height: 35px; border-radius: 50%; background: #eee;
  display: flex; align-items: center; justify-content: center; font-size: 12px;
  margin: 0 8px; flex-shrink: 0;
}
.user .avatar { background: #409EFF; color: white; }
.content {
  max-width: 70%; padding: 10px; border-radius: 8px; font-size: 14px;
  line-height: 1.5; word-break: break-all; white-space: pre-wrap; /* 允许 AI 回复换行 */
}
.assistant .content { background: white; color: #333; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }
.user .content { background: #409EFF; color: white; }
.chat-footer { padding: 10px; border-top: 1px solid #eee; }
.loading-dots { color: #909399; font-style: italic; }
</style>
