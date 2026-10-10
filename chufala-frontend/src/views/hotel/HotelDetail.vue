<template>
  <div class="hotel-detail">
    <!-- 1. 入住计划：日期只用于本页估算，订单页会重新核价与核库存。 -->
    <div class="stay-planner">
      <div class="stay-planner__heading">
        <span class="stay-planner__eyebrow">PLAN YOUR STAY</span>
        <strong>住几晚，先算清楚</strong>
      </div>
      <div class="stay-planner__field">
        <label for="hotel-check-in">入住</label>
        <el-date-picker id="hotel-check-in" v-model="checkIn" type="date" placeholder="入住日期" format="MM月DD日" value-format="YYYY-MM-DD" :clearable="false" :disabled-date="disablePastDate" @change="syncCheckOut" />
      </div>
      <div class="stay-planner__field">
        <label for="hotel-check-out">离店</label>
        <el-date-picker id="hotel-check-out" v-model="checkOut" type="date" placeholder="离店日期" format="MM月DD日" value-format="YYYY-MM-DD" :clearable="false" :disabled-date="disableCheckOutDate" />
      </div>
      <span class="stay-planner__nights">{{ stayNights }} 晚</span>
      <el-button type="primary" class="stay-planner__action" @click="scrollToRooms">查看房型</el-button>
    </div>

    <!-- 2. 面包屑导航 -->
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item to="/">首页</el-breadcrumb-item>
      <el-breadcrumb-item to="/hotel">酒店</el-breadcrumb-item>
      <el-breadcrumb-item>{{hotelData.name}}</el-breadcrumb-item>
    </el-breadcrumb>

    <!-- 3. 酒店基本信息 -->
    <div class="hotel-header">
      <div class="hotel-info-left">
        <h1 class="hotel-name">{{ hotelData.name }}</h1>

        <div class="hotel-address">
          <el-icon><Location /></el-icon>
          {{ hotelData.address }}
          <el-button type="text" @click="showMap = true">显示地图</el-button>
        </div>

        <div class="hotel-meta">
          <span>开业：{{ hotelData.openYear }}</span>
          <el-button type="text" @click="showMore = !showMore">
            {{ showMore ? '收起' : '查看更多' }}
          </el-button>
          <p v-show="showMore">
            北京古北禧玥酒店坐落于高档奢华的黄金城道毗邻高岛屋，周边有地铁2、11号线...
          </p>
        </div>
      </div>

      <div class="hotel-info-right">
        <span class="price">¥{{hotelData.price}} <span class="price-desc">起</span></span>
        <el-button type="primary" style="background-color: #0066f6; height: 45px;" @click="scrollToRooms">选择房间</el-button>
      </div>
    </div>

    <div class="hotel-media-section">
        <!-- 4. 图片展示区 -->
        <div class="hotel-images">
            <!-- 左侧主图 -->
            <el-image
            style="width: 400px; height: 400px"
            :src="hotelData.mainImage"
            :preview-src-list="hotelData.allImages" 
            fit="cover"
            />

            <!-- 右侧缩略图网格 -->
            <div class="thumb-grid">
            <el-image
                v-for="(img, idx) in hotelData.otherImages"
                :key="idx"
                style="width: 120px; height: 120px; margin-bottom: 10px"
                :src="img"
                :preview-src-list="hotelData.allImages" 
                fit="cover"
            />
            </div>
        </div>

        <!-- 5. 评分与设施 -->
        <div class="hotel-meta-info">
            <div class="rating">
                <el-rate v-model="hotelData.rating" disabled />
                <span class="score">{{hotelData.rating}}分</span>
                <el-tag type="success">超棒</el-tag>
                <p>显示所有3,568条点评</p>
                <p>前台热情、有设计感</p>
            </div>

            <div class="facilities">
                <p>无线WIFI免费 | 行李寄存 | 茶室 | 24小时前台</p>
                <el-button type="text" @click="showAllFacilities = !showAllFacilities">
                {{ showAllFacilities ? '收起设施' : '显示所有设施' }}
                </el-button>
                <div v-show="showAllFacilities" class="more-facilities">
                <p>健身房 | 游泳池 | 会议室...</p>
                </div>
            </div>

            <div class="location">
                <el-icon><Plane /></el-icon> 6.59公里（机场）
                <el-icon><Train /></el-icon> 7.51公里（火车站）
                <el-icon><Subway /></el-icon> 220米（地铁）
                <el-button type="text" @click="showMap = true">查看地图</el-button>
            </div>
        </div>
    </div>

    <!-- 6. 标签页（房间/点评/政策） -->
    <el-tabs v-model="activeTab" class="tabs" id="hotel-rooms">
      <el-tab-pane label="房间" name="rooms">
        <div class="room-list__heading">
          <div><strong>{{ formatShortDate(checkIn) }} — {{ formatShortDate(checkOut) }}</strong><span> · {{ stayNights }} 晚入住</span></div>
          <small>下方为房型价格估算，最终价格与库存会在订单页确认</small>
        </div>
        <div class="room-list">
          <div class="room-item" v-for="room in hotelData.roomList" :key="room.id">
            <div class="room-img">
              <img :src="room.imageList[0]" alt="房型图" />
            </div>
            <div class="room-basic">
              <h3>{{ room.name }}</h3>
              <p><el-icon><Bed /></el-icon> {{ room.bed }}</p>
              <p><el-icon style="color: #06875a;"><Window /></el-icon> 有窗</p>
              <p><el-icon><NoSmoking /></el-icon> 禁烟</p>
              <p>{{ room.area }}平方米 | {{ room.floor }}</p>
              <p><el-icon style="color: #06875a;"><Wifi /></el-icon> WIFI免费</p>
              <el-button type="text">房间详情</el-button>
            </div>
            <div class="room-summary">
              <h4>房型摘要</h4>
              <p v-if="!room.breakfast"><el-icon><Close /></el-icon> 无早餐</p>
              <p><el-icon><Check /></el-icon> 入住当天18:00前可免费取消</p>
              <p><el-icon><Check /></el-icon> 立即确认</p>
              <p><el-icon><Money /></el-icon> 在线付</p>
              <p><el-icon><Gift /></el-icon> 新客专享</p>
            </div>
            <div class="room-guest">
              <h4>可住人数</h4>
              <el-icon><User /></el-icon>
              <el-icon><User /></el-icon>
            </div>
            <div class="room-price">
              <span class="stock">该房型共{{ room.stock }}间</span>
              <div>
              <span class="price">¥{{ room.price }}<small>/晚起</small></span>
              <span class="room-price__estimate">{{ stayNights }} 晚参考房费 <strong>¥{{ formatAmount(Number(room.price) * stayNights) }}</strong></span>
              <el-button type="primary" @click="bookRoom(room.id)" style="height: 38px;width: 168px;background-color: #006ff6;">预订</el-button>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>
      <el-tab-pane label="点评" name="reviews">
        点评内容待加载...
      </el-tab-pane>
      <el-tab-pane label="政策" name="policies">
        政策内容待加载...
      </el-tab-pane>
    </el-tabs>

    <!-- 地图弹窗 -->
    <el-dialog v-model="showMap" title="酒店位置">
      <div class="map-placeholder">地图可视化内容</div>
    </el-dialog>
  </div>
