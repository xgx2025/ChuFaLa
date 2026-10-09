<template>
  <div class="travel-planner">
    <div class="planner-header">
      <div class="header-content">
        <span class="header-eyebrow">CHUFALA / TRAVEL STUDIO</span>
        <h1 class="main-title">
          把想去的地方，排成刚好的旅程
        </h1>
        <p class="subtitle">告诉我们目的地、时间和喜好，剩下的路线交给智能规划。</p>
      </div>
      <div class="header-actions">
        <div class="mode-switcher" aria-label="规划模式">
          <button type="button"
            class="mode-item"
            :class="{ active: currentMode === 'planning' }"
            :aria-pressed="currentMode === 'planning'"
            @click="currentMode = 'planning'"
          >
            <el-icon><MapLocation /></el-icon>
            <span>行程定制</span>
          </button>
          <button type="button"
            class="mode-item"
            :class="{ active: currentMode === 'chat' }"
            :aria-pressed="currentMode === 'chat'"
            @click="currentMode = 'chat'"
          >
            <el-icon><ChatDotRound /></el-icon>
            <span>AI 旅行顾问</span>
          </button>
        </div>
        <button type="button" class="history-btn" @click="showHistory = true">
          <el-icon><Clock /></el-icon>
          <span>规划历史</span>
        </button>
      </div>
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
    <div class="planner-container" :class="{ 'planner-container--has-result': itinerary.length > 0 && !isGenerating }" v-show="currentMode === 'planning'">
      <!-- 左侧输入面板 -->
      <div class="input-panel">
        <el-card class="input-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-header__number">01</span>
              <span>设定旅行需求</span>
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
            
            <el-form-item label="同行总预算（元）">
              <el-slider v-model="travelForm.budget" :step="100" :min="0" :max="20000" show-input />
              <p class="form-field-hint">按 {{ travelForm.people }} 人同行估算，可在生成后查看费用构成。</p>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="generateItinerary" :loading="isGenerating" class="generate-btn">
                <el-icon><MagicStick /></el-icon>
                {{ isGenerating ? '正在规划旅程...' : '生成我的行程' }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>

      <!-- 中间行程展示区 -->
      <div class="itinerary-panel">
        <el-card class="itinerary-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-header__number">{{ itinerary.length ? '03' : '02' }}</span>
              <span>{{ itinerary.length ? '按天探索行程' : '您的专属行程' }}</span>
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
            <div class="empty-state__route" aria-hidden="true">
              <span class="route-stamp">出发<br>啦</span>
              <span class="route-line"></span>
              <span class="route-point route-point--start"></span>
              <span class="route-point route-point--end"></span>
              <span class="route-label route-label--start">出发</span>
              <span class="route-label route-label--end">抵达心动的地方</span>
            </div>
            <span class="empty-state__eyebrow">下一站，由你决定</span>
            <h2>先选一个方向，再慢慢丰富旅程</h2>
            <p>填写左侧需求，生成包含每日路线、景点与预算的旅行提案。也可以从一份灵感开始。</p>
            <div class="inspiration-list" aria-label="旅行灵感示例">
              <button v-for="idea in tripIdeas" :key="idea.destination" type="button" class="inspiration-card" @click="applyTripIdea(idea)">
                <span class="inspiration-card__meta">{{ idea.kind }} / {{ idea.days }} DAYS</span>
                <strong>{{ idea.destination }}</strong>
                <small>{{ idea.caption }}</small>
                <el-icon><ArrowRight /></el-icon>
              </button>
            </div>
          </div>
          
          <div v-else-if="isGenerating" class="loading-state">
            <div class="loading-state__eyebrow"><span class="loading-state__pulse"></span> 正在规划 {{ planDestination }} 的旅程</div>
            <h2>正在串联你的旅行灵感</h2>
            <p aria-live="polite">{{ planningProgress }}</p>
            <ol class="planning-steps" aria-label="规划进度">
              <li v-for="(step, index) in planningSteps" :key="step" :class="{ 'is-active': planningStage === index, 'is-complete': planningStage > index }">
                <span>{{ String(index + 1).padStart(2, '0') }}</span>{{ step }}
              </li>
            </ol>
            <div class="loading-preview" aria-hidden="true">
              <div class="loading-preview__day"><span>DAY 01</span><i></i></div>
              <div class="loading-preview__stop"><b></b><span><i></i><i></i></span></div>
              <div class="loading-preview__stop"><b></b><span><i></i><i></i></span></div>
              <div class="loading-preview__stop"><b></b><span><i></i><i></i></span></div>
            </div>
            <small class="loading-state__note">完成后会自动显示每日路线和费用概览</small>
          </div>
          
          <el-timeline v-else>
            <el-timeline-item v-for="(day, index) in itinerary" :id="`itinerary-day-${index}`" :key="index" :timestamp="`DAY ${day.day}`" placement="top" :hollow="true" class="custom-timeline-item">
              <div class="day-weather">
                <img :src="`/weather/${day.weather}.png`" :alt="day.weather" />
                <span>{{ day.temperature }} · {{ day.weather }}</span>
              </div>
              <div class="day-card" shadow="hover">
                <div v-for="(item, itemIndex) in day.activities" :key="item.id" @click="focusOnLocation(item)">
                  <div class="activity-item">
                    <img :src="item.image" :alt="item.name" />
                    <div class="activity-content">
                      <div class="activity-header">
                        <h3><span class="activity-time">{{ item.time }}</span>{{ item.name }}</h3>
                        <el-button text @click.stop="navigateToDestination(item)" class="nav-btn">
                          <el-icon><Location /></el-icon>导航
                        </el-button>
                      </div>
                      <p class="activity-desc">{{ item.description }}</p>
                      <div class="activity-tags">
                        <el-tag v-for="(tag,i) in item.tags" :key="tag" :type="types[i%types.length]" size="small">{{ tag }}</el-tag>
                      </div>
                      <div class="activity-footer">
                        <p class="activity-tip" v-if="item.tip"><el-icon><Document /></el-icon>{{ item.tip }}</p>
                        <el-button class="activity-price" type="primary" size="small" @click.stop="router.push(`/attraction/detail/${item.id}`)">¥{{ item.price }} · 查看景点</el-button>
                      </div>
                    </div>
                  </div>
                  <div class="distance-car-time" v-if="itemIndex < day.activities.length - 1">
                    <span class="distance-car-time__line"></span>
                    <el-icon aria-label="距离"><Location /></el-icon>
                    <span>{{ item.distance }}</span>
                    <el-icon aria-label="车程"><Van /></el-icon>
                    <span>{{ item.drivingTime }}</span>
                  </div>
                </div>
              </div>
              <div class="hotel-recommendations">
                <h3>为您推荐的酒店</h3>
                <div class="hotel-cards-container">
                  <!-- 酒店卡片 -->
                  <div class="hotel-card" v-for="hotel in day.recommendHotels" :key="hotel.id">
                    <img :src="`${hotel.image}`" :alt="hotel.name" class="hotel-image">
                    <div class="hotel-info">
                      <h4>{{ hotel.name }}</h4>
                      <div class="rating"><el-icon><StarFilled /></el-icon>{{ hotel.rating }}</div>
                      <p class="location"><el-icon><Location /></el-icon>距离市中心{{hotel.downtownDistance}}</p>
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

      <!-- 地图和预算概览 -->
       <div class="map-panel" v-show="itinerary.length > 0 && !isGenerating">
        <div class="result-banner">
          <div class="result-banner__main">
            <span class="result-banner__eyebrow">YOUR ITINERARY</span>
            <h2>{{ planDestination }} · {{ itinerary.length }} 天的旅行提案</h2>
            <p>{{ planPeople }} 人同行 <span class="result-banner__divider"></span> {{ mapActivityCount }} 处行程站点 <span class="result-banner__divider"></span> {{ planBudget.toLocaleString() }} 元预算</p>
            <div class="result-banner__days" aria-label="跳转到每日行程">
              <button v-for="(day, index) in itinerary" :key="day.day ?? index" type="button" @click="scrollToDay(index)">第 {{ day.day }} 天 <el-icon><ArrowRight /></el-icon></button>
            </div>
          </div>
          <button type="button" class="result-banner__chat" :disabled="isGenerating" @click="continueWithAdvisor">
            <el-icon><ChatDotRound /></el-icon>
            和旅行顾问聊聊这份行程
            <el-icon><ArrowRight /></el-icon>
          </button>
        </div>
        <el-card class="map-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-header__number">02</span>
              <span>路线概览</span>
            </div>
          </template>
          <div id="amap-container"></div>
          <div class="map-stops">
            <div class="map-stops__heading">
              <div>
                <h3>途经景点 <span>{{ mapActivityCount }} 站</span></h3>
                <p>按游览顺序排列，点击站点查看位置和介绍</p>
              </div>
            </div>
            <div class="map-stops__scroll">
              <section v-for="(day, dayIdx) in itinerary" :key="day.day ?? dayIdx" class="map-stops__group">
                <div class="map-stops__day">
                  <span>DAY {{ day.day }}</span>
                  <small>{{ day.activities?.length || 0 }} 个景点</small>
                </div>
                <div class="map-stops__grid">
                  <button
                    v-for="(activity, actIdx) in day.activities"
                    :key="activity.id ?? actIdx"
                    type="button"
                    class="map-stop"
                    :class="{ 'map-stop--active': activeActivity === activity }"
                    :disabled="!activity.position"
                    :aria-pressed="activeActivity === activity"
                    :title="activity.position ? `查看${activity.name}的位置和介绍` : '该景点暂无定位信息'"
                    @click="focusOnLocation(activity)"
                  >
                    <span class="map-stop__order">{{ String(actIdx + 1).padStart(2, '0') }}</span>
                    <span class="map-stop__content">
                      <strong>{{ activity.name }}</strong>
                      <small>{{ activity.position ? (activity.time || '景点游览') : '暂无定位' }}</small>
                    </span>
                    <el-icon class="map-stop__icon"><Location /></el-icon>
                  </button>
                </div>
              </section>
            </div>
          </div>
        </el-card>

        <!-- 预算摘要卡片 -->
        <el-card v-if="budgetSummary.total > 0" class="budget-summary-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-header__number">¥</span>
              <span>费用概览</span>
            </div>
          </template>
          <div class="budget-overview">
            <div class="total-budget">
              <span class="label">预计总花费 · {{ planPeople }} 人</span>
              <span class="amount">¥ {{ budgetSummary.total.toLocaleString() }}</span>
            </div>
            <div class="budget-comparison">
              <span :class="{'over-budget': budgetSummary.total > planBudget}">
                {{ budgetSummary.total > planBudget ? '超出预算' : '预算结余' }}
                ¥ {{ Math.abs(budgetSummary.total - planBudget).toLocaleString() }}
              </span>
            </div>
          </div>
          <div class="budget-breakdown">
            <div v-for="(item) in budgetSummary.breakdown" :key="item.category" class="breakdown-item">
              <div class="item-header">
                <span class="item-label">{{item.category ||'其他费用' }}</span>
                <span class="item-amount">¥ {{ item.amount.toLocaleString() }}</span>
              </div>
              <el-progress :percentage="item.percentage*100" :color="getProgressColor(item.category)" :show-text="false" />
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
            
            <!-- 当前问答所引用的行程快照 -->
            <div class="context-card" v-if="currentHistoryId && itinerary.length">
              <div class="context-card__top">
                <span class="context-card__eyebrow"><el-icon><Link /></el-icon> 已引用行程</span>
                <button type="button" class="context-card__remove" title="取消引用行程" aria-label="取消引用行程" @click="currentHistoryId = null">
                  <el-icon><Close /></el-icon>
                </button>
              </div>
              <div class="context-card__title">
                <strong :title="planDestination">{{ planDestination }}</strong>
                <span>{{ itinerary.length }} 天</span>
              </div>
              <p class="context-card__hint">顾问会参考这份路线回答你的问题</p>
              <div class="context-card__facts">
                <div><small>同行人数</small><strong>{{ planPeople }} 人</strong></div>
                <div><small>同行预算</small><strong>¥{{ Number(planBudget).toLocaleString() }}</strong></div>
              </div>
              <button type="button" class="context-card__view" @click="openReferencedItinerary">
                查看完整行程 <el-icon><ArrowRight /></el-icon>
              </button>
            </div>
          </div>

        </div>

        <!-- 右侧对话主窗口 -->
        <div class="chat-main">
          <div class="chat-header-bar">
             <div class="model-switcher">
                <span class="label">旅行顾问</span>
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
             <button v-if="currentHistoryId && itinerary.length" type="button" class="plan-reference-tag" @click="openReferencedItinerary" :title="`查看已引用的${planDestination}行程`">
               <span class="plan-reference-tag__dot"></span>
               正参考 {{ planDestination }} · {{ itinerary.length }} 天行程
               <el-icon><ArrowRight /></el-icon>
             </button>
          </div>
          <div class="chat-body">
            <div v-if="chatMessages.length === 1 && !currentSessionId" class="chat-welcome">
              <span class="chat-welcome__eyebrow">ASK YOUR TRAVEL ADVISOR</span>
              <h2>你的旅途，随时可以接着聊</h2>
              <p>从景点、美食到住宿安排，挑一个问题开始，或在下方输入你的想法。</p>
              <div class="quick-actions">
                <button
                  v-for="(item, index) in quickQuestions"
                  :key="index"
                  type="button"
                  class="action-card"
                  :disabled="isGenerating"
                  @click="sendMessage(item.text)"
                >
                  <span class="action-icon">
                    <component :is="item.icon === 'Promotion' ? Promotion : item.icon === 'Ticket' ? Ticket : item.icon === 'Food' ? Food :item.icon === 'MapLocation' ? MapLocation : item.icon === 'House' ? House : Picture " />
                  </span>
                  <span>{{ item.text }}</span>
                </button>
              </div>
            </div>
            <div v-for="(msg, index) in chatMessages" v-show="!(chatMessages.length === 1 && !currentSessionId && msg.role === 'ai')" :key="msg.id" class="message-row" :class="msg.role">
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
import { ref, shallowRef, computed, onMounted, onUnmounted, nextTick, watch } from 'vue';
import { ElMessage, ElNotification } from 'element-plus';
import * as echarts from 'echarts';
import { loadAMap } from '@/utils/amap-loader';
import { Clock, Delete, ChatDotRound, MapLocation, Promotion, Food, Ticket, Lock, Plus, Download, Link, Picture, House, Loading, MagicStick, Location, ArrowRight, Document, Van, StarFilled, Close } from '@element-plus/icons-vue'
import router from '@/router'
import request from '@/utils/request';
import { getTripPlanService,getPlanHistoryService,getHistoricalItineraryService, sendChatStream, getChatHistoryService, getChatMessagesService, deleteConversationService, uploadChatImageService } from '@/api/agent';
import { useUserInfoStore } from '@/stores/userInfo';
import { config } from '@/utils/config';
import { useTokenStore } from '@/stores/token';
import { authSse } from '@/utils/authSse';
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

const tripIdeas = [
  { destination: '张家界', days: 3, budget: 5000, preferences: ['自然风光'], kind: '山野', caption: '在山间留一点慢下来的时间' },
  { destination: '杭州', days: 2, budget: 3500, preferences: ['历史文化', '美食探索'], kind: '城市', caption: '湖边散步，顺路尝尝当地味道' },
  { destination: '大理', days: 4, budget: 7000, preferences: ['自然风光', '休闲度假'], kind: '度假', caption: '把日程留给洱海和午后阳光' }
];

const applyTripIdea = (idea) => {
  travelForm.value = {
    ...travelForm.value,
    destination: idea.destination,
    days: idea.days,
    budget: idea.budget,
    preferences: [...idea.preferences]
  };
  document.querySelector('.input-card')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
};

const isGenerating = ref(false);
const planningProgress = ref('正在准备你的行程，请稍候。');
const planningSteps = ['收集旅行线索', '编排每日路线', '核对花费预算'];
const planningStage = ref(0);
const itinerary = ref([]);
const mapActivityCount = computed(() => itinerary.value.reduce((count, day) => count + (day.activities?.length || 0), 0));
const activeActivity = ref(null);
const planDestination = ref('张家界');
const planBudget = ref(5000);
const planPeople = ref(2);
const currentHistoryId = ref(null);
const scrollToDay = (index) => {
  document.getElementById(`itinerary-day-${index}`)?.scrollIntoView({ behavior: 'smooth', block: 'start' });
};
const openReferencedItinerary = () => {
  currentMode.value = 'planning';
  nextTick(() => document.querySelector('.result-banner')?.scrollIntoView({ behavior: 'smooth', block: 'start' }));
};
const continueWithAdvisor = () => {
  startNewChat();
  const routeSummary = itinerary.value.slice(0, 3)
    .map(day => `第${day.day}天：${(day.activities || []).map(activity => activity.name).join('、')}`)
    .join('；');
  chatInput.value = currentHistoryId.value
    ? `关于我的${planDestination.value}${itinerary.value.length}天行程，我想调整一下安排：`
    : `请参考我的${planDestination.value}${itinerary.value.length}天行程（${routeSummary}），我想调整一下安排：`;
  currentMode.value = 'chat';
  nextTick(() => document.querySelector('.chat-input input.el-input__inner')?.focus());
};
const map = shallowRef(null); // 保留高德地图原生实例，避免被 Vue 代理
const markers = shallowRef([]); // 保留原生 Marker 对象供地图使用
const markerDetails = new WeakMap(); // 行程景点与地图标记、说明窗的对应关系
let activeInfoWindow = null;
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

    await nextTick();
    renderBudgetChart();
    if (!map.value) await initMap();
    else {
      map.value.resize();
      addMarkersToMap();
    }

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
  showHistory.value = false;
  currentMode.value = 'planning';
  // 回填表单
  travelForm.value = {
    destination: item.destination,
    startDate: new Date(item.startDate), // 确保日期格式正确
    days: item.dayNum,
    people: item.people,
    preferences: item.preferences ? [...item.preferences] : [],
    budget: item.budget
  };
  planBudget.value = item.budget;
  planPeople.value = item.people;
  planDestination.value = item.destination;
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
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return date < today;
}

