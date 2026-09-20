<template>
  <div class="flight-page">
    <!-- Hero Section with Search -->
    <div class="hero-section">
      <el-carousel height="500px" :interval="5000" arrow="hover">
        <el-carousel-item v-for="(item, index) in slides" :key="index">
          <div class="hero-slide" :style="{ backgroundImage: `url(${item.image})` }">
            <div class="hero-overlay"></div>
          </div>
        </el-carousel-item>
      </el-carousel>
      
      <div class="hero-content">
        <h1 class="hero-title">探索世界，从这里起飞</h1>
        <p class="hero-subtitle">特价机票，全球航线，一站式预订</p>
        
        <!-- Search Card -->
        <el-card class="search-card">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="单程" name="one-way"></el-tab-pane>
            <el-tab-pane label="往返" name="round-trip"></el-tab-pane>
          </el-tabs>
          
          <el-form :model="searchForm" class="search-form" label-position="top">
            <div class="form-row">
              <el-form-item label="出发地" class="flex-item">
                <el-input v-model="searchForm.from" placeholder="城市/机场" prefix-icon="Location" />
              </el-form-item>
              
              <div class="exchange-icon">
                <el-button circle icon="Switch" @click="swapLocations" />
              </div>
              
              <el-form-item label="目的地" class="flex-item">
                <el-input v-model="searchForm.to" placeholder="城市/机场" prefix-icon="Location" />
              </el-form-item>
            </div>
            
            <div class="form-row">
              <el-form-item label="出发日期" class="flex-item">
                <el-date-picker 
                  v-model="searchForm.date" 
                  :type="activeTab === 'round-trip' ? 'daterange' : 'date'"
                  placeholder="选择日期"
                  style="width: 100%"
                />
              </el-form-item>
              
              <el-form-item label="舱位/人数" class="flex-item">
                <el-popover placement="bottom" :width="300" trigger="click">
                  <template #reference>
                    <el-input 
                      v-model="passengerSummary" 
                      readonly 
                      placeholder="选择舱位和人数" 
                      prefix-icon="User"
                    />
                  </template>
                  <div class="passenger-popover">
                    <div class="popover-row">
                      <span>成人</span>
                      <el-input-number v-model="searchForm.passengers.adult" :min="1" size="small" />
                    </div>
                    <div class="popover-row">
                      <span>儿童</span>
                      <el-input-number v-model="searchForm.passengers.child" :min="0" size="small" />
                    </div>
                    <el-divider />
                    <el-radio-group v-model="searchForm.class">
                      <el-radio-button label="economy">经济舱</el-radio-button>
                      <el-radio-button label="business">商务舱</el-radio-button>
                      <el-radio-button label="first">头等舱</el-radio-button>
                    </el-radio-group>
                  </div>
                </el-popover>
              </el-form-item>
            </div>
            
            <el-button type="primary" class="search-btn" size="large" @click="handleSearch">
              搜索航班
            </el-button>
          </el-form>
        </el-card>
      </div>
    </div>

    <!-- Popular Routes -->
    <div class="section-container">
      <h2 class="section-title">热门航线特惠</h2>
      <div class="routes-grid">
        <el-card v-for="route in popularRoutes" :key="route.id" class="route-card" :body-style="{ padding: '0px' }">
          <img v-lazy-img loading="lazy" :src="route.image" :alt="`${route.from} 到 ${route.to} 航线`" class="route-image">
          <div class="route-info">
            <div class="route-header">
              <!-- 原为 <i class="el-icon-right">：el-icon-* 是 Element UI v2 的类名写法，
                   Element Plus 里图标是组件，这样写渲染为空 -->
              <span class="route-cities">{{ route.from }} <el-icon class="route-arrow"><Right /></el-icon> {{ route.to }}</span>
              <span class="route-price">¥{{ route.price }}起</span>
            </div>
            <p class="route-date">{{ route.date }}</p>
            <el-tag size="small" effect="plain">{{ route.tag }}</el-tag>
          </div>
        </el-card>
      </div>
    </div>

    <!-- Features -->
    <div class="features-section">
      <div class="section-container">
        <div class="features-grid">
          <div class="feature-item">
            <el-icon class="feature-icon"><Plane /></el-icon>
            <h3>全球航线</h3>
            <p>覆盖全球200+国家和地区</p>
          </div>
          <div class="feature-item">
            <el-icon class="feature-icon"><Timer /></el-icon>
            <h3>极速出票</h3>
            <p>支付后极速出票，出行无忧</p>
          </div>
          <div class="feature-item">
            <el-icon class="feature-icon"><Service /></el-icon>
            <h3>7x24服务</h3>
            <p>专业客服团队随时为您服务</p>
          </div>
        </div>
      </div>
    </div>
    
    <el-backtop :right="100" :bottom="100" />
  </div>