</template>


<script setup>
import { ref, computed, onMounted } from 'vue'
import dayjs from 'dayjs'
import {Location,NoSmoking,Check,Close,Money,User} from '@element-plus/icons-vue'
import { Bed,Gift,Plane,Subway,Train,Wifi,Window } from '@/components/Icon.vue'
import { getHotelDetailService } from '@/api/hotel'
import router from '@/router'
import { useRoute } from 'vue-router'



// 接收路由参数（酒店ID）
const route = useRoute()
const hotelId = route.params.id // 从路由中获取ID
// 定义酒店详情的响应式数据
const hotelData = ref({
  name: '',
  price: 0,
  rating: 0,
  address: '',
  openYear: '',
  mainImage: '',
  otherImages: [],
  allImages: [],
  facilities: '',
  locationInfo: {},
  roomList: []
})

const today = dayjs().startOf('day')
const isValidStayDate = (value) => typeof value === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(value) && dayjs(value).isValid() && dayjs(value).format('YYYY-MM-DD') === value
const routeCheckIn = isValidStayDate(route.query.checkIn) && !dayjs(route.query.checkIn).isBefore(today) ? route.query.checkIn : today.format('YYYY-MM-DD')
const routeCheckOut = isValidStayDate(route.query.checkOut) && dayjs(route.query.checkOut).isAfter(dayjs(routeCheckIn)) ? route.query.checkOut : dayjs(routeCheckIn).add(1, 'day').format('YYYY-MM-DD')
const checkIn = ref(routeCheckIn)
const checkOut = ref(routeCheckOut)
const stayNights = computed(() => Math.max(1, dayjs(checkOut.value).diff(dayjs(checkIn.value), 'day')))
const disablePastDate = (date) => dayjs(date).isBefore(dayjs().startOf('day'))
const disableCheckOutDate = (date) => dayjs(date).isBefore(dayjs(checkIn.value).add(1, 'day'))
const syncCheckOut = () => {
  if (!checkIn.value) return
  if (!checkOut.value || !dayjs(checkOut.value).isAfter(dayjs(checkIn.value))) {
    checkOut.value = dayjs(checkIn.value).add(1, 'day').format('YYYY-MM-DD')
  }
}
const formatShortDate = (value) => dayjs(value).format('M月D日')
const formatAmount = (value) => Number.isFinite(value) ? value.toLocaleString('zh-CN', { maximumFractionDigits: 0 }) : '—'
const scrollToRooms = () => {
  activeTab.value = 'rooms'
  document.getElementById('hotel-rooms')?.scrollIntoView({ behavior: window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 'instant' : 'smooth', block: 'start' })
}
const bookRoom = (id) => router.push({ path: `/hotel/order/${id}`, query: { checkIn: checkIn.value, checkOut: checkOut.value } })

