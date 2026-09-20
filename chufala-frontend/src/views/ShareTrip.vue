<template>
  <div class="share-page">
    <div class="share-card" id="share-card">
      <!-- 顶部装饰 -->
      <div class="card-decoration"></div>

      <!-- 品牌头部 -->
      <div class="brand-header">
        <div class="brand-left">
          <div class="logo-box">
            <img src="@/assets/logo2.png" class="brand-logo" alt="Logo" />
          </div>
          <div class="brand-text">
            <span class="app-name">出发啦</span>
            <span class="app-slogan">您的专属 AI 旅行规划师</span>
          </div>
        </div>
        <div class="brand-right">
          <div class="date-badge">
            <span class="date-label">生成日期</span>
            <span class="date-value">{{ currentDate }}</span>
          </div>
        </div>
      </div>

      <!-- 行程标题区 -->
      <div class="trip-header">
        <div class="header-content">
          <h1 class="trip-title">{{ travelForm.destination || '精彩' }} · {{ travelForm.days }}日探索之旅</h1>
          <div class="trip-meta">
            <div class="meta-item">
              <el-icon><User /></el-icon>
              <span>{{ travelForm.people }}人出行</span>
            </div>
            <div class="meta-divider"></div>
            <div class="meta-item">
              <el-icon><Wallet /></el-icon>
              <span>预估 ¥{{ Number(budgetSummary.total).toLocaleString('zh-CN', { maximumFractionDigits: 0 }) }}</span>
            </div>
            <div class="meta-divider" v-if="travelForm.preferences.length"></div>
            <div class="meta-tags" v-if="travelForm.preferences.length">
              <span v-for="pref in travelForm.preferences" :key="pref" class="pref-tag">#{{ pref }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 主要内容区 -->
      <div class="card-body">
        <!-- 左侧：行程时间轴 -->
        <div class="left-column">
          <div class="column-header">
            <div class="header-icon-box"><el-icon><MapLocation /></el-icon></div>
            <span class="header-title">行程安排</span>
          </div>
          
          <div class="timeline-container">
            <el-timeline>
              <el-timeline-item 
                v-for="(day, index) in itinerary" 
                :key="index" 
                placement="top" 
                :hollow="true" 
                type="primary"
                size="large"
                class="custom-timeline-item"
              >
                <template #dot>
                  <div class="custom-dot">{{ index + 1 }}</div>
                </template>
                
                <div class="day-header">
                  <span class="day-label">DAY {{ day.day }}</span>
                  <div class="day-weather" v-if="day.weather">
                    <img :src="`/weather/${day.weather}.png`" class="weather-icon">
                    <span class="weather-text">{{ day.temperature }} {{ day.weather }}</span>
                  </div>
                </div>

                <div class="day-activities">
                  <div v-for="(item, itemIndex) in day.activities" :key="item.id" class="activity-wrapper">
                    <div class="activity-card">
                      <div class="activity-time-badge">
                        {{ item.time }}
                      </div>
                      <div class="activity-main">
                        <div class="activity-info">
                          <div class="activity-title-row">
                            <span class="activity-name">{{ item.name }}</span>
                            <el-tag v-for="(tag,i) in item.tags" :key="tag" :type="types[i%types.length]" size="small" effect="light" round class="activity-tag">{{ tag }}</el-tag>
                          </div>
                          <p class="activity-desc">{{ item.description }}</p>
                          <p class="activity-tip" v-if="item.tip">📝 {{ item.tip }}</p>
                        </div>
                        <div class="activity-image-box" v-if="item.image">
                          <img :src="item.image" class="activity-img">
                        </div>
                      </div>
                    </div>
                    
                    <!-- 交通连接 -->
                    <div class="transport-connector" v-if="itemIndex < day.activities.length - 1">
                      <div class="transport-badge">
                        <el-icon><Van /></el-icon>
                        <span>{{ item.distance }} · {{ item.drivingTime }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </el-timeline-item>
            </el-timeline>
          </div>
        </div>

        <!-- 右侧：地图与预算 -->
        <div class="right-column">
           <!-- 地图 -->
           <div class="side-card map-card">
             <div class="side-card-header">
               <el-icon><MapLocation /></el-icon>
               <span>路线概览</span>
             </div>
             <div class="map-wrapper">
               <div id="amap-container" class="amap-box"></div>
             </div>
           </div>
           
           <!-- 预算 -->
           <div class="side-card budget-card" v-if="budgetSummary.total > 0">
             <div class="side-card-header">
               <el-icon><Wallet /></el-icon>
               <span>预算分析</span>
             </div>
             <div class="budget-content">
               <div class="budget-total-display">
                  <span class="currency">¥</span>
                  <span class="amount">{{ budgetSummary.total.toLocaleString('zh-CN', { maximumFractionDigits: 0 }) }}</span>
                  <span class="label">预估总花费</span>
               </div>
               <div ref="budgetChart" class="budget-chart"></div>
             </div>
           </div>
           
           <!-- 底部品牌区域 -->
           <div class="footer-card">
              <div class="footer-content">
                <div class="qr-placeholder">
                  <el-icon :size="32"><Picture /></el-icon>
                </div>
                <div class="footer-text">
                  <div class="footer-title">扫码查看详情</div>
                  <div class="footer-subtitle">Powered by 出发啦 App</div>
                </div>
              </div>
              <div class="footer-decoration"></div>
           </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue';
import { useRoute } from 'vue-router';
import * as echarts from 'echarts';
import { loadAMap } from '@/utils/amap-loader';
import { getHistoricalItineraryService } from '@/api/agent';
import { User, Wallet, MapLocation, Picture, Van } from '@element-plus/icons-vue';

const route = useRoute();
const AMAP_KEY = import.meta.env.VITE_AMAP_API_KEY;

const itinerary = ref([]);
const budgetSummary = ref({ breakdown: [], total: 0 });
const travelForm = ref({ destination: '', days: 0, people: 0, budget: 0, preferences: [] });
const map = ref(null);
const markers = ref([]);
const budgetChart = ref(null);
const types = ["warning", "primary", "success", "danger"];
const currentDate = new Date().toLocaleDateString();

// 标记页面是否准备好供截图
window.screenshotReady = false;

onMounted(async () => {
  const id = route.query.id;
  if (id) {
    await initMap();
    await loadData(id);
  }
});

const loadData = async (id) => {
  try {
    const result = await getHistoricalItineraryService(id);
    const data = result.data || {};

    // 填充行程数据
    itinerary.value = data.dailySchedules || [];
    budgetSummary.value = data.budgetSummary || { breakdown: [], total: 0 };
    
    // 填充元数据
    travelForm.value = {
      destination: data.destination,
      days: data.dayNum,
      people: data.people,
      budget: data.budget || 0,
      preferences: data.preferences || []
    };

    // 处理标签
    itinerary.value.forEach(day => {
      day.activities.forEach(active => {
        if (typeof active.tags === 'string' && active.tags !== '') {
          active.tags = active.tags.split(',');
        } else if (!Array.isArray(active.tags)) {
          active.tags = [];
        }
      });
    });

    nextTick(() => {
      addMarkersToMap();
      renderBudgetChart();
      // 延迟一点点确保图片加载（实际生产中可能需要监听图片加载）
      setTimeout(() => {
        window.screenshotReady = true;
      }, 2000);
    });

  } catch (e) {
    console.error('加载行程失败', e);
    // 即使失败也标记为就绪，避免 Puppeteer 超时，这样截图能看到错误状态
    window.screenshotReady = true;
  }
};

const initMap = async () => {
  try {
    const AMap = await loadAMap(AMAP_KEY, ['AMap.ToolBar', 'AMap.Scale']);
    map.value = new AMap.Map('amap-container', {
      zoom: 11,
      center: [110.4800, 29.1167],
      viewMode: '3D',
      pitch: 45,
    });
    map.value.addControl(new AMap.Scale());
    map.value.addControl(new AMap.ToolBar());
  } catch (error) {
    console.error('地图初始化失败', error);
  }
};

const addMarkersToMap = () => {
  if (!map.value || !window.AMap) return;
  
  itinerary.value.forEach(day => {
    day.activities.forEach(activity => {
      if (activity.position && activity.name) {
        const marker = new window.AMap.Marker({
          position: activity.position,
          title: activity.name,
          icon: new window.AMap.Icon({
            size: new window.AMap.Size(24, 24),
            imageSize: new window.AMap.Size(24, 24),
            content: `<div style="width: 16px; height: 16px; border-radius: 50%; background-color: #2563eb; border: 2px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.2); margin: 4px auto;"></div>`
          }),
          anchor: new window.AMap.Pixel(12, 12)
        });
        marker.setMap(map.value);
        markers.value.push(marker);
      }
    });
  });

  if (markers.value.length > 0) {
    map.value.setFitView(markers.value);
  }
};

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

const renderBudgetChart = () => {
  if (!budgetChart.value || !budgetSummary.value.breakdown.length) return;
  const chart = echarts.init(budgetChart.value);
  const pieData = budgetSummary.value.breakdown.map(item => ({
    value: item.amount,
    name: item.category,
    itemStyle: { 
      color: getProgressColor(item.category) // 匹配进度条颜色
    }
  }));
  
  chart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: ¥{c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      textStyle: { fontSize: 12 }
    },
    series: [{
      name: '预算分配',
      type: 'pie',
      radius: ['40%', '70%'],
      data: pieData,
      emphasis: {
        itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' }
      }
    }]
  });
};
</script>

