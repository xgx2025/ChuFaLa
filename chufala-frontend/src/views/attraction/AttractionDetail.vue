<template>
  <div class="page-container">
    <div class="breadcrumb-area container">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item to="/attraction">景点列表</el-breadcrumb-item>
        <el-breadcrumb-item>{{ attraction.name || '加载中...' }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <el-skeleton v-if="loading" :rows="10" animated class="container" />

    <main v-else class="container main-content">
      <section class="hero-section">
        <div class="header-info">
          <h1 class="attraction-title">{{ attraction.name }}</h1>
          <div class="attraction-meta">
            <el-rate v-model="attraction.rating" disabled show-score text-color="#ff9900" />
            <span class="review-count">{{ attraction.reviewCount }}条点评</span>
            <el-tag size="small" effect="plain" class="attraction-tag">{{ attraction.type || '景点' }}</el-tag>
          </div>
        </div>

        <div class="gallery-grid">
          <button type="button" class="gallery-main" :aria-label="`查看${attraction.name}的照片，共${allImages.length}张`" @click="handlePreview(0)">
            <img :src="allImages[0]" class="gallery-img" :alt="`${attraction.name}主图`" />
            <span v-if="allImages.length" class="gallery-counter">查看全部 · {{ allImages.length }} 张照片</span>
          </button>
          <div class="gallery-sub">
            <button v-for="(img, index) in allImages.slice(1, 5)" :key="index" type="button" class="gallery-sub-item" :aria-label="`查看第${index + 2}张照片`" @click="handlePreview(index + 1)">
              <img :src="img" class="gallery-img" :alt="`${attraction.name}照片${index + 2}`" />
              <div v-if="index === 3 && allImages.length > 5" class="more-overlay">
                +{{ allImages.length - 5 }} 张
              </div>
            </button>
          </div>
        </div>
      </section>

      <div class="content-layout">
        <div class="left-column">
          <el-tabs v-model="activeTab" class="detail-tabs">
            <el-tab-pane label="景点介绍" name="intro">
              <div class="info-block">
                <h3 class="block-title">关于景点</h3>
                <p class="text-content">{{ attraction.description }}</p>
              </div>
              
              <el-divider />
              
              <div class="info-grid">
                <div class="info-item">
                  <el-icon><Timer /></el-icon>
                  <div class="info-text">
                    <strong>开放时间</strong>
                    <p>{{ attraction.openTime }}</p>
                  </div>
                </div>
                <div class="info-item">
                  <el-icon><LocationInformation /></el-icon>
                  <div class="info-text">
                    <strong>景点地址</strong>
                    <p>{{ attraction.address }}</p>
                  </div>
                </div>
                <div class="info-item">
                  <el-icon><Van /></el-icon>
                  <div class="info-text">
                    <strong>交通指南</strong>
                    <p>{{ attraction.transportation || '暂无交通信息' }}</p>
                  </div>
                </div>
              </div>
            </el-tab-pane>
            
            <el-tab-pane label="预订须知" name="notice">
              <div class="info-block">
                <p class="text-content">此处可展示预订规则、退改政策等信息。</p>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>

        <div class="right-column">
          <div class="booking-card">
            <div class="card-header">
              <span class="price-label">票价参考</span>
              <div class="price-display">
                <span class="currency">¥</span>
                <span class="amount">{{ selectedTicket.price }}</span>
                <span class="unit">/张</span>
              </div>
            </div>

            <div class="selection-area">
              <label class="sub-label">选择门票</label>
              <div class="ticket-list">
                <button
                  v-for="ticket in tickets" 
                  :key="ticket.id"
                  type="button"
                  class="ticket-chip"
                  :class="{ 'active': selectedTicketId === ticket.id }"
                  :aria-pressed="selectedTicketId === ticket.id"
                  @click="selectTicket(ticket.id)"
                >
                  <span class="ticket-chip-name">{{ ticket.name }}</span>
                  <span class="ticket-chip-price">¥{{ ticket.price }}</span>
                </button>
                <p v-if="!tickets.length" class="ticket-empty">暂无可展示的门票价格</p>
              </div>

              <label class="sub-label">出行日期</label>
              <el-date-picker
                v-model="form.date"
                type="date"
                placeholder="选择游玩日期"
                :disabled-date="disabledDate"
                style="width: 100%"
                size="large"
              />

              <label class="sub-label">购买数量</label>
              <el-input-number v-model="form.quantity" :min="1" :max="10" style="width: 100%" />
            </div>

            <div class="card-footer">
              <div class="booking-summary" aria-live="polite">
                <span class="booking-summary__label">已选行程</span>
                <strong>{{ selectedTicket.name }} <span aria-hidden="true">·</span> {{ visitDateLabel }} <span aria-hidden="true">·</span> {{ form.quantity }} 张</strong>
              </div>
              <div class="total-preview">
                参考合计: <span class="highlight">¥{{ totalPrice.toFixed(2) }}</span>
              </div>
              <el-button type="primary" size="large" class="book-btn" :disabled="!tickets.length" @click="openBookingModal">
                查看游玩计划
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 推荐酒店 -->
      <section class="recommended-hotels">
        <h2 class="section-title">附近推荐酒店</h2>
        <div class="hotel-grid">
          <div v-for="hotel in recommendedHotels" :key="hotel.id" class="hotel-card" @click="goToHotel(hotel.id)">
            <div class="hotel-img-wrapper">
              <img :src="hotel.image" class="hotel-img" />
              <div class="hotel-score">{{ hotel.score }}分</div>
            </div>
            <div class="hotel-info">
              <h3 class="hotel-name">{{ hotel.name }}</h3>
              <p class="hotel-location">{{ hotel.address }}</p>
              <div class="hotel-price">
                <span class="currency">¥</span>
                <span class="amount">{{ hotel.price }}</span>
                <span class="unit">起</span>
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>

    <el-dialog v-model="bookingDialogVisible" title="确认游玩计划" width="min(480px, calc(100vw - 32px))">
      <div class="booking-dialog-summary">
        <span class="booking-dialog-summary__eyebrow">YOUR VISIT</span>
        <strong>{{ attraction.name }}</strong>
        <p>{{ selectedTicket.name }} · {{ visitDateLabel }} · {{ form.quantity }} 张</p>
        <div>参考票价 <b>¥{{ totalPrice.toFixed(2) }}</b></div>
      </div>
      <p class="booking-dialog-note">门票预订服务暂未开放。这里仅供确认选择，当前不会创建订单或收取费用。</p>
      <template #footer>
        <el-button type="primary" @click="bookingDialogVisible = false">我知道了</el-button>
      </template>
    </el-dialog>

    <el-image-viewer v-if="showImageViewer" @close="showImageViewer = false" :url-list="allImages" :initial-index="previewIndex" />
  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { getAttractionDetailService } from '@/api/attraction'
import { ref, computed, onMounted } from 'vue'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { Timer, LocationInformation, Van } from '@element-plus/icons-vue' // 记得导入图标

const route = useRoute()
const router = useRouter()
const attractionId = route.params.id
const attraction = ref({}) // 修复：初始化为空对象
const loading = ref(true)
const activeTab = ref('intro')

// 推荐酒店数据
const recommendedHotels = ref([
  { id: 101, name: '君悦度假酒店', address: '距离景点 1.2km', price: 899, score: 4.8, image: 'https://images.unsplash.com/photo-1566073771259-6a8506099945?q=80&w=2070&auto=format&fit=crop' },
  { id: 102, name: '云端全景酒店', address: '距离景点 0.5km', price: 1280, score: 4.9, image: 'https://images.unsplash.com/photo-1582719508461-905c673771fd?q=80&w=2025&auto=format&fit=crop' },
  { id: 103, name: '山水间民宿', address: '距离景点 2.5km', price: 450, score: 4.6, image: 'https://images.unsplash.com/photo-1584132967334-10e028bd69f7?q=80&w=2070&auto=format&fit=crop' },
  { id: 104, name: '城市便捷酒店', address: '距离景点 3.0km', price: 299, score: 4.5, image: 'https://images.unsplash.com/photo-1564501049412-61c2a3083791?q=80&w=2832&auto=format&fit=crop' },
])

const goToHotel = (id) => {
  router.push(`/hotel/detail/${id}`)
}

// 图片预览控制
const showImageViewer = ref(false)
const previewIndex = ref(0)

const fetchAttractionDetail = async (id) => {
  try {
    loading.value = true
    const res = await getAttractionDetailService(id)
    const data = res.data
    attraction.value = {
      ...data,
      allImages: [data.mainImage, ...(data.otherImages || [])],
    }
    
    // 只展示接口给出的票价，避免用固定价格伪造套餐票。
    tickets.value = [
      { id: 1, name: '成人票', price: Number(data.adultTicketPrice) },
      { id: 2, name: '儿童票', price: Number(data.childTicketPrice) }
    ].filter((ticket) => Number.isFinite(ticket.price) && ticket.price >= 0 &&
      (ticket.id === 1 ? data.adultTicketPrice != null : data.childTicketPrice != null))
    selectedTicketId.value = tickets.value[0]?.id ?? null
  } catch (error) {
    console.error('获取失败', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchAttractionDetail(attractionId)
})

const allImages = computed(() => (attraction.value.allImages || []).filter(Boolean))

const tickets = ref([])

const selectedTicketId = ref(1)
const form = ref({
  date: '',
  quantity: 1
})
const bookingDialogVisible = ref(false)

const selectedTicket = computed(() => tickets.value.find(t => t.id === selectedTicketId.value) || tickets.value[0] || { name: '未选门票', price: 0 })
const totalPrice = computed(() => (selectedTicket.value?.price || 0) * (form.value.quantity || 0))
const visitDateLabel = computed(() => form.value.date ? dayjs(form.value.date).format('M月D日') : '待选日期')

const selectTicket = (id) => selectedTicketId.value = id
const disabledDate = (time) => time.getTime() < Date.now() - 8.64e7

const handlePreview = (index) => {
  previewIndex.value = index
  showImageViewer.value = true
}

// 仅展示所选行程；当前页面没有真实门票下单接口。
const openBookingModal = () => {
  if (!tickets.value.length) return
  if (!form.value.date) {
    ElMessage.warning('请先选择游玩日期')
    return
  }
  bookingDialogVisible.value = true
}

</script>

<style scoped>
/* 通用布局 */
.page-container {
  background-color: var(--c-bg-sub);
  min-height: 100vh;
  padding-bottom: 40px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.breadcrumb-area {
  padding: 20px 0;
}

/* 顶部 Hero 区域 */
.header-info {
  margin-bottom: 20px;
}

.attraction-title {
  font-size: var(--fs-h1);
  font-weight: 700;
  color: var(--c-ink);
  margin-bottom: 8px;
}

.attraction-meta {
  display: flex;
  align-items: center;
  gap: 15px;
  color: var(--c-ink-3);
}

/* 图片网格布局 (Airbnb 风格) */
.gallery-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 10px;
  height: 400px;
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 40px;
  cursor: pointer;
}

.gallery-main {
  height: 100%;
  width: 100%;
  position: relative;
  overflow: hidden;
  padding: 0;
  border: 0;
  background: #edf4f7;
  cursor: pointer;
}

.gallery-counter {
  position: absolute;
  left: 18px;
  bottom: 18px;
  padding: 8px 12px;
  border: 1px solid rgba(255, 255, 255, .5);
  border-radius: var(--r-full);
  background: rgba(15, 38, 59, .68);
  color: #fff;
  font-size: var(--fs-caption);
  font-weight: 600;
  backdrop-filter: blur(6px);
}

.gallery-sub {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 1fr 1fr;
  gap: 10px;
}

.gallery-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.gallery-img:hover {
  transform: scale(1.05);
}

.gallery-sub-item {
  position: relative;
  overflow: hidden;
  padding: 0;
  border: 0;
  background: #edf4f7;
  cursor: pointer;
}

.gallery-main:focus-visible, .gallery-sub-item:focus-visible { outline: 3px solid var(--c-travel-sun); outline-offset: -3px; }

.more-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.5);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--fs-h3);
  font-weight: bold;
}

