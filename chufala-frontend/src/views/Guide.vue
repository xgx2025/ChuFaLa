<template>
  <div class="travel-planner">
    <!-- 顶部标题区 -->
    <div class="planner-header">
      <div class="header-content">
        <h1 class="main-title">
          <el-icon><Compass /></el-icon>
          AI 智能旅行规划师
        </h1>
        <p class="subtitle">让每一次旅行都成为美好回忆</p>
      </div>
      
      <!-- 模式切换器 -->
      <div class="mode-switcher">
        <div 
          class="mode-item" 
          :class="{ active: currentMode === 'planning' }"
          @click="currentMode = 'planning'"
        >
          <el-icon><MapLocation /></el-icon>
          <span>行程定制</span>
        </div>
        <div 
          class="mode-item" 
          :class="{ active: currentMode === 'chat' }"
          @click="currentMode = 'chat'"
        >
          <el-icon><ChatDotRound /></el-icon>
          <span>AI 旅行顾问</span>
        </div>
      </div>

      <el-button class="history-btn" @click="showHistory = true" circle>
        <el-icon><Clock /></el-icon>
      </el-button>
    </div>

    <!-- 历史记录抽屉 -->
    <el-drawer
      v-model="showHistory"
      title="规划历史记录"
      direction="rtl"
      size="350px"
    >
      <div v-if="historyList.length === 0" class="empty-history">
        <el-empty description="暂无历史记录" :image-size="100"></el-empty>
      </div>
      <div v-else class="history-list">
        <div v-for="(item, index) in historyList" :key="index" class="history-item" @click="applyHistory(item)">
          <div class="history-item-header">
            <span class="history-dest">{{ item.destination }}</span>
            <el-button 
              type="danger" 
              link 
              :icon="Delete" 
              @click.stop="deleteHistory(index)"
              class="delete-btn"
            ></el-button>
          </div>
          <div class="history-info">
            <span class="info-tag">{{ item.dayNum }}天</span>
            <span class="info-tag">{{ item.people }}人</span>
            <span class="info-tag">¥{{ item.budget }}</span>
          </div>
          <div class="history-preferences">
            <span v-for="pref in (item.preferences || [])" :key="pref" class="pref-tag">{{ pref }}</span>
          </div>
          <div class="history-time">{{ dateFormat(item.createTime) }}</div>
        </div>
      </div>
    </el-drawer>

    <!-- 主要内容区：行程定制模式 -->
    <div class="planner-container" v-show="currentMode === 'planning'">
      <!-- 左侧输入面板 -->
      <div class="input-panel">
        <el-card class="input-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <Document style="width: 20px; height: 20px; color: #68BF7B;"/>
              <span>定制您的旅程</span>
            </div>
          </template>
          
          <el-form :model="travelForm" label-position="top">
            <el-form-item label="目的地">
              <el-input v-model="travelForm.destination" placeholder="例如：张家界"></el-input>
            </el-form-item>
            <el-form-item label="出发日期">
              <el-date-picker v-model="travelForm.startDate" type="date" placeholder="选择日期" :disabled-date="disabledBeforeNow" style="width: 100%;"></el-date-picker>
            </el-form-item>
            <el-form-item label="天数">
              <el-input-number v-model="travelForm.days" :min="1" :max="30" style="width: 100%;"></el-input-number>
            </el-form-item>
            <el-form-item label="人数">
              <el-input-number v-model="travelForm.people" :min="1" :max="20" style="width: 100%;"></el-input-number>
            </el-form-item>
            
            <el-form-item label="旅行偏好">
              <el-checkbox-group v-model="travelForm.preferences">
                <el-checkbox label="自然风光">自然风光</el-checkbox>
                <el-checkbox label="历史文化">历史文化</el-checkbox>
                <el-checkbox label="美食探索">美食探索</el-checkbox>
                <el-checkbox label="休闲度假">休闲度假</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            
            <el-form-item label="预算范围 (元/人)">
              <el-slider v-model="travelForm.budget" :step="100" :min="0" :max="20000" show-input />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="generateItinerary" :loading="isGenerating" class="generate-btn">
                <el-icon><MagicStick /></el-icon>
                {{ isGenerating ? 'AI正在规划中...' : '生成行程' }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>

      <!-- 中间行程展示区 -->
      <div class="itinerary-panel">
        <el-card class="itinerary-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <Itinerary style="width: 30px; height: 30px; color:#f4ea2a;"/>
              <span>您的专属行程</span>
              <el-button 
                v-if="itinerary.length > 0" 
                type="primary" 
                link 
                :icon="Download" 
                @click="exportImage"
                style="margin-left: auto;"
              >导出图片</el-button>
            </div>
          </template>
          
          <div v-if="!itinerary.length && !isGenerating" class="empty-state">
            <el-icon class="empty-state__icon"><Suitcase /></el-icon>
            <p>请填写您的旅行需求，AI将为您定制专属行程</p>
          </div>
          
          <div v-else-if="isGenerating" class="loading-state">
            <div class="loading-spinner"></div>
            <p>AI正在为您精心规划...</p>
          </div>
          
          <el-timeline v-else>
            <el-timeline-item v-for="(day, index) in itinerary" :key="index" :timestamp="`DAY ${day.day}`" placement="top" :hollow="true" class="custom-timeline-item">
              <div style="display: flex; justify-content: flex-end; align-items: center; gap: 8px; margin-bottom: 6px;">
                <img :src="`/weather/${day.weather}.png`" style="width: 40px; height: 40px;">
                <span>{{ day.temperature }} ({{ day.weather }})</span>
              </div>
              <div class="day-card" shadow="hover">
                <div v-for="(item, itemIndex) in day.activities" :key="item.id" @click="focusOnLocation(item)">
                  <div class="activity-item">
                    <img :src="item.image" style="width: 95px; height: 135px; border-radius: 8px;"></img>
                    <div class="activity-content">
                      <div class="activity-header">
                        <h3>{{ item.time }} {{ item.name }}</h3>
                        <el-button text @click.stop="navigateToDestination(item)" class="nav-btn">
                          📍导航
                        </el-button>
                      </div>
                      <p class="activity-desc">{{ item.description }}</p>
                      <div class="activity-tags">
                        <el-tag v-for="(tag,i) in item.tags" :key="tag" :type="types[i%types.length]" size="small">{{ tag }}</el-tag>
                      </div>
                      <div class="activity-footer">
                        <p class="activity-tip" v-if="item.tip">📝 {{ item.tip }}</p>
                        <el-button type="primary" size="small" @click="router.push(`/attraction/detail/${item.id}`)" style="background-color: #ff9500;border-color: #ff9500;">￥{{ item.price }}</el-button>
                      </div>
                    </div>
                  </div>
                  <div class="distance-car-time" v-if="itemIndex < day.activities.length - 1">
                    <img src="/icon/distance.png" style="width: 20px; height: 20px;">
                    <span style="margin-right: 20px;">{{ item.distance }}</span>
                    <img src="/icon/car.png" style="width: 20px; height: 20px;">
                    <span>{{ item.drivingTime }}</span>
                  </div>
                </div>
              </div>
              <div class="hotel-recommendations">
                <h3>为您推荐的酒店</h3>
                <div class="hotel-cards-container">
                  <!-- 酒店卡片 -->
                  <div class="hotel-card" v-for="hotel in day.recommendHotels" :key="hotel.id">
                    <img :src="`${hotel.image}`" class="hotel-image">
                    <div class="hotel-info">
                      <h4>{{ hotel.name }}</h4>
                      <div class="rating">⭐{{ hotel.rating }}</div>
                      <p class="location">📍 距离市中心{{hotel.downtownDistance}}</p>
                      <p class="price">¥{{hotel.price}}/晚</p>
                      <button class="select-btn"  @click="router.push(`/hotel/detail/${hotel.id}`)">选择此酒店</button>
                    </div>
                  </div>
                </div>
              </div>    
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </div>

      <!-- 右侧地图和预算展示区 -->
       <div class="map-panel">
        <el-card class="map-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <Route style="width: 25px; height: 25px; color: #e69255;"/>
              <span>行程地图</span>
            </div>
          </template>
          <div id="amap-container"></div>
          <!-- 从后端itinerary中提取景点名称，移除color相关逻辑 -->
          <div class="map-legend">
            <span v-for="(day, dayIdx) in itinerary" :key="dayIdx">
              <span v-for="(activity, actIdx) in day.activities" :key="actIdx" class="legend-item">
                <el-icon style="color: var(--c-primary-600);"><Location /></el-icon> {{ activity.name }}
              </span>
            </span>
          </div>
        </el-card>

        <!-- 预算摘要卡片 -->
        <el-card v-if="budgetSummary.total > 0" class="budget-summary-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <Budget style="width:25px; height: 20px; color: #1296db;"/>
              <span>预算摘要</span>
            </div>
          </template>
          <div class="budget-overview">
            <div class="total-budget">
              <span class="label">总预算</span>
              <span class="amount">¥ {{ budgetSummary.total.toLocaleString() }}</span>
            </div>
            <div class="budget-comparison">
              <span :class="{'over-budget': budgetSummary.total > travelForm.budget}">
                {{ budgetSummary.total > travelForm.budget ? '超出' : '剩余' }}: 
                ¥ {{ Math.abs(budgetSummary.total - travelForm.budget).toLocaleString() }}
              </span>
            </div>
          </div>
          <div class="budget-breakdown">
            <div v-for="(item) in budgetSummary.breakdown" :key="item.category" class="breakdown-item">
              <div class="item-header">
                <span class="item-label">{{item.category ||'其他费用' }}</span>
                <span class="item-amount">¥ {{ item.amount.toLocaleString() }}</span>
              </div>
              <el-progress :percentage="item.percentage*100" :color="getProgressColor(key)" :show-text="false" />
            </div>
          </div>
          <!-- ECharts 饼图容器 -->
          <div ref="budgetChart" class="budget-chart-container"></div>
        </el-card>
      </div>
    </div>

    <!-- 主要内容区：智能助手模式 -->
    <div class="chat-mode-container" v-show="currentMode === 'chat'">
      <div class="chat-layout">
        <!-- 快捷指令面板 -->
        <div class="sidebar-quick-panel">
            <div class="sidebar-header-small">
              <h3>快捷指令</h3>
              <p>点击快速提问</p>
            </div>
            <div class="quick-actions">
              <div 
                v-for="(item, index) in quickQuestions" 
                :key="index" 
                class="action-card"
                :class="{ 'disabled-card': isGenerating }"
                @click="!isGenerating && sendMessage(item.text)"
              >
                <div class="action-icon">
                  <component :is="item.icon === 'Promotion' ? Promotion : item.icon === 'Ticket' ? Ticket : item.icon === 'Food' ? Food :item.icon === 'MapLocation' ? MapLocation : item.icon === 'House' ? House : Picture " />
                </div>
                <span>{{ item.text }}</span>
              </div>
            </div>
        </div>

        <!-- 历史记录栏 -->
        <div class="chat-sidebar">
          <!-- 历史记录面板 -->
          <div class="sidebar-history-panel">
            <!-- 新建对话按钮 -->
            <div class="new-chat-section">
               <el-button type="primary" class="new-chat-btn" @click="startNewChat" round :disabled="isGenerating">
                 <el-icon style="margin-right: 5px"><Plus /></el-icon> 新建对话
               </el-button>
            </div>

            <!-- 历史对话列表 -->
            <div class="chat-history-section">
               <div class="section-title">历史对话</div>
               <div class="history-list-container">
                  <div v-for="session in chatHistory" :key="session.id" 
                       class="chat-session-item" 
                       :class="{ active: currentSessionId === session.id, 'disabled-item': isGenerating }"
                       @click="!isGenerating && loadSession(session)">
                    <span class="session-title">{{ session.title }}</span>
                    <el-icon class="delete-session-btn" @click.stop="!isGenerating && deleteSession(session.id)"><Delete /></el-icon>
                  </div>
               </div>
            </div>
            
            <!-- 上下文摘要 -->
            <div class="context-card" v-if="currentHistoryId">
              <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
                <h4 style="margin: 0;">当前规划</h4>
                <el-icon class="close-context-btn" @click="currentHistoryId = null" style="cursor: pointer; color: #909399;"><CircleClose /></el-icon>
              </div>
              <p>{{ travelForm.destination }} {{ travelForm.days }}日游</p>
              <div class="context-tags">
                <span class="tag">{{ travelForm.people }}人</span>
                <span class="tag">¥{{ travelForm.budget }}</span>
              </div>
            </div>
          </div>

        </div>

        <!-- 右侧对话主窗口 -->
        <div class="chat-main">
          <div class="chat-header-bar" style="padding: 10px 20px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; background: #fff;">
             <div class="model-switcher" style="display: flex; align-items: center; gap: 10px;">
                <span class="label" style="font-weight: bold; color: #555;">模型:</span>
                <el-radio-group v-model="selectedModel" size="small" @change="handleModelChange">
                  <el-radio-button label="">Qwen</el-radio-button>
                  <el-radio-button label="deepseek">
                    Deepseek
                    <el-icon v-if="userInfoStore.info.vip !== 1" style="margin-left: 4px;"><Lock /></el-icon>
                  </el-radio-button>
                </el-radio-group>
                <el-button v-if="userInfoStore.info.vip !== 1" type="warning" link size="small" @click="router.push('/my/subscription')" style="margin-left: 5px; font-weight: bold;">
                  升级解锁
                </el-button>
             </div>
             <div v-if="currentHistoryId" class="plan-reference-tag" style="font-size: 12px; color: #67c23a; display: flex; align-items: center; gap: 4px;">
                <el-icon><Link /></el-icon> 已引用当前行程
                <el-icon class="close-reference-btn" @click="currentHistoryId = null" style="cursor: pointer; margin-left: 4px;"><CircleClose /></el-icon>
             </div>
          </div>
          <div class="chat-body">
            <div v-for="(msg, index) in chatMessages" :key="msg.id" class="message-row" :class="msg.role">
              <div class="message-avatar">
                <img v-if="msg.role === 'ai'" src="@/assets/b.jpg" alt="AI">
                <el-avatar v-else :src="userInfoStore.info?.avatar" :size="40" style="background:var(--c-primary-600)">
                  <span v-if="!userInfoStore.info?.avatar">User</span>
                </el-avatar>
              </div>
              <div class="message-content">
                <div class="message-bubble">
                  <div v-if="msg.role === 'ai'">
                    <div v-if="msg.content === '...' && isGenerating && index === chatMessages.length - 1" class="typing-indicator">
                      <span></span><span></span><span></span>
                    </div>
                    <div v-else class="markdown-body" :class="{ 'streaming-cursor': isGenerating && index === chatMessages.length - 1 }">
                      <MarkdownRender :content="preprocessMarkdown(msg.content)" :max-live-nodes="0" />
                    </div>
                  </div>
                  <div v-else style="white-space: pre-wrap;">{{ msg.content }}</div>
                </div>
                <span class="message-time">{{ msg.time }}</span>
              </div>
            </div>
          </div>
          
          <div class="chat-footer">
            <div v-if="uploadImages.length > 0" class="upload-preview-list">
              <div v-for="(img, index) in uploadImages" :key="img.id" class="preview-item">
                <el-image :src="img.url" fit="cover" class="preview-img" />
                <div class="loading-mask" v-if="img.loading">
                  <el-icon class="is-loading"><Loading /></el-icon>
                </div>
                <div class="delete-mask" v-else>
                  <el-icon @click="removeImage(index)"><Delete /></el-icon>
                </div>
              </div>
            </div>
            <div class="input-wrapper">
              <el-input
                v-model="chatInput"
                placeholder="输入您的问题，例如：张家界哪里最好玩？"
                @keyup.enter="!isGenerating && sendMessage()"
                class="chat-input"
                :disabled="isGenerating"
              >
                <template #prefix>
                  <el-upload
                    class="chat-uploader"
                    action="#"
                    :auto-upload="false"
                    :show-file-list="false"
                    :on-change="handleFileSelect"
                    accept="image/*"
                    :limit="3"
                    :on-exceed="handleExceed"
                    :disabled="uploadImages.length >= 3 || isGenerating"
                  >
                    <el-button circle class="upload-btn" :disabled="uploadImages.length >= 3 || isGenerating">
                      <el-icon><Picture /></el-icon>
                    </el-button>
                  </el-upload>
                </template>
                <template #suffix>
                  <el-button type="primary" circle @click="sendMessage()" :disabled="isGenerating">
                    <el-icon v-if="isGenerating"><Loading /></el-icon>
                    <el-icon v-else><Promotion /></el-icon>
                  </el-button>
                </template>
              </el-input>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineOptions({
  name: 'Guide'
})
import { ref, onMounted, nextTick, watch } from 'vue';
import { ElMessage, ElNotification } from 'element-plus';
import * as echarts from 'echarts';
import { loadAMap } from '@/utils/amap-loader';
import {Document,Itinerary,Route,Budget} from '@/components/Icon.vue'
import { Clock, Delete, ChatDotRound, MapLocation, Promotion, Food, Ticket, Lock, Plus, Download, Link, Picture, House, Loading, CircleClose, Compass, MagicStick, Suitcase, Location } from '@element-plus/icons-vue'
import router from '@/router'
import request from '@/utils/request';
import { getTripPlanService,getPlanHistoryService,getHistoricalItineraryService, sendChatStream, getChatHistoryService, getChatMessagesService, deleteConversationService, uploadChatImageService } from '@/api/agent';
import { useUserInfoStore } from '@/stores/userInfo';
import { config } from '@/utils/config';
import { useTokenStore } from '@/stores/token';
import MarkdownRender from 'markstream-vue';
import 'markstream-vue/index.css';

const preprocessMarkdown = (content) => {
  if (!content) return '';
  let processedContent = content;

  // 0. 预处理：尝试修复丢失的空格 (针对紧凑输出)
  //  "##标题" -> "## 标题" (限制为 ##+ 以避免 #1 标签)
  processedContent = processedContent.replace(/(#{2,6})([^\s#])/g, '$1 $2');
  // "1.内容" -> "1. 内容" (排除数字，如 1.2，且要求前面是行首或空白符)
  processedContent = processedContent.replace(/(^|[\s\n])(\d+\.)([^\s\d])/g, '$1$2 $3');
  // " -内容" -> " - 内容" (仅处理前面有空格的情况，避免破坏 "上海-北京")
  processedContent = processedContent.replace(/(\s-)([^\s])/g, '$1 $2');

  // 紧凑的中文列表格式 (包括前面无空格的情况): "文字-列表项:" -> "文字\n\n- 列表项:"
  processedContent = processedContent.replace(/([^\n])\s*-\s*(\**[\u4e00-\u9fa5]{2,10}\**[:：])/g, '$1\n\n- $2');

  // 流式输出可能导致换行符丢失，手动补充 Markdown 换行
  // 1. 标题前加换行 (吞掉前面的空格，确保标题顶格)
  processedContent = processedContent.replace(/([^\n])\s*(#{1,6}\s)/g, '$1\n\n$2');
  // 2. 有序列表前加换行 (吞掉前面的空格)
  processedContent = processedContent.replace(/([^\n])\s*(\d+\.\s)/g, '$1\n\n$2');
  // 3. 无序列表前加换行 (吞掉前面的空格)
  processedContent = processedContent.replace(/([^\n])\s*(-\s)/g, '$1\n\n$2'); 
  // 4. 代码块前加换行 (吞掉前面的空格)
  processedContent = processedContent.replace(/([^\n])\s*(```)/g, '$1\n\n$2');

  // 5. 修复紧凑的列表项 (针对截图中的问题)
  // 情况A: "标点-文字" -> "标点\n\n- 文字" (如 "时间：-清晨", "建议：-使用")
  processedContent = processedContent.replace(/([：:。；;！!])\s*-([^\s\-])/g, '$1\n\n- $2');
  
  // 情况B: 列表项缺少空格 "\n-文字" -> "\n- 文字"
  processedContent = processedContent.replace(/\n-([^\s\-])/g, '\n- $1');

  return processedContent;
};




const AMAP_KEY = import.meta.env.VITE_AMAP_SERVICE_KEY;
const userInfoStore = useUserInfoStore();
const tokenStore = useTokenStore();

// --- 聊天历史记录逻辑 ---
const chatHistory = ref([]);
const currentSessionId = ref(null);

const loadChatHistory = async () => {
  try {
    const res = await getChatHistoryService();
    if (res.data) {
      chatHistory.value = res.data.map(item => ({
        id: item.id.toString(), 
        title: item.title,
        time: new Date(item.createTime).toLocaleDateString()
      }));
    }
  } catch (error) {
    console.error('Failed to load chat history', error);
  }
};

const startNewChat = () => {
  currentSessionId.value = null;
  chatMessages.value = [
    { 
      id: Date.now(), 
      role: 'ai', 
      content: '您好！我是您的AI旅行顾问。您可以问我关于景点、酒店、美食的任何问题，或者让我为您推荐行程。',
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    }
  ];
  chatInput.value = '';
};

const loadSession = async (session) => {
  currentSessionId.value = session.id;
  try {
    const res = await getChatMessagesService(session.id);
    if (res.data) {
      chatMessages.value = res.data.map(msg => ({
        id: msg.id,
        role: msg.role === 'user' ? 'user' : 'ai',
        content: msg.content,
        time: new Date(msg.createTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      }));
      
      nextTick(() => {
        const chatBody = document.querySelector('.chat-body');
        if (chatBody) chatBody.scrollTop = chatBody.scrollHeight;
      });
    }
  } catch (error) {
    console.error('Failed to load chat messages', error);
    ElMessage.error('加载会话失败');
  }
};

const deleteSession = async (id) => {
  try {
    await deleteConversationService(id);
    chatHistory.value = chatHistory.value.filter(item => item.id !== id);
    if (currentSessionId.value === id) {
      startNewChat();
    }
    ElMessage.success('删除成功');
  } catch (error) {
    console.error('Failed to delete session', error);
    ElMessage.error('删除会话失败');
  }
};

// --- 模式切换与聊天逻辑 ---
const currentMode = ref('planning'); // 'planning' | 'chat'
const selectedModel = ref(''); // 当前选择的模型

const handleModelChange = (val) => {
  if (val === 'deepseek' && userInfoStore.info.vip !== 1) {
    // 延迟一下切回，让用户看到点击效果
    nextTick(() => {
      selectedModel.value = '';
    });
    
    ElNotification({
      title: '会员权益提示',
      message: 'Deepseek 模型仅限高级会员使用，升级即可解锁更强大的智能助手！',
      type: 'warning',
      duration: 5000,
      onClick: () => router.push('/my/subscription')
    });
  }
};

const chatInput = ref('');
const uploadImages = ref([]);

const handleExceed = (files) => {
  ElMessage.warning(`最多只能上传 3 张图片`);
};

const handleFileSelect = async (uploadFile) => {
  const file = uploadFile.raw;
  if (!file) return;
  
  if (!file.type.startsWith('image/')) {
    ElMessage.error('只能上传图片文件！');
    return;
  }
  
  if (uploadImages.value.length >= 3) {
    ElMessage.warning('最多只能上传 3 张图片');
    return;
  }

  const imageItem = {
    id: Date.now(),
    url: URL.createObjectURL(file),
    loading: true
  };
  
  uploadImages.value.push(imageItem);
  
  const formData = new FormData();
  formData.append('files', file);

  try {
    const response = await uploadChatImageService(formData);
    console.log('Upload response:', response);
  
      // 我们将 ID 存储起来，图片预览继续使用本地的 blob URL
      // 注意：必须修改数组中的响应式对象，而不是原始的 imageItem 对象，否则视图不会更新
      const itemToUpdate = uploadImages.value.find(item => item.id === imageItem.id);
      if (itemToUpdate) {
        // 从 response.data 中获取文件 ID
        if (response.data && response.data.length > 0) {
          itemToUpdate.fileId = response.data[0];
        }
        itemToUpdate.loading = false;
      }
      ElMessage.success('图片上传成功');
  } catch (error) {
    console.error(error);
    ElMessage.error('图片上传失败');
    const index = uploadImages.value.findIndex(item => item.id === imageItem.id);
    if (index !== -1) uploadImages.value.splice(index, 1);
  }
};

const removeImage = (index) => {
  uploadImages.value.splice(index, 1);
};
const chatMessages = ref([
  { 
    id: 1, 
    role: 'ai', 
    content: '您好！我是您的AI旅行助手。您可以问我关于景点、酒店、美食的任何问题，或者让我为您推荐行程。',
    time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  }
]);

const quickQuestions = [
  { icon: 'Promotion', text: '推荐附近的酒店' },
  { icon: 'Ticket', text: '张家界门票多少钱？' },
  { icon: 'Food', text: '当地有什么特色美食？' },
  { icon: 'MapLocation', text: '帮我规划三日游' },
  { icon: 'Picture', text: '我想看一下张家界景点的照片' },
  { icon: 'House', text: '帮我订购北京的酒店' },
];

const sendMessage = async (text) => {
  const content = text || chatInput.value;
  if (!content.trim()) return;

  isGenerating.value = true;

  // 添加用户消息
  chatMessages.value.push({
    id: Date.now(),
    role: 'user',
    content: content,
    time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  });

  if (!text) chatInput.value = '';

  // 创建AI消息占位
  const aiMsgId = Date.now() + 1;
  chatMessages.value.push({
    id: aiMsgId,
    role: 'ai',
    content: '...', // 正在思考中
    time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  });

  // 滚动到底部
  nextTick(() => {
    const chatBody = document.querySelector('.chat-body');
    if (chatBody) chatBody.scrollTop = chatBody.scrollHeight;
  });

  // 获取图片ID列表
  const fileIds = uploadImages.value.map(img => img.fileId).filter(id => id);

  try {
    const response = await sendChatStream({ 
        message: content, 
        conversationId: typeof currentSessionId.value === 'string' ? currentSessionId.value : null,
        model: selectedModel.value,
        planId: currentHistoryId.value,
        fileIds: fileIds
    });

    // 发送后清空图片和输入框
    if (!text) {
      chatInput.value = '';
      uploadImages.value = [];
    }

    if (!response.ok) {
        if (response.status === 403) {
          throw new Error('VIP_REQUIRED');
        }
       throw new Error('Network response was not ok');
    }

    // 获取会话ID
    const conversationId = response.headers.get('X-Conversation-Id');
    if (conversationId && currentSessionId.value !== conversationId) {
        currentSessionId.value = conversationId;
        
        // 添加到历史记录
        const exists = chatHistory.value.find(h => h.id === conversationId);
        if (!exists) {
             chatHistory.value.unshift({
              id: conversationId,
              title: content.length > 10 ? content.substring(0, 10) + '...' : content,
              time: new Date().toLocaleDateString()
            });
        }
    }

    // 读取流
    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let aiContent = '';
    let buffer = '';
    
    // 获取响应式消息对象引用
    const aiMessage = chatMessages.value.find(m => m.id === aiMsgId);

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;
      
      const chunk = decoder.decode(value, { stream: true });
      
      // 检查是否是 SSE 格式
      const isSSE = response.headers.get('Content-Type')?.includes('text/event-stream');
      
      if (isSSE) {
        buffer += chunk;
        const lines = buffer.split('\n');
        buffer = lines.pop() || ''; // 保留未完成的行

        for (const line of lines) {
          if (line.startsWith('data:')) {
            // 注意：这里不要使用 trim()，否则会丢失开头的空格（对于流式输出很重要）
            // data: 后面通常有一个空格，slice(5) 会保留这个空格
            // 如果是 JSON，JSON.parse 会忽略空格
            // 如果是纯文本，我们需要这个空格
            const data = line.slice(5);
            
            // 忽略 [DONE] 标记
            if (data.trim() === '[DONE]') continue;
            
            try {
              // 尝试解析 JSON
              const parsed = JSON.parse(data);
              const text = typeof parsed === 'object' ? (parsed.content || parsed.text || parsed.message || '') : parsed;
              aiContent += text;
            } catch {
              // 如果不是 JSON，直接追加文本
              // 注意：如果 data 是 " hello"，slice(5) 是 " hello"
              // 如果后端发送的是 "data: hello"，slice(5) 是 " hello"
              // 如果后端发送的是 "data:hello"，slice(5) 是 "hello"
              // 通常 SSE 规范建议 data: 后有个空格
              // 这里我们假设如果开头有空格，是协议分隔符，如果内容本身需要空格，后端应该处理好
              // 为了安全起见，如果 data 开头是空格，我们去掉第一个空格
              // 但如果内容就是 " " (空格)，data:  -> slice(5) -> "  " -> 去掉第一个 -> " "
              
              let text = data;
              if (text.startsWith(' ')) {
                  text = text.substring(1);
              }
              aiContent += text;
            }
          }
        }
      } else {
        // 非 SSE 模式，直接追加
        aiContent += chunk;
      }
      
      if (aiMessage && aiContent) {
          aiMessage.content = aiContent;
      }
      
      nextTick(() => {
        const chatBody = document.querySelector('.chat-body');
        if (chatBody) chatBody.scrollTop = chatBody.scrollHeight;
      });
    }

    // 响应完成后，刷新当前会话以确保 Markdown 格式正确
    if (conversationId && currentSessionId.value === conversationId) {
      const res = await getChatMessagesService(conversationId);
      if (res.data) {
        chatMessages.value = res.data.map(msg => ({
          id: msg.id,
          role: msg.role === 'user' ? 'user' : 'ai',
          content: msg.content,
          time: new Date(msg.createTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
        }));
        
        nextTick(() => {
          const chatBody = document.querySelector('.chat-body');
          if (chatBody) chatBody.scrollTop = chatBody.scrollHeight;
        });
      }
    }

  } catch (error) {
    console.error('Chat error:', error);
    const aiMessage = chatMessages.value.find(m => m.id === aiMsgId);
    if (aiMessage) {
        if (error.message === 'VIP_REQUIRED') {
             aiMessage.content = '非VIP用户不可使用 DeepSeek 模型，请升级为VIP用户后重试。';
        } else {
             aiMessage.content += '\n[网络异常，请稍后再试]';
        }
    }
  } finally {
    isGenerating.value = false;
  }
};

// --- 响应式数据 ---
const travelForm = ref({
  destination: '张家界',
  startDate: new Date(),
  days: 3,
  people: 2,
  preferences: ['自然风光'],
  budget: 5000,
});

const isGenerating = ref(false);
const itinerary = ref([]);
const currentHistoryId = ref(null);
const map = ref(null); // 存储地图实例
const markers = ref([]); // 存储所有标记点
const budgetChart = ref(null); // ECharts 容器的引用

const budgetSummary = ref({
  breakdown: [],
  total: 0
});

// --- 历史记录逻辑 ---
const showHistory = ref(false);
const historyList = ref([]);

const loadHistory = async () => {
  try {
    const result = await getPlanHistoryService();
    historyList.value = result.data || [];
  } catch (e) {
    console.error('读取历史记录失败', e);
    historyList.value = [];
    ElMessage.error('读取历史记录失败');
  }
};

const deleteHistory = async (index) => {
  try {
    const historyId = historyList.value[index].id; // 假设后端返回的历史记录有id字段
    await request.delete(`/agent/history/${historyId}`);
    
    // 重新加载历史记录，确保数据最新
    await loadHistory();
    ElMessage.success('删除成功');
  } catch (e) {
    console.error('删除历史记录失败', e);
    ElMessage.error('删除历史记录失败');
  }
};

const getHistoricalItinerary = async (historyId) => {
  try {
    const result = await getHistoricalItineraryService(historyId);
    const data = result.data || [];

    itinerary.value = data.dailySchedules;
    budgetSummary.value = data.budgetSummary; 

    itinerary.value.forEach(day =>{
      day.activities.forEach(active =>{
        if(typeof active.tags === 'string' && active.tags !== ''){
          active.tags = active.tags.split(',');
        }else{
          active.tags = [];
        }
      })
    })

    // 生成后，在地图上标记所有点
    nextTick(() => {
      addMarkersToMap();
      renderBudgetChart(); // 每次生成行程后都重新渲染图表
    });

  } catch (e) {
    console.error('获取历史行程失败', e);
    itinerary.value = [];
    ElMessage.error('获取历史行程失败');
  }
}
const dateFormat = (createTime) =>{
  const dateString = createTime;
  const date = new Date(dateString);
  return date.toLocaleString();
}
const applyHistory = async(item) => {
  currentHistoryId.value = item.id;
  // 回填表单
  travelForm.value = {
    destination: item.destination,
    startDate: new Date(item.startDate), // 确保日期格式正确
    days: item.dayNum,
    people: item.people,
    preferences: item.preferences ? [...item.preferences] : [],
    budget: item.budget
  };
  await getHistoricalItinerary(item.id);
};

// --- 模拟数据 ---
// 注意：这里的经纬度是高德地图坐标系(GCJ02)
const locations = [
  {id:1, name: '张家界森林公园南门', position: [110.4801, 29.3322], color: '#f56c6c' },
  {id:2, name: '袁家界景区', position: [110.5225, 29.3389], color: '#2563eb' },
  {id:3, name: '天子山景区', position: [110.5158, 29.3582], color: '#67c23a' },
  {id:4, name: '湘味特色餐厅', position: [110.4822, 29.3356], color: '#e6a23c' },
];

const types = ["warning", "primary", "success", "danger"]

// 禁用当前时间之前的日期+时间
const disabledBeforeNow = (date) => {
  // 直接对比当前完整时间（无需重置时分秒）
  return date < new Date()
}

// 2. 修正进度条颜色映射（按后端中文分类匹配颜色）
const getProgressColor = (category) => {
  const colors = {
    '门票费用': '#E6A23C', // 橙色-门票
    '住宿费用': '#67C23A', // 绿色-住宿
    '餐饮费用': '#F56C6C', // 红色-餐饮
    '交通费用': '#81D4FA', // 蓝色-交通
    '其他费用': '#909399'  // 灰色-其他
  };
  return colors[category] || '#2563eb'; // 默认蓝色（与设计令牌主色 #2563eb 保持一致）
};

// 渲染 ECharts 饼图
const renderBudgetChart = () => {
  if (!budgetChart.value || !budgetSummary.value.breakdown.length) return;

  const chart = echarts.init(budgetChart.value);
  
  // 从后端返回的 breakdown 数组中提取饼图数据
  const pieData = budgetSummary.value.breakdown.map(item => ({
    value: item.amount,        // 预算金额
    name: item.category,       // 预算分类（后端中文）
    itemStyle: { 
      color: getProgressColor(item.category) // 匹配进度条颜色
    }
  }));

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: ¥{c} ({d}%)' // 提示框显示“分类：金额（百分比）”
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      textStyle: { fontSize: 12 }
    },
    series: [
      {
        name: '预算分配',
        type: 'pie',
        radius: '50%',
        data: pieData,
        emphasis: {
          itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' }
        }
      }
    ]
  };
  
  chart.setOption(option);
  window.addEventListener('resize', () => chart.resize());
};


const generateItinerary = async () => { 
  // 检查会员状态和使用次数 (模拟)
  if (userInfoStore.info.vip !== 1) {
    // 这里可以添加一个本地计数器，或者直接提示
    // 为了演示，我们假设非会员只能生成一次，或者每次都提示升级
    // 实际项目中应该由后端返回错误码或剩余次数
    
    // 简单提示
    ElNotification({
      title: '会员权益提示',
      message: '您当前使用的是普通版，升级会员可享受无限次AI规划及更优路线推荐！',
      type: 'info',
      duration: 5000
    });
  }

  isGenerating.value = true;
  const userPlan = {
    destination: travelForm.value.destination,
    startDate: travelForm.value.startDate,
    dayNum: travelForm.value.days,
    people: travelForm.value.people,
    preferences: travelForm.value.preferences,
    budget: travelForm.value.budget
  };
  const result = await getTripPlanService(userPlan);
  const taskId = result.data;
  console.log("任务ID"+taskId)
  const eventSource = new EventSource(`/api/agent/progress/${taskId}`);

  eventSource.addEventListener('complete', async(event) => {
    const data = JSON.parse(event.data);
    eventSource.close();
    itinerary.value = data.dailySchedules;
    budgetSummary.value = data.budgetSummary; 

    itinerary.value.forEach(day =>{
      day.activities.forEach(active =>{
        if(typeof active.tags === 'string' && active.tags !== ''){
          active.tags = active.tags.split(',');
        }else{
          active.tags = [];
        }
      })
    })

    isGenerating.value = false;
    ElMessage.success('行程规划成功！');
    // 生成后，在地图上标记所有点
    nextTick(() => {
      addMarkersToMap();
      renderBudgetChart(); // 每次生成行程后都重新渲染图表
    });
    await loadHistory(); // 保存历史记录，等待异步操作完成
    currentHistoryId.value = historyList.value[0]?.id;
  })
  eventSource.addEventListener('progress', (event) => {
    const data = JSON.parse(event.data);
    ElNotification({
      message: data.message,
      duration: 5000,
      type: 'primary'
    });
  })

  eventSource.onerror = (error) => { 
    eventSource.close();
    isGenerating.value = false;
    ElMessage.error('服务器繁忙，请稍后重试！');
  };
}

const exportImage = async () => {
  if (!currentHistoryId.value) return;
  try {
    ElMessage.primary('正在生成图片，请稍候...');
    const token = tokenStore.accessToken;
    const response = await fetch(`${config.screenshotBaseUrl}/screenshot?id=${currentHistoryId.value}&token=${token}`);
    if (!response.ok) throw new Error('Export failed');
    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `trip-${currentHistoryId.value}.png`;
    a.click();
    window.URL.revokeObjectURL(url);
    ElMessage.success('导出成功');
  } catch (e) {
    console.error(e);
    ElMessage.error('导出失败，请确保截图服务已启动');
  }
};


// 初始化高德地图
const initMap = async () => {
  try {
    const AMap = await loadAMap(AMAP_KEY, ['AMap.ToolBar','AMap.Scale']);
      map.value = new AMap.Map('amap-container', {
        zoom: 11,
        center: [110.4800, 29.1167], // 张家界市中心
        viewMode: '3D', // 开启3D视图，效果更佳
        pitch: 45, // 地图俯仰角度
      });
      map.value.on('ready', () => {
        addMarkersToMap();
      });

      // 添加地图控件
      map.value.addControl(new AMap.Scale());
      map.value.addControl(new AMap.ToolBar());
      console.log('地图初始化成功！');
  } catch (error) {
    console.error('地图初始化失败：', error);
  }
};


// 在地图上添加标记
const addMarkersToMap = () => {
  if (!map.value) return;
  // 清除旧的标记
  if (markers.value.length > 0) {
    map.value.remove(markers.value);
    markers.value = [];
  }

  // 直接从后端itinerary中提取景点位置信息（ID、名称、经纬度）
  itinerary.value.forEach(day => {
    day.activities.forEach(activity => {
      if (activity.position && activity.name) {
        // 固定使用默认蓝色，移除color字段
        const markerColor = '#2563eb';

        // 自定义 Marker 图标
        const customIcon = new AMap.Icon({
          size: new AMap.Size(24, 24),
          imageSize: new AMap.Size(24, 24),
          content: `
            <div style="
              width: 16px; 
              height: 16px; 
              border-radius: 50%; 
              background-color: ${markerColor};  
              border: 2px solid white; 
              box-shadow: 0 2px 4px rgba(0,0,0,0.2);  
              margin: 4px auto; 
            "></div>
          `
        });

        // 创建 Marker
        const marker = new AMap.Marker({
          position: activity.position,
          title: activity.name,
          icon: customIcon,
          anchor: new AMap.Pixel(12, 12)
        });
        
        marker.setMap(map.value);
        markers.value.push(marker);

        // 信息窗口
        const infoWindow = new AMap.InfoWindow({
          content: `<div class="info-window-content"><h3>${activity.name}</h3><br>${activity.description}</div>`,
          anchor: 'bottom-center',
        });
        
        marker.on('click', () => {
          infoWindow.open(map.value, marker.getPosition());
        });
      }
    });
  });
  
  // 调整地图视角
  if (markers.value.length > 0) {
    map.value.setFitView(markers.value);
  }
};

// 点击行程项，聚焦地图位置
const focusOnLocation = (activity) => {
  if (map.value && activity.position) {
    map.value.setZoomAndCenter(14, activity.position);
  }
};

// 调用高德地图导航
const navigateToDestination = (activity) => {
  const destinationName = activity.name;
  const destinationPosition = activity.position;

  if (!destinationPosition) {
    ElMessage.warning('该地点暂无导航信息');
    return;
  }

  const url = `https://uri.amap.com/navigation?to=${destinationPosition.join(',')},${destinationName}&mode=car&policy=1&src=mypage&coordinate=gaode&callnative=1`;

  if (/Android/i.test(navigator.userAgent) || /iPhone|iPad|iPod/i.test(navigator.userAgent)) {
    window.location.href = url;
  } else {
    window.open(url, '_blank');
  }
};

// 监听模式切换，修复地图显示问题
watch(currentMode, (newVal) => {
  if (newVal === 'planning' && map.value) {
    nextTick(() => {
      map.value.resize();
    });
  }
});

// --- 生命周期 ---
onMounted(async () => {
  await loadHistory();
  await loadChatHistory();
  // 确保高德地图JS API加载完成后再初始化地图
  if (window.AMap) {
    initMap();
  } else {
    // 如果API还没加载完，可以加个延时或监听事件
    setTimeout(initMap, 500);
  }
});
</script>

<style scoped>
.travel-planner {
  min-height: 100vh;
  background: linear-gradient(135deg, var(--c-violet-500) 0%, var(--c-violet-700) 100%);
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
}

.planner-header {
  padding: 2rem 1rem;
  text-align: center;
  color: white;
  position: relative;
}

.history-btn {
  position: absolute;
  right: 2rem;
  top: 50%;
  transform: translateY(-50%);
  background-color: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.4);
  color: white;
  font-size: var(--fs-body-lg);
  width: 40px;
  height: 40px;
}
.history-btn:hover {
  background-color: rgba(255, 255, 255, 0.3);
  color: white;
  border-color: white;
}

.header-content .main-title {
  font-size: var(--fs-h1);
  margin-bottom: 0.5rem;
  font-weight: 300;
  text-shadow: 0 2px 4px rgba(0,0,0,0.2);
}

.subtitle {
  font-size: var(--fs-body-lg);
  opacity: 0.9;
}

.planner-container {
  display: grid;
  grid-template-columns: 320px 1fr 450px;
  gap: 1.5rem;
  padding: 0 1.5rem 2rem;
  max-width: 1600px;
  margin: 0 auto;
}

.generate-btn {
  width: 100%;
  font-size: var(--fs-body-lg);
  padding: 12px 0;
  border-radius: 8px;
  background: linear-gradient(90deg, var(--c-violet-500), var(--c-violet-700));
  border: none;
}

.empty-state, .loading-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--c-ink-3);
  padding: var(--sp-12) var(--sp-4);
}
/* el-icon 渲染出的就是 <i class="el-icon">，因此这条规则依然命中新图标 */
.empty-state i, .loading-state i {
  font-size: 4rem;
  margin-bottom: var(--sp-4);
}
.empty-state__icon {
  color: var(--c-primary-200);
}
.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid var(--c-line-2);
  border-top: 4px solid var(--c-primary-600);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}
@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.input-panel, .itinerary-panel, .map-panel {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.input-card, .itinerary-card, .map-card, .budget-summary-card{
  flex: 0 0 auto; 
  min-height: 400px; 
  display: flex;
  flex-direction: column;
  border-radius: 12px;
   backdrop-filter: blur(10px);
}

:deep(.custom-timeline-item .el-timeline-item__timestamp) {
  font-size: var(--fs-h3); 
  color: black;   
  font-weight: bold;
}

.day-card {
  display: flex;
  flex-direction: column;
}
.activity-item {
  display: flex; 
  align-items: flex-start; 
  gap: 16px; 
  padding:1rem 0.5rem 1rem 0.5rem;
  border-bottom: 1px solid var(--c-line);
  cursor: pointer;
  transition: background-color 0.3s;
  border-radius: 10px;
  background-color: var(--c-bg-sub);
}

.activity-item img {
  width: 95px;
  height: 135px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0; 
}

.activity-content {
  flex: 1;
}
.activity-item:last-child {
  border-bottom: none;
}
.activity-item:hover {
  background-color: var(--c-bg-sub);
}
.activity-header {
  display: flex;
  flex: 1;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}
.activity-header h3 {
  margin: 0;
  font-size: var(--fs-body-lg);
  color: var(--c-ink);
}
.nav-btn {
  color: var(--c-primary-600);
  padding: 0;
}
.activity-desc {
  color: var(--c-ink-2);
  font-size: var(--fs-body);
  margin-bottom: 0.8rem;
}

.activity-footer { 
  display: flex;
  align-items: center;
  justify-content: space-between;
}


.activity-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 0.8rem;
}
.activity-tip {
  font-size: var(--fs-caption);
  color: var(--c-ink-3);
  margin: 0;
}

.distance-car-time{
  display: flex;
  align-items: center;
  margin-top: 10px;
  margin-bottom: 10px;
  flex-direction: row;
  gap: 10px;
}

.hotel-recommendations {
  margin: 20px 0;
}

.hotel-cards-container {
  display: flex;
  max-width: 670px;
  gap: 20px;
  overflow-x: auto;
  padding: 8px 0;
  scroll-behavior: smooth;
  border-radius: 10px;
  background-color: var(--c-bg-sub);
}

.hotel-cards-container::-webkit-scrollbar {
  height: 8px;
}

.hotel-cards-container::-webkit-scrollbar-track {
  background: var(--c-bg-sub);
  border-radius: 10px;
}

.hotel-cards-container::-webkit-scrollbar-thumb {
  background: var(--c-ink-3);
  border-radius: 10px;
}

.hotel-card {
  min-width: 200px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  display: flex;
  flex-direction: column;
}

.hotel-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.15);
}

.hotel-image {
  width: 100%;
  height: 120px;
  object-fit: cover;
}

.hotel-info { 
  padding: 6px 8px 10px 8px;
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.hotel-info h4 {
  margin: 0 0 5px 0;
  font-size: var(--fs-body-lg);
  color: var(--c-ink);
  white-space: normal;
  word-break: break-all;
}

.rating {
  color: var(--c-danger);
  font-weight: bold;
  margin: 0 0 3px 0;
}

.location {
  color: var(--c-ink-3);
  font-size: var(--fs-caption);
  margin: 0 0 3px 0;
}

.price {
  font-size: var(--fs-h3);
  font-weight: bold;
  color: var(--c-success);
  margin:0 0 6px 0;
}

.select-btn {
  width: 100%;
  padding: 10px;
  background: linear-gradient(135deg, var(--c-violet-500) 0%, var(--c-violet-700) 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: var(--fs-body);
  font-weight: bold;
  transition: background 0.3s ease;
  margin-top: auto;
}

.select-btn:hover {
  background: linear-gradient(135deg, var(--c-violet-700) 0%, var(--c-violet-500) 100%);
}

#amap-container {
  width: 100%;
  min-height: 400px;
  border-radius: 8px;
  overflow: hidden;
}

.card-header{
  display: flex;
  gap: 1rem;
}

.map-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  padding-top: 10px;
  border-top: 1px solid var(--c-line);
  margin-top: 10px;
}
.legend-item {
  font-size: var(--fs-body);
  color: var(--c-ink-2);
  display: flex;
  align-items: center;
}
.legend-item i {
  margin-right: 5px;
}

/* 预算摘要样式 */
.budget-summary-card {
  margin-bottom: 0; 
}
.budget-overview {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--c-line);
}
.total-budget .label {
  font-size: var(--fs-body);
  color: var(--c-ink-3);
}
.total-budget .amount {
  display: block;
  font-size: 1.8rem;
  font-weight: bold;
  color: var(--c-ink);
}
.budget-comparison {
  text-align: right;
  font-size: var(--fs-body);
}
.budget-comparison .over-budget {
  color: var(--c-danger);
  font-weight: bold;
}
.budget-breakdown {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.breakdown-item .item-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.5rem;
}
.item-label {
  font-size: var(--fs-body);
  color: var(--c-ink-2);
}
.item-amount {
  font-size: var(--fs-body);
  font-weight: bold;
  color: var(--c-ink);
}

