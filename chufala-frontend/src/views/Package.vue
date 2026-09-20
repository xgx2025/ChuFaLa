<template>
  <div class="package-page">
    <!-- 页面标题 -->
    <h1 class="page-title">旅游套餐</h1>

    <!-- 搜索和筛选区 -->
    <el-card class="filter-card" shadow="never">
      <div class="filter-container">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索套餐名称 / 目的地"
          class="filter-container__search"
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <el-select v-model="filterType" placeholder="类型" class="filter-container__select">
          <el-option label="全部类型" value="" />
          <el-option label="国内游" value="国内游" />
          <el-option label="国外游" value="国外游" />
          <el-option label="周边游" value="周边游" />
        </el-select>

        <el-select v-model="sortBy" placeholder="排序" class="filter-container__select">
          <el-option label="综合排序" value="default" />
          <el-option label="价格从低到高" value="price-asc" />
          <el-option label="价格从高到低" value="price-desc" />
          <el-option label="评分最高" value="rating" />
          <el-option label="销量最高" value="sold" />
        </el-select>
      </div>
    </el-card>

    <!-- 首次加载：骨架屏 -->
    <div v-if="firstLoading" class="package-list">
      <el-card v-for="n in 6" :key="n" class="package-card" shadow="never">
        <el-skeleton animated>
          <template #template>
            <el-skeleton-item variant="image" style="width: 100%; height: 180px" />
            <div style="padding: 16px">
              <el-skeleton-item variant="h3" style="width: 70%" />
              <el-skeleton-item variant="text" style="margin-top: 12px" />
              <el-skeleton-item variant="text" style="margin-top: 8px; width: 80%" />
            </div>
          </template>
        </el-skeleton>
      </el-card>
    </div>

    <!-- 套餐列表 -->
    <div v-else class="package-list">
      <el-card
        v-for="pkg in filteredPackages"
        :key="pkg.id"
        class="package-card"
        shadow="hover"
        :body-style="{ padding: '0px' }"
      >
        <div class="package-card__image-container">
          <img
            v-lazy-img
            loading="lazy"
            :src="pkg.image"
            :alt="`${pkg.name} 的行程风景`"
            class="package-card__image"
          />
          <div class="package-card__days-tag">{{ pkg.days }}天{{ pkg.nights }}晚</div>
        </div>

        <div class="package-card__content">
          <div class="package-card__header">
            <span class="package-card__type-tag">{{ pkg.type }}</span>
            <div class="package-card__rating">
              <el-icon class="package-card__star"><StarFilled /></el-icon>
              <span class="package-card__rating-text">{{ pkg.rating }}</span>
            </div>
          </div>

          <h3 class="package-card__name">{{ pkg.name }}</h3>
          <p class="package-card__desc">{{ pkg.description }}</p>

          <div class="package-card__sales">
            <el-icon class="package-card__sales-icon"><UserFilled /></el-icon>
            <span>已售 {{ pkg.sold }} 份</span>
          </div>

          <div class="package-card__price-area">
            <div>
              <span class="package-card__price">¥{{ pkg.price }}</span>
              <span class="package-card__price-unit">起 / 人</span>
            </div>
            <el-button type="primary" size="small" @click="handleBook(pkg)">查看详情</el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 空态 -->
    <div v-if="!firstLoading && filteredPackages.length === 0" class="empty-state">
      <el-empty :image-size="100" description="没有找到符合条件的套餐">
        <el-button @click="resetFilters">重置筛选条件</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, StarFilled, UserFilled } from '@element-plus/icons-vue'
import router from '@/router'