/* 内容两栏布局 */
.content-layout {
  display: flex;
  gap: 40px;
  position: relative;
}

.left-column {
  flex: 1; /* 占据剩余空间 */
  background: #fff;
  padding: 30px;
  border-radius: 12px;
}

.right-column {
  width: 360px; /* 固定宽度 */
  flex-shrink: 0;
}

/* 左侧信息 */
.detail-tabs :deep(.el-tabs__item) {
  font-size: var(--fs-body-lg);
  font-weight: bold;
}

.info-block {
  margin-bottom: 30px;
}

.block-title {
  font-size: var(--fs-h3);
  margin-bottom: 15px;
}

.text-content {
  line-height: 1.8;
  color: var(--c-ink-2);
  font-size: var(--fs-body);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.info-item {
  display: flex;
  gap: 12px;
}

.info-item .el-icon {
  font-size: var(--fs-h2);
  color: var(--c-primary-600);
  margin-top: 2px;
}

.info-text strong {
  display: block;
  margin-bottom: 4px;
  color: var(--c-ink);
}

.info-text p {
  color: var(--c-ink-3);
  font-size: var(--fs-body);
  margin: 0;
}

/* 右侧吸顶卡片 */
.booking-card {
  background: #fff;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.08);
  border: 1px solid var(--c-line-2);
  position: sticky; /* 吸顶核心代码 */
  top: 20px; 
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 20px;
  border-bottom: 1px solid var(--c-line);
  padding-bottom: 15px;
}

.price-display {
  color: var(--c-danger);
  font-weight: bold;
}

.currency { font-size: var(--fs-body-lg); }
.amount { font-size: var(--fs-h2); }
.unit { font-size: var(--fs-body); color: var(--c-ink-4); font-weight: normal; }

.selection-area {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.sub-label {
  font-size: var(--fs-body);
  font-weight: bold;
  color: var(--c-ink);
  margin-bottom: -5px;
  display: block;
}

.ticket-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.ticket-chip {
  width: 100%;
  background: var(--c-bg);
  color: var(--c-ink);
  font: inherit;
  text-align: left;
  border: 1px solid var(--c-line);
  border-radius: 8px;
  padding: 10px 15px;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  transition: border-color 0.2s, background-color 0.2s, box-shadow 0.2s;
}
.ticket-empty { margin: 0; color: var(--c-ink-3); font-size: var(--fs-caption); }

.ticket-chip:hover {
  border-color: var(--c-primary-600);
}

.ticket-chip.active {
  border-color: var(--c-primary-600);
  background-color: var(--c-primary-50);
  color: var(--c-primary-600);
}

.ticket-chip-price {
  font-weight: bold;
}

.card-footer {
  margin-top: 25px;
  padding-top: 20px;
  border-top: 1px solid var(--c-line);
}
.booking-summary { display: flex; flex-direction: column; gap: 3px; padding: var(--sp-3); margin-bottom: var(--sp-4); border: 1px solid #d8e7e8; border-radius: var(--r-sm); background: linear-gradient(110deg, #eff8f7, #fffaf1); }
.booking-summary__label { color: var(--c-travel-blue); font-size: var(--fs-caption); }
.booking-summary strong { color: var(--c-ink-2); font-size: var(--fs-body); font-weight: 600; }

.total-preview {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  font-size: var(--fs-body-lg);
  font-weight: bold;
}

.total-preview .highlight {
  color: var(--c-danger);
  font-size: var(--fs-h3);
}

.book-btn {
  width: 100%;
  font-weight: bold;
  border-radius: 8px;
  height: 48px;
}

/* 弹窗底部 */
.booking-dialog-summary { display: flex; flex-direction: column; gap: var(--sp-2); padding: var(--sp-5); border: 1px solid #d8e7e8; border-radius: var(--r-md); background: linear-gradient(120deg, #edf7f6, #fffaf1); }
.booking-dialog-summary__eyebrow { color: var(--c-travel-blue); font-family: var(--font-num); font-size: 10px; font-weight: 700; letter-spacing: .16em; }
.booking-dialog-summary strong { font-size: var(--fs-body-lg); }
.booking-dialog-summary p { margin: 0; color: var(--c-ink-2); }
.booking-dialog-summary div { display: flex; justify-content: space-between; color: var(--c-ink-3); }
.booking-dialog-summary b { color: var(--c-danger); font-family: var(--font-num); font-size: var(--fs-h3); }
.booking-dialog-note { margin: var(--sp-4) 0 0; color: var(--c-ink-3); line-height: 1.6; }

/* 推荐酒店区域 */
.recommended-hotels {
  margin-top: 40px;
}

.section-title {
  font-size: var(--fs-h2);
  font-weight: 600;
  margin-bottom: 20px;
  color: var(--c-ink);
}

.hotel-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
}

.hotel-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}

.hotel-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.1);
}

