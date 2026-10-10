<template>
  <div class="package-page" :class="{ 'package-page--comparing': comparedPackages.length }">
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

    <div class="discovery-bar" aria-label="按旅行灵感筛选">
      <div class="discovery-bar__intro">
        <span class="discovery-bar__eyebrow">PICK YOUR PACE</span>
        <span>这次想怎么出发？</span>
      </div>
      <div class="discovery-bar__options">
        <button
          v-for="idea in tripIdeas"
          :key="idea.key"
          type="button"
          class="idea-chip"
          :class="{ 'idea-chip--active': selectedIdea === idea.key }"
          :aria-pressed="selectedIdea === idea.key"
          @click="selectedIdea = selectedIdea === idea.key ? '' : idea.key"
        >
          <span class="idea-chip__mark" aria-hidden="true">{{ idea.mark }}</span>
          {{ idea.label }}
        </button>
      </div>
    </div>

    <div class="results-heading" aria-live="polite">
      <div><strong>{{ filteredPackages.length }}</strong> 个旅行提案<span v-if="selectedIdea"> · {{ activeIdeaLabel }}</span></div>
      <span>点选两款套餐，可并排看清差别</span>
    </div>

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
    <TransitionGroup v-else name="package-grid" tag="div" class="package-list">
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
          <button
            type="button"
            class="package-card__compare"
            :class="{ 'package-card__compare--selected': isCompared(pkg.id) }"
            :aria-pressed="isCompared(pkg.id)"
            :aria-label="`${isCompared(pkg.id) ? '移出' : '加入'}对比：${pkg.name}`"
            @click="toggleCompare(pkg.id)"
          >
            <span aria-hidden="true">{{ isCompared(pkg.id) ? '✓' : '+' }}</span>
            {{ isCompared(pkg.id) ? '已加入' : '加入对比' }}
          </button>
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
    </TransitionGroup>

    <!-- 空态 -->
    <div v-if="!firstLoading && filteredPackages.length === 0" class="empty-state">
      <el-empty :image-size="100" description="没有找到符合条件的套餐">
        <el-button @click="resetFilters">重置筛选条件</el-button>
      </el-empty>
    </div>

    <Transition name="compare-dock">
      <aside v-if="comparedPackages.length" class="compare-dock" aria-label="套餐对比栏">
        <div class="compare-dock__heading">
          <span class="compare-dock__eyebrow">TRIP COMPARE</span>
          <strong>把心动的旅程放在一起</strong>
          <small>{{ comparedPackages.length }}/2 已选择</small>
        </div>
        <div class="compare-dock__items">
          <div v-for="pkg in comparedPackages" :key="pkg.id" class="compare-dock__item">
            <img :src="pkg.image" :alt="pkg.name" />
            <span>{{ pkg.name }}</span>
            <button type="button" :aria-label="`移出对比：${pkg.name}`" @click="toggleCompare(pkg.id)">×</button>
          </div>
          <div v-if="comparedPackages.length === 1" class="compare-dock__placeholder">再选 1 款，就能对比</div>
        </div>
        <button type="button" class="compare-dock__action" :disabled="comparedPackages.length < 2" @click="compareVisible = true">查看对比 <span aria-hidden="true">↗</span></button>
      </aside>
    </Transition>

    <el-dialog v-model="compareVisible" title="两段旅程，怎么选？" width="min(760px, calc(100vw - 32px))" class="package-compare-dialog">
      <div class="compare-table" v-if="comparedPackages.length === 2">
        <div class="compare-table__label">旅行提案</div>
        <div v-for="pkg in comparedPackages" :key="`name-${pkg.id}`" class="compare-table__name">{{ pkg.name }}</div>
        <template v-for="row in compareRows" :key="row.label">
          <div class="compare-table__label">{{ row.label }}</div>
          <div v-for="pkg in comparedPackages" :key="`${row.label}-${pkg.id}`" class="compare-table__value">{{ row.value(pkg) }}</div>
        </template>
      </div>
      <p class="compare-note">当前为展示套餐，用于比较行程方向；价格与可订状态以正式上线数据为准。</p>
    </el-dialog>
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
const selectedIdea = ref('')
const comparedIds = ref([])
const compareVisible = ref(false)
const tripIdeas = [
  { key: 'weekend', label: '周末轻出发', mark: '↗' },
  { key: 'family', label: '带孩子去玩', mark: '✳' },
  { key: 'long', label: '慢慢走远方', mark: '◌' },
]
const activeIdeaLabel = computed(() => tripIdeas.find((idea) => idea.key === selectedIdea.value)?.label || '')
const comparedPackages = computed(() => comparedIds.value.map((id) => packages.value.find((pkg) => pkg.id === id)).filter(Boolean))
const compareRows = [
  { label: '起价 / 人', value: (pkg) => `¥${pkg.price.toLocaleString()}` },
  { label: '旅行时间', value: (pkg) => `${pkg.days} 天 ${pkg.nights} 晚` },
  { label: '旅行类型', value: (pkg) => pkg.type },
  { label: '评分', value: (pkg) => `${pkg.rating} / 5` },
  { label: '展示销量', value: (pkg) => `${pkg.sold.toLocaleString()} 份` },
  { label: '行程亮点', value: (pkg) => pkg.description },
]