/* ECharts 图表容器样式 */
.budget-chart-container {
  width: 100%;
  height: 220px; 
  margin-top: 1rem;
}


/* 响应式设计 */
@media (max-width: 1400px) {
  .planner-container {
    grid-template-columns: 300px 1fr 400px;
  }
}
@media (max-width: 1200px) {
  .planner-container {
    grid-template-columns: 1fr;
  }
  .input-panel, .itinerary-panel, .map-panel {
    height: auto;
  }
  #amap-container {
    min-height: 300px;
  }
}

/* 历史记录样式 */
.history-list {
  padding: 0 10px;
}
.history-item {
  background-color: var(--c-bg-sub);
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: background-color 0.3s, box-shadow 0.3s;
  border: 1px solid transparent;
}
.history-item:hover {
  background-color: var(--c-primary-50);
  border-color: var(--c-primary-300);
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.history-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.history-dest {
  font-weight: bold;
  font-size: var(--fs-body-lg);
  color: var(--c-ink);
}
.history-info {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}
.info-tag {
  background-color: #fff;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: var(--fs-caption);
  color: var(--c-ink-2);
  border: 1px solid var(--c-line);
}
.history-preferences {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 8px;
}
.pref-tag {
  background-color: var(--c-primary-50);
  color: var(--c-primary-600);
  font-size: var(--fs-caption);
  padding: 2px 6px;
  border-radius: 4px;
}
.history-time {
  font-size: var(--fs-caption);
  color: var(--c-ink-3);
  text-align: right;
}
.delete-btn {
  padding: 4px;
  height: auto;
}
.delete-btn:hover {
  color: var(--c-danger);
  background-color: rgba(245, 108, 108, 0.1);
}

/* --- 模式切换器样式 --- */
.mode-switcher {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin-top: 1rem;
}

.mode-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 20px;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 20px;
  color: white;
  cursor: pointer;
  transition: background-color 0.3s ease, border-color 0.3s ease, transform 0.3s ease;
  font-size: var(--fs-body-lg);
}

