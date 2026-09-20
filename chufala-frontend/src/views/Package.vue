<template>
  <div class="attraction-result-page">
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

        <el-select v-model="filterRating" placeholder="评分" style="width: 120px; margin-left: 10px">
          <el-option label="不限" value="" />
          <el-option label="4.5分以上" value="4.5" />
          <el-option label="4分以上" value="4" />
          <el-option label="3.5分以上" value="3.5" />
        </el-select>

        <el-select v-model="filterType" placeholder="类型" style="width: 120px; margin-left: 10px">
          <el-option label="不限" value="" />
          <el-option label="自然景观" value="nature" />
          <el-option label="人文古迹" value="culture" />
          <el-option label="主题乐园" value="park" />
          <el-option label="美食" value="food" />
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
      >
        <div class="card-content">
          <img :src="attraction.image" :alt="attraction.name" class="attraction-image" />
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
            <div class="attraction-type">
              <el-tag>{{ attraction.type }}</el-tag>
            </div>
            <p class="attraction-desc">{{ attraction.description }}</p>
            <div class="attraction-address">
              <el-icon><Location /></el-icon> {{ attraction.address }}
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 空状态 -->
    <div v-if="attractions.length === 0 && !loading" class="empty-state">
      <el-empty description="暂无旅游套餐" />
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <el-loading :visible="loading" text="正在加载景点数据..." />
    </div>

    <!-- 分页 -->
    <div v-if="attractions.length > 0" class="pagination-container">
      <el-pagination
        background
        :current-page="currentPage"
        :page-size="pageSize"
        :total="totalAttractions"
        layout="prev, pager, next, jumper"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted,watch } from 'vue'
import { Location } from '@element-plus/icons-vue'
import axios from 'axios'

// 状态
const searchKeyword = ref('')
const filterRating = ref('')
const filterType = ref('')
const sortBy = ref('distance')
const attractions = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(8)
const totalAttractions = ref(0)

// 获取景点数据
const fetchAttractions = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/attractions', {
      params: {
        keyword: searchKeyword.value,
        rating: filterRating.value,
        type: filterType.value,
        sort: sortBy.value,
        page: currentPage.value,
        size: pageSize.value
      }
    })
    attractions.value = res.data.list
    totalAttractions.value = res.data.total
  } catch (err) {
    console.error('获取景点数据失败', err)
    attractions.value = []
    totalAttractions.value = 0
  } finally {
    loading.value = false
  }
}

// 分页变化
const handlePageChange = (page) => {
  currentPage.value = page
  fetchAttractions()
}

// 筛选变化
const handleFilterChange = () => {
  currentPage.value = 1 // 重置到第一页
  fetchAttractions()
}

// 监听筛选条件变化
watch([filterRating, filterType, sortBy], handleFilterChange)

// 初始化
onMounted(() => {
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