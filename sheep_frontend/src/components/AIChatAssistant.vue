<template>
  <div
    class="ai-assistant-wrapper"
    :class="{ 'is-hidden': isHidden, 'is-dragging': isDragging }"
    :style="{ left: position.x + 'px', top: position.y + 'px' }"
    @mousedown="handleMouseDown"
  >
    <div v-if="isHidden" class="side-handle" @click.stop="toggleExpand">
      <el-icon>
        <ArrowLeft v-if="atRightSide" />
        <ArrowRight v-else />
      </el-icon>
    </div>

    <div v-else class="chat-trigger" @click.stop="toggleChat">
      <el-icon v-if="!isOpen" :size="28"><ChatDotRound /></el-icon>
      <el-icon v-else :size="28"><ArrowDown /></el-icon>
    </div>

    <transition name="el-zoom-in-bottom">
      <div v-show="isOpen && !isHidden" class="chat-window" @mousedown.stop>
        <div class="chat-header">
          <div class="header-left">
            <span>🐏 养殖智能助手</span>
            <span class="status-dot" :class="{ 'is-active': aiStore.allContext }"></span>
          </div>
          <el-button link @click="isOpen = false">
            <el-icon color="white"><Close /></el-icon>
          </el-button>
        </div>

        <div class="chat-body" ref="chatBox">
          <div v-for="(msg, index) in messages" :key="index" :class="['message', msg.role]">
            <div class="avatar">{{ msg.role === 'user' ? '我' : 'AI' }}</div>
            <div class="content" v-html="msg.role === 'assistant' ? renderMarkdown(msg.content) : msg.content"></div>
          </div>
          <div v-if="loading" class="message assistant">
            <div class="avatar">AI</div>
            <div class="content loading-dots">正在思考中...</div>
          </div>
        </div>

        <div class="chat-footer">
          <el-input
            v-model="userInput"
            placeholder="输入问题，按回车发送..."
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
import { ref, reactive, nextTick, watch } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import { ChatDotRound, ArrowDown, Close, ArrowLeft, ArrowRight } from '@element-plus/icons-vue';
import { useAIStore } from '@/stores/aiStore';

const aiStore = useAIStore();
const isOpen = ref(false);
const isHidden = ref(false);
const isDragging = ref(false);
const atRightSide = ref(true);
const loading = ref(false);
const userInput = ref('');
const chatBox = ref(null);

const position = reactive({
  x: window.innerWidth - 80,
  y: window.innerHeight - 150
});

const messages = ref([
  { role: 'assistant', content: '你好！我是你的养殖助手。点击页面上的条目，我会自动获取数据并为你提供建议。' }
]);

// ✨ 新增：监听autoQuestion信号，自动打开并发送
watch(() => aiStore.autoQuestion, async (question) => {
  if (!question) return;
  isOpen.value = true;
  await nextTick();
  userInput.value = question;
  await handleSend();
  aiStore.autoQuestion = ''; // 重置信号
});