.mode-item:hover {
  background: rgba(255, 255, 255, 0.3);
}

.mode-item.active {
  background: white;
  color: var(--c-violet-700);
  font-weight: bold;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

/* --- 智能助手模式样式 --- */
.chat-mode-container {
  max-width: 1600px;
  margin: 0;
  padding: 0 1.5rem 2rem;
  height: calc(100vh - 180px); /* 减去头部高度 */
}

.chat-layout {
  display: flex;
  height: 100%;
  gap: 1.5rem;
}

.chat-sidebar {
  width: 300px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  background: transparent;
  backdrop-filter: none;
  padding: 0;
}

.sidebar-history-panel {
  flex: 1;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  overflow: hidden;
}

.sidebar-quick-panel {
  width: 260px;
  flex-shrink: 0;
  height: fit-content;
  background: linear-gradient(135deg, var(--c-primary-50) 0%, var(--c-success-soft) 100%);
  border: 1px solid #fff;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  border-radius: 12px;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.sidebar-header h3 {
  margin: 0;
  color: var(--c-ink);
}
.sidebar-header p {
  margin: 5px 0 0;
  color: var(--c-ink-3);
  font-size: var(--fs-body);
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.action-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s, transform 0.2s, box-shadow 0.2s;
  border: 1px solid rgba(255,255,255,0.5);
}

.action-card:hover {
  background: var(--c-primary-50);
  transform: translateX(5px);
}

.action-icon {
  width: 32px;
  height: 32px;
  background: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--c-primary-600);
}