/**
 * 说明：后端目前**没有**旅游套餐接口。
 * 已有的 Controller 只有 agent / attraction / auth / captcha / hotels /
 * hotelOrders / membership / pay / rag / rooms / user。
 *
 * 本页原先请求的是 `/api/attractions`（景点接口），并且用的是裸 axios：
 *   - 页面标题写"旅游套餐"，数据却是景点，语义错位
 *   - 裸 axios 不走 @/utils/request，没有 Authorization 头，必然 401
 *   - 裸 axios 的 res.data 是后端统一包装 Result{code,message,data}，
 *     代码却按 res.data.list 取值 → 恒为 undefined，列表永远是空的
 *
 * 因此这里改为使用本地数据（与首页 Home.vue 的 packages 保持同一结构），
 * 让页面在语义与功能上都自洽。
 *
 * TODO: 后端补上 /packages 系列接口后，把 packages 换成接口请求即可，
 *       模板与筛选逻辑都不需要动。
 */
const packages = ref([
  { id: 1, name: '云南昆明-大理-丽江 7日游', description: '玉龙雪山 + 洱海骑行 + 古城漫步，含机票酒店', price: 3599, days: 7, nights: 6, type: '国内游', rating: 4.7, sold: 1256, image: 'https://picsum.photos/600/400?random=13' },
  { id: 2, name: '泰国普吉岛 5日游', description: '皇帝岛浮潜 + 人妖秀 + 海鲜大餐，含接送机', price: 2899, days: 5, nights: 4, type: '国外游', rating: 4.6, sold: 987, image: 'https://picsum.photos/600/400?random=14' },
  { id: 3, name: '上海迪士尼亲子 2日游', description: '乐园门票 + 酒店住宿 + 快速通道，适合带娃家庭', price: 1699, days: 2, nights: 1, type: '周边游', rating: 4.8, sold: 2103, image: 'https://picsum.photos/600/400?random=15' },
  { id: 4, name: '新疆喀纳斯深度 8日游', description: '禾木村 + 五彩滩 + 魔鬼城，全程越野车', price: 5299, days: 8, nights: 7, type: '国内游', rating: 4.9, sold: 642, image: 'https://picsum.photos/600/400?random=16' },
  { id: 5, name: '日本关西赏樱 6日游', description: '京都 + 大阪 + 奈良，含 JR 周游券', price: 6899, days: 6, nights: 5, type: '国外游', rating: 4.8, sold: 431, image: 'https://picsum.photos/600/400?random=17' },
  { id: 6, name: '莫干山避暑 3日游', description: '民宿 + 竹林徒步 + 亲子手作，周末可出发', price: 1299, days: 3, nights: 2, type: '周边游', rating: 4.5, sold: 1874, image: 'https://picsum.photos/600/400?random=18' },
  { id: 7, name: '西藏拉萨-林芝 9日游', description: '布达拉宫 + 巴松措 + 雅鲁藏布大峡谷', price: 7999, days: 9, nights: 8, type: '国内游', rating: 4.9, sold: 288, image: 'https://picsum.photos/600/400?random=19' },
  { id: 8, name: '马尔代夫蜜月 6日游', description: '水上屋 + 浮潜 + 蜜月布置，一价全含', price: 12999, days: 6, nights: 5, type: '国外游', rating: 4.9, sold: 176, image: 'https://picsum.photos/600/400?random=20' },
  { id: 9, name: '千岛湖骑行 2日游', description: '环湖骑行 + 皮划艇 + 鱼头宴，含装备', price: 899, days: 2, nights: 1, type: '周边游', rating: 4.4, sold: 2560, image: 'https://picsum.photos/600/400?random=21' },
])

const searchKeyword = ref('')
const filterType = ref('')
const sortBy = ref('default')
const loading = ref(false)

// 首屏骨架屏：数据是本地同步的，这里给一帧过渡，避免骨架屏一闪而过
const firstLoading = computed(() => loading.value)

const filteredPackages = computed(() => {
  const kw = searchKeyword.value.trim().toLowerCase()

  let list = packages.value.filter((pkg) => {
    if (filterType.value && pkg.type !== filterType.value) return false
    if (kw && !(`${pkg.name} ${pkg.description}`.toLowerCase().includes(kw))) return false
    return true
  })

  const sorters = {
    'price-asc': (a, b) => a.price - b.price,
    'price-desc': (a, b) => b.price - a.price,
    rating: (a, b) => b.rating - a.rating,
    sold: (a, b) => b.sold - a.sold,
  }
  const sorter = sorters[sortBy.value]
  // 不直接改原数组，避免排序污染后续筛选
  return sorter ? [...list].sort(sorter) : list
})

