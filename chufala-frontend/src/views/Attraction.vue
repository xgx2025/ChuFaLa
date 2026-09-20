<template>
  <div 
    class="attraction-result-page"
    v-infinite-scroll="loadMore"
    :infinite-scroll-disabled="disabled"
    infinite-scroll-distance="100"
  >
    <!-- 搜索和筛选区 -->
    <el-card class="filter-card">
      <div class="filter-container">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索景点名称"
          prefix-icon="Search"
          style="width: 300px"
          @keyup.enter="fetchAttractions"
        />
        <el-button type="primary" @click="fetchAttractions">搜索</el-button>

        <el-select v-model="filterCity" placeholder="城市" style="width: 120px; margin-left: 10px" filterable>
          <el-option
            v-for="item in cityOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>

        <el-select v-model="filterStars" placeholder="等级" style="width: 120px; margin-left: 10px">
          <el-option label="不限" value="" />
          <el-option label="5 A级" value="5" />
          <el-option label="4 A级" value="4" />
          <el-option label="3 A级" value="3" />
        </el-select>

        <el-select v-model="filterType" placeholder="类型" style="width: 120px; margin-left: 10px" filterable>
          <el-option
            v-for="item in options"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>

        <el-select v-model="sortBy" placeholder="排序" style="width: 120px; margin-left: 10px">
          <el-option label="距离最近" value="distance" />
          <el-option label="评分最高" value="rating" />
          <el-option label="热度优先" value="popularity" />
        </el-select>
      </div>
    </el-card>

    <!-- 景点列表 -->
    <div class="attraction-list">
      <el-card
        v-for="attraction in attractions"
        :key="attraction.id"
        class="attraction-card"
        hover
        @click="router.push(`/attraction/detail/${attraction.id}`)"
      >
        <div class="card-content">
          <img :src="attraction.mainImage" :alt="attraction.name" class="attraction-image" />
          <div class="attraction-info">
            <h3 class="attraction-name">{{ attraction.name }}</h3>
            <div class="attraction-rating">
              <el-rate
                :model-value="attraction.rating"
                disabled
                show-score
                score-template="{value}"
              />
            </div>
            <div class="attraction-distance">{{ attraction.distance }} km</div>
            <div class="attraction-type tag-container">
              <el-tag v-for="tag in attraction.tags.split(',')" :key="tag" size="small" type="warning">
                {{ tag }}
              </el-tag>
            </div>
            <p class="attraction-desc">{{ attraction.description }}</p>
            <div class="attraction-address">
              <el-icon><Location /></el-icon> {{ attraction.address }}
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 底部加载状态 -->
    <div class="loading-state">
       <p v-if="loading">加载中...</p>
       <p v-if="noMore">没有更多了 (共 {{ totalAttractions }} 个结果)</p>
       <p v-else-if="!loading && attractions.length > 0">上滑加载更多 (当前 {{ attractions.length }} / 共 {{ totalAttractions }})</p>
    </div>

    <!-- 空状态 -->
    <div v-if="attractions.length === 0 && !loading" class="empty-state">
      <el-empty description="暂无景点数据" />
    </div>
  <el-backtop :right="100" :bottom="100" style="color:rgb(82, 233, 200);"/>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { Location } from '@element-plus/icons-vue'
import { getAttractionList } from '@/api/attraction'
import { useGeoStore } from '@/stores/geo'
import { useRoute } from 'vue-router'
import router from '@/router'

const route = useRoute()
const options = [
  { label: '不限', value: '' },
  { label: '历史古迹', value: '历史古迹' },
  { label: '自然景观', value: '自然景观' },
  { label: '文化场馆', value: '文化场馆' },
  { label: '宗教场所', value: '宗教场所' },
  { label: '主题公园', value: '主题公园' },
  { label: '城市地标', value: '城市地标' },
  { label: '休闲度假', value: '休闲度假' },
  { label: '购物街区', value: '购物街区' },
  { label: '美食街区', value: '美食街区' },
  { label: '红色旅游', value: '红色旅游' },
  { label: '户外探险', value: '户外探险' },
  { label: '动物园', value: '动物园' },
  { label: '植物园', value: '植物园' },
  { label: '峡谷', value: '峡谷' },
  { label: '溶洞', value: '溶洞' },
  { label: '温泉', value: '温泉' },
  { label: '商圈街巷', value: '商圈街巷' },
  { label: '亲子乐园', value: '亲子乐园' },
  { label: '极限运动', value: '极限运动' },
  { label: '江河湖泊', value: '江河湖泊' },
  { label: '名人故居', value: '名人故居' },
  { label: '瀑布', value: '瀑布' },
  { label: '综合度假区', value: '综合度假区' },
  { label: '漂流', value: '漂流' }
]