.context-card {
  margin-top: auto;
  background: linear-gradient(135deg, var(--c-violet-500) 0%, var(--c-violet-700) 100%);
  color: white;
  padding: 1rem;
  border-radius: 8px;
}
.context-card h4 { margin: 0 0 8px 0; }
.context-card p { margin: 0 0 8px 0; font-size: var(--fs-body); opacity: 0.9; }
.context-tags { display: flex; gap: 8px; }
.context-tags .tag { background: rgba(255,255,255,0.2); padding: 2px 6px; border-radius: 4px; font-size: var(--fs-caption); }

.chat-main {
  flex: 1;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-body {
  flex: 1;
  padding: 2rem;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.message-row {
  display: flex;
  gap: 1rem;
  max-width: 80%;
}

.message-row.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-avatar img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--c-bg-sub);
}

.message-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.message-bubble {
  padding: 16px 20px;
  border-radius: 16px;
  font-size: var(--fs-body);
  line-height: 1.6;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  max-width: 100%;
}

.message-row.ai .message-bubble {
  background: #ffffff;
  color: var(--c-ink);
  border-top-left-radius: 4px;
  border: 1px solid var(--c-line);
}

.message-row.user .message-bubble {
  background: var(--c-primary-600);
  color: white;
  border-top-right-radius: 4px;
  box-shadow: 0 2px 12px rgba(37, 99, 235, 0.2);
}

