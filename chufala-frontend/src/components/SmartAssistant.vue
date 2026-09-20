<template>
  <div class="smart-assistant">
    <!-- Chat Window -->
    <transition name="fade">
      <div v-if="isOpen" class="chat-window">
        <div class="chat-header">
          <div class="header-title">
            <span class="robot-icon-small">🤖</span>
            <span>智能助手</span>
          </div>
          <span class="close-btn" @click="toggleChat">×</span>
        </div>
        <div class="chat-messages" ref="messagesContainer">
          <div v-for="(msg, index) in messages" :key="index" :class="['message', msg.type]">
            <div 
              v-if="msg.type === 'assistant'" 
              class="message-content markdown-body" 
              :class="{ 'streaming-cursor': isStreaming && index === messages.length - 1 }"
              v-html="renderMarkdown(msg.text)"
            ></div>
            <div v-else class="message-content">{{ msg.text }}</div>
          </div>
          <div v-if="isTyping" class="message assistant typing">
            <span></span><span></span><span></span>
          </div>
        </div>
        
        <!-- Quick Replies -->
        <div class="quick-replies">
          <div 
            v-for="(reply, index) in quickReplies" 
            :key="index" 
            class="reply-tag"
            @click="handleQuickReply(reply)"
          >
            {{ reply }}
          </div>
        </div>

        <div class="chat-input">
          <input 
            v-model="inputMessage" 
            @keyup.enter="sendMessage" 
            placeholder="请输入您的问题..."
            type="text"
          />
          <button @click="sendMessage" :disabled="!inputMessage.trim()">发送</button>
        </div>
      </div>
    </transition>

    <!-- Floating Button -->
    <div class="floating-btn" @click="toggleChat" :class="{ 'is-open': isOpen }">
      <!-- User provided SVG -->
      <svg t="1766575889723" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="12399" width="40" height="40">
        <path d="M512 38.641509a96.603774 96.603774 0 0 1 28.981132 188.763774V386.415094h-57.962264v-159.009811A96.642415 96.642415 0 0 1 512 38.641509z" fill="#333C50" p-id="12400"></path>
        <path d="M164.226415 463.698113a154.566038 154.566038 0 0 1 154.566038-154.566038h386.415094a154.566038 154.566038 0 0 1 154.566038 154.566038v347.773585a154.566038 154.566038 0 0 1-154.566038 154.566038h-386.415094a154.566038 154.566038 0 0 1-154.566038-154.566038V463.698113z" fill="#64EDAC" p-id="12401"></path>
        <path d="M589.476226 712.433509a28.981132 28.981132 0 0 1 38.525585 43.297812L608.603774 734.188679a2292.388226 2292.388226 0 0 1 19.359396 21.561963l-0.057962 0.057962-0.115925 0.096604-0.25117 0.193207-0.618264 0.579623a106.070943 106.070943 0 0 1-8.153358 6.066717c-5.255245 3.535698-12.751698 7.998792-22.412076 12.365283-19.494642 8.771623-47.683623 17.040906-84.354415 17.040905-36.690113 0-64.859774-8.269283-84.335094-17.040905a150.798491 150.798491 0 0 1-22.450717-12.365283 106.070943 106.070943 0 0 1-8.134038-6.086038l-0.637585-0.540981-0.231849-0.212528-0.115925-0.096604-0.057962-0.057962S395.998189 755.731321 415.396226 734.188679l-19.398037 21.542642a29.000453 29.000453 0 0 1 38.641509-43.181887v-0.038642l-0.115924-0.077283-0.057963-0.077283 0.367095 0.309132c0.463698 0.386415 1.410415 1.101283 2.801509 2.02868 2.801509 1.893434 7.399849 4.675623 13.795019 7.535094 12.732377 5.738264 32.845283 11.959547 60.570566 11.959547 27.705962 0 47.838189-6.221283 60.570566-11.940226 6.375849-2.898113 11.01283-5.660981 13.775698-7.535095 1.410415-0.966038 2.337811-1.680906 2.82083-2.048l0.347774-0.309132-0.038642 0.077283zM898.415094 483.018868a96.603774 96.603774 0 0 1 96.603774 96.603774v115.924528a96.603774 96.603774 0 0 1-96.603774 96.603773V483.018868zM125.584906 792.150943a96.603774 96.603774 0 0 1-96.603774-96.603773v-115.924528a96.603774 96.603774 0 0 1 96.603774-96.603774v309.132075z" fill="#333C50" p-id="12402"></path>
        <path d="M338.113208 579.622642a57.962264 38.641509 90 1 0 77.283018 0 57.962264 38.641509 90 1 0-77.283018 0Z" fill="#333C50" p-id="12403"></path>
        <path d="M608.603774 579.622642a57.962264 38.641509 90 1 0 77.283018 0 57.962264 38.641509 90 1 0-77.283018 0Z" fill="#333C50" p-id="12404"></path>
      </svg>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue';
