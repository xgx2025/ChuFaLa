<template>
  <div class="root-container">
    <main class="main-content">
      <!-- 英雄区域 -->
      <section class="hero-section">
        <!-- 轮播图 -->
        <div class="hero-section__slides">
          <div 
            class="hero-slide"
            :class="currentSlideIndex === index ? 'hero-slide--active' : ''"
            :style="{ '--slide-image': `url(${slide.image})` }"
            v-for="(slide, index) in slides" 
            :key="index"
          ></div>
        </div>
        
        <!-- 渐变遮罩 -->
        <div class="hero-section__overlay"></div>
        
        <!-- 搜索框 + 标题 -->
        <div class="hero-section__content">
          <div class="hero-section__text">
            <h1 class="hero-section__title">发现完美住宿</h1>
            <p class="hero-section__desc">从豪华酒店到特色民宿，为您的旅程找到理想下榻之处</p>
            
            <!-- 酒店搜索框 -->
            <form class="search-form" @submit.prevent="handleSearch">
              <div class="search-form__group">
                <el-icon class="search-form__icon"><Location /></el-icon>
                <div class="search-input-wrapper">
                  <p class="search-form__label">目的地</p>
                  <el-input 
                    v-model="searchForm.destination" 
                    placeholder="您要去哪里" 
                    class="custom-input"
                    clearable
                  />
                </div>
              </div>
              
              <div class="search-form__group">
                <el-icon class="search-form__icon"><Calendar /></el-icon>
                <div class="search-input-wrapper">
                  <p class="search-form__label">日期</p>
                  <el-date-picker
                    v-model="searchForm.dates"
                    type="daterange"
                    range-separator="-"
                    start-placeholder="入住"
                    end-placeholder="退房"
                    format="MM/DD"
                    value-format="YYYY-MM-DD"
                    class="custom-date-picker"
                    :prefix-icon="null"
                    style="width: 100%;"
                  />
                </div>
              </div>
              
              <div class="search-form__group">
                <el-icon class="search-form__icon"><User /></el-icon>
                <div class="search-input-wrapper">
                  <p class="search-form__label">客人</p>
                  <el-popover placement="bottom" :width="200" trigger="click">
                    <template #reference>
                      <div class="guest-selector">
                        {{ searchForm.guests }} 人入住
                      </div>
                    </template>
                    <div class="guest-counter">
                      <span>人数</span>
                      <el-input-number v-model="searchForm.guests" :min="1" :max="10" size="small" />
                    </div>
                  </el-popover>
                </div>
              </div>
              
              <button type="button" class="search-form__btn" @click="handleSearch">
                <el-icon class="search-form__btn-icon"><Search /></el-icon>
                <span>搜索酒店</span>
              </button>
            </form>
          </div>
        </div>
        
        <!-- 轮播控制 -->
        <div class="hero-section__controls">
          <button 
            class="hero-control__dot"
            :class="currentSlideIndex === i-1 ? 'hero-control__dot--active' : ''"
            v-for="i in slides.length" 
            :key="i"
            @click="goToSlide(i-1)"
          ></button>
        </div>
      </section>

      <!-- 酒店筛选 -->
      <section class="filters-section">
        <div class="section-container">
          <div class="filters-container">
            <div class="filter-group">
              <span class="filter-label">价格范围</span>
              <div class="filter-options">
                <button class="filter-btn" :class="{ 'filter-btn--active': priceFilter === 'all' }" @click="priceFilter = 'all'">全部价格</button>
                <button class="filter-btn" :class="{ 'filter-btn--active': priceFilter === '0-500' }" @click="priceFilter = '0-500'">¥500以下</button>
                <button class="filter-btn" :class="{ 'filter-btn--active': priceFilter === '500-1000' }" @click="priceFilter = '500-1000'">¥500-1000</button>
                <button class="filter-btn" :class="{ 'filter-btn--active': priceFilter === '1000-2000' }" @click="priceFilter = '1000-2000'">¥1000-2000</button>
                <button class="filter-btn" :class="{ 'filter-btn--active': priceFilter === '2000+' }" @click="priceFilter = '2000+'">¥2000以上</button>
              </div>
            </div>
            
            <div class="filter-group">
              <span class="filter-label">星级</span>
              <div class="filter-options">
                <button class="filter-btn" :class="{ 'filter-btn--active': starFilter === 'all' }" @click="starFilter = 'all'">全部</button>
                <button class="filter-btn" :class="{ 'filter-btn--active': starFilter === '5' }" @click="starFilter = '5'">五星级</button>
                <button class="filter-btn" :class="{ 'filter-btn--active': starFilter === '4' }" @click="starFilter = '4'">四星级</button>
                <button class="filter-btn" :class="{ 'filter-btn--active': starFilter === '3' }" @click="starFilter = '3'">三星级</button>
                <button class="filter-btn" :class="{ 'filter-btn--active': starFilter === 'unrated' }" @click="starFilter = 'unrated'">未评级</button>
              </div>
            </div>
            
            <div class="filter-group">
              <span class="filter-label">设施</span>
              <div class="filter-options">
                <button class="filter-btn" :class="{ 'filter-btn--active': facilityFilters.pool }" @click="facilityFilters.pool = !facilityFilters.pool">游泳池</button>
                <button class="filter-btn" :class="{ 'filter-btn--active': facilityFilters.wifi }" @click="facilityFilters.wifi = !facilityFilters.wifi">免费WiFi</button>
                <button class="filter-btn" :class="{ 'filter-btn--active': facilityFilters.parking }" @click="facilityFilters.parking = !facilityFilters.parking">免费停车</button>
                <button class="filter-btn" :class="{ 'filter-btn--active': facilityFilters.spa }" @click="facilityFilters.spa = !facilityFilters.spa">SPA</button>
                <button class="filter-btn" :class="{ 'filter-btn--active': facilityFilters.breakfast }" @click="facilityFilters.breakfast = !facilityFilters.breakfast">含早餐</button>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 热门酒店 -->
      <section class="hotels-section">
        <div class="section-container">
          <div class="section-header">
            <h2 class="section-title">热门酒店</h2>
            <div class="sort-options">
              <span class="sort-label">排序方式:</span>
              <select class="sort-select" v-model="sortOption">
                <option value="recommended">推荐</option>
                <option value="price-asc">价格从低到高</option>
                <option value="price-desc">价格从高到低</option>
                <option value="rating">评分最高</option>
                <option value="distance">距离最近</option>
              </select>
            </div>
          </div>
          
          <!-- 注意：这里是内部滚动容器（max-height + overflow-y），
               其子元素被裁剪后 IntersectionObserver 不会判定为"进入视口"，
               因此本容器内的卡片一律不加 v-reveal。 -->
          <div class="hotels-list" ref="hotelsListRef" @scroll="handleScroll">
            <!-- 首屏骨架屏（原实现首屏是空白，数据回来才"啪"地出现） -->
            <template v-if="firstLoading">
              <div class="hotel-card" v-for="n in 4" :key="'sk' + n">
                <el-skeleton animated>
                  <template #template>
                    <el-skeleton-item variant="image" style="width: 100%; height: 200px" />
                    <div style="padding: 16px">
                      <el-skeleton-item variant="h3" style="width: 55%" />
                      <el-skeleton-item variant="text" style="margin-top: 12px; width: 70%" />
                      <el-skeleton-item variant="text" style="margin-top: 12px" />
                      <el-skeleton-item variant="text" style="margin-top: 8px; width: 60%" />
                    </div>
                  </template>
                </el-skeleton>
              </div>
            </template>

            <!-- 无结果（原实现完全没有空态，筛不出结果时是一片空白） -->
            <div v-else-if="filteredHotels.length === 0" class="hotels-empty">
              <el-empty :image-size="100" description="没有找到符合条件的酒店">
                <el-button @click="resetFilters">重置筛选条件</el-button>
              </el-empty>
            </div>

            <template v-else>
            <div 
              class="hotel-card"
              v-for="(hotel, index) in filteredHotels" 
              :key="hotel.id"
            >
              <div class="hotel-card__image-container">
                <img 
                  v-lazy-img
                  loading="lazy"
                  :src="hotel.mainImage" 
                  :alt="`${hotel.name}酒店`" 
                  class="hotel-card__image"
                >
                <div v-if="hotel.promotion" class="hotel-card__promotion">{{ hotel.promotion }}</div>
              </div>
              
              <div class="hotel-card__content">
                <div class="hotel-card__header">
                    <h3 class="hotel-card__name">{{ hotel.name }}</h3>
                      <el-rate class="hotel-card__star" v-model="hotel.stars" disabled fill-icon="StarFilled" void-icon="Star" />
                </div>
                
                <div class="hotel-card__location">
                  <el-icon class="hotel-card__location-icon"><Location /></el-icon>
                  <span>{{ hotel.location }}</span>
                  <span class="hotel-card__distance">{{ hotel.distance }}公里</span>
                </div>
                
                <div class="hotel-card__facilities">
                  <span class="hotel-card__facility" v-for="(facility, i) in hotel.facilities.slice(0, 3)" :key="i">{{ facility }}</span>
                  <span v-if="hotel.facilities.length > 3" class="hotel-card__more-facilities">+{{ hotel.facilities.length - 3 }}个设施</span>
                </div>
                
                <div class="hotel-card__rating">
                  <div class="rating-score">{{ hotel.overallRating }}</div>
                  <div class="rating-reviews">{{ hotel.reviewCount }}条评价</div>
                  <div class="rating-tag">{{ hotel.ratingTag }}</div>
                </div>
                
                <div class="hotel-card__price-area">
                  <div>
                    <span class="hotel-card__price">¥{{ hotel.price }}</span>
                    <span class="hotel-card__price-unit">/晚起</span>
                    <span v-if="hotel.originalPrice" class="hotel-card__original-price">¥{{ hotel.originalPrice }}</span>
                  </div>
                  <button class="hotel-card__btn" @click="router.push(`/hotel/detail/${hotel.id}`)">查看详情</button>
                </div>
              </div>
            </div>
            </template>
          </div>
          <!-- 2.加载状态提示 -->
          <div class="loading-status" v-if="loadingStatus !== 'none'">
            <span v-if="loadingStatus === 'loading'">加载中...</span>
            <span v-if="loadingStatus === 'no-more'">已加载全部酒店</span>
            <span v-if="loadingStatus === 'error'" @click="loadMore">加载失败，点击重试</span>
          </div>
        </div>
      </section>

      <!-- 酒店类型推荐 -->
      <section class="hotel-types-section">
        <div class="section-container">
          <div class="section-header">
            <h2 class="section-title">按类型查找酒店</h2>
          </div>
          
          <div class="hotel-types-list">
            <div 
              class="hotel-type-card"
              v-for="(type, index) in hotelTypes" 
              :key="index"
            >
              <img 
                :src="type.image" 
                :alt="`图片展示的是${type.name}类型的酒店`" 
                class="hotel-type-card__image"
              >
              <div class="hotel-type-card__overlay"></div>
              <div class="hotel-type-card__content">
                <h3 class="hotel-type-card__name">{{ type.name }}</h3>
                <p class="hotel-type-card__count">{{ type.count }}家酒店</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 酒店特惠 -->
      <section class="hotel-deals-section">
        <div class="section-container">
          <div class="section-header">
            <h2 class="section-title">酒店特惠</h2>
            <a href="#" class="section-more">
              更多优惠
              <el-icon class="section-more__icon"><ArrowRight /></el-icon>
            </a>
          </div>
          
          <div class="deals-container">
            <div class="deals-scroll">
              <div class="deals-list">
                <div 
                  class="deal-card"
                  v-for="(deal, index) in hotelDeals" 
                  :key="index"
                >
                  <div class="deal-card__image-container">
                    <img 
                      :src="deal.image" 
                      :alt="`图片展示的是${deal.name}酒店特惠`" 
                      class="deal-card__image"
                    >
                    <div class="deal-card__discount">{{ deal.discount }}</div>
                  </div>
                  
                  <div class="deal-card__content">
                    <h3 class="deal-card__name">{{ deal.name }}</h3>
                    <p class="deal-card__desc">{{ deal.description }}</p>
                    
                    <div class="deal-card__price-area">
                      <div>
                        <span class="deal-card__price">¥{{ deal.price }}</span>
                        <span class="deal-card__original-price">¥{{ deal.originalPrice }}</span>
                      </div>
                      <button class="deal-card__btn">立即预订</button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 滚动指示器 -->
            <div class="deals-indicators">
              <span class="deal-indicator deal-indicator--active"></span>
              <span class="deal-indicator"></span>
              <span class="deal-indicator"></span>
            </div>
          </div>
        </div>
      </section>

      <!-- 酒店评价 -->
      <section class="reviews-section">
        <div class="section-container">
          <div class="section-header">
            <h2 class="section-title">宾客真实评价</h2>
            <a href="#" class="section-more">
              查看全部
              <el-icon class="section-more__icon"><ArrowRight /></el-icon>
            </a>
          </div>
          
          <div class="reviews-list">
            <div 
              class="review-card"
              v-for="(review, index) in hotelReviews" 
              :key="index"
            >
              <div class="review-card__user">
                <img 
                  :src="review.avatar" 
                  :alt="`图片展示的是${review.name}的头像`" 
                  class="review-card__avatar"
                >
                <div class="review-card__user-info">
                  <h4 class="review-card__user-name">{{ review.name }}</h4>
                  <div class="review-card__stars">
                    <el-icon v-for="i in 5" :key="i" :class="{ 'review-card__star--empty': i > review.rating }"><StarFilled /></el-icon>
                  </div>
                </div>
              </div>
              
              <h4 class="review-card__hotel-name">入住酒店：{{ review.hotelName }}</h4>
              
              <p class="review-card__content">"{{ review.content }}"</p>
              
              <div class="review-card__images">
                <img 
                  :src="img" 
                  :alt="`酒店评价图片${i+1}`" 
                  class="review-card__image"
                  v-for="(img, i) in review.images" 
                  :key="i"
                >
              </div>
              
              <div class="review-card__footer">
                <span class="review-card__date">{{ review.date }}</span>
                <div class="review-card__actions">
                  <button class="review-card__action-btn"><el-icon><Pointer /></el-icon> 有用 ({{ review.useful }})</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 订阅区域 -->
      <section class="subscribe-section">
        <div class="subscribe-section__decor">
          <el-icon class="decor-icon--plane"><Plane /></el-icon>
          <el-icon class="decor-icon--sun"><Sunny /></el-icon>
          <el-icon class="decor-icon--ship"><Compass /></el-icon>
        </div>
        
        <div class="subscribe-section__content">
          <h2 class="subscribe-section__title">获取酒店独家优惠</h2>
          <p class="subscribe-section__desc">订阅我们的邮件，第一时间获取酒店特价和限时优惠</p>
          
          <form class="subscribe-form">
            <input 
              type="email" 
              placeholder="请输入您的邮箱地址" 
              class="subscribe-form__input"
            >
            <button type="button" class="subscribe-form__btn">立即订阅</button>
          </form>
          
          <p class="subscribe-section__privacy">
            我们尊重您的隐私，不会向第三方分享您的信息
          </p>
        </div>
      </section>
    </main>

    <!-- el-backtop 必须放在根节点**内部**，否则组件渲染成 Fragment 根，
         Layout.vue 的 <transition mode="out-in"> 无法对其执行 leave 过渡，
         离开本页后主内容区会永久空白（必须刷新才恢复）。 -->
    <el-backtop :right="100" :bottom="100" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted,watch} from 'vue';