.message-time {
  font-size: var(--fs-caption);
  color: var(--c-ink-3);
  align-self: flex-start;
}
.message-row.user .message-time {
  align-self: flex-end;
}

.chat-footer {
  padding: 1.5rem;
  background: white;
  border-top: 1px solid var(--c-line);
}

.input-wrapper {
  max-width: 800px;
  margin: 0 auto;
}

/* --- 新增聊天历史相关样式 --- */
.new-chat-section {
  margin-bottom: 1rem;
}
.new-chat-btn {
  width: 100%;
  justify-content: center;
  background: linear-gradient(90deg, var(--c-violet-500), var(--c-violet-700));
  border: none;
}

.chat-history-section {
  flex: 1;
  overflow-y: auto;
  margin-bottom: 1rem;
  min-height: 100px;
}

.section-title {
  font-size: var(--fs-caption);
  color: var(--c-ink-3);
  margin-bottom: 0.5rem;
  padding-left: 4px;
}

.history-list-container {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.chat-session-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
  color: var(--c-ink-2);
  font-size: var(--fs-body);
}

.chat-session-item:hover {
  background-color: var(--c-bg-sub);
}

.chat-session-item.active {
  background-color: var(--c-primary-50);
  color: var(--c-primary-600);
  font-weight: 500;
}