import MarkdownIt from 'markdown-it';
import DOMPurify from 'dompurify';
import { sendChatStream } from '@/api/agent';

const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  breaks: true
});

const renderMarkdown = (content: string) => {
  if (!content) return '';
  
  // 优化：自动补全未闭合的代码块，防止流式输出时样式错乱
  let processedContent = content;
  const codeBlockCount = (content.match(/```/g) || []).length;
  if (codeBlockCount % 2 !== 0) {
    processedContent += '\n```';
  }
  
  const html = md.render(processedContent);
  return DOMPurify.sanitize(html);
};

const isOpen = ref(false);
const inputMessage = ref('');
const messages = ref([
  { type: 'assistant', text: '您好！我是出发啦智能助手，有什么可以帮您的吗？' }
]);
const isTyping = ref(false);
const isStreaming = ref(false);
const messagesContainer = ref<HTMLElement | null>(null);

const quickReplies = [
  '机票查询',
  '酒店预订',
  '热门景点',
  '旅游套餐',
  '智能规划',
  '我的订单',
  '联系客服'
];

const toggleChat = () => {
  isOpen.value = !isOpen.value;
  if (isOpen.value) {
    scrollToBottom();
  }
};

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
    }
  });
};

const conversationId = ref<string | null>(null);

const processMessage = async (text: string) => {
  messages.value.push({ type: 'user', text });
  scrollToBottom();
  
  isTyping.value = true;
  
  let assistantMessageIndex = -1;

  try {
    const response = await sendChatStream({ 
      message: text, 
      conversationId: conversationId.value,
      model: 'qwen'
    });

    isStreaming.value = true;
    if (!response.ok) {
      throw new Error(`Error: ${response.statusText}`);
    }

    const reader = response.body?.getReader();
    if (!reader) throw new Error('No reader available');

    const decoder = new TextDecoder();
    isTyping.value = false;
    
    messages.value.push({ type: 'assistant', text: '' });
    assistantMessageIndex = messages.value.length - 1;

    // Check if response is SSE
    const isSSE = response.headers.get('Content-Type')?.includes('text/event-stream');
    let buffer = '';

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;
      
      const chunk = decoder.decode(value, { stream: true });
      
      if (isSSE) {
        buffer += chunk;
        const lines = buffer.split('\n');
        buffer = lines.pop() || '';

        for (const line of lines) {
          if (line.startsWith('data:')) {
            const data = line.slice(5).trim();
            if (data === '[DONE]') continue;
            
            try {
              const parsed = JSON.parse(data);
              // Adapt to common formats: { content: "..." } or "..."
              const text = typeof parsed === 'object' ? (parsed.content || parsed.text || parsed.message || '') : parsed;
              messages.value[assistantMessageIndex].text += text;
            } catch {
              messages.value[assistantMessageIndex].text += data;
            }
            scrollToBottom();
          }
        }
      } else {
        messages.value[assistantMessageIndex].text += chunk;
        scrollToBottom();
      }
    }
  } catch (error) {
    console.error('Chat error:', error);
    isTyping.value = false;
    if (assistantMessageIndex === -1) {
      messages.value.push({ type: 'assistant', text: '' });
      assistantMessageIndex = messages.value.length - 1;
    }
    messages.value[assistantMessageIndex].text = '抱歉，遇到了一些问题，请稍后再试。';
    scrollToBottom();
  } finally {
    isStreaming.value = false;
  }
};

const sendMessage = () => {
  const text = inputMessage.value.trim();
  if (!text) return;

  inputMessage.value = '';
  processMessage(text);
};

const handleQuickReply = (text: string) => {
  processMessage(text);
};
</script>