<style>
body {
  margin: 0;
  padding: 0;
}

.share-page {
  min-height: 100vh;
  width: 100%;
  background-color: var(--c-bg-sub); 
  padding: 40px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  font-family: 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  color: var(--c-ink);
  box-sizing: border-box;
  -webkit-print-color-adjust: exact;
  print-color-adjust: exact;
}
</style>

<style scoped>
.share-card {
  width: 1200px;
  background: #ffffff;
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  position: relative;
}

.card-decoration {
  height: 8px;
  background: linear-gradient(90deg, var(--c-violet-500) 0%, var(--c-primary-300) 100%);
  width: 100%;
}

/* Header Styles */
.brand-header {
  padding: 30px 50px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid rgba(0,0,0,0.05);
  background: linear-gradient(90deg, var(--c-violet-500) 0%, var(--c-primary-300) 100%);
}

.brand-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.logo-box {
  width: 56px;
  height: 56px;
  background: var(--c-primary-50);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.brand-logo {
  width: 40px;
  height: 40px;
}

.brand-text {
  display: flex;
  flex-direction: column;
}

.app-name {
  font-size: var(--fs-h3);
  font-weight: 800;
  color: var(--c-ink);
  line-height: 1.2;
  letter-spacing: -0.5px;
}

.app-slogan {
  font-size: var(--fs-body);
  color: var(--c-ink);
  letter-spacing: 1px;
  font-weight: 500;
}

.date-badge {
  background: var(--c-bg-sub);
  padding: 8px 16px;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.date-label {
  font-size: var(--fs-caption);
  color: var(--c-ink-2);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.date-value {
  font-size: var(--fs-h3);
  font-weight: 700;
  color: var(--c-ink);
  font-family: monospace;
  margin-top: 2px;
}

/* Trip Header */
.trip-header {
  padding: 50px 50px 30px;
  text-align: center;
  background: linear-gradient(180deg, var(--c-primary-200) 0%, var(--c-primary-50) 100%);
}

.trip-title {
  font-size: 42px;
  color: var(--c-ink);
  margin-bottom: 24px;
  font-weight: 800;
  letter-spacing: -1px;
}

.trip-meta {
  display: inline-flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid var(--c-primary-100);
  border-radius: 50px;
  padding: 8px 24px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
  backdrop-filter: blur(4px);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--c-primary-600);
  font-weight: 600;
  font-size: var(--fs-body);
}

.meta-divider {
  width: 1px;
  height: 16px;
  background: var(--c-primary-100);
  margin: 0 20px;
}

.meta-tags {
  display: flex;
  gap: 12px;
}

.pref-tag {
  color: var(--c-primary-600);
  font-weight: 600;
  font-size: var(--fs-body);
}

/* Body Layout */
.card-body {
  display: flex;
  padding: 40px 50px 60px;
  gap: 50px;
  background: var(--c-primary-50);
}

.left-column {
  flex: 1;
}

.right-column {
  width: 360px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.column-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 30px;
}

.header-icon-box {
  width: 36px;
  height: 36px;
  background: var(--c-primary-50);
  color: var(--c-primary-600);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--fs-body-lg);
}

.header-title {
  font-size: var(--fs-h3);
  font-weight: 700;
  color: var(--c-ink);
}

/* Timeline Customization */
.timeline-container {
  padding-left: 10px;
}

:deep(.el-timeline-item__tail) {
  border-left: 2px dashed var(--c-line);
}

:deep(.el-timeline-item__node--primary) {
  background-color: transparent;
}

.custom-dot {
  width: 28px;
  height: 28px;
  background: var(--c-primary-600);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: var(--fs-body);
  box-shadow: 0 0 0 4px var(--c-primary-50);
}

.day-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--c-line);
}