.session-title {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}

.delete-session-btn {
  opacity: 0;
  transition: opacity 0.2s;
  font-size: var(--fs-body);
  padding: 4px;
}
.delete-session-btn:hover {
  color: var(--c-danger);
  background-color: rgba(245, 108, 108, 0.1);
  border-radius: 4px;
}

.chat-session-item:hover .delete-session-btn {
  opacity: 1;
}

.sidebar-header-small h3 {
  margin: 0;
  font-size: var(--fs-body-lg);
  color: var(--c-ink);
}
.sidebar-header-small p {
  margin: 4px 0 10px;
  color: var(--c-ink-3);
  font-size: var(--fs-caption);
}

:deep(.chat-input .el-input__wrapper) {
  border-radius: 24px;
  padding-left: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
  transition: box-shadow 0.3s ease;
}

:deep(.chat-input .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.5), 0 2px 12px rgba(37, 99, 235, 0.2);
}

/* Markdown Styles */
:deep(.markdown-body) {
  font-size: var(--fs-body);
  line-height: 1.7;
  color: var(--c-ink);
}

:deep(.markdown-body p) {
  margin-bottom: 12px;
}

:deep(.markdown-body h1),
:deep(.markdown-body h2),
:deep(.markdown-body h3),
:deep(.markdown-body h4) {
  margin-top: 24px;
  margin-bottom: 16px;
  font-weight: 700;
  color: var(--c-ink);
  border-bottom: none;
}