import { getHotelListService } from '@/api/hotel';
import { useGeoStore } from '@/stores/geo';
import router from '@/router';
import { Plane } from '@/components/Icon.vue';
// 图标统一使用 Element Plus 图标集（项目已有依赖）。
// 原模板使用的是 FontAwesome 类名（fa fa-*），但项目并未引入 FontAwesome，
// 这些 <i> 元素全部渲染为空，导致搜索框图标、星级、箭头等一律不可见。
import { Location, Calendar, User, Search, ArrowRight, StarFilled, Pointer, Sunny, Compass } from '@element-plus/icons-vue';


const geoStore = useGeoStore();

// 搜索表单数据
const searchForm = ref({
  destination: '',
  dates: [],
  guests: 1
});

const handleSearch = () => {
  console.log('搜索条件:', searchForm.value);
  // TODO: 实现搜索跳转逻辑
};

// 1. 新增：滚动加载核心变量
const hotelsListRef = ref(null); // 滚动容器的ref（绑定.hotels-list）
const page = ref(1); // 当前页码（初始第1页）
const pageSize = ref(8); // 每页加载8条（首屏友好）
const hasMore = ref(true); // 是否有下一页数据
const isLoading = ref(false); // 加载锁（防止重复请求）
const loadingStatus = ref('none'); // 加载状态：none/loading/error/no-more
const loadedHotels = ref([]); // 已加载的酒店数据（分页追加）


