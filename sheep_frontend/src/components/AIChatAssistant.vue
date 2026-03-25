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
            <div class="content">{{ msg.content }}</div>
          </div>
          <div v-if="loading" class="message assistant">
            <div class="avatar">AI</div>
            <div class="content loading-dots">正在思考中...</div>
          </div>
        </div>

        <div class="chat-footer">
          <!-- <div class="inject-hint" v-if="aiStore.allContext">
            <el-icon><Check /></el-icon> 已同步当前页面养殖数据
          </div> -->
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
import { ref, reactive, nextTick} from 'vue';
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

// 初始位置设置在右下角
const position = reactive({
  x: window.innerWidth - 80,
  y: window.innerHeight - 150
});

const messages = ref([
  { role: 'assistant', content: '你好！我是你的养殖助手。点击页面上的条目，我会自动获取数据并为你提供建议。' }
]);

// --- 拖拽与吸附逻辑 ---
let startPos = { x: 0, y: 0 };
let hasMoved = false; // 用于判断是点击还是拖拽

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
    checkAdsorption(); // 检查是否吸附边缘
  };

  document.addEventListener('mousemove', onMouseMove);
  document.addEventListener('mouseup', onMouseUp);
};

const checkAdsorption = () => {
  const threshold = 100;
  const screenWidth = window.innerWidth;

  if (position.x + 30 > screenWidth - threshold) {
    position.x = screenWidth - 25; // 吸附到右侧，只留一点边缘
    isHidden.value = true;
    atRightSide.value = true;
    isOpen.value = false;
  } else if (position.x < threshold) {
    position.x = -15; // 吸附到左侧
    isHidden.value = true;
    atRightSide.value = false;
    isOpen.value = false;
  }
};

const toggleExpand = () => {
  isHidden.value = false;
  // 弹回可视区
  position.x = atRightSide.value ? window.innerWidth - 80 : 30;
};

const toggleChat = () => {
  if (hasMoved) return; // 如果刚才是在拖拽，则不打开窗口
  isOpen.value = !isOpen.value;
};

// --- 业务逻辑 ---
const scrollToBottom = async () => {
  await nextTick();
  if (chatBox.value) chatBox.value.scrollTop = chatBox.value.scrollHeight;
};

const handleSend = async () => {
  if (!userInput.value.trim() || loading.value) return;

  const content = userInput.value;
  const currentContext = aiStore.allContext;

  // 构造 Prompt
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
  } catch  {
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

/* 拖拽时的平滑过渡 */
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

.chat-window {
  position: absolute; bottom: 70px; right: 0; width: 360px; height: 500px;
  background: white; border-radius: 12px; box-shadow: 0 8px 30px rgba(0,0,0,0.2);
  display: flex; flex-direction: column; overflow: hidden; border: 1px solid #eee;
}

.chat-header {
  padding: 15px; background: #409EFF; color: white;
  display: flex; justify-content: space-between; align-items: center;
}
.header-left { display: flex; align-items: center; gap: 8px; font-weight: bold; }
.status-dot { width: 8px; height: 8px; background: #909399; border-radius: 50%; border: 1.5px solid white; }
.status-dot.is-active { background: #67c23a; box-shadow: 0 0 5px #67c23a; }

.chat-body { flex: 1; padding: 15px; overflow-y: auto; background: #f5f7fa; }
.message { display: flex; margin-bottom: 15px; }
.message.user { flex-direction: row-reverse; }
.avatar {
  width: 32px; height: 32px; border-radius: 50%; background: #ddd;
  display: flex; align-items: center; justify-content: center; font-size: 11px; margin: 0 8px; flex-shrink: 0;
}
.user .avatar { background: #409EFF; color: white; }
.content {
  max-width: 70%; padding: 10px; border-radius: 8px; font-size: 14px; line-height: 1.5;
}
.assistant .content { background: white; color: #333; box-shadow: 0 2px 5px rgba(0,0,0,0.05); }
.user .content { background: #409EFF; color: white; }

.chat-footer { padding: 10px; border-top: 1px solid #eee; background: white; }
.inject-hint {
  font-size: 12px; color: #67c23a; margin-bottom: 8px; display: flex;
  align-items: center; gap: 4px; justify-content: center; background: #f0f9eb; padding: 4px; border-radius: 4px;
}
.loading-dots { color: #999; font-style: italic; }
</style>
