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
            <h2 class="hero-section__title">发现完美住宿</h2>
            <p class="hero-section__desc">从豪华酒店到特色民宿，为您的旅程找到理想下榻之处</p>
            
            <!-- 酒店搜索框 -->
            <form class="search-form" @submit.prevent="handleSearch">
              <div class="search-form__group">
                <i class="fa fa-map-marker search-form__icon"></i>
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
                <i class="fa fa-calendar search-form__icon"></i>
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
                <i class="fa fa-user search-form__icon"></i>
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
                <i class="fa fa-search mr-2"></i>
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
          
          <div class="hotels-list" ref="hotelsListRef" @scroll="handleScroll">
            <div 
              class="hotel-card"
              v-for="(hotel, index) in filteredHotels" 
              :key="hotel.id"
            >
              <div class="hotel-card__image-container">
                <img 
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
                  <i class="fa fa-map-marker hotel-card__location-icon"></i>
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
              <i class="fa fa-arrow-right section-more__icon"></i>
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
              <i class="fa fa-arrow-right section-more__icon"></i>
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
                    <i class="fa fa-star" v-for="i in 5" :key="i" :class="{ 'review-card__star--empty': i > review.rating }"></i>
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
                  <button class="review-card__action-btn"><i class="fa fa-thumbs-up"></i> 有用 ({{ review.useful }})</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 订阅区域 -->
      <section class="subscribe-section">
        <div class="subscribe-section__decor">
          <i class="fa fa-paper-plane decor-icon--plane"></i>
          <i class="fa fa-sun-o decor-icon--sun"></i>
          <i class="fa fa-ship decor-icon--ship"></i>
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
  </div>
  <el-backtop :right="100" :bottom="100" style="color:rgb(82, 233, 200);"/>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted,watch} from 'vue';
import { getHotelListService } from '@/api/hotel';
import { useGeoStore } from '@/stores/geo';
import router from '@/router';


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
  background-color: #424244; /* 深灰色背景兜底，防止图片加载失败时显示空白 */
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

.hero-section__text {
  max-width: 4xl;
}

.hero-section__title {
  font-size: clamp(2rem, 5vw, 3.5rem);
  font-weight: bold;
  color: #ffffff;
  line-height: 1.2;
  margin-bottom: 0.5rem;
}

.hero-section__desc {
  font-size: clamp(1rem, 2vw, 1.25rem);
  color: #f3f4f6;
  margin-bottom: 2rem;
  max-width: 2xl;
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
  border: 1px solid #e5e7eb;
  border-radius: 0.75rem;
  background-color: #ffffff;
  transition: all 0.2s ease;
}