// 导航栏滚动效果
const scrolled = ref(false);
const handleNavScroll  = () => {
  scrolled.value = window.scrollY > 50;
};

// 轮播图
const slides = ref([
  { image: 'https://images.unsplash.com/photo-1566073771259-6a8506099945?q=80&w=1920&auto=format&fit=crop' },
  { image: 'https://images.unsplash.com/photo-1582719508461-905c673771fd?q=80&w=1920&auto=format&fit=crop' },
  { image: 'https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?q=80&w=1920&auto=format&fit=crop' }
]);
const currentSlideIndex = ref(0);

const goToSlide = (index) => {
  currentSlideIndex.value = index;
};

let slideInterval;
const startSlideInterval = () => {
  slideInterval = setInterval(() => {
    currentSlideIndex.value = (currentSlideIndex.value + 1) % slides.value.length;
  }, 5000);
};

// 酒店筛选条件
const priceFilter = ref('all');
const starFilter = ref('all');
const facilityFilters = ref({
  pool: false,
  wifi: false,
  parking: false,
  spa: false,
  breakfast: false
});
const sortOption = ref('recommended');

const fetchHotels = async (pageNum, pageSizeNum) => {
  // 检查地理信息是否存在，若不存在则重新获取
  if (!geoStore.lat || !geoStore.lng) {
    await geoStore.getCityByBrowser();
  }
  
  // 1. 构造后端需要的查询参数（核心：映射筛选条件）
  const params = {
    page: pageNum,
    size: pageSizeNum,
    // 星级参数映射
    stars: starFilter.value === 'all' ? undefined 
      : starFilter.value === 'unrated' ? 0 
      : Number(starFilter.value),
    // 价格参数映射
    minPrice: undefined,
    maxPrice: undefined,
    userLng: geoStore.lng,
    userLat: geoStore.lat
  };

  // 处理价格范围
  if (priceFilter.value !== 'all') {
    const [min, max] = priceFilter.value.split('-').map(Number);
    params.minPrice = min;
    params.maxPrice = max || undefined; // "2000+" 时max为undefined
  }

  const activeFacilities = Object.entries(facilityFilters.value)
  .filter(([_, isChecked]) => isChecked) // 筛选选中的设施
  .map(([key]) => {
    const facilityMap = {
      pool: '游泳池',
      wifi: '免费WiFi',
      parking: '免费停车',
      spa: 'SPA',
      breakfast: '含早餐'
    };
    return facilityMap[key];
  });

  // 若有选中的设施，添加到参数中
  if (activeFacilities.length > 0) {
    params.facilities = activeFacilities; 
  }
  // 2. 调用分页查询接口
  const response = await getHotelListService(params);
  const rawHotels = Array.isArray(response.data.data) ? response.data.data : [];
  
  // 处理 facilities 字段（字符串转数组，过滤空元素）
  const currentHotels = rawHotels.map(hotel => ({
    ...hotel, // 保留原有所有字段
    facilities: typeof hotel.facilities === 'string' 
      ? hotel.facilities.split(',') 
      : [],
    stars: (() => {
      if (hotel.stars === undefined || hotel.stars === null || isNaN(Number(hotel.stars))) {
        return 0;
      }
      return Math.max(0, Math.min(5, Number(hotel.stars)));
    })()
  }));
  const hasMore = response.data.hasMore || false;
  console.log('处理后的酒店数据：', currentHotels.map(h => ({ id: h.id, stars: h.stars })));
  return { data: currentHotels, hasMore:hasMore };
};