const resetFilters = () => {
  searchKeyword.value = ''
  filterType.value = ''
  sortBy.value = 'default'
}

const handleBook = (pkg) => {
  ElMessage.info(`「${pkg.name}」详情页尚未开发`)
}

onMounted(() => {
  // 模拟一次请求，让骨架屏有机会展示
  loading.value = true
  window.setTimeout(() => {
    loading.value = false
  }, 220)
})
</script>

<style scoped>
.package-page {
  max-width: var(--container-max);
  margin: 0 auto;
  padding: var(--sp-5);
}

.filter-card {
  margin-bottom: var(--sp-5);
}

.filter-container {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--sp-3);
}

.filter-container__search {
  width: 300px;
}

.filter-container__select {
  width: 150px;
}

.package-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: var(--sp-5);
  margin-bottom: var(--sp-8);
}

.package-card {
  border-radius: var(--r-lg);
  overflow: hidden;
  border: 1px solid var(--c-line);
  will-change: transform;
  backface-visibility: hidden;
  transition: transform var(--dur-base) var(--ease-out),
    box-shadow var(--dur-base) var(--ease-out),
    border-color var(--dur-base) var(--ease-out);
}

.package-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--sh-hover);
  border-color: transparent;
}

.package-card__image-container {
  position: relative;
  height: 180px;
  overflow: hidden;
}

.package-card__image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--dur-slower) var(--ease-out);
}

.package-card:hover .package-card__image {
  transform: scale(1.06);
}

.package-card__days-tag {
  position: absolute;
  top: var(--sp-3);
  right: var(--sp-3);
  background-color: rgba(255, 255, 255, 0.92);
  -webkit-backdrop-filter: blur(4px);
  backdrop-filter: blur(4px);
  color: var(--c-primary-600);
  font-size: var(--fs-caption);
  font-weight: 500;
  padding: var(--sp-1) var(--sp-2);
  border-radius: var(--r-full);
}

.package-card__content {
  padding: var(--sp-4);
}

.package-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--sp-2);
}

.package-card__type-tag {
  background-color: var(--c-primary-50);
  color: var(--c-primary-600);
  font-size: var(--fs-caption);
  padding: var(--sp-1) var(--sp-2);
  border-radius: var(--r-xs);
}

.package-card__rating {
  display: flex;
  align-items: center;
  gap: var(--sp-1);
  color: var(--c-star);
  font-size: var(--fs-caption);
}

.package-card__rating-text {
  color: var(--c-ink-3);
}

.package-card__name {
  margin: 0 0 var(--sp-2);
  font-size: var(--fs-body-lg);
  line-height: 1.4;
  font-weight: 600;
  color: var(--c-ink);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color var(--dur-base) var(--ease-out);
}

.package-card:hover .package-card__name {
  color: var(--c-primary-600);
}

.package-card__desc {
  margin: 0 0 var(--sp-3);
  font-size: var(--fs-caption);
  line-height: var(--lh-body);
  color: var(--c-ink-3);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.package-card__sales {
  display: flex;
  align-items: center;
  gap: var(--sp-1);
  margin-bottom: var(--sp-4);
  font-size: var(--fs-caption);
  color: var(--c-ink-3);
}

.package-card__price-area {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--sp-3);
}

.package-card__price {
  color: var(--c-danger);
  font-family: var(--font-num);
  font-variant-numeric: tabular-nums;
  font-weight: 600;
  font-size: var(--fs-h3);
}

.package-card__price-unit {
  margin-left: var(--sp-1);
  font-size: var(--fs-caption);
  color: var(--c-ink-3);
}

.empty-state {
  padding: var(--sp-12) 0;
  text-align: center;
}
</style>