<style scoped>
.smart-assistant {
  position: fixed;
  bottom: 30px;
  right: 30px;
  z-index: 1000;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.floating-btn {
  width: 60px;
  height: 60px;
  background-color: #fff;
  border-radius: 50%;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.floating-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

.floating-btn.is-open {
  transform: rotate(90deg);
}

.chat-window {
  position: absolute;
  bottom: 80px;
  right: 0;
  width: 350px;
  height: 500px;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #e5e7eb;
}

.chat-header {
  background: linear-gradient(135deg, #64EDAC 0%, #3b82f6 100%);
  color: #fff;
  padding: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.close-btn {
  cursor: pointer;
  font-size: 20px;
  opacity: 0.8;
}

.close-btn:hover {
  opacity: 1;
}

.chat-messages {
  flex: 1;
  padding: 15px;
  overflow-y: auto;
  background-color: #f9fafb;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.message {
  max-width: 80%;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.5;
  word-wrap: break-word;
}

.message.assistant {
  align-self: flex-start;
  background-color: #fff;
  border: 1px solid #e5e7eb;
  color: #374151;
  border-bottom-left-radius: 2px;
}

.message.user {
  align-self: flex-end;
  background-color: #3b82f6;
  color: #fff;
  border-bottom-right-radius: 2px;
}

.typing {
  display: flex;
  align-items: center;
  min-height: 24px;
}

.typing span {
  display: inline-block;
  width: 6px;
  height: 6px;
  background-color: #9ca3af;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out both;
  margin: 0 3px;
}

.typing span:nth-child(1) { animation-delay: -0.32s; }
.typing span:nth-child(2) { animation-delay: -0.16s; }

@keyframes typing {
  0%, 80%, 100% { transform: translateY(0); }
  40% { transform: translateY(-6px); }
}

/* Streaming Cursor */
.streaming-cursor::after {
  content: '';
  display: inline-block;
  width: 6px;
  height: 14px;
  background-color: #64EDAC;
  margin-left: 2px;
  vertical-align: middle;
  animation: blink 1s step-end infinite;
  border-radius: 1px;
}

.streaming-cursor :deep(p:last-child) {
  display: inline;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

.chat-input {
  padding: 10px;
  border-top: 1px solid #e5e7eb;
  display: flex;
  gap: 10px;
  background-color: #fff;
}

.chat-input input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 20px;
  outline: none;
  font-size: 14px;
}

.chat-input input:focus {
  border-color: #3b82f6;
}

.chat-input button {
  padding: 8px 16px;
  background-color: #3b82f6;
  color: #fff;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s;
}

.chat-input button:disabled {
  background-color: #9ca3af;
  cursor: not-allowed;
}

.chat-input button:not(:disabled):hover {
  background-color: #2563eb;
}

/* Transition */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

.quick-replies {
  padding: 10px 15px;
  display: flex;
  gap: 8px;
  overflow-x: auto;
  white-space: nowrap;
  background-color: #fff;
  border-top: 1px solid #f3f4f6;
  /* Hide scrollbar */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none;  /* IE 10+ */
}

.quick-replies::-webkit-scrollbar { 
  display: none;  /* Chrome Safari */
}

.reply-tag {
  padding: 6px 12px;
  background-color: #f3f4f6;
  color: #4b5563;
  border-radius: 16px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
  border: 1px solid transparent;
}

.reply-tag:hover {
  background-color: #e0f2fe;
  color: #0284c7;
  border-color: #bae6fd;
}

/* Markdown Styles */
:deep(.markdown-body) {
  font-size: 14px;
  line-height: 1.5;
  color: #374151;
}

:deep(.markdown-body p) {
  margin-bottom: 8px;
}

:deep(.markdown-body p:last-child) {
  margin-bottom: 0;
}

:deep(.markdown-body h1),
:deep(.markdown-body h2),
:deep(.markdown-body h3),
:deep(.markdown-body h4) {
  margin-top: 12px;
  margin-bottom: 6px;
  font-weight: 600;
  line-height: 1.25;
}

:deep(.markdown-body ul),
:deep(.markdown-body ol) {
  padding-left: 18px;
  margin-bottom: 8px;
}

:deep(.markdown-body li) {
  margin-bottom: 2px;
}

:deep(.markdown-body strong) {
  font-weight: 600;
  color: #111827;
}

:deep(.markdown-body code) {
  padding: 0.2em 0.4em;
  margin: 0;
  font-size: 85%;
  background-color: rgba(27,31,35,0.05);
  border-radius: 3px;
  font-family: monospace;
}

:deep(.markdown-body pre) {
  padding: 10px;
  overflow: auto;
  font-size: 85%;
  line-height: 1.45;
  background-color: #f6f8fa;
  border-radius: 3px;
  margin-bottom: 8px;
}

:deep(.markdown-body pre code) {
  padding: 0;
  margin: 0;
  font-size: 100%;
  word-break: normal;
  white-space: pre;
  background: transparent;
  border: 0;
}

:deep(.markdown-body blockquote) {
  padding: 0 1em;
  color: #6a737d;
  border-left: 0.25em solid #dfe2e5;
  margin: 0 0 8px 0;
}

:deep(.markdown-body a) {
  color: #0366d6;
  text-decoration: none;
}

:deep(.markdown-body a:hover) {
  text-decoration: underline;
}
</style>