// 筛选后的酒店列表
const firstLoading = computed(() => isLoading.value && loadedHotels.value.length === 0);

// 重置全部筛选条件
const resetFilters = () => {
  priceFilter.value = 'all';
  starFilter.value = 'all';
  facilityFilters.value = { pool: false, wifi: false, parking: false, spa: false, breakfast: false };
};

const filteredHotels = computed(() => {
  return loadedHotels.value.filter(hotel => {
    // 价格筛选
    if (priceFilter.value !== 'all') {
      const [min, max] = priceFilter.value.split('-').map(Number);
      if (max) {
        if (hotel.price < min || hotel.price > max) return false;
      } else {
        if (hotel.price < min) return false;
      }
    }
    
  
    // 星级筛选（支持小数范围）
    if (starFilter.value !== 'all') {
      if (starFilter.value === 'unrated') {
        if (hotel.stars !== 0) return false;
      } else {
        const target = Number(starFilter.value);
        const min = target;
        const max = target === 5 ? 5 : target + 1;
        if (!(hotel.stars >= min && hotel.stars < max)) return false;
      }
    }
    
    // 设施筛选
    const activeFacilities = Object.entries(facilityFilters.value)
      .filter(([_, value]) => value)
      .map(([key]) => {
        // 转换设施键为显示名称
        const facilityMap = {
          pool: '游泳池',
          wifi: '免费WiFi',
          parking: '免费停车',
          spa: 'SPA',
          breakfast: '含早餐'
        };
        return facilityMap[key];
      });
      
    if (activeFacilities.length > 0) {
      for (const facility of activeFacilities) {
        if (!hotel.facilities.includes(facility)) {
          return false;
        }
      }
    }
    
    return true;
  }).sort((a, b) => {
    // 排序
    switch (sortOption.value) {
      case 'price-asc':
        return a.price - b.price;
      case 'price-desc':
        return b.price - a.price;
      case 'rating':
        return b.rating - a.rating;
      case 'distance':
        return a.distance - b.distance;
      default:
        return 0;
    }
  });
});

// 新增：筛选/排序变化时，重置分页状态（核心！避免旧数据残留）
const resetPagination = () => {
  page.value = 1; // 重置页码为1
  loadedHotels.value = []; // 清空已加载数据
  hasMore.value = true; // 重置hasMore
  loadingStatus.value = 'none'; // 重置加载状态
  initLoad(); // 重新加载第一页
};
// 给筛选条件添加“变化监听”：筛选/排序变了，调用resetPagination
// 1. 价格筛选变化监听
watch(priceFilter, resetPagination);
// 2. 星级筛选变化监听
watch(starFilter, resetPagination);
// 3. 设施筛选变化监听（深层监听，因为是对象）
watch(facilityFilters, resetPagination, { deep: true });
// 4. 排序方式变化监听
watch(sortOption, resetPagination);

// 新增：滚动容器的滚动事件（判断是否触发加载）
const handleScroll = () => {
  if (!hotelsListRef.value) return; // 防止DOM未渲染

  // 1. 获取滚动容器的3个核心高度（关键公式）
  const container = hotelsListRef.value;
  const clientHeight = container.clientHeight; // 容器可视高度
  const scrollTop = container.scrollTop; // 已滚动距离
  const scrollHeight = container.scrollHeight; // 容器内容总高度

  // 2. 触发条件：滚动到距离底部100px时加载（提前加载，提升体验）
  const shouldLoad = clientHeight + scrollTop >= scrollHeight - 100;

  // 3. 满足条件 + 有下一页 + 未加载中 → 触发加载
  if (shouldLoad && hasMore.value && !isLoading.value) {
    loadMore();
  }
};

const initLoad = async () => {
  await loadMore();
};
// 新增：加载下一页数据（核心滚动加载逻辑）
const loadMore = async () => {
  if (isLoading.value) return; // 加载锁：防止重复请求
  isLoading.value = true;
  loadingStatus.value = 'loading';

  try {
    // 调用fetchHotels获取当前页数据
    const result = await fetchHotels(page.value, pageSize.value);
    // 追加新数据到已加载列表
    loadedHotels.value = [...loadedHotels.value, ...result.data];
    // 更新“是否有下一页”状态
    hasMore.value = result.hasMore;
    console.log('当前已加载酒店数量：', loadedHotels.value.length);
    console.log('最新加载的酒店数据：', result.data);
    // 页码+1，为下次加载做准备
    page.value += 1;
    // 更新加载状态（有下一页则隐藏提示，否则显示“已加载全部”）
    loadingStatus.value = result.hasMore ? 'none' : 'no-more';
  } catch (error) {
    console.error('酒店数据加载失败：', error);
    loadingStatus.value = 'error'; // 加载失败，提示重试
  } finally {
    isLoading.value = false; // 释放加载锁
  }
};


// 酒店类型数据
const hotelTypes = ref([
  {
    name: '豪华酒店',
    count: 256,
    image: 'https://picsum.photos/400/300?random=29'
  },
  {
    name: '经济型酒店',
    count: 892,
    image: 'https://picsum.photos/400/300?random=30'
  },
  {
    name: '民宿',
    count: 453,
    image: 'https://picsum.photos/400/300?random=31'
  },
  {
    name: '度假酒店',
    count: 187,
    image: 'https://picsum.photos/400/300?random=32'
  }
]);

// 酒店特惠数据
const hotelDeals = ref([
  {
    name: '五星酒店两晚套餐',
    description: '含双早+免费升级房型+延迟退房',
    price: 1999,
    originalPrice: 2999,
    discount: '6.7折',
    image: 'https://picsum.photos/400/300?random=33'
  },
  {
    name: '周末度假酒店',
    description: '周六入住享8折优惠，含双人晚餐',
    price: 899,
    originalPrice: 1099,
    discount: '8.2折',
    image: 'https://picsum.photos/400/300?random=34'
  },
  {
    name: '商务出差特惠',
    description: '连住5晚以上享7折，含早餐+洗衣服务',
    price: 2499,
    originalPrice: 3599,
    discount: '立减1100',
    image: 'https://picsum.photos/400/300?random=35'
  },
  {
    name: '亲子主题酒店',
    description: '含2大1小早餐+儿童乐园门票',
    price: 799,
    originalPrice: 1099,
    discount: '7.3折',
    image: 'https://picsum.photos/400/300?random=36'
  }
]);