.hotel-img-wrapper {
  position: relative;
  height: 180px;
}

.hotel-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hotel-score {
  position: absolute;
  bottom: 10px;
  right: 10px;
  background: rgba(0,0,0,0.7);
  color: #fff;
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: bold;
  font-size: var(--fs-body);
}

.hotel-info {
  padding: 15px;
}

.hotel-name {
  font-size: var(--fs-body-lg);
  font-weight: bold;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.hotel-location {
  color: var(--c-ink-3);
  font-size: var(--fs-caption);
  margin-bottom: 10px;
}

.hotel-price {
  color: var(--c-danger);
  display: flex;
  align-items: baseline;
}

.hotel-price .currency {
  font-size: var(--fs-body);
}

.hotel-price .amount {
  font-size: var(--fs-h3);
  font-weight: bold;
  margin: 0 2px;
}

.hotel-price .unit {
  font-size: var(--fs-caption);
  color: var(--c-ink-4);
}

/* 响应式适配 */
@media (max-width: 768px) {
  .gallery-grid {
    height: auto;
    display: flex;
    overflow-x: auto;
  }
  
  .gallery-main {
    width: 100%;
    height: 250px;
  }
  
  .gallery-sub {
    display: none; /* 移动端隐藏小图 */
  }
  
  .content-layout {
    flex-direction: column;
  }
  
  .right-column {
    width: 100%;
  }
}
</style>
