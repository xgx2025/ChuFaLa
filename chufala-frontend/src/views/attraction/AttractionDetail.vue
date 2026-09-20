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
          <div class="gallery-main" @click="handlePreview(0)">
            <img :src="allImages[0]" class="gallery-img" alt="主图" />
          </div>
          <div class="gallery-sub">
            <div v-for="(img, index) in allImages.slice(1, 5)" :key="index" class="gallery-sub-item" @click="handlePreview(index + 1)">
              <img :src="img" class="gallery-img" />
              <div v-if="index === 3 && allImages.length > 5" class="more-overlay">
                +{{ allImages.length - 5 }} 张
              </div>
            </div>
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
              <span class="price-label">价格</span>
              <div class="price-display">
                <span class="currency">¥</span>
                <span class="amount">{{ selectedTicket.price }}</span>
                <span class="unit">/起</span>
              </div>
            </div>

            <div class="selection-area">
              <label class="sub-label">选择门票</label>
              <div class="ticket-list">
                <div 
                  v-for="ticket in tickets" 
                  :key="ticket.id"
                  class="ticket-chip"
                  :class="{ 'active': selectedTicketId === ticket.id }"
                  @click="selectTicket(ticket.id)"
                >
                  <span class="ticket-chip-name">{{ ticket.name }}</span>
                  <span class="ticket-chip-price">¥{{ ticket.price }}</span>
                </div>
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
              <div class="total-preview">
                总计: <span class="highlight">¥{{ totalPrice.toFixed(2) }}</span>
              </div>
              <el-button type="primary" size="large" class="book-btn" @click="openBookingModal">
                立即预订
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

    <el-dialog v-model="bookingDialogVisible" title="填写预订信息" width="500px" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="ticketFormRef" label-position="top">
        <el-alert 
          :title="`您正在预订：${selectedTicket.name} x ${form.quantity}张`" 
          type="info" 
          :closable="false" 
          show-icon 
          style="margin-bottom: 20px"
        />
        
        <el-form-item label="联系人姓名" prop="name">
          <el-input v-model="form.name" placeholder="需与证件姓名一致" />
        </el-form-item>

        <el-form-item label="手机号码" prop="phone">
          <el-input v-model="form.phone" placeholder="用于接收取票码" />
        </el-form-item>

        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" placeholder="入园需刷身份证" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <span class="footer-price">应付：¥{{ totalPrice.toFixed(2) }}</span>
          <div>
            <el-button @click="bookingDialogVisible = false">取消</el-button>
            <el-button type="primary" :loading="submitting" @click="handleBuyNow">确认支付</el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <el-image-viewer v-if="showImageViewer" @close="showImageViewer = false" :url-list="allImages" :initial-index="previewIndex" />
  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { getAttractionDetailService } from '@/api/attraction'
import { ref, computed, onMounted } from 'vue'
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
    
    // Update tickets based on fetched data
    tickets.value = [
      { id: 1, name: '成人票', price: data.adultTicketPrice, originalPrice: data.adultTicketPrice },
      { id: 2, name: '儿童票', price: data.childTicketPrice, originalPrice: data.childTicketPrice },
      { id: 3, name: '亲子套票 (2大1小)', price: 490, originalPrice: 580 }
    ]
  } catch (error) {
    console.error('获取失败', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchAttractionDetail(attractionId)
})

const allImages = computed(() => attraction.value.allImages || [])

// 模拟门票数据
const tickets = ref([
  { id: 1, name: '成人票', price: 190, originalPrice: 230 },
  { id: 2, name: '儿童票', price: 115, originalPrice: 230 },
  { id: 3, name: '亲子套票 (2大1小)', price: 490, originalPrice: 580 }
])

const selectedTicketId = ref(1)
const form = ref({
  date: '',
  quantity: 1,
  name: '',
  phone: '',
  idCard: ''
})

// 规则保持不变
const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, pattern: /^1[3-9]\d{9}$/, message: '手机号格式错误', trigger: 'blur' }],
  idCard: [{ required: true, pattern: /(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '身份证格式错误', trigger: 'blur' }]
}

const ticketFormRef = ref()
const bookingDialogVisible = ref(false)
const submitting = ref(false)

const selectedTicket = computed(() => tickets.value.find(t => t.id === selectedTicketId.value) || tickets.value[0])
const totalPrice = computed(() => (selectedTicket.value?.price || 0) * (form.value.quantity || 0))

const selectTicket = (id) => selectedTicketId.value = id
const disabledDate = (time) => time.getTime() < Date.now() - 8.64e7

const handlePreview = (index) => {
  previewIndex.value = index
  showImageViewer.value = true
}

// 点击“立即预订”打开弹窗
const openBookingModal = () => {
  if (!form.value.date) {
    ElMessage.warning('请先选择游玩日期')
    return
  }
  bookingDialogVisible.value = true
}

// 提交订单
const handleBuyNow = () => {
  ticketFormRef.value.validate(valid => {
    if (valid) {
      submitting.value = true
      // 模拟API请求
      setTimeout(() => {
        submitting.value = false
        bookingDialogVisible.value = false
        ElMessage.success('预订成功！短信已发送')
      }, 1500)
    }
  })
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
}

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
  border: 1px solid var(--c-line);
  border-radius: 8px;
  padding: 10px 15px;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  transition: border-color 0.2s, background-color 0.2s, box-shadow 0.2s;
}

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
.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-price {
  font-size: var(--fs-body-lg);
  font-weight: bold;
  color: var(--c-danger);
}

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