// 酒店评价数据
const hotelReviews = ref([
  {
    name: '陈女士',
    avatar: 'https://picsum.photos/100/100?random=37',
    rating: 5,
    hotelName: '海景豪华大酒店',
    content: '酒店位置绝佳，出门就是海滩，房间干净整洁，服务也很周到。早餐种类丰富，味道很好。强烈推荐给来三亚度假的朋友！',
    images: [
      'https://picsum.photos/200/200?random=38',
      'https://picsum.photos/200/200?random=39'
    ],
    date: '2023-07-12',
    useful: 36
  },
  {
    name: '张先生',
    avatar: 'https://picsum.photos/100/100?random=40',
    rating: 4,
    hotelName: '城市中心商务酒店',
    content: '位置很好，离地铁站很近，出行方便。房间隔音效果不错，适合商务出行。唯一不足的是停车场有点小，高峰期可能需要排队。',
    images: [],
    date: '2023-06-28',
    useful: 15
  },
  {
    name: '王女士',
    avatar: 'https://picsum.photos/100/100?random=41',
    rating: 5,
    hotelName: '山间民宿',
    content: '太喜欢这家民宿了！老板很热情，房间布置得很有特色，周围环境安静优美。晚上还能看到星星，远离城市喧嚣，非常惬意。',
    images: [
      'https://picsum.photos/200/200?random=42',
      'https://picsum.photos/200/200?random=43',
      'https://picsum.photos/200/200?random=44'
    ],
    date: '2023-07-05',
    useful: 42
  }
]);

// 生命周期钩子
onMounted(() => {
  window.addEventListener('scroll', handleNavScroll);
  startSlideInterval();
  initLoad();
});

onUnmounted(() => {
  window.removeEventListener('scroll', handleNavScroll);
  clearInterval(slideInterval);
});
</script>

<style scoped>

/* 页面骨架
   以下这批类在模板里一直有使用，但本文件的 <style> 里从未定义过
   （同名定义只存在于 Home.vue 的 scoped 样式里，不会作用到本页）。
   本次从 Home.vue 移植补齐，不新造设计。 */
.root-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--c-bg-sub);
}

.main-content {
  flex-grow: 1;
  background-color: var(--c-bg);
}

/* 英雄区域样式 */
.hero-section {
  position: relative;
  height: 500px;
  overflow: hidden;
}

@media (min-width: 768px) {
  .hero-section {
    height: 600px;
  }
}

.hero-section__slides {
  position: absolute;
  inset: 0;
}

.hero-slide {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  opacity: 0;
  transition: opacity 1s ease;
  background-image: var(--slide-image);
  background-color: var(--c-ink); /* 深灰色背景兜底，防止图片加载失败时显示空白 */
}

.hero-slide--active {
  z-index: 1;
  opacity: 1;
}

.hero-section__overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.6), transparent);
}