const geoStore = useGeoStore();

// 状态
const searchKeyword = ref('')
const filterCity = ref('')
const filterStars = ref('')
const filterType = ref('')
const sortBy = ref('distance')
const attractions = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(12)
const totalAttractions = ref(0)

const cityOptions = [
  { label: '不限', value: '' },
  { label: '北京', value: '北京' },
  { label: '上海', value: '上海' },
  { label: '广州', value: '广州' },
  { label: '深圳', value: '深圳' },
  { label: '杭州', value: '杭州' },
  { label: '成都', value: '成都' },
  { label: '西安', value: '西安' },
  { label: '南京', value: '南京' },
  { label: '重庆', value: '重庆' },
  { label: '武汉', value: '武汉' },
  { label: '苏州', value: '苏州' },
  { label: '厦门', value: '厦门' },
  { label: '青岛', value: '青岛' },
  { label: '大连', value: '大连' },
  { label: '天津', value: '天津' },
  { label: '长沙', value: '长沙' },
  { label: '昆明', value: '昆明' },
  { label: '三亚', value: '三亚' },
  { label: '张家界', value: '张家界' },
]

const noMore = computed(() => attractions.value.length >= totalAttractions.value && totalAttractions.value > 0)
const disabled = computed(() => loading.value || noMore.value)

// 核心数据加载函数
const loadAttractionsData = async (append = false) => {
  loading.value = true
  try {
    if (!geoStore.lat || !geoStore.lng) {
      await geoStore.getCityByBrowser();
    }
    
    const res = await getAttractionList({
      keyword: searchKeyword.value,
      city: filterCity.value,
      stars: filterStars.value,
      tags: filterType.value,
      sort: sortBy.value,
      offset: (currentPage.value - 1) * pageSize.value,
      size: pageSize.value,
      userLng: geoStore.lng,
      userLat: geoStore.lat
    })
    
    if (append) {
      attractions.value.push(...(res.data.data || []))
    } else {
      attractions.value = res.data.data || []
    }
    totalAttractions.value = res.data.total || 0
  } catch (err) {
    console.error('获取景点数据失败', err)
    if (!append) attractions.value = []
  } finally {
    loading.value = false
  }
}

// 搜索/重置 (替换列表)
const fetchAttractions = () => {
  currentPage.value = 1
  loadAttractionsData(false)
}

// 加载更多 (追加列表)
const loadMore = () => {
  console.log('尝试触发加载更多...', { loading: loading.value, noMore: noMore.value, current: attractions.value.length, total: totalAttractions.value })
  if (loading.value || noMore.value) return
  currentPage.value++
  loadAttractionsData(true)
}

// 筛选变化
const handleFilterChange = () => {
  fetchAttractions()
}

// 监听筛选条件变化
watch([filterCity, filterStars, filterType, sortBy], handleFilterChange)

// 初始化
onMounted(() => {
  // 从路由参数初始化筛选条件
  if (route.query.keyword) searchKeyword.value = route.query.keyword
  if (route.query.city) filterCity.value = route.query.city ? route.query.city : ''
  if (route.query.type) filterType.value = route.query.type ? route.query.type : ''
  if (route.query.stars) filterStars.value = route.query.stars ? route.query.stars : ''
  
  fetchAttractions()
})
</script>

<style scoped>
.attraction-result-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-container {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.attraction-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.attraction-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  transition: transform 0.3s, box-shadow 0.3s;
}

.attraction-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.card-content {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.attraction-image {
  width: 100%;
  height: 180px;
  object-fit: cover;
  border-radius: 8px 8px 0 0;
}

.attraction-info {
  padding: 14px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.attraction-name {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
}

.attraction-rating {
  margin-bottom: 6px;
}

.attraction-distance {
  color: #666;
  font-size: 14px;
  margin-bottom: 6px;
}

.attraction-type {
  margin-bottom: 8px;
}

.tag-container {
  display: flex;
  gap: 8px; 
  flex-wrap: wrap;
}

.attraction-desc {
  flex: 1;
  font-size: 14px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  margin-bottom: 8px;
}

.attraction-address {
  font-size: 12px;
  color: #999;
  display: flex;
  align-items: center;
}

.pagination-container {
  display: flex;
  justify-content: center;
}

.empty-state, .loading-state {
  text-align: center;
  padding: 50px 0;
}
</style>