</template>
<script setup lang="ts">
import { ref, computed } from 'vue'
import { Location, Switch, User, Timer, Service, Right } from '@element-plus/icons-vue'
import { Plane } from '@/components/Icon.vue'

const activeTab = ref('one-way')

const slides = [
  { image: 'https://images.unsplash.com/photo-1436491865332-7a61a109cc05?q=80&w=2074&auto=format&fit=crop' },
  { image: 'https://images.unsplash.com/photo-1556388169-db19adc96088?q=80&w=2188&auto=format&fit=crop' },
  { image: 'https://images.unsplash.com/photo-1569154941061-e231b4725ef1?q=80&w=2070&auto=format&fit=crop' },
]

const searchForm = ref({
  from: '',
  to: '',
  date: '',
  passengers: {
    adult: 1,
    child: 0
  },
  class: 'economy'
})

const passengerSummary = computed(() => {
  const { adult, child } = searchForm.value.passengers
  const classMap: Record<string, string> = {
    economy: '经济舱',
    business: '商务舱',
    first: '头等舱'
  }
  return `${adult + child}人 · ${classMap[searchForm.value.class]}`
})

const swapLocations = () => {
  const temp = searchForm.value.from
  searchForm.value.from = searchForm.value.to
  searchForm.value.to = temp
}

const handleSearch = () => {
  console.log('Searching flights:', searchForm.value)
  // Implement search logic here
}

const popularRoutes = [
  {
    id: 1,
    from: '北京',
    to: '上海',
    price: 450,
    date: '10月20日',
    tag: '特价',
    image: 'https://images.unsplash.com/photo-1548957175-84f0f9af659e?q=80&w=2082&auto=format&fit=crop'
  },
  {
    id: 2,
    from: '广州',
    to: '成都',
    price: 680,
    date: '10月22日',
    tag: '热销',
    image: 'https://images.unsplash.com/photo-1565619624098-e659884d3c36?q=80&w=2070&auto=format&fit=crop'
  },
  {
    id: 3,
    from: '深圳',
    to: '三亚',
    price: 520,
    date: '10月25日',
    tag: '度假',
    image: 'https://images.unsplash.com/photo-1535913989690-f90e1c2d4cfa?q=80&w=1974&auto=format&fit=crop'
  },
  {
    id: 4,
    from: '杭州',
    to: '西安',
    price: 390,
    date: '10月28日',
    tag: '历史',
    image: 'https://images.unsplash.com/photo-1575615358363-2d852e34137c?q=80&w=1974&auto=format&fit=crop'
  }
]
</script>

<style scoped>
.flight-page {
  min-height: 100vh;
  background-color: var(--c-bg-sub);
  padding-bottom: 40px;
}

.hero-section {
  position: relative;
  margin-bottom: 100px;
}

.hero-slide {
  height: 100%;
  background-size: cover;
  background-position: center;
  position: relative;
}

.hero-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.3);
}