.hero-section__content {
  position: relative;
  z-index: 10;
  height: 100%;
  display: flex;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

/* 原值 `max-width: 4xl` / `max-width: 2xl` 为无效 CSS（Tailwind 类名误当 CSS 值），
   浏览器直接丢弃，文字宽度约束完全失效。宽度约束下沉到标题与描述上，
   避免把下方的多列搜索表单挤变形。 */
.hero-section__text {
  width: 100%;
}

.hero-section__title {
  max-width: 640px;
  font-size: clamp(2rem, 5vw, 3.25rem);
  font-weight: 700;
  color: #ffffff;
  line-height: var(--lh-display);
  letter-spacing: -0.02em;
  /* 由 h2 提升为 h1，显式写 margin 避免受浏览器默认外边距影响 */
  margin: 0 0 var(--sp-3);
  text-shadow: 0 2px 16px rgba(15, 23, 42, 0.35);
}

.hero-section__desc {
  max-width: 560px;
  font-size: clamp(1rem, 1.6vw, 1.125rem);
  /* 原为 #f3f4f6（浅灰白），批量令牌化时被映射成 --c-bg-sub（背景令牌），
     语义错位。这里是压在深色照片上的文字，显式写成半透明白。 */
  color: rgba(255, 255, 255, 0.88);
  line-height: var(--lh-body-lg);
  margin-bottom: var(--sp-8);
  text-shadow: 0 1px 8px rgba(15, 23, 42, 0.3);
}

.search-form {
  background-color: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 1rem;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  padding: 1rem;
  display: grid;
  grid-template-columns: 1fr;
  gap: 1rem;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

@media (min-width: 768px) {
  .search-form {
    grid-template-columns: repeat(4, 1fr);
    padding: 1.5rem;
    gap: 1.5rem;
  }
}

.search-form__group {
  display: flex;
  align-items: center;
  padding: 0.75rem 1rem;
  border: 1px solid var(--c-line);
  border-radius: 0.75rem;
  background-color: #ffffff;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.search-form__group:focus-within {
  border-color: var(--c-primary-600);
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.search-form__icon {
  color: var(--c-primary-600);
  margin-right: 0.75rem;
  font-size: var(--fs-h3);
}

.search-form__label {
  font-size: var(--fs-caption);
  color: var(--c-ink-3);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 0.1rem;
}

.search-form__hint {
  font-weight: 600;
  color: var(--c-ink);
  font-size: var(--fs-body);
}

.search-form__btn {
  background: linear-gradient(to right, var(--c-primary-600), var(--c-primary-500));
  color: #ffffff;
  border: none;
  border-radius: 0.75rem;
  padding: 0.75rem 1rem;
  cursor: pointer;
  transition: box-shadow 0.2s ease, transform 0.2s ease, filter 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  font-weight: 600;
  font-size: var(--fs-body-lg);
  box-shadow: 0 4px 6px -1px rgba(37, 99, 235, 0.3);
}

.search-form__btn:hover {
  filter: brightness(1.05);
  box-shadow: 0 8px 24px -6px rgba(37, 99, 235, 0.45);
}

.search-form__btn:active {
  transform: scale(0.98);
}

.search-form__btn-icon {
  font-size: var(--fs-body-lg);
}

.search-form__btn:hover {
  background: linear-gradient(to right, var(--c-primary-700), var(--c-primary-600));
  transform: translateY(-1px);
  box-shadow: 0 6px 8px -1px rgba(37, 99, 235, 0.4);
}

/* 覆盖 Element Plus 样式以适应设计 */
:deep(.custom-input .el-input__wrapper) {
  box-shadow: none !important;
  background-color: transparent !important;
  padding: 0;
}
:deep(.custom-input .el-input__inner) {
  font-weight: 600;
  color: var(--c-ink);
  font-size: var(--fs-body);
  height: auto;
  line-height: 1.2;
}

:deep(.custom-date-picker) {
  box-shadow: none !important;
  background-color: transparent !important;
  padding: 0 !important;
}
:deep(.custom-date-picker .el-range-input) {
  font-weight: 600;
  color: var(--c-ink);
  font-size: var(--fs-body);
  background-color: transparent !important;
}
:deep(.custom-date-picker .el-range-separator) {
  color: var(--c-ink-4);
  line-height: 1.5;
}

.guest-selector {
  font-weight: 600;
  color: var(--c-ink);
  font-size: var(--fs-body);
  cursor: pointer;
  padding: 2px 0;
  user-select: none;
}

.guest-counter {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem;
}

.search-input-wrapper {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.hero-section__controls {
  position: absolute;
  bottom: 1.5rem;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  gap: 0.5rem;
  z-index: 10;
}

.hero-control__dot {
  width: 0.75rem;
  height: 0.75rem;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.5);
  border: none;
  cursor: pointer;
  transition: background-color 0.2s ease, width 0.2s ease;
}

.hero-control__dot--active {
  background-color: #ffffff;
  width: 1.5rem;
}




/* 筛选区域样式 */
.filters-section {
  background-color: #ffffff;
  border-bottom: 1px solid var(--c-line);
  padding: 0.75rem 0;
}

/* .filters-container 是横向滚动容器，分组必须禁止收缩，
   否则各组会被压扁而不是触发横向滚动 */
.filter-group {
  flex-shrink: 0;
}

.filters-container {
  display: flex;
  overflow-x: auto;
  padding: 0 1rem 0.5rem; 
  gap: 1.5rem;
  max-width: 1200px; 
  margin: 0 auto; 
}


.filter-label {
  display: block;
  font-weight: 500;
  color: var(--c-ink);
  margin-bottom: 0.5rem;
  font-size: var(--fs-body);
  padding-left: 0.25rem; 
}


.filter-options {
  display: flex;
  gap: 0.75rem; 
  flex-wrap: wrap; 
}


.filter-btn {
  background-color: var(--c-bg-sub); 
  color: var(--c-ink-2); 
  border: 1px solid var(--c-line); 
  border-radius: 9999px; 
  padding: 0.4rem 0.9rem; 
  font-size: var(--fs-body);
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease, border-color 0.2s ease;
  white-space: nowrap; 
}


.filter-btn:hover:not(.filter-btn--active) {
  background-color: var(--c-bg-sub);
  border-color: var(--c-ink-4);
  color: var(--c-ink);
}


.filter-btn--active {
  background-color: var(--c-primary-600); 
  color: #ffffff; 
  border-color: var(--c-primary-600); 
  box-shadow: 0 1px 2px 0 rgba(37, 99, 235, 0.2);
}


.filter-btn--active:hover {
  background-color: var(--c-primary-700); 
  border-color: var(--c-primary-700);
}

/* 酒店列表样式 */
.hotels-section {
  padding: 3rem 0;
  background-color: var(--c-bg-sub); /* 更现代的浅灰色背景 */
}

.section-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1.5rem;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.section-title {
  font-size: var(--fs-h2);
  font-weight: 700;
  color: var(--c-ink);
  position: relative;
  padding-left: 1rem;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 5px;
  height: 24px;
  background: linear-gradient(to bottom, var(--c-primary-600), var(--c-primary-400));
  border-radius: 4px;
}

.sort-options {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  background: white;
  padding: 0.5rem 1rem;
  border-radius: 2rem;
  box-shadow: 0 2px 5px rgba(0,0,0,0.05);
}

.sort-label {
  color: var(--c-ink-2);
  font-size: var(--fs-body);
  font-weight: 500;
}

.sort-select {
  padding: 0.25rem 0.5rem;
  border: none;
  background-color: transparent;
  color: var(--c-ink);
  font-weight: 600;
  cursor: pointer;
  outline: none;
}

.hotels-list {
  display: grid;
  grid-template-columns: 1fr;
  gap: 2rem;
  /* 固定高度和滚动 */
  max-height: 900px; 
  overflow-y: auto; 
  overflow-x: hidden;
  padding: 0.5rem; /* 防止阴影被切 */
  /* 滚动条美化 */
  scrollbar-width: thin;
  scrollbar-color: var(--c-ink-4) var(--c-bg-sub);
}

.hotels-list::-webkit-scrollbar {
  width: 8px;
}
.hotels-list::-webkit-scrollbar-track {
  background: var(--c-bg-sub);
  border-radius: 4px;
}
.hotels-list::-webkit-scrollbar-thumb {
  background-color: var(--c-ink-4);
  border-radius: 4px;
}

/* 加载状态提示样式 */
.loading-status {
  text-align: center;
  padding: 2rem 0;
  color: var(--c-ink-3);
  font-size: var(--fs-body);
  font-weight: 500;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.5rem;
}
.loading-status span[onclick] {
  cursor: pointer;
  color: var(--c-primary-600);
  text-decoration: underline;
}

@media (min-width: 768px) {
  .hotels-list {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (min-width: 1024px) {
  .hotels-list {
    grid-template-columns: repeat(3, 1fr);
  }
}

.hotel-card {
  background-color: var(--c-bg);
  border-radius: var(--r-lg);
  overflow: hidden;
  box-shadow: var(--sh-2);
  transition: transform var(--dur-base) var(--ease-out),
    box-shadow var(--dur-base) var(--ease-out),
    border-color var(--dur-base) var(--ease-out);
  display: flex;
  flex-direction: column;
  border: 1px solid var(--c-line);
  will-change: transform;
  backface-visibility: hidden;
}

.hotel-card:hover {
  box-shadow: var(--sh-hover);
  transform: translateY(-4px);
  border-color: transparent;
}

/* 列表无结果时的占位 */
.hotels-empty {
  padding: var(--sp-12) 0;
  text-align: center;
}

.hotel-card__image-container {
  position: relative;
  height: 15rem;
  overflow: hidden;
}

.hotel-card__image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.7s ease;
}

.hotel-card:hover .hotel-card__image {
  transform: scale(1.1);
}

.hotel-card__promotion {
  position: absolute;
  top: 1rem;
  left: 1rem;
  background: linear-gradient(135deg, var(--c-danger), var(--c-danger));
  color: #ffffff;
  font-size: var(--fs-caption);
  font-weight: 700;
  padding: 0.35rem 0.75rem;
  border-radius: 2rem;
  box-shadow: 0 2px 4px rgba(239, 68, 68, 0.3);
  z-index: 1;
}

.hotel-card__content {
  padding: 1.5rem;
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.hotel-card__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 0.75rem;
  gap: 0.5rem;
}

.hotel-card__name {
  flex: 1;
  font-weight: 700;
  color: var(--c-ink);
  font-size: var(--fs-h3);
  line-height: 1.4;
  transition: color 0.2s ease;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.hotel-card__star {
   margin-left: 0;
   flex-shrink: 0;
}

.hotel-card:hover .hotel-card__name {
  color: var(--c-primary-600);
}

.hotel-card__location {
  display: flex;
  align-items: center;
  color: var(--c-ink-3);
  font-size: var(--fs-body);
  margin-bottom: 1rem;
}

.hotel-card__location-icon {
  margin-right: 0.35rem;
  color: var(--c-ink-4);
}

.hotel-card__distance {
  margin-left: auto;
  color: var(--c-primary-600);
  font-size: var(--fs-caption);
  background-color: var(--c-primary-50);
  padding: 0.15rem 0.5rem;
  border-radius: 0.25rem;
}

.hotel-card__facilities {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 1.25rem;
}

.hotel-card__facility {
  background-color: var(--c-bg-sub);
  color: var(--c-ink-2);
  font-size: var(--fs-caption);
  padding: 0.25rem 0.6rem;
  border-radius: 0.375rem;
  border: 1px solid var(--c-bg-sub);
}

.hotel-card__more-facilities {
  color: var(--c-ink-3);
  font-size: var(--fs-caption);
  padding: 0.25rem 0.5rem;
  background-color: var(--c-bg-sub);
  border-radius: 0.375rem;
}

.hotel-card__rating {
  display: flex;
  align-items: center;
  margin-bottom: 1.25rem;
  background-color: var(--c-bg-sub);
  padding: 0.5rem;
  border-radius: 0.5rem;
}

.rating-score {
  background-color: var(--c-primary-600);
  color: #ffffff;
  font-weight: 800;
  padding: 0.25rem 0.5rem;
  border-radius: 0.375rem;
  margin-right: 0.75rem;
  font-size: var(--fs-body);
  box-shadow: 0 2px 4px rgba(37, 99, 235, 0.2);
}

.rating-reviews {
  color: var(--c-ink-2);
  font-size: var(--fs-caption);
  margin-right: auto;
}

.rating-tag {
  color: var(--c-success);
  font-size: var(--fs-caption);
  font-weight: 600;
}

.hotel-card__price-area {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-top: auto;
  padding-top: 1.25rem;
  border-top: 1px dashed var(--c-line);
}

.hotel-card__price {
  color: var(--c-danger);
  font-weight: 800;
  font-size: var(--fs-h2);
  line-height: 1;
}

.hotel-card__price-unit {
  color: var(--c-ink-4);
  font-size: var(--fs-caption);
  margin-left: 0.15rem;
}

.hotel-card__original-price {
  color: var(--c-ink-4);
  font-size: var(--fs-caption);
  text-decoration: line-through;
  margin-left: 0.5rem;
  display: block;
  margin-bottom: 0.25rem;
}

.hotel-card__btn {
  background: linear-gradient(to right, var(--c-primary-600), var(--c-primary-500));
  color: #ffffff;
  border: none;
  border-radius: 0.5rem;
  padding: 0.6rem 1.25rem;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
  box-shadow: 0 4px 6px -1px rgba(37, 99, 235, 0.3);
}

.hotel-card__btn:hover {
  background: linear-gradient(to right, var(--c-primary-700), var(--c-primary-600));
  box-shadow: 0 6px 8px -1px rgba(37, 99, 235, 0.4);
  transform: translateY(-1px);
}

/* 酒店类型样式 */
.hotel-types-section {
  padding: 3rem 0;
  background-color: #ffffff;
}

.hotel-types-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.5rem;
}

@media (min-width: 768px) {
  .hotel-types-list {
    grid-template-columns: repeat(4, 1fr);
  }
}

.hotel-type-card {
  position: relative;
  border-radius: 0.75rem;
  overflow: hidden;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  height: 14rem;
  cursor: pointer;
}

.hotel-type-card:hover {
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.2);
  transform: translateY(-5px);
}

.hotel-type-card__image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.7s ease;
}

.hotel-type-card:hover .hotel-type-card__image {
  transform: scale(1.1);
}

.hotel-type-card__overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.7), rgba(0, 0, 0, 0.2), transparent);
}