// 酒店基本信息
const showMore = ref(false)
const showMap = ref(false)
const showAllFacilities = ref(false)
const activeTab = ref('rooms')


// 请求酒店详情
const fetchHotelDetail = async (id) => {
  try {
   
    const res = await getHotelDetailService(id)
    const data = res.data
    // 后端在「酒店只有一张图」时可能返回 otherImages: null/undefined，
    // 直接展开会抛 "otherImages is not iterable"。该异常会被下面的 catch 吞掉，
    // 结果 hotelData 不赋值 → 整个详情页空白，且日志里只有一句 console.error。
    const otherImages = Array.isArray(data.otherImages) ? data.otherImages : []
    hotelData.value = {
      name: data.name,
      price: data.price,
      rating: data.overallRating,
      type: data.type,
      address: data.address,
      openYear: data.openYear,
      mainImage: data.mainImage,
      otherImages,
      // filter(Boolean) 同时兜住 mainImage 为空的情况，避免图集里混进 null
      allImages: [data.mainImage, ...otherImages].filter(Boolean),
      facilities: data.facilities,
      roomList: data.roomList
    }
    console.log('获取酒店详情成功：', data)
  } catch (error) {
     console.error('获取酒店详情失败（真实错误）：', error.message || error)
    console.error('获取酒店详情失败：', error)
  }
}
// 组件挂载时触发数据请求
onMounted(() => {
  fetchHotelDetail(hotelId)
})
</script>


<style scoped>
.hotel-detail {
  max-width: 1600px;
  margin: 0 auto;
  padding: 20px;
  background-color: #ffffff;
}

.stay-planner { display: flex; align-items: end; flex-wrap: wrap; gap: var(--sp-4); padding: var(--sp-4) var(--sp-5); margin-bottom: var(--sp-5); border: 1px solid #d9e5e8; border-radius: var(--r-lg); background: linear-gradient(110deg, #edf7f6, #fffaf1); }
.stay-planner__heading { display: flex; flex-direction: column; min-width: 190px; margin-right: auto; }
.stay-planner__heading strong { font-size: var(--fs-body-lg); }
.stay-planner__eyebrow { color: var(--c-travel-blue); font-family: var(--font-num); font-size: 10px; font-weight: 700; letter-spacing: .16em; }
.stay-planner__field { display: flex; flex-direction: column; gap: 5px; }
.stay-planner__field label { color: var(--c-ink-3); font-size: var(--fs-caption); }
.stay-planner__field :deep(.el-date-editor) { width: 150px; }
.stay-planner__nights { align-self: center; color: var(--c-travel-blue-deep); font-weight: 700; white-space: nowrap; }
.stay-planner__action { align-self: center; }

/* 面包屑 */
.breadcrumb {
  margin-bottom: 20px;
}

/* 酒店头部信息 */
.hotel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.hotel-info-left {
  flex: 1;
}
.hotel-name {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}
.hotel-address,
.hotel-meta {
  margin: 10px 0;
}
.hotel-info-right {
  text-align: right;
}
.stock{
  font-size: var(--fs-caption);
  color: var(--c-danger);
}
.price {
  font-size: var(--fs-h2);
  font-weight: bold;
  color: var(--c-primary-600);
  margin-right: 10px;
}
.price-desc {
  font-size: var(--fs-body);
  color: var(--c-ink-3);
}

/* 图片与右侧文字的整体容器（水平排列） */
.hotel-media-section {
  display: flex;
  gap: 20px; /* 图片与文字的间距 */
  align-items: flex-start; /* 顶部对齐 */
  margin-bottom: 20px;
}

/* 图片轮播 */
.hotel-images {
  display: flex;
  gap: 20px; /* 主图与缩略图之间的间距 */
  align-items: flex-start;
  margin-bottom: 20px;
}

/* 缩略图网格：3列布局 */
.thumb-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr); /* 3列等宽 */
  gap: 10px; /* 缩略图之间的间距 */
}