.day-label {
  font-size: var(--fs-body-lg);
  font-weight: 800;
  color: var(--c-ink);
}

.day-weather {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--c-primary-50);
  padding: 4px 12px;
  border-radius: 20px;
}

.weather-icon {
  width: 24px;
  height: 24px;
}

.weather-text {
  font-size: var(--fs-caption);
  color: var(--c-primary-600);
  font-weight: 600;
}

/* Activity Cards */
.activity-wrapper {
  margin-bottom: 0;
}

.activity-card {
  display: flex;
  gap: 4px;
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 8px 24px rgba(149, 157, 165, 0.1);
  border: 1px solid var(--c-bg-sub);
  transition: transform 0.2s;
}

.activity-time-badge {
  font-size: var(--fs-body);
  font-weight: 700;
  color: var(--c-ink);
  min-width: 40px;
  padding-top: 2px;
}

.activity-main {
  flex: 1;
  display: flex;
  gap: 20px;
}

.activity-info {
  flex: 1;
}

.activity-title-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 8px;
}

.activity-name {
  font-size: var(--fs-body-lg);
  font-weight: 700;
  color: var(--c-ink);
}

.activity-desc {
  font-size: var(--fs-body);
  color: var(--c-ink-2);
  line-height: 1.6;
  margin: 0;
}