.hotel-type-card__content {
  position: absolute;
  bottom: 0;
  left: 0;
  padding: 1rem;
  width: 100%;
}

.hotel-type-card__name {
  font-size: var(--fs-h3);
  font-weight: bold;
  color: #ffffff;
  margin-bottom: 0.25rem;
}

.hotel-type-card__count {
  /* 原为 #f3f4f6，令牌化时被映射成背景令牌，语义错位。
     这里是压在深色照片上的文字，显式写半透明白。 */
  color: rgba(255, 255, 255, 0.82);
  font-size: var(--fs-body);
}

/* 酒店特惠样式 */
.hotel-deals-section {
  padding: 3rem 0;
  background-color: var(--c-bg-sub);
}

/* 区块右上角的「更多优惠 / 查看全部」链接（特惠区与评价区共用） */
.section-more {
  display: flex;
  align-items: center;
  gap: var(--sp-1);
  font-size: var(--fs-body);
  font-weight: 500;
  color: var(--c-primary-600);
  text-decoration: none;
  transition: color var(--dur-base) var(--ease-out);
}

.section-more:hover {
  color: var(--c-primary-700);
}

.section-more__icon {
  font-size: var(--fs-body);
  transition: transform var(--dur-base) var(--ease-out);
}

.section-more:hover .section-more__icon {
  transform: translateX(4px);
}

.deals-container {
  position: relative;
}

.deals-scroll {
  overflow-x: auto;
  padding-bottom: var(--sp-4);
  /* 横向滚动条在卡片下方很破坏观感，这里隐藏（仍可触摸/滚轮滚动） */
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.deals-scroll::-webkit-scrollbar {
  display: none;
}

.deals-list {
  display: flex;
  gap: var(--sp-4);
  width: max-content;
}

.deal-card {
  width: 18rem;
  background-color: var(--c-bg);
  border: 1px solid var(--c-line);
  border-radius: var(--r-lg);
  overflow: hidden;
  box-shadow: var(--sh-1);
  will-change: transform;
  backface-visibility: hidden;
  transition: transform var(--dur-base) var(--ease-out),
    box-shadow var(--dur-base) var(--ease-out),
    border-color var(--dur-base) var(--ease-out);
}

.deal-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--sh-hover);
  border-color: transparent;
}

.deal-card__image-container {
  position: relative;
  height: 12rem;
  overflow: hidden;
}

.deal-card__image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--dur-slower) var(--ease-out);
}

.deal-card:hover .deal-card__image {
  transform: scale(1.06);
}

.deal-card__discount {
  position: absolute;
  top: var(--sp-3);
  left: var(--sp-3);
  background-color: var(--c-danger);
  color: #ffffff;
  font-size: var(--fs-caption);
  font-weight: 600;
  padding: var(--sp-1) var(--sp-2);
  border-radius: var(--r-xs);
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.35);
}

.deal-card__content {
  padding: var(--sp-4);
}