.hero-content {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100%;
  max-width: 1000px;
  text-align: center;
  z-index: 10;
  padding: 0 20px;
}

.hero-title {
  color: white;
  font-size: 3.5rem;
  margin-bottom: 10px;
  text-shadow: 0 2px 10px rgba(0,0,0,0.3);
}

.hero-subtitle {
  color: rgba(255, 255, 255, 0.9);
  font-size: var(--fs-h2);
  margin-bottom: 40px;
  text-shadow: 0 2px 5px rgba(0,0,0,0.3);
}

.search-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  backdrop-filter: blur(10px);
  border: none;
  box-shadow: 0 15px 30px rgba(0,0,0,0.2);
  text-align: left;
}

.search-form {
  margin-top: 20px;
}

.form-row {
  display: flex;
  gap: 20px;
  align-items: center;
  margin-bottom: 20px;
}

.flex-item {
  flex: 1;
  margin-bottom: 0 !important;
}

.exchange-icon {
  padding-top: 20px;
}

.search-btn {
  width: 100%;
  height: 50px;
  font-size: var(--fs-body-lg);
  border-radius: 8px;
  margin-top: 10px;
}

.passenger-popover {
  padding: 10px;
}

.popover-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.section-container {
  max-width: var(--container-max);
  margin: 0 auto;
  padding: 0 var(--sp-5);
}

.section-title {
  font-size: var(--fs-h2);
  line-height: var(--lh-h2);
  font-weight: 600;
  letter-spacing: -0.01em;
  margin-bottom: var(--sp-8);
  color: var(--c-ink);
  position: relative;
  padding-left: var(--sp-4);
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 24px;
  background: var(--c-primary-600);
  border-radius: var(--r-full);
}

.routes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--sp-5);
  margin-bottom: var(--sp-16);
}

.route-card {
  transition: transform var(--dur-base) var(--ease-out),
    box-shadow var(--dur-base) var(--ease-out);
  cursor: pointer;
  border: none;
  overflow: hidden;
  border-radius: var(--r-md);
  box-shadow: var(--sh-1);
  will-change: transform;
  backface-visibility: hidden;
}

.route-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--sh-hover);
}

.route-image {
  width: 100%;
  height: 180px;
  object-fit: cover;
  transition: transform var(--dur-slower) var(--ease-out);
}

.route-card:hover .route-image {
  transform: scale(1.06);
}

.route-info {
  padding: var(--sp-4);
}

.route-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.route-cities {
  display: flex;
  align-items: center;
  gap: var(--sp-1);
  font-weight: 600;
  font-size: var(--fs-body-lg);
  color: var(--c-ink);
}

.route-arrow {
  color: var(--c-ink-4);
  font-size: var(--fs-body);
}

.route-price {
  color: var(--c-danger);
  font-family: var(--font-num);
  font-variant-numeric: tabular-nums;
  font-weight: 700;
  font-size: var(--fs-body-lg);
}

.route-date {
  color: var(--c-ink-3);
  font-size: var(--fs-body);
  margin-bottom: 10px;
}

.features-section {
  background: white;
  padding: 60px 0;
  margin-top: 40px;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 40px;
  text-align: center;
}

.feature-item h3 {
  margin: 15px 0 10px;
  font-size: var(--fs-body-lg);
}

.feature-item p {
  color: var(--c-ink-3);
  font-size: var(--fs-body);
}

.feature-icon {
  font-size: 48px;
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  padding: 20px;
  border-radius: 50%;
  margin-bottom: 10px;
}

@media (max-width: 768px) {
  .hero-title {
    font-size: var(--fs-h1);
  }
  
  .hero-subtitle {
    font-size: var(--fs-body-lg);
  }
  
  .form-row {
    flex-direction: column;
    gap: 10px;
  }
  
  .exchange-icon {
    padding-top: 0;
    transform: rotate(90deg);
  }
  
  .features-grid {
    grid-template-columns: 1fr;
  }
}
</style> 