// ✨ 新增：简单Markdown渲染（加粗、换行、列表）
const renderMarkdown = (text) => {
  if (!text) return '';
  return text
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/^### (.*$)/gm, '<h4 style="margin:8px 0 4px;color:#409EFF">$1</h4>')
    .replace(/^## (.*$)/gm, '<h3 style="margin:10px 0 5px;color:#303133">$1</h3>')
    .replace(/^# (.*$)/gm, '<h2 style="margin:12px 0 6px;color:#303133">$1</h2>')
    .replace(/^- (.*$)/gm, '<li style="margin:2px 0;padding-left:4px">$1</li>')
    .replace(/(<li.*<\/li>)/gs, '<ul style="padding-left:16px;margin:4px 0">$1</ul>')
    .replace(/\n/g, '<br/>');
};

// 拖拽逻辑（保持不变）
let startPos = { x: 0, y: 0 };
let hasMoved = false;

const handleMouseDown = (e) => {
  isDragging.value = true;
  hasMoved = false;
  startPos = { x: e.clientX - position.x, y: e.clientY - position.y };

  const onMouseMove = (moveEvent) => {
    hasMoved = true;
    position.x = moveEvent.clientX - startPos.x;
    position.y = moveEvent.clientY - startPos.y;
  };

  const onMouseUp = () => {
    isDragging.value = false;
    document.removeEventListener('mousemove', onMouseMove);
    document.removeEventListener('mouseup', onMouseUp);
    checkAdsorption();
  };

  document.addEventListener('mousemove', onMouseMove);
  document.addEventListener('mouseup', onMouseUp);
};

const checkAdsorption = () => {
  const threshold = 100;
  const screenWidth = window.innerWidth;
  if (position.x + 30 > screenWidth - threshold) {
    position.x = screenWidth - 25;
    isHidden.value = true;
    atRightSide.value = true;
    isOpen.value = false;
  } else if (position.x < threshold) {
    position.x = -15;
    isHidden.value = true;
    atRightSide.value = false;
    isOpen.value = false;
  }
};

const toggleExpand = () => {
  isHidden.value = false;
  position.x = atRightSide.value ? window.innerWidth - 80 : 30;
};

const toggleChat = () => {
  if (hasMoved) return;
  isOpen.value = !isOpen.value;
};

const scrollToBottom = async () => {
  await nextTick();
  if (chatBox.value) chatBox.value.scrollTop = chatBox.value.scrollHeight;
};

const handleSend = async () => {
  if (!userInput.value.trim() || loading.value) return;

  const content = userInput.value;
  const currentContext = aiStore.allContext;

  let finalMessage = currentContext
    ? `【养殖场数据上下文】：\n${currentContext}\n\n【用户问题】：${content}`
    : content;

  messages.value.push({ role: 'user', content });
  userInput.value = '';
  loading.value = true;
  await scrollToBottom();

  try {
    const response = await axios.post('http://localhost:8080/api/ai/chat', { message: finalMessage });
    const aiData = response.data;
    const finalContent = aiData?.choices?.[0]?.message?.content || aiData?.content || '未收到有效回复';
    messages.value.push({ role: 'assistant', content: finalContent });
  } catch {
    ElMessage.error('AI服务连接失败');
  } finally {
    loading.value = false;
    await scrollToBottom();
  }
};
</script>

<style scoped>
.ai-assistant-wrapper {
  position: fixed;
  z-index: 9999;
  user-select: none;
  touch-action: none;
}

.ai-assistant-wrapper:not(.is-dragging) {
  transition: all 0.4s cubic-bezier(0.18, 0.89, 0.32, 1.28);
}

.chat-trigger {
  width: 56px; height: 56px; background: #409EFF; border-radius: 50%;
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.4); display: flex;
  align-items: center; justify-content: center; color: white; cursor: grab;
}

.side-handle {
  width: 40px; height: 80px; background: #409EFF; color: white;
  display: flex; align-items: center; justify-content: center;
  border-radius: 8px; cursor: pointer; opacity: 0.7;
}
.side-handle:hover { opacity: 1; width: 45px; }

/* ✨ 窗口放大：560x680 */
.chat-window {
  position: absolute;
  bottom: 70px;
  right: 0;
  width: 560px;
  height: 680px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(0,0,0,0.2);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #eee;
}

.chat-header {
  padding: 15px 20px;
  background: #409EFF;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-left { display: flex; align-items: center; gap: 8px; font-weight: bold; font-size: 15px; }
.status-dot { width: 8px; height: 8px; background: #909399; border-radius: 50%; border: 1.5px solid white; }
.status-dot.is-active { background: #67c23a; box-shadow: 0 0 5px #67c23a; }

.chat-body { flex: 1; padding: 16px; overflow-y: auto; background: #f5f7fa; }
.message { display: flex; margin-bottom: 16px; }
.message.user { flex-direction: row-reverse; }
.avatar {
  width: 36px; height: 36px; border-radius: 50%; background: #ddd;
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; margin: 0 10px; flex-shrink: 0;
}
.user .avatar { background: #409EFF; color: white; }
.content {
  max-width: 65%;
  padding: 12px 14px;
  border-radius: 10px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}
.assistant .content { background: white; color: #333; box-shadow: 0 2px 5px rgba(0,0,0,0.06); }
.user .content { background: #409EFF; color: white; }

.chat-footer { padding: 12px 16px; border-top: 1px solid #eee; background: white; }
.loading-dots { color: #999; font-style: italic; }
</style>