// 2. 修正进度条颜色映射（按后端中文分类匹配颜色）
const getProgressColor = (category) => {
  const colors = {
    '门票费用': '#D9A061',
    '住宿费用': '#4B9485',
    '餐饮费用': '#D17D6D',
    '交通费用': '#6EA4BD',
    '其他费用': '#9DAEB5'
  };
  return colors[category] || '#397C93';
};

// 渲染 ECharts 饼图
const renderBudgetChart = () => {
  if (!budgetChart.value || !budgetSummary.value.breakdown.length) return;

  const chart = echarts.getInstanceByDom(budgetChart.value) || echarts.init(budgetChart.value);
  
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
        radius: ['46%', '68%'],
        label: { show: false },
        itemStyle: { borderColor: '#fff', borderWidth: 2 },
        data: pieData,
        emphasis: {
          itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' }
        }
      }
    ]
  };
  
  chart.setOption(option);
};

const resizeBudgetChart = () => {
  if (budgetChart.value) echarts.getInstanceByDom(budgetChart.value)?.resize();
};


const generateItinerary = async () => { 
  if (!travelForm.value.destination?.trim()) {
    ElMessage.warning('请先填写目的地');
    return;
  }
  if (!travelForm.value.startDate) {
    ElMessage.warning('请选择出发日期');
    return;
  }
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
  planningProgress.value = '正在准备你的行程，请稍候。';
  planningStage.value = 0;
  planBudget.value = travelForm.value.budget;
  planPeople.value = travelForm.value.people;
  planDestination.value = travelForm.value.destination.trim();
  const userPlan = {
    destination: travelForm.value.destination,
    startDate: travelForm.value.startDate,
    dayNum: travelForm.value.days,
    people: travelForm.value.people,
    preferences: travelForm.value.preferences,
    budget: travelForm.value.budget
  };
  try {
    const result = await getTripPlanService(userPlan);
    const taskId = result.data;
    const eventSource = authSse(`/api/agent/progress/${taskId}`, {
      onMessage: async (message, event) => {
        if (event === 'progress') {
          const data = JSON.parse(message);
          planningProgress.value = data.message || '正在安排每日路线、景点和预算。';
          if (data.node === 'PlanItineraryNode' || data.node === 'RecommendHotelNode') planningStage.value = Math.max(planningStage.value, 1);
          if (data.node === 'CalculateBudgetNode') planningStage.value = 2;
          return;
        }
        if (event === 'failed') {
          eventSource.close();
          isGenerating.value = false;
          ElMessage.error('行程规划失败，请稍后重试！');
          return;
        }
        if (event !== 'complete') return;

        eventSource.close();
        try {
          const data = JSON.parse(message);
          currentHistoryId.value = null;
          itinerary.value = data.dailySchedules;
          budgetSummary.value = data.budgetSummary;
          itinerary.value.forEach(day => {
            day.activities.forEach(activity => {
              activity.tags = typeof activity.tags === 'string' && activity.tags
                ? activity.tags.split(',')
                : [];
            });
          });

          isGenerating.value = false;
          await nextTick();
          renderBudgetChart();
          if (!map.value) await initMap();
          else {
            map.value.resize();
            addMarkersToMap();
          }
          ElMessage.success('行程规划成功！');
          await loadHistory();
          currentHistoryId.value = historyList.value[0]?.id;
        } catch (error) {
          console.error('处理行程规划结果失败', error);
          isGenerating.value = false;
          ElMessage.error('行程结果加载失败，请稍后重试');
        }
      },
      onError: () => {
        isGenerating.value = false;
        ElMessage.error('服务器繁忙，请稍后重试！');
      }
    });
  } catch (error) {
    console.error('启动行程规划失败', error);
    isGenerating.value = false;
    ElMessage.error('行程规划启动失败，请稍后重试');
  }
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
  activeInfoWindow?.close();
  activeInfoWindow = null;
  activeActivity.value = null;
  // 清除旧的标记
  if (markers.value.length > 0) {
    map.value.remove(markers.value);
    markers.value = [];
  }

  // 直接从后端itinerary中提取景点位置信息（ID、名称、经纬度）
  itinerary.value.forEach(day => {
    day.activities.forEach(activity => {
      if (activity.position && activity.name) {
        const markerContent = document.createElement('div');
        markerContent.className = 'trip-map-marker';
        const labelContent = document.createElement('span');
        labelContent.className = 'trip-map-label';
        labelContent.textContent = activity.name;

        const marker = new AMap.Marker({
          position: activity.position,
          title: activity.name,
          content: markerContent,
          anchor: 'bottom-center',
          label: {
            content: labelContent.outerHTML,
            direction: 'bottom',
            offset: [0, 4]
          }
        });
        
        marker.setMap(map.value);
        markers.value.push(marker);

        const infoContent = document.createElement('div');
        infoContent.className = 'info-window-content';
        const infoTitle = document.createElement('h3');
        infoTitle.textContent = activity.name;
        infoContent.appendChild(infoTitle);
        if (activity.description) {
          const infoDescription = document.createElement('p');
          infoDescription.textContent = activity.description;
          infoContent.appendChild(infoDescription);
        }
        const infoWindow = new AMap.InfoWindow({
          content: infoContent,
          anchor: 'bottom-center',
          autoMove: true
        });
        markerDetails.set(activity, { marker, infoWindow });
        marker.on('click', () => {
          activeActivity.value = activity;
          activeInfoWindow = infoWindow;
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
    activeActivity.value = activity;
    map.value.setZoomAndCenter(14, activity.position);
    const detail = markerDetails.get(activity);
    if (detail) {
      activeInfoWindow = detail.infoWindow;
      detail.infoWindow.open(map.value, detail.marker.getPosition());
    }
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
  window.addEventListener('resize', resizeBudgetChart);
  await loadHistory();
  await loadChatHistory();
});
onUnmounted(() => window.removeEventListener('resize', resizeBudgetChart));
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
  position: relative;
  overflow: hidden;
  padding: 15px;
  border: 1px solid #d4e8e4;
  border-radius: 12px;
  background: radial-gradient(circle at 100% 0%, rgba(178, 219, 209, .32), transparent 45%), #f3f9f7;
  color: #173e4c;
}
.context-card::before { content: ''; position: absolute; top: 0; left: 15px; right: 15px; height: 2px; border-radius: 0 0 2px 2px; background: #64a99a; }
.context-card__top { display: flex; align-items: center; justify-content: space-between; gap: 8px; margin-bottom: 12px; }
.context-card__eyebrow { display: inline-flex; align-items: center; gap: 5px; color: #36796f; font-size: 11px; font-weight: 700; letter-spacing: .03em; }
.context-card__eyebrow .el-icon { font-size: 13px; }
.context-card__remove { display: grid; place-items: center; flex: 0 0 26px; width: 26px; height: 26px; padding: 0; border: 0; border-radius: 50%; background: transparent; color: #7e9b97; font-size: 14px; cursor: pointer; }
.context-card__remove:hover { background: #e1efea; color: #255f59; }
.context-card__title { display: flex; align-items: baseline; gap: 7px; min-width: 0; }
.context-card__title strong { overflow: hidden; color: #143b49; font-size: 19px; font-weight: 700; line-height: 1.3; text-overflow: ellipsis; white-space: nowrap; }
.context-card__title span { flex: 0 0 auto; color: #4c817d; font-size: 12px; font-weight: 600; }
.context-card__hint { margin: 5px 0 12px; color: #718e8c; font-size: 11px; line-height: 1.5; }
.context-card__facts { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; padding: 11px 0 12px; border-top: 1px solid #dcebe7; border-bottom: 1px solid #dcebe7; }
.context-card__facts div { display: flex; flex-direction: column; gap: 3px; min-width: 0; }
.context-card__facts small { color: #839995; font-size: 10px; }
.context-card__facts strong { overflow: hidden; color: #214e55; font: 700 13px var(--font-num); text-overflow: ellipsis; white-space: nowrap; }
.context-card__view { display: flex; align-items: center; justify-content: space-between; width: 100%; margin-top: 10px; padding: 2px 0; border: 0; background: transparent; color: #287667; font: inherit; font-size: 11px; font-weight: 700; text-align: left; cursor: pointer; }
.context-card__view:hover { color: #16594e; }
.context-card__remove:focus-visible, .context-card__view:focus-visible, .plan-reference-tag:focus-visible { outline: 2px solid #2d877c; outline-offset: 2px; }

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

.message-avatar {
  /* 头像尺寸不能随消息文本长度变化。
     .message-row 是 flex 容器，而 flex 项默认 flex-shrink: 1 —— 消息文本越长，
     需要收缩的空间越多，头像按 flex-basis 比例分到的收缩量也越大，宽度被压到 40px 以下；
     而下方 img 的 height 固定 40px、width 又被 base.css 的 `img { max-width: 100% }` 卡住，
     于是宽度跟着容器缩、高度不变 → 渲染成竖长椭圆。
     实测（1440px 视口）：3 字时 40×40 正常，50 字时 28.6×40，1024px 视口下 50 字只剩 11.6×40。 */
  flex-shrink: 0;
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

/* Desktop travel workspace */
.travel-planner {
  background: radial-gradient(circle at 88% 18%, #e9f4f5 0, transparent 30%), var(--c-travel-warm-bg);
  font-family: var(--font-sans);
  padding-bottom: 48px;
}

.planner-header {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: end;
  gap: 40px;
  padding: 42px max(32px, calc((100vw - 1500px) / 2)) 38px;
  text-align: left;
  background: radial-gradient(circle at 82% -30%, rgba(247, 200, 117, .26), transparent 42%),
    linear-gradient(118deg, var(--c-travel-blue-deep) 0%, #246b91 64%, var(--c-travel-blue) 100%);
  overflow: hidden;
}

.planner-header::after {
  content: '';
  position: absolute;
  width: 440px;
  height: 440px;
  right: 9%;
  bottom: -360px;
  border: 1px solid rgba(255, 255, 255, .14);
  border-radius: 50%;
  pointer-events: none;
}

.header-content, .mode-switcher, .history-btn { z-index: 1; }
.header-eyebrow, .result-banner__eyebrow, .chat-welcome__eyebrow {
  display: block;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: .18em;
}
.header-eyebrow { color: #9bd3e8; margin-bottom: 14px; }
.header-content .main-title {
  max-width: 780px;
  margin: 0 0 12px;
  font-size: clamp(30px, 3.1vw, 42px);
  font-weight: 600;
  line-height: 1.22;
  letter-spacing: -.025em;
  text-shadow: none;
}
.subtitle { margin: 0; color: #d5e7f0; opacity: 1; font-size: 15px; }
.mode-switcher {
  margin: 0;
  padding: 5px;
  gap: 4px;
  background: rgba(255, 255, 255, .12);
  border: 1px solid rgba(255, 255, 255, .18);
  border-radius: var(--r-full);
}
.mode-item {
  border: 0;
  background: transparent;
  border-radius: var(--r-full);
  padding: 10px 17px;
  font-size: 14px;
  color: #e7f4fa;
  white-space: nowrap;
}
.mode-item:hover { background: rgba(255, 255, 255, .14); }
.mode-item.active, .mode-item.active:hover { background: #fff; color: #173b55; box-shadow: var(--sh-1); }
.history-btn {
  top: 31px;
  right: max(32px, calc((100vw - 1500px) / 2));
  transform: none;
  width: auto;
  height: 34px;
  padding: 0 12px;
  gap: 7px;
  border-radius: var(--r-full);
  font-size: 13px;
}

.planner-container {
  max-width: 1500px;
  grid-template-columns: 310px minmax(0, 1fr);
  grid-template-areas: 'input itinerary';
  gap: 22px;
  padding: 28px 32px 0;
  align-items: start;
}
.planner-container--has-result { grid-template-areas: 'input map' 'input itinerary'; }
.input-panel { grid-area: input; position: sticky; top: 86px; min-width: 0; }
.itinerary-panel { grid-area: itinerary; min-width: 0; }
.map-panel {
  grid-area: map;
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(320px, .85fr);
  gap: 18px;
  min-width: 0;
}
.input-card, .itinerary-card, .map-card, .budget-summary-card {
  min-height: 0;
  border-radius: var(--r-lg);
  border: 1px solid #e5ebea;
  background: #fff;
  box-shadow: 0 10px 28px -24px rgba(23, 57, 83, .32);
  backdrop-filter: none;
}
:deep(.input-card .el-card__header),
:deep(.itinerary-card .el-card__header),
:deep(.map-card .el-card__header),
:deep(.budget-summary-card .el-card__header) {
  border-bottom: 1px solid #edf1f5;
  padding: 18px 22px;
}
:deep(.input-card .el-card__body),
:deep(.itinerary-card .el-card__body),
:deep(.map-card .el-card__body),
:deep(.budget-summary-card .el-card__body) { padding: 22px; }
.card-header { align-items: center; gap: 10px; font-size: 16px; font-weight: 600; color: #17344c; }
.card-header__number {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border-radius: 9px;
  background: #e8f3f8;
  color: #266383;
  font-size: 12px;
  font-family: var(--font-num);
  font-weight: 700;
}
:deep(.input-card .el-form-item) { margin-bottom: 19px; }
:deep(.input-card .el-form-item__label) { color: #405568; font-weight: 600; padding-bottom: 5px; }
:deep(.input-card .el-checkbox-group) { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 7px; width: 100%; }
:deep(.input-card .el-checkbox) {
  margin: 0;
  padding: 3px 8px;
  border: 1px solid #e6edf2;
  border-radius: 8px;
  background: #f8fafc;
  min-width: 0;
}
:deep(.input-card .el-checkbox.is-checked) { background: #edf6fa; border-color: #b8dce9; }
:deep(.input-card .el-checkbox__label) { font-size: 12px; padding-left: 5px; }
.generate-btn {
  height: 44px;
  margin-top: 6px;
  background: #1d6389;
  box-shadow: 0 9px 18px -11px #1d6389;
  font-weight: 600;
}
.generate-btn:hover { background: #174f70; }

.result-banner {
  grid-column: 1 / -1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 23px 28px;
  border-radius: var(--r-lg);
  background: #e8f3f8;
  border: 1px solid #d9ebf3;
  color: #173e57;
  overflow: hidden;
}
.result-banner__eyebrow { color: #47809a; margin-bottom: 8px; }
.result-banner h2 { margin: 0 0 6px; font-size: 23px; line-height: 1.35; }
.result-banner p { margin: 0; color: #536b7c; font-size: 13px; }
.result-banner__seal {
  display: grid;
  place-items: center;
  flex: 0 0 auto;
  width: 58px;
  height: 58px;
  border: 1px solid #9dc6d6;
  border-radius: 50%;
  color: #2f708d;
  font-weight: 700;
  line-height: 1.1;
  text-align: center;
  transform: rotate(12deg);
}
.map-card, .budget-summary-card { min-width: 0; }
#amap-container { min-height: 244px; height: 244px; border-radius: 10px; }
.map-stops { min-width: 0; margin-top: 17px; padding-top: 16px; border-top: 1px solid #e5ecf1; }
.map-stops__heading h3 { display: flex; align-items: baseline; gap: 9px; margin: 0; color: #18364d; font-size: 15px; }
.map-stops__heading h3 span { color: #2c7b98; font-family: var(--font-num); font-size: 12px; font-weight: 600; }
.map-stops__heading p { margin: 4px 0 15px; color: #738596; font-size: 12px; }
.map-stops__scroll { max-height: 290px; overflow-y: auto; padding-right: 5px; scrollbar-width: thin; scrollbar-color: #b7cbd6 transparent; }
.map-stops__group + .map-stops__group { margin-top: 16px; }
.map-stops__day { display: flex; align-items: center; gap: 9px; margin-bottom: 8px; }
.map-stops__day span { color: #28718f; font-family: var(--font-num); font-size: 11px; font-weight: 700; letter-spacing: .08em; }
.map-stops__day small { color: #8a9aa6; font-size: 11px; }
.map-stops__grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 8px; }
.map-stop {
  display: flex;
  align-items: center;
  gap: 9px;
  min-width: 0;
  min-height: 58px;
  padding: 8px 10px;
  border: 1px solid #e3edf2;
  border-radius: 10px;
  background: #f8fbfd;
  color: #19394e;
  cursor: pointer;
  text-align: left;
  transition: border-color .2s ease, background-color .2s ease, transform .2s ease;
}
.map-stop:hover:not(:disabled) { transform: translateY(-1px); border-color: #9fc9da; background: #edf6fa; }
.map-stop--active { border-color: #78b5ce; background: #e7f3f8; box-shadow: inset 3px 0 #2e819f; }
.map-stop:disabled { cursor: not-allowed; opacity: .55; }
.map-stop__order {
  display: grid;
  place-items: center;
  flex: 0 0 28px;
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: #e5f0f6;
  color: #347d9c;
  font-family: var(--font-num);
  font-size: 11px;
  font-weight: 700;
}
.map-stop--active .map-stop__order { background: #2e819f; color: #fff; }
.map-stop__content { display: flex; flex: 1; flex-direction: column; gap: 3px; min-width: 0; }
.map-stop__content strong { overflow: hidden; font-size: 13px; font-weight: 600; text-overflow: ellipsis; white-space: nowrap; }
.map-stop__content small { color: #7a8c99; font-size: 11px; }
.map-stop__icon { flex: 0 0 auto; color: #5493aa; font-size: 15px; }
.budget-overview { margin-bottom: 12px; padding-bottom: 12px; }
.total-budget .amount { font-family: var(--font-num); font-size: 29px; color: #173e57; }
.budget-comparison { font-size: 12px; color: #268268; }
.budget-breakdown { gap: 9px; }
.breakdown-item .item-header { margin-bottom: 3px; }
.budget-chart-container { height: 154px; margin-top: 7px; }
.empty-state, .loading-state { min-height: 520px; text-align: center; }
.empty-state__graphic {
  display: grid;
  place-items: center;
  width: 98px;
  height: 98px;
  margin-bottom: 28px;
  border-radius: 26px;
  background: linear-gradient(140deg, #e7f4f8, #f6f2e7);
  transform: rotate(-8deg);
}
.empty-state__icon { font-size: 48px; color: #3485a0; transform: rotate(8deg); }
.empty-state__eyebrow { color: #4b8ba4; font-size: 12px; font-weight: 700; letter-spacing: .15em; }
.empty-state h2, .loading-state h2 { color: #18364d; font-size: 24px; margin: 14px 0 8px; }
.empty-state p, .loading-state p { max-width: 420px; line-height: 1.7; margin: 0; }

:deep(.custom-timeline-item .el-timeline-item__timestamp) { color: #1a5f81; font-size: 19px; letter-spacing: .08em; }
:deep(.itinerary-card .el-timeline) { padding-left: 6px; }
.day-weather { display: flex; align-items: center; gap: 7px; margin: 0 0 12px; color: #63788a; font-size: 13px; }
.day-weather img { width: 30px; height: 30px; object-fit: contain; }
.day-card { gap: 0; }
.activity-item { padding: 17px; border: 1px solid #e7edf1; border-radius: 12px; background: #fff; }
.activity-item:hover { background: #f7fbfd; border-color: #b9d9e8; }
.activity-item img { width: 104px; height: 104px; border-radius: 8px; }
.activity-header { align-items: flex-start; }
.activity-header h3 { display: flex; align-items: baseline; gap: 10px; line-height: 1.4; }
.activity-time { color: #2b7a9b; font-family: var(--font-num); font-size: 14px; white-space: nowrap; }
.activity-desc { margin: 0 0 10px; line-height: 1.6; }
.activity-tags { margin-bottom: 10px; }
.activity-price { background: #e9f3f8; border-color: #e9f3f8; color: #1b6485; font-weight: 600; }
.activity-price:hover { background: #dcebf3; border-color: #dcebf3; color: #164b68; }
.distance-car-time { gap: 7px; margin: 0 0 0 22px; padding: 11px 0; color: #6b7d8b; font-size: 12px; }
.distance-car-time img { width: 17px; height: 17px; }
.distance-car-time__line { width: 1px; height: 20px; margin-right: 5px; background: #cbdce5; }
.hotel-recommendations { margin: 18px 0 28px; }
.hotel-recommendations h3 { color: #18364d; font-size: 15px; }
.hotel-cards-container { max-width: 100%; gap: 12px; background: transparent; }
.hotel-card { min-width: 215px; border: 1px solid #e7edf1; box-shadow: none; }
.hotel-card:hover { transform: translateY(-2px); box-shadow: var(--sh-2); }
.hotel-info { padding: 12px; }
.rating { color: #b97d13; }
.price { color: #b16c19; }
.select-btn { background: #1d6389; font-weight: 600; }
.select-btn:hover { background: #174f70; }

.chat-mode-container { max-width: 1500px; height: calc(100vh - 278px); min-height: 590px; margin: 0 auto; padding: 28px 32px 0; }
.chat-layout { gap: 20px; }
.chat-sidebar { width: 258px; }
.sidebar-history-panel, .chat-main { background: #fff; border: 1px solid #e4ebf1; border-radius: var(--r-lg); box-shadow: 0 10px 28px -22px rgba(23, 57, 83, .45); backdrop-filter: none; }
.new-chat-btn { background: #1d6389; }
.chat-header-bar { display: flex; justify-content: space-between; align-items: center; gap: 20px; padding: 16px 24px; border-bottom: 1px solid #e8eef2; }
.model-switcher { display: flex; align-items: center; gap: 16px; }
.model-switcher .label { color: #18364d; font-size: 15px; font-weight: 700; }
.chat-body { background: #fcfdfe; }
.chat-welcome { max-width: 790px; margin: 14px auto 22px; width: 100%; }
.chat-welcome__eyebrow { color: #3c8eaa; }
.chat-welcome h2 { color: #18364d; font-size: 27px; margin: 11px 0 7px; }
.chat-welcome p { color: #657b8b; margin: 0 0 24px; }
.quick-actions { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 10px; }
.action-card { min-height: 64px; text-align: left; border: 1px solid #e0eaf0; background: #fff; color: #2d485c; font: inherit; }
.action-card:hover { transform: translateY(-2px); background: #f2f8fb; box-shadow: var(--sh-1); }
.action-icon { flex: 0 0 30px; width: 30px; height: 30px; background: #e8f3f8; color: #237494; }
.chat-footer { padding: 18px 24px; }
.message-row.ai .message-bubble { box-shadow: none; }
.message-row.user .message-bubble { background: #1d6389; }

/* Compact planning masthead */
.planner-header {
  min-height: 126px;
  align-items: center;
  gap: 32px;
  padding-top: 20px;
  padding-bottom: 20px;
  background: radial-gradient(circle at 83% -65%, rgba(247, 200, 117, .31), transparent 42%),
    linear-gradient(112deg, var(--c-travel-blue-deep) 0%, #246b91 64%, var(--c-travel-blue) 100%);
}
.planner-header::after { width: 280px; height: 280px; bottom: -242px; right: 17%; }
.header-content { min-width: 0; }
.header-eyebrow { margin-bottom: 7px; font-size: 10px; letter-spacing: .19em; color: #ffe2a8; }
.header-content .main-title { margin-bottom: 5px; font-size: clamp(25px, 2vw, 32px); line-height: 1.25; }
.subtitle { font-size: 12px; color: #c2dae3; }
.header-actions { z-index: 1; display: flex; align-items: center; gap: 13px; justify-self: end; }
.mode-switcher { flex: 0 0 auto; padding: 4px; gap: 2px; margin: 0; }
.mode-item { min-height: 34px; padding: 6px 13px; gap: 6px; font-size: 12px; }
.history-btn {
  position: static;
  transform: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  flex: 0 0 auto;
  height: 35px;
  padding: 0 12px;
  border: 1px solid rgba(215, 239, 244, .34);
  border-radius: var(--r-full);
  background: rgba(255, 255, 255, .07);
  color: #e8f4f7;
  font: inherit;
  font-size: 12px;
  cursor: pointer;
  transition: background-color .2s ease, border-color .2s ease;
}
.history-btn:hover { background: rgba(255, 255, 255, .15); border-color: rgba(255, 255, 255, .58); }
.mode-item:focus-visible, .history-btn:focus-visible, .inspiration-card:focus-visible,
.result-banner__days button:focus-visible, .result-banner__chat:focus-visible {
  outline: 2px solid #e6ba78;
  outline-offset: 3px;
}
.planner-container { padding-top: 22px; }
.input-panel { top: 82px; }
.form-field-hint { margin: 4px 0 0; color: #8394a0; font-size: 11px; line-height: 1.5; }

/* A small route sketch gives the planning blank state a clear travel identity. */
.empty-state { min-height: 520px; padding: 28px 22px 35px; }
.empty-state__route {
  position: relative;
  width: min(100%, 650px);
  height: 150px;
  margin-bottom: 21px;
  overflow: hidden;
  border: 1px solid #dae9e9;
  border-radius: 15px;
  background: radial-gradient(circle at 16% 88%, rgba(247, 200, 117, .62), transparent 38%),
    repeating-radial-gradient(circle at 85% 80%, transparent 0 24px, rgba(72, 131, 143, .075) 25px 26px),
    #f4f8f4;
}
.route-stamp {
  position: absolute;
  top: 22px;
  right: 28px;
  display: grid;
  place-items: center;
  width: 66px;
  height: 66px;
  border: 1px solid rgba(31, 107, 128, .35);
  border-radius: 50%;
  color: #276b81;
  font-size: 16px;
  font-weight: 700;
  line-height: 1.1;
  transform: rotate(12deg);
}
.route-line {
  position: absolute;
  left: 17%;
  top: 62px;
  width: 52%;
  height: 47px;
  border-top: 2px dashed #6ca4aa;
  border-radius: 55% 55% 0 0;
  transform: rotate(-5deg);
}
.route-point { position: absolute; width: 13px; height: 13px; border: 3px solid #fff; border-radius: 50%; box-shadow: 0 0 0 2px #4b90a2; background: #4b90a2; }
.route-point--start { left: 16%; top: 71px; }
.route-point--end { left: 68%; top: 55px; background: #c48838; box-shadow: 0 0 0 2px #c48838, 0 0 0 7px rgba(247, 200, 117, .24); }
.route-label { position: absolute; color: #476979; font-size: 12px; font-weight: 600; }
.route-label--start { left: 13%; top: 95px; }
.route-label--end { left: 61%; top: 79px; }
.empty-state__eyebrow { color: #347a8d; font-size: 11px; }
.empty-state h2 { margin: 10px 0 6px; font-size: 23px; }
.empty-state p { max-width: 540px; font-size: 13px; }
.inspiration-list { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 10px; width: min(100%, 650px); margin-top: 24px; }
.inspiration-card {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-height: 112px;
  padding: 15px 15px 14px;
  border: 1px solid #e0e9e9;
  border-radius: 12px;
  background: #fff;
  color: #23475b;
  text-align: left;
  cursor: pointer;
  transition: border-color .2s ease, background-color .2s ease, transform .2s ease, box-shadow .2s ease;
}
.inspiration-card:hover { transform: translateY(-2px); border-color: #b8d9d6; background: #fffdf7; box-shadow: 0 10px 24px -17px rgba(28, 82, 98, .28); }
.inspiration-card__meta { color: #a47345; font-size: 10px; font-weight: 700; letter-spacing: .09em; }
.inspiration-card strong { font-size: 17px; line-height: 1.3; }
.inspiration-card small { padding-right: 12px; color: #7c8e98; font-size: 11px; line-height: 1.45; }
.inspiration-card .el-icon { position: absolute; right: 13px; top: 16px; color: #418c9e; font-size: 14px; }

.loading-state { min-height: 520px; padding: 34px 24px; }
.loading-state__eyebrow { display: inline-flex; align-items: center; gap: 8px; color: #287d8b; font-size: 12px; font-weight: 700; letter-spacing: .04em; }
.loading-state__pulse { width: 8px; height: 8px; border-radius: 50%; background: #4a9ca5; box-shadow: 0 0 0 5px rgba(74, 156, 165, .13); }
.loading-state h2 { margin-top: 15px; }
.loading-state p { max-width: 540px; font-size: 13px; }
.planning-steps { display: flex; justify-content: center; flex-wrap: wrap; gap: 8px; width: min(100%, 510px); margin: 24px auto 0; padding: 0; list-style: none; }
.planning-steps li { display: inline-flex; align-items: center; gap: 6px; padding: 6px 9px; border: 1px solid #e2eaea; border-radius: var(--r-full); color: #95a4a9; background: #f9fbfa; font-size: 11px; }
.planning-steps li span { font: 700 10px var(--font-num); }
.planning-steps li.is-active { border-color: #a9cdd0; background: #eaf5f4; color: #226f80; font-weight: 700; }
.planning-steps li.is-complete { border-color: #c8e3dc; color: #4a8c7d; }
.loading-preview { width: min(100%, 510px); margin: 28px auto 12px; padding: 18px 22px; border: 1px solid #e3ecec; border-radius: 13px; background: #fbfdfc; text-align: left; }
.loading-preview__day { display: flex; align-items: center; gap: 12px; margin-bottom: 17px; color: #4a8291; font: 700 12px var(--font-num); letter-spacing: .08em; }
.loading-preview__day i, .loading-preview__stop i { display: block; height: 10px; margin-bottom: 0; font-size: inherit; border-radius: 6px; background: linear-gradient(90deg, #e8f0ef 15%, #f6f9f8 45%, #e8f0ef 75%); background-size: 200% 100%; animation: guide-shimmer 1.7s linear infinite; }
.loading-preview__day i { width: 95px; }
.loading-preview__stop { display: flex; align-items: center; gap: 15px; margin: 0 0 14px; }
.loading-preview__stop:last-child { margin-bottom: 0; }
.loading-preview__stop b { width: 42px; height: 42px; flex: 0 0 42px; border-radius: 8px; background: #e8f0ef; }
.loading-preview__stop span { display: flex; flex-direction: column; gap: 8px; width: 62%; }
.loading-preview__stop i:first-child { width: 78%; }
.loading-preview__stop i:last-child { width: 52%; height: 8px; }
.loading-state__note { color: #8697a0; font-size: 11px; }
@keyframes guide-shimmer { to { background-position-x: -200%; } }

.result-banner { align-items: center; gap: 20px; padding: 19px 24px; background: linear-gradient(110deg, #eaf5f4, #fff8eb); border-color: #e2e9df; scroll-margin-top: 84px; }
.result-banner__main { min-width: 0; }
.result-banner__eyebrow { margin-bottom: 5px; color: #44889a; font-size: 10px; }
.result-banner h2 { margin-bottom: 4px; font-size: 21px; }
.result-banner p { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; font-size: 12px; }
.result-banner__divider { width: 3px; height: 3px; border-radius: 50%; background: #91afb6; }
.result-banner__days { display: flex; flex-wrap: wrap; gap: 7px; margin-top: 12px; }
.result-banner__days button { display: inline-flex; align-items: center; gap: 3px; min-height: 26px; padding: 3px 9px; border: 1px solid #cbdfe2; border-radius: var(--r-full); background: rgba(255, 255, 255, .72); color: #276a7c; font: inherit; font-size: 11px; cursor: pointer; }
.result-banner__days button:hover { border-color: #80aeb8; background: #fff; }
.result-banner__chat { display: inline-flex; align-items: center; justify-content: center; gap: 8px; flex: 0 0 auto; min-height: 39px; padding: 0 14px; border: 1px solid #1e687e; border-radius: 9px; background: #1e687e; color: #fff; font: inherit; font-size: 12px; font-weight: 600; cursor: pointer; transition: transform .2s ease, background-color .2s ease; }
.result-banner__chat:hover { transform: translateY(-1px); background: #17566a; }
.custom-timeline-item { scroll-margin-top: 84px; }
.nav-btn { display: inline-flex; align-items: center; gap: 4px; }
.activity-tip, .rating, .hotel-info .location { display: inline-flex; align-items: center; gap: 5px; }
.activity-tip .el-icon, .hotel-info .location .el-icon { flex: 0 0 auto; color: #6793a1; }
.rating .el-icon { color: #c4914f; }
.distance-car-time .el-icon { color: #6c9db0; font-size: 15px; }
.chat-mode-container { height: calc(100vh - 212px); padding-top: 22px; }
.plan-reference-tag { display: inline-flex; align-items: center; gap: 7px; flex: 0 1 auto; min-width: 0; min-height: 29px; padding: 5px 9px; border: 1px solid #cde5df; border-radius: var(--r-full); background: #f1faf6; color: #34776b; font: inherit; font-size: 11px; font-weight: 600; white-space: nowrap; cursor: pointer; }
.plan-reference-tag:hover { border-color: #a5cec4; background: #e9f6f1; }
.plan-reference-tag__dot { flex: 0 0 6px; width: 6px; height: 6px; border-radius: 50%; background: #5da88d; }
.plan-reference-tag .el-icon { margin-left: 1px; font-size: 12px; }
@media (prefers-reduced-motion: reduce) {
  .loading-preview__day i, .loading-preview__stop i { animation: none; }
  .inspiration-card, .result-banner__chat { transition: none; }
}

@media (max-width: 1320px) {
  .planner-container--has-result .map-panel { grid-template-columns: minmax(0, 1fr) minmax(290px, .9fr); }
  .planner-container { grid-template-columns: 288px minmax(0, 1fr); }
}
@media (max-width: 1180px) {
  .planner-container--has-result .map-panel { grid-template-columns: 1fr; }
}

@media (max-width: 960px) {
  .planner-container,
  .planner-container--has-result {
    grid-template-columns: minmax(0, 1fr);
    grid-template-areas: 'input' 'itinerary';
  }
  .planner-container--has-result { grid-template-areas: 'input' 'map' 'itinerary'; }
  .input-panel { position: static; }
  .map-panel { grid-template-columns: minmax(0, 1fr); }
  .chat-mode-container { height: auto; min-height: 0; }
  .chat-layout { flex-direction: column; }
  .chat-sidebar { width: 100%; }
  .chat-main { min-height: 590px; }
}

@media (max-width: 760px) {
  .planner-header {
    grid-template-columns: minmax(0, 1fr);
    gap: 20px;
    padding: 24px 20px;
  }
  .header-actions { justify-self: start; flex-wrap: wrap; }
  .planner-container, .chat-mode-container { padding: 18px 20px 0; }
}

@media (max-width: 600px) {
  .planner-header { gap: 18px; padding: 22px 16px; }
  .header-content .main-title { font-size: 25px; }
  .header-actions, .mode-switcher { width: 100%; }
  .mode-item { flex: 1; justify-content: center; }
  .planner-container, .chat-mode-container { padding: 16px 16px 0; }
  .empty-state { min-height: 0; padding: 26px 12px 34px; }
  .empty-state__route { height: 140px; }
  .inspiration-list { grid-template-columns: minmax(0, 1fr); }
  .inspiration-card { min-height: 84px; }
  .chat-header-bar, .model-switcher { flex-wrap: wrap; }
  .chat-header-bar { padding: 16px; }
  .quick-actions { grid-template-columns: minmax(0, 1fr); }
}
</style>

<style>
/* 全局样式，用于修改信息窗体 */
.travel-planner .trip-map-marker {
  width: 18px;
  height: 18px;
  border: 3px solid #fff;
  border-radius: 50%;
  background: #1d6389;
  box-shadow: 0 0 0 4px rgba(29, 99, 137, .18), 0 3px 9px rgba(18, 57, 83, .28);
}
.travel-planner .amap-marker-label {
  padding: 0;
  border: 0;
  background: transparent;
  box-shadow: none;
}
.travel-planner .trip-map-label {
  display: inline-block;
  max-width: 180px;
  padding: 5px 9px;
  overflow: hidden;
  border: 1px solid #d7e8ef;
  border-radius: 7px;
  background: rgba(255, 255, 255, .98);
  box-shadow: 0 3px 10px rgba(18, 57, 83, .13);
  color: #173e57;
  font-size: 12px;
  font-weight: 600;
  line-height: 1.3;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.info-window-content {
  max-width: 260px;
  padding: 4px 6px;
  color: #173e57;
}
.info-window-content h3 {
  margin: 0 0 7px;
  font-size: 15px;
}
.info-window-content p {
  margin: 0;
  color: #526b7c;
  font-size: 12px;
  line-height: 1.6;
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
