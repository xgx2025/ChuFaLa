<template>
  <div class="hotel-detail">
    <!-- 1. 顶部搜索栏 -->
    <div class="search-bar">
      <el-row :gutter="16">
        <el-col :span="5">
          <el-input v-model="destination" placeholder="目的地/酒店名称" />
        </el-col>
        <el-col :span="3">
          <el-date-picker
            v-model="checkIn"
            type="date"
            placeholder="入住时间"
            format="MM月DD日"
            value-format="MM月DD日"
          />
        </el-col>
        <el-col :span="2">
          <el-input v-model.number="nightCount" type="number" placeholder="晚数" />
        </el-col>
        <el-col :span="3">
          <el-date-picker
            v-model="checkOut"
            type="date"
            placeholder="退房时间"
            format="MM月DD日"
            value-format="MM月DD日"
          />
        </el-col>
        <el-col :span="3">
          <el-select v-model="roomGuest" placeholder="房间及住客">
            <el-option label="1间, 1位" value="1-1"></el-option>
          </el-select>
        </el-col>
        <el-col :span="4" class="search-btn-col">
          <el-input v-model="keyword" placeholder="关键词（机场/火车站等）"/>
          <el-button type="primary">搜索</el-button>
        </el-col>
      </el-row>
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
        <el-button type="primary" style="background-color: #0066f6; height: 45px;">选择房间</el-button>
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
    <el-tabs v-model="activeTab" class="tabs">
      <el-tab-pane label="房间" name="rooms">
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
              <span class="stock">仅剩{{ room.stock }}间</span>
              <div>
              <span class="price">¥{{ room.price }}</span>
              <el-button type="primary" @click="router.push(`/hotel/order/${room.id}`)" style="height: 38px;width: 168px;background-color: #006ff6;">预订</el-button>
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
import { ref,onMounted } from 'vue'
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

// 搜索栏数据
// const destination = ref('上海')
// const checkIn = ref('09月14日')
// const nightCount = ref(1)
// const checkOut = ref('09月15日')
// const roomGuest = ref('1-1')
// const keyword = ref('')

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
    hotelData.value = {
      name: data.name,
      price: data.price,
      rating: data.overallRating,
      type: data.type,
      address: data.address,
      openYear: data.openYear,
      mainImage: data.mainImage,
      otherImages: data.otherImages,
      allImages: [data.mainImage, ...data.otherImages],
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

/* 搜索栏 */
.search-bar {
  margin-bottom: 20px;
}

.search-btn-col {
  display: flex;
  gap: 8px; /* 输入框和按钮之间的间距 */
  align-items: center; /* 垂直居中对齐 */
}

.search-btn-col .el-input {
  flex: 1; /* 让输入框占满剩余空间 */
}

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
  font-size: 12px;
  color: #f5190a;
}
.price {
  font-size: 24px;
  font-weight: bold;
  color: #006ff6;
  margin-right: 10px;
}
.price-desc {
  font-size: 14px;
  color: #666;
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
  font-size: 20px;
  margin-right: 10px;
}

/* 房型列表 */
.room-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.room-item {
  display: flex;
  border: 1px solid #eee;
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
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>