:deep(.markdown-body h3) {
    font-size: 1.1em;
}

:deep(.markdown-body h4) {
    font-size: 1em;
}

:deep(.markdown-body ul),
:deep(.markdown-body ol) {
  padding-left: 24px;
  margin-bottom: 16px;
}

:deep(.markdown-body li) {
  margin-bottom: 6px;
}

:deep(.markdown-body strong) {
  font-weight: 600;
  color: #000;
}

:deep(.markdown-body code) {
  color: var(--c-accent);
  background-color: rgba(230, 162, 60, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: SFMono-Regular,Consolas,Liberation Mono,Menlo,monospace;
}

:deep(.markdown-body pre) {
  background-color: var(--c-ink);
  color: var(--c-ink-4);
  padding: 16px;
  border-radius: 8px;
  margin: 16px 0;
  overflow: auto;
  font-size: 85%;
  line-height: 1.45;
}

:deep(.markdown-body pre code) {
  color: inherit;
  background-color: transparent;
  padding: 0;
  margin: 0;
  font-size: 100%;
  word-break: normal;
  white-space: pre;
  border: 0;
}

:deep(.markdown-body blockquote) {
  padding: 12px 16px;
  color: var(--c-ink-3);
  background-color: var(--c-bg-sub);
  border-left: 4px solid var(--c-primary-600);
  border-radius: 4px;
  margin: 16px 0;
}

:deep(.markdown-body a) {
  color: var(--c-primary-600);
  text-decoration: none;
  font-weight: 500;
}

:deep(.markdown-body a:hover) {
  text-decoration: underline;
}

:deep(.markdown-body table) {
  display: block;
  width: 100%;
  overflow: auto;
  margin-bottom: 16px;
  border-radius: 8px;
  box-shadow: 0 0 0 1px var(--c-line);
  border-spacing: 0;
  border-collapse: collapse;
}

:deep(.markdown-body th) {
  background-color: var(--c-bg-sub);
  font-weight: 600;
  color: var(--c-ink-2);
  border: 1px solid var(--c-line);
  padding: 12px;
}

:deep(.markdown-body td) {
  border: 1px solid var(--c-line);
  padding: 12px;
  color: var(--c-ink-2);
}

:deep(.markdown-body tr:nth-child(2n)) {
  background-color: var(--c-bg-sub);
}

:deep(.markdown-body img) {
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  margin: 12px 0;
  max-width: 100%;
  box-sizing: content-box;
  background-color: #fff;
}

/* Disabled States */
.disabled-card,
.disabled-item {
  opacity: 0.6;
  cursor: not-allowed !important;
  pointer-events: none;
}

/* Streaming Cursor */
.streaming-cursor::after {
  content: '';
  display: inline-block;
  width: 8px;
  height: 16px;
  background-color: var(--c-primary-600);
  margin-left: 4px;
  vertical-align: middle;
  animation: blink 1s step-end infinite;
  border-radius: 1px;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

/* Typing Indicator */
.typing-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 4px;
  min-height: 24px;
}

.typing-indicator span {
  width: 6px;
  height: 6px;
  background-color: var(--c-ink-3);
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out both;
}

.typing-indicator span:nth-child(1) {
  animation-delay: -0.32s;
}

.typing-indicator span:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes typing {
  0%, 80%, 100% { 
    transform: scale(0);
    opacity: 0.5;
  }
  40% { 
    transform: scale(1);
    opacity: 1;
  }
}
</style>

<style>
/* 全局样式，用于修改信息窗体 */
.info-window-content {
  padding: 10px;
  max-width: 200px;
}

/* Upload Styles */
.upload-preview-list {
  display: flex;
  gap: 10px;
  padding: 10px 20px 0;
  background: #fff;
}
.preview-item {
  position: relative;
  width: 60px;
  height: 60px;
  border-radius: 4px;
  overflow: hidden;
  border: 1px solid var(--c-line-2);
}
.preview-img {
  width: 100%;
  height: 100%;
}
.loading-mask, .delete-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}
.delete-mask {
  opacity: 0;
  transition: opacity 0.3s;
  cursor: pointer;
}
.preview-item:hover .delete-mask {
  opacity: 1;
}
.chat-uploader {
  display: flex;
  align-items: center;
  margin-right: 10px;
}
.upload-btn {
  border: none !important;
  background: transparent !important;
  font-size: var(--fs-h3);
  color: var(--c-ink-2);
  padding: 8px !important;
  height: auto !important;
  width: auto !important;
}
.upload-btn:hover {
  color: var(--c-primary-600);
  background: var(--c-bg-sub) !important;
}
.upload-btn.is-disabled {
  color: var(--c-ink-4);
  cursor: not-allowed;
}
</style>