.deal-card__name {
  font-weight: 600;
  color: var(--c-ink);
  margin-bottom: var(--sp-1);
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.deal-card__desc {
  color: var(--c-ink-3);
  font-size: var(--fs-caption);
  line-height: var(--lh-body);
  margin-bottom: var(--sp-3);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.deal-card__price-area {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.deal-card__price {
  color: var(--c-danger);
  font-family: var(--font-num);
  font-variant-numeric: tabular-nums;
  font-weight: 600;
  font-size: var(--fs-h3);
}

.deal-card__original-price {
  color: var(--c-ink-4);
  font-family: var(--font-num);
  font-size: var(--fs-caption);
  text-decoration: line-through;
  margin-left: var(--sp-1);
}

.deal-card__btn {
  background-color: var(--c-primary-600);
  color: #ffffff;
  font-family: inherit;
  font-size: var(--fs-caption);
  border: none;
  border-radius: var(--r-full);
  padding: var(--sp-2) var(--sp-3);
  cursor: pointer;
  transition: background-color var(--dur-base) var(--ease-out),
    box-shadow var(--dur-base) var(--ease-out),
    transform var(--dur-fast) var(--ease-out);
}

.deal-card__btn:hover {
  background-color: var(--c-primary-700);
  box-shadow: var(--sh-primary);
}

.deal-card__btn:active {
  transform: scale(0.97);
}

.deals-indicators {
  display: flex;
  justify-content: center;
  gap: 0.25rem;
  margin-top: 1.5rem;
}

.deal-indicator {
  width: 0.5rem;
  height: 0.5rem;
  border-radius: 50%;
  background-color: var(--c-line);
}

.deal-indicator--active {
  background-color: var(--c-primary-600);
}

/* 酒店评价样式 */
.reviews-section {
  padding: 3rem 0;
  background-color: #ffffff;
}

/* 评价卡作者区：头像 + 昵称 + 星级 */
.review-card__user {
  display: flex;
  align-items: center;
  margin-bottom: var(--sp-4);
}

.review-card__avatar {
  width: 3rem;
  height: 3rem;
  border-radius: 50%;
  object-fit: cover;
  /* .review-card__user 是 flex 容器，flex 项默认可收缩：用户名一长，
     头像宽度就会被压到 3rem 以下，而 height 固定 → 渲染成竖长椭圆 */
  flex-shrink: 0;
}

.review-card__user-info {
  margin-left: var(--sp-4);
}

.review-card__user-name {
  font-weight: 600;
  color: var(--c-ink);
}

.review-card__stars {
  display: flex;
  color: var(--c-star);
  font-size: var(--fs-caption);
  margin-top: 0.25rem;
}

.review-card__star--empty {
  color: var(--c-line);
}

.review-card__content {
  color: var(--c-ink-2);
  font-style: italic;
  line-height: var(--lh-body-lg);
  margin-bottom: var(--sp-4);
}

.reviews-list {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1.5rem;
}

@media (min-width: 1024px) {
  .reviews-list {
    grid-template-columns: repeat(2, 1fr);
  }
}

.review-card {
  background-color: var(--c-bg-sub);
  border-radius: 0.75rem;
  padding: 1.5rem;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
}

.review-card__hotel-name {
  font-weight: 500;
  color: var(--c-ink);
  margin: 0.75rem 0;
}

.review-card__images {
  display: flex;
  gap: 0.5rem;
  margin: 1rem 0;
}

.review-card__image {
  width: 6rem;
  height: 6rem;
  object-fit: cover;
  border-radius: 0.5rem;
}

.review-card__footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid var(--c-line);
}

.review-card__actions {
  display: flex;
  gap: 1rem;
}

.review-card__action-btn {
  background: none;
  border: none;
  color: var(--c-ink-3);
  font-size: var(--fs-body);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.review-card__action-btn:hover {
  color: var(--c-primary-600);
}

/* 订阅区域样式 */
.subscribe-section {
  padding: var(--sp-20) 0;
  background-color: var(--c-primary-600);
  position: relative;
  overflow: hidden;
}

.subscribe-section__decor {
  position: absolute;
  inset: 0;
  opacity: 0.12;
  pointer-events: none;
}

.decor-icon--plane {
  position: absolute;
  color: #ffffff;
  font-size: 200px;
  top: -50px;
  left: -50px;
  transform: rotate(45deg);
}

.decor-icon--sun {
  position: absolute;
  color: #ffffff;
  font-size: 150px;
  bottom: -30px;
  right: 50px;
}

.decor-icon--ship {
  position: absolute;
  color: #ffffff;
  font-size: 100px;
  top: 30%;
  right: 20%;
}

.subscribe-section__content {
  position: relative;
  z-index: 10;
  /* 容器最大宽度。原值 `max-width: 2xl` 是 Tailwind 类名误当 CSS 值，无效，此处写成实际像素 */
  max-width: 672px;
  margin: 0 auto;
  text-align: center;
}

.subscribe-section__title {
  font-size: clamp(1.5rem, 3vw, var(--fs-h1));
  line-height: var(--lh-h1);
  font-weight: 600;
  letter-spacing: -0.01em;
  color: #ffffff;
  margin-bottom: var(--sp-3);
}

.subscribe-section__desc {
  color: rgba(255, 255, 255, 0.85);
  line-height: var(--lh-body-lg);
  margin-bottom: var(--sp-8);
}

.subscribe-form {
  display: flex;
  flex-direction: column;
  gap: var(--sp-3);
}

@media (min-width: 640px) {
  .subscribe-form {
    flex-direction: row;
  }
}

.subscribe-form__input {
  flex-grow: 1;
  padding: var(--sp-3) var(--sp-4);
  font-family: inherit;
  font-size: var(--fs-body-lg);
  color: var(--c-ink);
  background-color: #ffffff;
  border-radius: var(--r-sm);
  border: none;
  outline: none;
  transition: box-shadow var(--dur-base) var(--ease-out);
}

.subscribe-form__input::placeholder {
  color: var(--c-ink-4);
}

.subscribe-form__input:focus {
  box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.55);
}

.subscribe-form__btn {
  background-color: var(--c-star);
  color: var(--c-ink);
  font-family: inherit;
  font-size: var(--fs-body-lg);
  font-weight: 600;
  border: none;
  border-radius: var(--r-sm);
  padding: var(--sp-3) var(--sp-6);
  cursor: pointer;
  white-space: nowrap;
  transition: background-color var(--dur-base) var(--ease-out),
    box-shadow var(--dur-base) var(--ease-out),
    transform var(--dur-fast) var(--ease-out);
}

.subscribe-form__btn:hover {
  background-color: var(--c-accent);
  box-shadow: 0 8px 24px -6px rgba(250, 204, 21, 0.5);
}

.subscribe-form__btn:active {
  transform: scale(0.98);
}

.subscribe-section__privacy {
  color: rgba(255, 255, 255, 0.7);
  font-size: var(--fs-caption);
  margin-top: var(--sp-4);
}
</style>