.search-form__group:focus-within {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.search-form__icon {
  color: #2563eb;
  margin-right: 0.75rem;
  font-size: 1.25rem;
}

.search-form__label {
  font-size: 0.75rem;
  color: #6b7280;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 0.1rem;
}

.search-form__hint {
  font-weight: 600;
  color: #1f2937;
  font-size: 0.95rem;
}

.search-form__btn {
  background: linear-gradient(to right, #2563eb, #3b82f6);
  color: #ffffff;
  border: none;
  border-radius: 0.75rem;
  padding: 0.75rem 1rem;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 1rem;
  box-shadow: 0 4px 6px -1px rgba(37, 99, 235, 0.3);
}

.search-form__btn:hover {
  background: linear-gradient(to right, #1d4ed8, #2563eb);
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
  color: #1f2937;
  font-size: 0.95rem;
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
  color: #1f2937;
  font-size: 0.95rem;
  background-color: transparent !important;
}
:deep(.custom-date-picker .el-range-separator) {
  color: #9ca3af;
  line-height: 1.5;
}

.guest-selector {
  font-weight: 600;
  color: #1f2937;
  font-size: 0.95rem;
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
  transition: all 0.2s ease;
}

.hero-control__dot--active {
  background-color: #ffffff;
  width: 1.5rem;
}




/* 筛选区域样式 */
.filters-section {
  background-color: #ffffff;
  border-bottom: 1px solid #e5e7eb;
  padding: 0.75rem 0;
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
  color: #1f2937;
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
  padding-left: 0.25rem; 
}


.filter-options {
  display: flex;
  gap: 0.75rem; 
  flex-wrap: wrap; 
}


.filter-btn {
  background-color: #f9fafb; 
  color: #4b5563; 
  border: 1px solid #e5e7eb; 
  border-radius: 9999px; 
  padding: 0.4rem 0.9rem; 
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s ease; 
  white-space: nowrap; 
}


.filter-btn:hover:not(.filter-btn--active) {
  background-color: #f3f4f6;
  border-color: #d1d5db;
  color: #1f2937;
}


.filter-btn--active {
  background-color: #2563eb; 
  color: #ffffff; 
  border-color: #2563eb; 
  box-shadow: 0 1px 2px 0 rgba(37, 99, 235, 0.2);
}


.filter-btn--active:hover {
  background-color: #1d4ed8; 
  border-color: #1d4ed8;
}

/* 酒店列表样式 */
.hotels-section {
  padding: 3rem 0;
  background-color: #f3f4f6; /* 更现代的浅灰色背景 */
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
  font-size: 1.875rem;
  font-weight: 700;
  color: #111827;
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
  background: linear-gradient(to bottom, #2563eb, #60a5fa);
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
  color: #4b5563;
  font-size: 0.875rem;
  font-weight: 500;
}

.sort-select {
  padding: 0.25rem 0.5rem;
  border: none;
  background-color: transparent;
  color: #1f2937;
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
  scrollbar-color: #cbd5e1 #f1f5f9;
}

.hotels-list::-webkit-scrollbar {
  width: 8px;
}
.hotels-list::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 4px;
}
.hotels-list::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 4px;
}

/* 加载状态提示样式 */
.loading-status {
  text-align: center;
  padding: 2rem 0;
  color: #6b7280;
  font-size: 0.95rem;
  font-weight: 500;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.5rem;
}
.loading-status span[onclick] {
  cursor: pointer;
  color: #2563eb;
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
  background-color: #ffffff;
  border-radius: 1rem;
  overflow: hidden;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  border: 1px solid rgba(229, 231, 235, 0.5);
}

.hotel-card:hover {
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  transform: translateY(-5px);
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
  background: linear-gradient(135deg, #ef4444, #dc2626);
  color: #ffffff;
  font-size: 0.75rem;
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
  color: #111827;
  font-size: 1.25rem;
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
  color: #2563eb;
}

.hotel-card__location {
  display: flex;
  align-items: center;
  color: #6b7280;
  font-size: 0.875rem;
  margin-bottom: 1rem;
}

.hotel-card__location-icon {
  margin-right: 0.35rem;
  color: #9ca3af;
}

.hotel-card__distance {
  margin-left: auto;
  color: #2563eb;
  font-size: 0.8rem;
  background-color: #eff6ff;
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
  background-color: #f9fafb;
  color: #4b5563;
  font-size: 0.75rem;
  padding: 0.25rem 0.6rem;
  border-radius: 0.375rem;
  border: 1px solid #f3f4f6;
}

.hotel-card__more-facilities {
  color: #6b7280;
  font-size: 0.75rem;
  padding: 0.25rem 0.5rem;
  background-color: #f9fafb;
  border-radius: 0.375rem;
}

.hotel-card__rating {
  display: flex;
  align-items: center;
  margin-bottom: 1.25rem;
  background-color: #f8fafc;
  padding: 0.5rem;
  border-radius: 0.5rem;
}

.rating-score {
  background-color: #2563eb;
  color: #ffffff;
  font-weight: 800;
  padding: 0.25rem 0.5rem;
  border-radius: 0.375rem;
  margin-right: 0.75rem;
  font-size: 0.9rem;
  box-shadow: 0 2px 4px rgba(37, 99, 235, 0.2);
}

.rating-reviews {
  color: #4b5563;
  font-size: 0.85rem;
  margin-right: auto;
}

.rating-tag {
  color: #059669;
  font-size: 0.8rem;
  font-weight: 600;
}

.hotel-card__price-area {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-top: auto;
  padding-top: 1.25rem;
  border-top: 1px dashed #e5e7eb;
}

.hotel-card__price {
  color: #ef4444;
  font-weight: 800;
  font-size: 1.5rem;
  line-height: 1;
}

.hotel-card__price-unit {
  color: #9ca3af;
  font-size: 0.8rem;
  margin-left: 0.15rem;
}

.hotel-card__original-price {
  color: #9ca3af;
  font-size: 0.8rem;
  text-decoration: line-through;
  margin-left: 0.5rem;
  display: block;
  margin-bottom: 0.25rem;
}

.hotel-card__btn {
  background: linear-gradient(to right, #2563eb, #3b82f6);
  color: #ffffff;
  border: none;
  border-radius: 0.5rem;
  padding: 0.6rem 1.25rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 4px 6px -1px rgba(37, 99, 235, 0.3);
}

.hotel-card__btn:hover {
  background: linear-gradient(to right, #1d4ed8, #2563eb);
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
  transition: all 0.3s ease;
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
  font-size: 1.25rem;
  font-weight: bold;
  color: #ffffff;
  margin-bottom: 0.25rem;
}

.hotel-type-card__count {
  color: #f3f4f6;
  font-size: 0.875rem;
}

/* 酒店特惠样式 */
.hotel-deals-section {
  padding: 3rem 0;
  background-color: #f9fafb;
}

/* 酒店评价样式 */
.reviews-section {
  padding: 3rem 0;
  background-color: #ffffff;
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
  background-color: #f9fafb;
  border-radius: 0.75rem;
  padding: 1.5rem;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
}

.review-card__hotel-name {
  font-weight: 500;
  color: #1f2937;
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
  border-top: 1px solid #e5e7eb;
}

.review-card__actions {
  display: flex;
  gap: 1rem;
}

.review-card__action-btn {
  background: none;
  border: none;
  color: #6b7280;
  font-size: 0.875rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.review-card__action-btn:hover {
  color: #2563eb;
}
</style>