/* 评分与设施 */
.hotel-meta-info {
  display: flex;
  flex-direction: column;
  gap: 16px; /* 评分、设施、位置之间的垂直间距 */
  flex: 1; /* 占据剩余宽度 */
}
.rating,
.facilities,
.location {
  flex: 1;
  margin-right: 0;;
}
.score {
  font-size: var(--fs-h3);
  margin-right: 10px;
}

/* 房型列表 */
.room-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.room-list__heading { display: flex; align-items: baseline; justify-content: space-between; flex-wrap: wrap; gap: var(--sp-2); padding: var(--sp-3) var(--sp-4); margin-bottom: var(--sp-4); border-radius: var(--r-sm); background: var(--c-bg-sub); }
.room-list__heading strong { color: var(--c-travel-blue-deep); }
.room-list__heading small,
.room-list__heading span { color: var(--c-ink-3); }
.room-item {
  display: flex;
  border: 1px solid var(--c-line-2);
  border-radius: 4px;
  overflow: hidden;
}
.room-img {
  width: 250px;
}
.room-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.room-basic,
.room-summary,
.room-guest,
.room-price {
  padding: 10px;
}
.room-price > div { display: flex; flex-direction: column; align-items: center; gap: var(--sp-2); }
.room-price .price { margin: 0; white-space: nowrap; }
.room-price .price small { margin-left: 4px; color: var(--c-ink-3); font-size: var(--fs-caption); font-weight: 400; }
.room-price__estimate { color: var(--c-ink-3); font-size: var(--fs-caption); white-space: nowrap; }
.room-price__estimate strong { color: var(--c-travel-blue-deep); }
#hotel-rooms { scroll-margin-top: calc(var(--header-h) + var(--sp-4)); }
.room-basic {
  flex: 2;
}
.room-summary,
.room-guest {
  flex: 1;
}
.room-price {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

/* 地图弹窗 */
.map-placeholder {
  width: 400px;
  height: 300px;
  background: var(--c-bg-sub);
  display: flex;
  align-items: center;
  justify-content: center;
}
@media (max-width: 768px) {
  .hotel-detail { width: 100%; padding: var(--sp-4); overflow: hidden; }
  .stay-planner { gap: var(--sp-3); padding: var(--sp-4); }
  .stay-planner__heading { width: 100%; }
  .stay-planner__field { flex: 1 1 130px; min-width: 0; }
  .stay-planner__field :deep(.el-date-editor) { width: 100%; }
  .stay-planner__action { margin-left: auto; }
  .hotel-header { align-items: flex-start; flex-direction: column; gap: var(--sp-3); }
  .hotel-info-right { display: flex; align-items: center; justify-content: space-between; width: 100%; text-align: left; }
  .hotel-media-section { flex-direction: column; gap: var(--sp-4); width: 100%; }
  .hotel-images { flex-direction: column; width: 100%; gap: var(--sp-2); margin-bottom: 0; }
  .hotel-images > :deep(.el-image) { width: 100% !important; height: 230px !important; }
  .thumb-grid { display: flex; width: 100%; overflow-x: auto; }
  .thumb-grid :deep(.el-image) { width: 86px !important; height: 70px !important; flex: none; margin-bottom: 0 !important; }
  .hotel-meta-info { width: 100%; }
  .rating, .facilities, .location { margin-right: 0; }
  .room-item { flex-direction: column; }
  .room-img { width: 100%; height: 180px; }
  .room-price { flex-direction: row; align-items: center; justify-content: space-between; gap: var(--sp-3); border-top: 1px dashed var(--c-line); }
  .room-price > div { align-items: flex-end; }
  .map-placeholder { max-width: 100%; }
}
</style>