.activity-tip {
  font-size: var(--fs-caption);
  color: var(--c-accent);
  background: var(--c-accent-soft);
  padding: 8px 12px;
  border-radius: 8px;
  margin-top: 8px;
  line-height: 1.5;
  margin-bottom: 0;
}

.activity-image-box {
  width: 200px;
  height: 150px;
  border-radius: 10px;
  overflow: hidden;
  flex-shrink: 0;
}

.activity-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* Transport Connector */
.transport-connector {
  padding: 16px 0 16px 80px;
  position: relative;
}

.transport-connector::before {
  content: '';
  position: absolute;
  left: 30px; /* Adjust based on timeline line position */
  top: 0;
  bottom: 0;
  width: 2px;
  background: transparent; /* Handled by timeline tail */
}

.transport-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: var(--c-primary-50);
  padding: 6px 16px;
  border-radius: 20px;
  color: var(--c-primary-600);
  font-size: var(--fs-caption);
  font-weight: 500;
}

/* Side Cards */
.side-card {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 8px 24px rgba(149, 157, 165, 0.1);
  border: 1px solid var(--c-bg-sub);
}

.side-card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  font-size: var(--fs-body-lg);
  font-weight: 700;
  color: var(--c-ink);
}

.map-wrapper {
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid var(--c-line);
}

.amap-box {
  height: 280px;
  width: 100%;
}

.budget-content {
  text-align: center;
}

.budget-total-display {
  margin-bottom: 20px;
}

.budget-total-display .currency {
  font-size: var(--fs-h3);
  color: var(--c-danger);
  font-weight: bold;
  vertical-align: top;
  margin-right: 2px;
}

.budget-total-display .amount {
  font-size: var(--fs-h1);
  color: var(--c-danger);
  font-weight: 800;
  line-height: 1;
}

.budget-total-display .label {
  display: block;
  font-size: var(--fs-caption);
  color: var(--c-ink-3);
  margin-top: 4px;
}

.budget-chart {
  height: 220px;
}

/* Footer Card */
.footer-card {
  background: var(--c-ink);
  border-radius: 20px;
  padding: 24px;
  color: #fff;
  position: relative;
  overflow: hidden;
}

.footer-content {
  display: flex;
  align-items: center;
  gap: 20px;
  position: relative;
  z-index: 2;
}

.qr-placeholder {
  width: 64px;
  height: 64px;
  background: #fff;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--c-ink);
}

.footer-text {
  flex: 1;
}

.footer-title {
  font-size: var(--fs-body-lg);
  font-weight: 700;
  margin-bottom: 4px;
}

.footer-subtitle {
  font-size: var(--fs-caption);
  opacity: 0.7;
}

.footer-decoration {
  position: absolute;
  top: -20px;
  right: -20px;
  width: 100px;
  height: 100px;
  background: rgba(255,255,255,0.1);
  border-radius: 50%;
  z-index: 1;
}
</style>