const isCompared = (id) => comparedIds.value.includes(id)
const toggleCompare = (id) => {
  if (isCompared(id)) {
    comparedIds.value = comparedIds.value.filter((item) => item !== id)
    if (comparedIds.value.length < 2) compareVisible.value = false
    return
  }
  if (comparedIds.value.length === 2) {
    ElMessage.info('最多对比 2 款套餐，先移出一款再添加')
    return
  }
  comparedIds.value = [...comparedIds.value, id]
}

// 首屏骨架屏：数据是本地同步的，这里给一帧过渡，避免骨架屏一闪而过
const firstLoading = computed(() => loading.value)

const filteredPackages = computed(() => {
  const kw = searchKeyword.value.trim().toLowerCase()

  let list = packages.value.filter((pkg) => {
    if (filterType.value && pkg.type !== filterType.value) return false
    if (kw && !(`${pkg.name} ${pkg.description}`.toLowerCase().includes(kw))) return false
    if (selectedIdea.value === 'weekend' && pkg.days > 3) return false
    if (selectedIdea.value === 'family' && !/亲子|带娃|乐园|手作/.test(`${pkg.name} ${pkg.description}`)) return false
    if (selectedIdea.value === 'long' && pkg.days < 6) return false
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
  selectedIdea.value = ''
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

.package-page--comparing {
  padding-bottom: 164px;
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

.discovery-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: var(--sp-4);
  padding: var(--sp-4) var(--sp-5);
  margin: 0 0 var(--sp-6);
  border: 1px solid #dbe8e9;
  border-radius: var(--r-lg);
  background: linear-gradient(110deg, #edf7f6 0%, #f8fafc 58%, #fffaf0 100%);
}

.discovery-bar__intro {
  display: flex;
  flex-direction: column;
  gap: 2px;
  color: var(--c-ink);
  font-size: var(--fs-body-lg);
  font-weight: 600;
}

.discovery-bar__eyebrow,
.compare-dock__eyebrow {
  color: var(--c-travel-blue);
  font-family: var(--font-num);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: .16em;
}

.discovery-bar__options {
  display: flex;
  flex-wrap: wrap;
  gap: var(--sp-2);
}

.idea-chip {
  display: inline-flex;
  align-items: center;
  gap: var(--sp-2);
  min-height: 38px;
  padding: 0 var(--sp-3);
  border: 1px solid #d3e4e5;
  border-radius: var(--r-full);
  background: rgba(255, 255, 255, .84);
  color: var(--c-ink-2);
  font: inherit;
  cursor: pointer;
  transition: transform var(--dur-fast) var(--ease-out), background var(--dur-base) var(--ease-out), border-color var(--dur-base) var(--ease-out), color var(--dur-base) var(--ease-out);
}

.idea-chip:hover { transform: translateY(-2px); border-color: var(--c-travel-blue); }
.idea-chip:active { transform: scale(.97); }
.idea-chip--active { background: var(--c-travel-blue-deep); border-color: var(--c-travel-blue-deep); color: #fff; }
.idea-chip__mark { font-size: 18px; line-height: 1; }

.results-heading {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: var(--sp-3);
  margin: 0 0 var(--sp-3);
  color: var(--c-ink-3);
  font-size: var(--fs-caption);
}

.results-heading strong {
  color: var(--c-travel-blue-deep);
  font-family: var(--font-num);
  font-size: var(--fs-h2);
  font-weight: 700;
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
  border-color: #c8dce2;
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
  display: inline-flex;
  align-items: center;
  gap: 7px;
  background-color: rgba(255, 255, 255, 0.92);
  -webkit-backdrop-filter: blur(4px);
  backdrop-filter: blur(4px);
  color: var(--c-primary-600);
  font-size: var(--fs-caption);
  font-weight: 500;
  padding: var(--sp-1) var(--sp-2);
  border-radius: var(--r-full);
}

.package-card__days-tag::before {
  content: '';
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--c-travel-sun);
  box-shadow: 0 0 0 3px rgba(247, 200, 117, .18);
}

.package-card__compare {
  position: absolute;
  left: var(--sp-3);
  bottom: var(--sp-3);
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 34px;
  padding: 0 11px;
  border: 1px solid rgba(255, 255, 255, .76);
  border-radius: var(--r-full);
  background: rgba(15, 23, 42, .68);
  color: #fff;
  font: inherit;
  font-size: var(--fs-caption);
  cursor: pointer;
  -webkit-backdrop-filter: blur(8px);
  backdrop-filter: blur(8px);
  transition: background var(--dur-base) var(--ease-out), transform var(--dur-fast) var(--ease-out);
}

.package-card__compare:hover { background: var(--c-travel-blue-deep); }
.package-card__compare:active { transform: scale(.96); }
.package-card__compare--selected { background: var(--c-travel-blue-deep); }
.package-card__compare span { font-size: 18px; line-height: 1; }

.package-card__content {
  padding: var(--sp-4);
  background: linear-gradient(180deg, #fff 0%, #fffdf8 100%);
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
  position: relative;
  margin-top: var(--sp-4);
  padding-top: var(--sp-4);
  border-top: 1px dashed #c9dbe1;
}

.package-card__price-area::before,
.package-card__price-area::after {
  content: '';
  position: absolute;
  top: -10px;
  width: 18px;
  height: 18px;
  border: 1px solid #d7e2e5;
  border-radius: 50%;
  background: var(--c-bg);
}

.package-card__price-area::before { left: -26px; }
.package-card__price-area::after { right: -26px; }
.package-card:hover .package-card__price-area { border-color: #9dc6d0; }

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

.package-grid-move,
.package-grid-enter-active,
.package-grid-leave-active { transition: opacity var(--dur-slow) var(--ease-out), transform var(--dur-slow) var(--ease-out); }
.package-grid-enter-from,
.package-grid-leave-to { opacity: 0; transform: translateY(12px); }
.package-grid-leave-active { position: absolute; }

.compare-dock {
  position: fixed;
  z-index: 40;
  left: 50%;
  bottom: var(--sp-4);
  width: min(940px, calc(100vw - 32px));
  display: grid;
  grid-template-columns: 190px minmax(0, 1fr) auto;
  align-items: center;
  gap: var(--sp-4);
  padding: var(--sp-3) var(--sp-4);
  border: 1px solid #cee1e1;
  border-radius: var(--r-lg);
  background: rgba(255, 255, 255, .96);
  box-shadow: var(--sh-3);
  -webkit-backdrop-filter: blur(16px);
  backdrop-filter: blur(16px);
  transform: translateX(-50%);
}

.compare-dock-enter-active,
.compare-dock-leave-active { transition: opacity var(--dur-slow) var(--ease-out), transform var(--dur-slow) var(--ease-out); }
.compare-dock-enter-from,
.compare-dock-leave-to { opacity: 0; transform: translate(-50%, 20px); }

.compare-dock__heading { display: flex; flex-direction: column; gap: 2px; }
.compare-dock__heading strong { font-size: var(--fs-body); line-height: 1.3; }
.compare-dock__heading small { color: var(--c-ink-3); }
.compare-dock__items { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: var(--sp-2); min-width: 0; }
.compare-dock__item,
.compare-dock__placeholder { min-width: 0; min-height: 52px; border-radius: var(--r-sm); }
.compare-dock__item { display: flex; align-items: center; gap: var(--sp-2); padding: 5px; background: var(--c-bg-sub); }
.compare-dock__item img { width: 46px; height: 42px; flex: none; object-fit: cover; border-radius: 5px; }
.compare-dock__item span { min-width: 0; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: var(--fs-caption); }
.compare-dock__item button { flex: none; border: 0; background: none; color: var(--c-ink-3); font-size: 21px; cursor: pointer; }
.compare-dock__item button:hover { color: var(--c-danger); }
.compare-dock__placeholder { display: grid; place-items: center; border: 1px dashed #c8d7db; color: var(--c-ink-3); font-size: var(--fs-caption); }
.compare-dock__action { min-height: 44px; padding: 0 var(--sp-4); border: 0; border-radius: var(--r-sm); background: var(--c-travel-blue-deep); color: #fff; font: inherit; font-weight: 600; cursor: pointer; }
.compare-dock__action:hover:not(:disabled) { background: var(--c-travel-blue); }
.compare-dock__action:disabled { opacity: .48; cursor: not-allowed; }

.compare-table { display: grid; grid-template-columns: 112px repeat(2, minmax(0, 1fr)); border-top: 1px solid var(--c-line); border-left: 1px solid var(--c-line); }
.compare-table > div { min-width: 0; padding: var(--sp-3); border-right: 1px solid var(--c-line); border-bottom: 1px solid var(--c-line); line-height: 1.5; }
.compare-table__label { background: var(--c-bg-sub); color: var(--c-ink-3); font-size: var(--fs-caption); }
.compare-table__name { color: var(--c-travel-blue-deep); font-weight: 600; }
.compare-table__value { color: var(--c-ink-2); }
.compare-note { margin: var(--sp-3) 0 0; color: var(--c-ink-3); font-size: var(--fs-caption); }

@media (max-width: 1000px) {
  .package-page--comparing { padding-bottom: 210px; }
  .compare-dock { bottom: 104px; }
}

@media (max-width: 700px) {
  .package-page { padding: var(--sp-4); }
  .discovery-bar { padding: var(--sp-4); }
  .results-heading > span { display: none; }
  .compare-dock { grid-template-columns: minmax(0, 1fr) auto; gap: var(--sp-2); padding: var(--sp-2); }
  .compare-dock__heading { grid-column: 1 / -1; flex-direction: row; align-items: center; justify-content: space-between; padding: 0 var(--sp-1); }
  .compare-dock__eyebrow { display: none; }
  .compare-dock__heading strong { font-size: var(--fs-caption); }
  .compare-dock__items { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .compare-dock__item,
  .compare-dock__placeholder { min-height: 42px; }
  .compare-dock__item img { display: none; }
  .compare-dock__action { align-self: end; }
  .compare-table { grid-template-columns: 76px repeat(2, minmax(0, 1fr)); font-size: var(--fs-caption); }
  .compare-table > div { padding: var(--sp-2); }
}

@media (prefers-reduced-motion: reduce) {
  .package-grid-move,
  .package-grid-enter-active,
  .package-grid-leave-active,
  .compare-dock-enter-active,
  .compare-dock-leave-active { transition: none; }
}
</style>
