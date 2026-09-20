<template>
  <div class="root-container">
    <!-- 注意：这里原本有一个多余的 <router-view>。Home 本身就是 Layout 的子路由，
         自身再放 router-view 只会渲染空内容；且 Layout 已经提供了 <main>，
         这里改为 <div> 避免出现嵌套 <main> 的语义错误。 -->
    <div class="main-content">
      <!-- 英雄区域 -->
      <section class="hero-section">
        <!-- 轮播图 -->
       <div class="hero-section__slides">
          <!-- indicator-position 的合法值只有 "" / "none" / "outside"，
               原写法 "bottom" 非法（会回落成默认值 ""，也就是"内部底部"，渲染效果一致），
               但会在控制台刷 prop 校验警告。默认值即为所需效果，直接不传。 -->
          <el-carousel autoplay :interval="3000" arrow="hover" height="500px">
            <el-carousel-item v-for="(slide, index) in slides" :key="index">
              <img
                :src="slide.image"
                :alt="`轮播图${index+1}`"
                class="carousel-image"
                :fetchpriority="index === 0 ? 'high' : 'auto'"
              >
            </el-carousel-item>
          </el-carousel>
        </div>

        <!-- 遮罩层：原实现没有这一层，白色标题直接压在照片上，
             遇到浅色天空/雪景时几乎读不清。改为双向渐变 scrim。 -->
        <div class="hero-section__overlay"></div>

        <!-- 搜索框 + 标题 -->
        <div class="hero-section__content">
          <div class="hero-section__text">
            <h1 class="hero-section__title">探索世界的每一个角落</h1>
            <p class="hero-section__desc">从热门景点到隐秘宝藏，让我们带你领略不一样的旅行体验</p>
            
            <!-- 搜索框 -->
            <form class="search-form" @submit.prevent="handleSearch">
              <div class="search-form__group">
                <el-icon class="search-form__icon"><Search /></el-icon>
                <div class="search-input-wrapper">
                  <p class="search-form__label">关键词</p>
                  <el-input 
                    v-model="searchForm.keyword" 
                    placeholder="景点名称 / 城市" 
                    class="custom-input"
                    clearable
                  />
                </div>
              </div>
              
              <div class="search-form__group">
                <el-icon class="search-form__icon"><PriceTag /></el-icon>
                <div class="search-input-wrapper">
                  <p class="search-form__label">类型</p>
                  <el-select 
                    v-model="searchForm.type" 
                    placeholder="景点类型" 
                    class="custom-select"
                    clearable
                  >
                    <el-option 
                      v-for="item in attractionTypes" 
                      :key="item.value" 
                      :label="item.label" 
                      :value="item.value" 
                    />
                  </el-select>
                </div>
              </div>
              
              <div class="search-form__group">
                <el-icon class="search-form__icon"><StarFilled /></el-icon>
                <div class="search-input-wrapper">
                  <p class="search-form__label">等级</p>
                  <el-select 
                    v-model="searchForm.stars" 
                    placeholder="景区等级" 
                    class="custom-select"
                    clearable
                  >
                    <el-option label="不限" value="" />
                    <el-option label="5A级景区" value="5" />
                    <el-option label="4A级景区" value="4" />
                    <el-option label="3A级景区" value="3" />
                  </el-select>
                </div>
              </div>
              
              <button type="button" class="search-form__btn" @click="handleSearch">
              <!-- <el-button type="primary" class="search-form__btn" :icon="Search" @click="handleSearch"> -->

                <span>搜索景点</span>
              </button>
            </form>
          </div>
        </div>
      </section>

      <!-- 分类导航 -->
      <section class="category-nav">
        <div class="category-nav__container section-container">
          <div class="category-nav__list">
            <div class="category-nav__item">
              <div class="category-nav__icon-container" @click="router.push(`/attraction`)">
              <Ticket/>
              </div>
              <span class="category-nav__text">景点门票</span>
            </div>
            
            <div class="category-nav__item">
              <div class="category-nav__icon-container" @click="router.push(`/flight`)">
                <Plane/>
              </div>
              <span class="category-nav__text">机票</span>
            </div>
            
            <div class="category-nav__item">
              <div class="category-nav__icon-container" @click="router.push(`/hotel`)">
                <Hotel/>
              </div>
              <span class="category-nav__text">酒店</span>
            </div>
            
            <div class="category-nav__item">
              <div class="category-nav__icon-container" @click="router.push(`/package`)">
                <TravelPackage/>
              </div>
              <span class="category-nav__text">旅游套餐</span>
            </div>
            
            <div class="category-nav__item">
              <div class="category-nav__icon-container">
                <Taxi/>
              </div>
              <span class="category-nav__text">租车</span>
            </div>
            
            <div class="category-nav__item">
              <div class="category-nav__icon-container">
                <More/>
              </div>
              <span class="category-nav__text">更多</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 热门目的地 -->
      <section class="destinations-section">
        <div class="section-container">
          <div class="section-header">
            <h2 class="section-title">热门目的地</h2>
            <a href="#" class="section-more">
              查看全部
              <el-icon class="section-more__icon"><ArrowRight /></el-icon>
            </a>
          </div>
          
          <div class="destinations-list">
            <div 
              class="destination-card"
              v-for="(destination, index) in destinations" 
              :key="index"
              v-reveal="index * 60"
            >
              <img
                v-lazy-img
                loading="lazy"
                :src="destination.image"
                :alt="`图片展示的是${destination.name}的风景`"
                class="destination-card__image"
              >
              <div class="destination-card__overlay"></div>
              <div class="destination-card__content">
                <h3 class="destination-card__name">{{ destination.name }}</h3>
                <div class="destination-card__location">
                  <el-icon class="destination-card__location-icon"><Location /></el-icon>
                  <span>{{ destination.location }}</span>
                </div>
                <div class="destination-card__rating">
                  <div class="rating-tag">
                    <el-icon class="rating-tag__star"><StarFilled /></el-icon>
                    <span>{{ destination.rating }}</span>
                  </div>
                  <span class="destination-card__reviews">{{ destination.reviews }}条评价</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 特惠活动 -->
      <section class="deals-section">
        <div class="section-container">
          <div class="section-header">
            <h2 class="section-title">特惠活动</h2>
            <a href="#" class="section-more">
              更多优惠
              <el-icon class="section-more__icon"><ArrowRight /></el-icon>
            </a>
          </div>
          
          <div class="deals-container">
            <div class="deals-scroll">
              <div class="deals-list">
                <!-- 注意：这里不能加 v-reveal。特惠卡是横向滚动列表（.deals-list 宽度 max-content），
                     第 5 张卡初始位于可视区右侧之外，IntersectionObserver 永远不会判定它进入视口，
                     结果是它会被永久留在 opacity:0 的初始态（实测确实如此）。 -->
                <div class="deal-card" v-for="(deal, index) in deals" :key="index">
                  <div class="deal-card__image-container">
                    <img v-lazy-img loading="lazy" :src="deal.image" :alt="deal.name" class="deal-card__image">
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
                      <button class="deal-card__btn">立即抢购</button>
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

      <!-- 精选旅游套餐 -->
      <section class="packages-section">
        <div class="section-container">
          <div class="section-header">
            <h2 class="section-title">精选旅游套餐</h2>
            <div class="packages-filter">
              <button class="filter-btn filter-btn--active">全部</button>
              <button class="filter-btn">国内游</button>
              <button class="filter-btn">国外游</button>
              <button class="filter-btn">周边游</button>
            </div>
          </div>
          
          <div class="packages-list">
            <div 
              class="package-card"
              v-for="(tourPackage, index) in packages" 
              :key="index"
              v-reveal="(index % 3) * 60"
            >
              <div class="package-card__image-container">
                <img
                  v-lazy-img
                  loading="lazy"
                  :src="tourPackage.image"
                  :alt="`图片展示的是${tourPackage.name}旅游套餐的相关风景`"
                  class="package-card__image"
                >
                <div class="package-card__days-tag">{{ tourPackage.days }}天{{ tourPackage.nights }}晚</div>
              </div>
              
              <div class="package-card__content">
                <div class="package-card__header">
                  <span class="package-card__type-tag">{{ tourPackage.type }}</span>
                  <div class="package-card__rating">
                    <el-icon class="package-card__star"><StarFilled /></el-icon>
                    <el-icon class="package-card__star"><StarFilled /></el-icon>
                    <el-icon class="package-card__star"><StarFilled /></el-icon>
                    <el-icon class="package-card__star"><StarFilled /></el-icon>
                    <el-icon class="package-card__star"><Star /></el-icon>
                    <span class="package-card__rating-text">{{ tourPackage.rating }}</span>
                  </div>
                </div>
                
                <h3 class="package-card__name">{{ tourPackage.name }}</h3>
                <p class="package-card__desc">{{ tourPackage.description }}</p>
                
                <div class="package-card__sales">
                  <el-icon class="package-card__sales-icon"><UserFilled /></el-icon>
                  <span>已售 {{ tourPackage.sold }} 份</span>
                </div>
                
                <div class="package-card__price-area">
                  <div>
                    <span class="package-card__price">¥{{ tourPackage.price }}</span>
                    <span class="package-card__price-unit">/人起</span>
                  </div>
                  <button class="package-card__btn">查看详情</button>
                </div>
              </div>
            </div>
          </div>
          
          <button class="load-more-btn">加载更多</button>
        </div>
      </section>

      <!-- 旅游攻略 -->
      <section class="guides-section">
        <div class="section-container">
          <div class="section-header">
            <h2 class="section-title">旅游攻略</h2>
            <a href="#" class="section-more">
              更多攻略
              <el-icon class="section-more__icon"><ArrowRight /></el-icon>
            </a>
          </div>
          
          <div class="guides-list">
            <!-- 主要攻略卡片 -->
            <div class="guide-card--featured" v-reveal>
              <div class="guide-card__content--featured">
                <img
                  v-lazy-img
                  loading="lazy"
                  src="https://picsum.photos/600/400?random=10"
                  alt="图片展示的是热门旅游攻略的相关风景"
                  class="guide-card__image--featured"
                >
                <div class="guide-card__text--featured">
                  <div>
                    <div class="guide-card__tags">
                      <span class="guide-card__hot-tag">热门</span>
                      <span class="guide-card__views">
                        <el-icon class="guide-card__views-icon"><View /></el-icon> 2.5k 阅读
                      </span>
                      <span class="guide-card__comments">
                        <el-icon class="guide-card__comments-icon"><ChatDotRound /></el-icon> 128 评论
                      </span>
                    </div>
                    <h3 class="guide-card__title--featured">2023年夏季最值得去的10个海岛，你去过几个？</h3>
                    <p class="guide-card__desc--featured">
                      夏天来了，海岛游成为了许多人的首选。本文为你推荐10个性价比超高的海岛，从国内到国外，从热门到小众，总有一个适合你。带上防晒霜，一起去海边吧！
                    </p>
                  </div>
                  <div class="guide-card__author">
                    <img
                      v-lazy-img
                      loading="lazy"
                      src="https://picsum.photos/50/50?random=20"
                      alt="图片展示的是旅游攻略作者的头像"
                      class="guide-card__author-avatar"
                    >
                    <div class="guide-card__author-info">
                      <p class="guide-card__author-name">旅行达人小明</p>
                      <p class="guide-card__date">发布于 3天前</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 次要攻略卡片 -->
            <div class="guide-card--small" v-reveal="120">
              <img
                v-lazy-img
                loading="lazy"
                src="https://picsum.photos/300/200?random=11"
                alt="图片展示的是美食攻略的相关内容"
                class="guide-card__image--small"
              >
              <div class="guide-card__text--small">
                <h3 class="guide-card__title--small">成都美食攻略：除了火锅，这些小吃也不能错过</h3>
                <p class="guide-card__date--small">
                  <el-icon class="guide-card__date-icon"><Clock /></el-icon> 5天前
                </p>
              </div>
            </div>
            
            <div class="guide-card--small" v-reveal="120">
              <img
                v-lazy-img
                loading="lazy"
                src="https://picsum.photos/300/200?random=12"
                alt="图片展示的是摄影攻略的相关风景"
                class="guide-card__image--small"
              >
              <div class="guide-card__text--small">
                <h3 class="guide-card__title--small">青海湖摄影攻略：最佳拍摄地点和时间</h3>
                <p class="guide-card__date--small">
                  <el-icon class="guide-card__date-icon"><Clock /></el-icon> 1周前
                </p>
              </div>
            </div>
            
            <div class="guide-card--small" v-reveal="120">
              <img
                v-lazy-img
                loading="lazy"
                src="https://picsum.photos/300/200?random=13"
                alt="图片展示的是徒步攻略的相关路线"
                class="guide-card__image--small"
              >
              <div class="guide-card__text--small">
                <h3 class="guide-card__title--small">徒步虎跳峡：新手也能完成的经典路线</h3>
                <p class="guide-card__date--small">
                  <el-icon class="guide-card__date-icon"><Clock /></el-icon> 2周前
                </p>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 用户评价 -->
      <section class="reviews-section">
        <div class="section-container">
          <h2 class="reviews-title">用户真实评价</h2>
          
          <div class="reviews-list">
            <div 
              class="review-card"
              v-for="(review, index) in reviews" 
              v-reveal="index * 80"
              :key="index"
            >
              <div class="review-card__user">
                <img
                  v-lazy-img
                  loading="lazy"
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
              
              <p class="review-card__content">"{{ review.content }}"</p>
              
              <div class="review-card__footer">
                <span class="review-card__destination">{{ review.destination }}</span>
                <span class="review-card__date">{{ review.date }}</span>
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
          <h2 class="subscribe-section__title">获取最新旅游资讯和独家优惠</h2>
          <p class="subscribe-section__desc">订阅我们的邮件，第一时间获取旅游灵感和限时优惠</p>
          
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
    </div>

    <!-- el-backtop 必须放在根节点**内部**。
         它一旦与根 <div> 平级，组件就会渲染成 Fragment 根节点，
         而 Layout.vue 的 <transition mode="out-in"> 无法对 Fragment 根
         执行 leave 过渡，离开本页后主内容区会永久空白（必须刷新才恢复）。
         el-backtop 是 position: fixed，放在这里不影响布局与定位。 -->
    <el-backtop :right="100" :bottom="100" />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted} from 'vue';
import { getUserInfoService } from '@/api/user';
import {useUserInfoStore} from '@/stores/userInfo'
import router from '@/router'
import { useTokenStore } from '@/stores/token'
import {ElMessageBox,ElNotification} from 'element-plus'
import {Plane,Hotel,Taxi,TravelPackage,More} from '@/components/Icon.vue'
import { Ticket, Search, PriceTag, Star, StarFilled, ArrowRight, Location, UserFilled, View, ChatDotRound, Clock, Sunny, Compass } from '@element-plus/icons-vue'

const tokenStore = useTokenStore()
const userInfoStore = useUserInfoStore()

// 搜索表单
const searchForm = ref({
  keyword: '',
  type: '',
  stars: ''
})

// 景点类型选项（精简版）
const attractionTypes = [
  { label: '不限', value: '' },
  { label: '历史古迹', value: '历史古迹' },
  { label: '自然景观', value: '自然景观' },
  { label: '文化场馆', value: '文化场馆' },
  { label: '主题公园', value: '主题公园' },
  { label: '城市地标', value: '城市地标' },
  { label: '休闲度假', value: '休闲度假' },
]

const handleSearch = () => {
  router.push({
    path: '/attraction',
    query: {
      keyword: searchForm.value.keyword,
      type: searchForm.value.type,
      stars: searchForm.value.stars
    }
  })
}

// 导航栏滚动效果
const scrolled = ref(false);
const handleScroll = () => {
  scrolled.value = window.scrollY > 50;
};

// 移动端菜单
const isMenuOpen = ref(false);

// 轮播图
const slides = ref([
  { image: 'https://chufala.oss-cn-shenzhen.aliyuncs.com/34c3bdc8-371b-4696-9cd1-198ba7eabba4.png' },
  { image: 'https://chufala.oss-cn-shenzhen.aliyuncs.com/562cc35e-83c3-4065-913c-d32b0015faac.png' },
  { image: 'https://chufala.oss-cn-shenzhen.aliyuncs.com/1523d62c-1d5a-46c0-b146-ac66129b0b29.png' }
]);


// 热门目的地数据
const destinations = ref([
  {
    name: '巴厘岛',
    location: '印度尼西亚',
    rating: 4.8,
    reviews: 2568,
    image: 'https://picsum.photos/600/400?random=4'
  },
  {
    name: '京都',
    location: '日本',
    rating: 4.7,
    reviews: 1892,
    image: 'https://picsum.photos/600/400?random=5'
  },
  {
    name: '马尔代夫',
    location: '马尔代夫',
    rating: 4.9,
    reviews: 3254,
    image: 'https://picsum.photos/600/400?random=6'
  },
  {
    name: '三亚',
    location: '中国·海南',
    rating: 4.6,
    reviews: 4125,
    image: 'https://picsum.photos/600/400?random=7'
  }
]);

// 特惠活动数据
const deals = ref([
  {
    name: '泰国曼谷-清迈6日游',
    description: '含机票+酒店+景点门票，全程中文导游',
    price: 2999,
    originalPrice: 4599,
    discount: '立减1600',
    image: 'https://picsum.photos/400/300?random=8'
  },
  {
    name: '三亚5星级酒店3晚套餐',
    description: '含双早+接送机+水上项目2选1',
    price: 1599,
    originalPrice: 2399,
    discount: '6.7折',
    image: 'https://picsum.photos/400/300?random=9'
  },
  {
    name: '日本东京迪士尼门票',
    description: '快速通道+导游服务，无需排队',
    price: 699,
    originalPrice: 899,
    discount: '7.8折',
    image: 'https://picsum.photos/400/300?random=10'
  },
  {
    name: '张家界3日游',
    description: '含住宿+门票+导游，含玻璃栈道',
    price: 899,
    originalPrice: 1299,
    discount: '立减400',
    image: 'https://picsum.photos/400/300?random=11'
  },
  {
    name: '欧洲法意瑞10日游',
    description: '含往返机票+酒店+早餐+景点门票',
    price: 9999,
    originalPrice: 12999,
    discount: '立减3000',
    image: 'https://picsum.photos/400/300?random=12'
  }
]);

// 旅游套餐数据
const packages = ref([
  {
    name: '云南昆明-大理-丽江7日游',
    description: '玉龙雪山+洱海骑行+古城漫步，含机票酒店',
    price: 3599,
    days: 7,
    nights: 6,
    type: '国内游',
    rating: 4.7,
    sold: 1256,
    image: 'https://picsum.photos/600/400?random=13'
  },
  {
    name: '泰国普吉岛5日游',
    description: '皇帝岛浮潜+人妖秀+海鲜大餐，含接送机',
    price: 2899,
    days: 5,
    nights: 4,
    type: '国外游',
    rating: 4.6,
    sold: 987,
    image: 'https://picsum.photos/600/400?random=14'
  },
  {
    name: '上海迪士尼亲子2日游',
    description: '乐园门票+酒店住宿+快速通道，适合带娃家庭',
    price: 1699,
    days: 2,
    nights: 1,
    type: '周边游',
    rating: 4.8,
    sold: 2103,
    image: 'https://picsum.photos/600/400?random=15'
  }
]);

// 用户评价数据
const reviews = ref([
  {
    name: '李先生',
    avatar: 'https://picsum.photos/100/100?random=16',
    rating: 5,
    content: '这次巴厘岛之旅非常完美，酒店位置绝佳，导游服务也很贴心，行程安排合理，不会太赶。强烈推荐给大家！',
    destination: '巴厘岛5日游',
    date: '2023-06-15'
  },
  {
    name: '张女士',
    avatar: 'https://picsum.photos/100/100?random=17',
    rating: 4,
    content: '云南的风景真的太美了，尤其是玉龙雪山，非常壮观。唯一有点遗憾的是在大理遇到了雨天，但总体还是很满意的。',
    destination: '云南7日游',
    date: '2023-05-28'
  },
  {
    name: '王先生',
    avatar: 'https://picsum.photos/100/100?random=18',
    rating: 5,
    content: '性价比很高的一次旅行，价格比其他平台便宜不少，服务却一点不差。客服响应很及时，有问题都能很快解决。',
    destination: '泰国普吉岛5日游',
    date: '2023-06-02'
  }
]);

// 生命周期钩子
onMounted(() => {
  window.addEventListener('scroll', handleScroll);
});



const getUserInfo = async() => { 
  const result = await getUserInfoService()
  userInfoStore.setInfo(result.data)
};
getUserInfo()

const logout = ()=>{
  ElMessageBox.confirm(
    '是否要退出登录?',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(() => {
      tokenStore.removeToken()
      router.push('/login')
      ElNotification.success({
        title: '提示',
        message: '您已成功退出登录！'})
    })
    .catch(() => {
      ElNotification.primary('您已取消 “退出登录” 操作',)
    })
}
</script>

<style scoped>
/* 基础样式 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html {
  scroll-behavior: smooth;
}

.root-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--c-bg-sub);
}

.main-content {
  flex-grow: 1;
  /* 原值为 #1890ff（高饱和蓝），疑为调试遗留；改为页面底色 */
  background-color: var(--c-bg);
}

.section-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

/* 英雄区域样式 */
.hero-section {
  position: relative;
  /* 原来固定 500px，在矮屏笔记本上会占满整屏、在手机上又显得空 */
  height: clamp(440px, 62vh, 640px);
  overflow: hidden;
  background-color: var(--c-ink);
}

.hero-section__slides {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.hero-section__slides .el-carousel,
.hero-section__slides .el-carousel__container,
.hero-section__slides .el-carousel-item {
  width: 100% !important;
  height: 100% !important;
  margin: 0 !important;
  padding: 0 !important;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  display: block;
}

/* Ken Burns：轮播图缓慢推近，比纯淡入切换高级得多。
   动画挂在 .is-active 上，Element Plus 每次激活都会重新应用动画 → 自动重播。 */
.hero-section__slides .el-carousel-item.is-active .carousel-image {
  animation: heroKenBurns 9s var(--ease-out) forwards;
}

@keyframes heroKenBurns {
  from {
    transform: scale(1);
  }
  to {
    transform: scale(1.08);
  }
}

/* 双向渐变遮罩：顶部压暗保证导航可读，底部压暗保证标题与搜索框可读 */
.hero-section__overlay {
  position: absolute;
  inset: 0;
  z-index: 2;
  pointer-events: none;
  background: linear-gradient(
      180deg,
      rgba(15, 23, 42, 0.55) 0%,
      rgba(15, 23, 42, 0.12) 28%,
      rgba(15, 23, 42, 0.28) 62%,
      rgba(15, 23, 42, 0.82) 100%
    ),
    linear-gradient(90deg, rgba(15, 23, 42, 0.5) 0%, rgba(15, 23, 42, 0) 68%);
}

.hero-section__content {
  position: relative;
  z-index: 10;
  height: 100%;
  display: flex;
  align-items: center;
  max-width: var(--container-max);
  margin: 0 auto;
  padding: 0 var(--sp-4);
}

/* 原值 `max-width: 4xl` 是无效 CSS —— 把 Tailwind 的类名当成 CSS 值写了，
   浏览器直接丢弃该声明，导致文字宽度约束完全失效。
   注意：本容器同时包着下方的搜索表单，若直接给容器限宽会把 4 列搜索表单挤变形，
   因此把宽度约束下沉到标题与描述两个子元素上。 */
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
  color: rgba(255, 255, 255, 0.88);
  line-height: var(--lh-body-lg);
  margin-bottom: var(--sp-8);
  text-shadow: 0 1px 8px rgba(15, 23, 42, 0.3);
}

/* 悬浮白卡：半透明 + 毛玻璃 + 大圆角，是"高级感"最直接的一处体现 */
.search-form {
  background-color: rgba(255, 255, 255, 0.96);
  -webkit-backdrop-filter: blur(12px);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: var(--r-lg);
  box-shadow: var(--sh-3);
  padding: var(--sp-3);
  display: grid;
  grid-template-columns: 1fr;
  gap: var(--sp-3);
}

@media (min-width: 768px) {
  .search-form {
    grid-template-columns: repeat(4, 1fr);
    padding: var(--sp-4);
  }
}

.search-form__group {
  display: flex;
  align-items: center;
  padding: var(--sp-2) var(--sp-3);
  background-color: var(--c-bg);
  border: 1px solid var(--c-line);
  border-radius: var(--r-sm);
  transition: border-color var(--dur-base) var(--ease-out),
    box-shadow var(--dur-base) var(--ease-out);
}

.search-form__group:hover {
  border-color: var(--c-primary-300);
  box-shadow: 0 0 0 3px var(--c-primary-50);
}

.search-form__icon {
  color: var(--c-primary-600);
  margin-right: var(--sp-2);
  font-size: var(--fs-body-lg);
  flex-shrink: 0;
}

.search-form__label {
  font-size: var(--fs-caption);
  line-height: 1.4;
  color: var(--c-ink-3);
}

.search-form__hint {
  font-weight: 500;
  color: var(--c-ink);
}

.search-form__btn {
  background-color: var(--c-primary-600);
  color: #ffffff;
  border: none;
  border-radius: var(--r-sm);
  padding: var(--sp-3) var(--sp-4);
  font-family: inherit;
  font-size: var(--fs-body-lg);
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--sp-2);
  transition: background-color var(--dur-base) var(--ease-out),
    box-shadow var(--dur-base) var(--ease-out),
    transform var(--dur-fast) var(--ease-out);
}

.search-form__btn:hover {
  background-color: var(--c-primary-700);
  box-shadow: var(--sh-primary);
}

.search-form__btn:active {
  transform: scale(0.98);
}

/* 覆盖 Element Plus 样式 */
:deep(.custom-input .el-input__wrapper),
:deep(.custom-select .el-select__wrapper) {
  box-shadow: none !important;
  background-color: transparent !important;
  padding: 0;
}

:deep(.custom-input .el-input__inner),
:deep(.custom-select .el-select__selected-item) {
  font-weight: 600;
  color: var(--c-ink);
  font-size: var(--fs-body);
  height: auto;
  line-height: 1.2;
}

.search-input-wrapper {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

/* 说明：原来这里还有 .hero-section__controls / .hero-control__dot 两组样式，
   但模板中从未使用（首页轮播用的是 Element Plus 自带的指示器），已删除。 */

/* 分类导航样式 */
.category-nav {
  padding: var(--sp-8) 0;
  background-color: var(--c-bg);
}

.category-nav__list {
  display: grid;
  /* 移动端由 2 列改为 3 列：56px 的图标在 390px 屏宽下 3 列完全放得下，
     2 列会显得空旷 */
  grid-template-columns: repeat(3, 1fr);
  gap: var(--sp-2);
}

@media (min-width: 640px) {
  .category-nav__list {
    grid-template-columns: repeat(6, 1fr);
    gap: var(--sp-4);
  }
}

.category-nav__item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--sp-4) var(--sp-2);
  border-radius: var(--r-md);
  cursor: pointer;
  transition: background-color var(--dur-base) var(--ease-out);
}

.category-nav__item:hover {
  background-color: var(--c-bg-sub);
}

/* 由正圆改为圆角方块（更现代），并加上悬浮上移 + 主色底 */
.category-nav__icon-container {
  width: 56px;
  height: 56px;
  /* 图标用 currentColor，颜色在这里统一给，子 svg 自动跟随 */
  color: var(--c-primary-600);
  background-color: var(--c-primary-50);
  border-radius: var(--r-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--sp-3);
  cursor: pointer;
  will-change: transform;
  backface-visibility: hidden;
  transition: transform var(--dur-base) var(--ease-out),
    background-color var(--dur-base) var(--ease-out),
    color var(--dur-base) var(--ease-out),
    box-shadow var(--dur-base) var(--ease-out);
}

/* 尺寸统一下沉到 CSS，替掉模板里的 style="width:25px;height:25px" */
.category-nav__icon-container > svg {
  width: 26px;
  height: 26px;
}

.category-nav__item:hover .category-nav__icon-container {
  transform: translateY(-4px);
  color: var(--c-primary-700);
  background-color: var(--c-primary-100);
  box-shadow: var(--sh-2);
}

.category-nav__item:active .category-nav__icon-container {
  transform: translateY(-1px) scale(0.96);
}

/* 说明：原有一条 .category-nav__icon 规则，但模板中没有任何元素使用该类（图标是
   直接放在 .category-nav__icon-container 里的 svg），属于死代码，已删除。 */

.category-nav__text {
  font-size: var(--fs-body);
  font-weight: 500;
  color: var(--c-ink);
  transition: color var(--dur-base) var(--ease-out);
}

.category-nav__item:hover .category-nav__text {
  color: var(--c-primary-600);
}

/* 通用区域标题样式 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: var(--sp-4);
  margin-bottom: var(--sp-8);
}

.section-title {
  font-size: var(--fs-h2);
  line-height: var(--lh-h2);
  font-weight: 600;
  letter-spacing: -0.01em;
  color: var(--c-ink);
}

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

/* 热门目的地样式 */
.destinations-section {
  padding: var(--sp-16) 0;
  /* 原值 #def5e9 是又一处薄荷绿，与蓝色主色系冲突，改为中性底色 */
  background-color: var(--c-bg-sub);
}

.destinations-list {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1.5rem;
}

@media (min-width: 640px) {
  .destinations-list {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (min-width: 1024px) {
  .destinations-list {
    grid-template-columns: repeat(4, 1fr);
  }
}

.destination-card {
  position: relative;
  height: 16rem;
  border-radius: var(--r-lg);
  overflow: hidden;
  box-shadow: var(--sh-2);
  will-change: transform;
  backface-visibility: hidden;
  transition: transform var(--dur-base) var(--ease-out),
    box-shadow var(--dur-base) var(--ease-out);
}

.destination-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--sh-hover);
}

.destination-card__image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--dur-slower) var(--ease-out);
}

.destination-card:hover .destination-card__image {
  transform: scale(1.08);
}

.destination-card__overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to top,
    rgba(15, 23, 42, 0.82) 0%,
    rgba(15, 23, 42, 0.25) 45%,
    rgba(15, 23, 42, 0) 100%
  );
}

.destination-card__content {
  position: absolute;
  bottom: 0;
  left: 0;
  padding: var(--sp-4);
  width: 100%;
}

.destination-card__name {
  font-size: var(--fs-h3);
  line-height: 1.3;
  font-weight: 600;
  color: #ffffff;
  margin-bottom: var(--sp-1);
}

.destination-card__location {
  display: flex;
  align-items: center;
  color: rgba(255, 255, 255, 0.82);
  font-size: var(--fs-caption);
}

.destination-card__location-icon {
  margin-right: var(--sp-1);
  font-size: var(--fs-caption);
}

.destination-card__rating {
  display: flex;
  align-items: center;
  margin-top: var(--sp-2);
}

.rating-tag {
  background-color: rgba(255, 255, 255, 0.22);
  -webkit-backdrop-filter: blur(4px);
  backdrop-filter: blur(4px);
  border-radius: var(--r-full);
  padding: var(--sp-1) var(--sp-2);
  color: #ffffff;
  font-size: var(--fs-caption);
  display: flex;
  align-items: center;
}

.rating-tag__star {
  color: var(--c-star);
  margin-right: 0.25rem;
  font-size: var(--fs-caption);
}

.destination-card__reviews {
  margin-left: var(--sp-2);
  color: rgba(255, 255, 255, 0.82);
  font-size: var(--fs-caption);
}

/* 特惠活动样式
   原背景 var(--c-bg-sub) 是奶黄色，与「薄荷绿 + 蓝」凑成三种底色，
   页面显得杂。改为白底，与上方 destinations 的浅灰底形成克制的交替。 */
.deals-section {
  padding: var(--sp-16) 0;
  background-color: var(--c-bg);
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

/* 精选旅游套餐样式 */
.packages-section {
  padding: var(--sp-16) 0;
  /* 原背景 #f5ecfb 是淡紫色 —— 加上前面的薄荷绿与奶黄，
     整个首页出现三种粉彩底色，是"廉价感"的主要来源之一。改为中性浅灰。 */
  background-color: var(--c-bg-sub);
}

.packages-filter {
  display: none;
  gap: var(--sp-2);
}

@media (min-width: 768px) {
  .packages-filter {
    display: flex;
  }
}

.filter-btn {
  padding: var(--sp-2) var(--sp-4);
  border-radius: var(--r-full);
  font-family: inherit;
  font-size: var(--fs-caption);
  font-weight: 500;
  background-color: var(--c-bg);
  color: var(--c-ink-2);
  border: 1px solid var(--c-line);
  cursor: pointer;
  transition: background-color var(--dur-base) var(--ease-out),
    color var(--dur-base) var(--ease-out),
    border-color var(--dur-base) var(--ease-out);
}

.filter-btn:hover {
  background-color: var(--c-bg-mute);
  border-color: var(--c-ink-4);
}

.filter-btn--active {
  background-color: var(--c-primary-600);
  color: #ffffff;
  border-color: var(--c-primary-600);
}

.packages-list {
  display: grid;
  grid-template-columns: 1fr;
  gap: var(--sp-6);
}

@media (min-width: 768px) {
  .packages-list {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (min-width: 1024px) {
  .packages-list {
    grid-template-columns: repeat(3, 1fr);
  }
}

.package-card {
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

.package-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--sh-hover);
  border-color: transparent;
}

.package-card__image-container {
  position: relative;
  height: 12rem;
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
  margin-bottom: var(--sp-2);
}

.package-card__type-tag {
  background-color: var(--c-primary-50);
  color: var(--c-primary-600);
  font-size: var(--fs-caption);
  padding: var(--sp-1) var(--sp-2);
  border-radius: var(--r-xs);
  margin-right: var(--sp-2);
}

.package-card__rating {
  display: flex;
  align-items: center;
  color: var(--c-star);
  font-size: var(--fs-caption);
}

.package-card__rating-text {
  color: var(--c-ink-3);
  margin-left: var(--sp-1);
}

.package-card__name {
  font-weight: 600;
  color: var(--c-ink);
  margin-bottom: var(--sp-2);
  transition: color var(--dur-base) var(--ease-out);
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.package-card:hover .package-card__name {
  color: var(--c-primary-600);
}

.package-card__desc {
  color: var(--c-ink-3);
  font-size: var(--fs-caption);
  line-height: var(--lh-body);
  margin-bottom: var(--sp-3);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.package-card__sales {
  display: flex;
  align-items: center;
  color: var(--c-ink-3);
  font-size: var(--fs-caption);
  margin-bottom: var(--sp-4);
}

.package-card__sales-icon {
  margin-right: var(--sp-1);
}

/* 评分星：颜色继承自 .package-card__rating 的 --c-star */
.package-card__star {
  font-size: var(--fs-caption);
}

.package-card__star + .package-card__star {
  margin-left: 1px;
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
  color: var(--c-ink-3);
  font-size: var(--fs-caption);
}

.package-card__btn {
  background-color: var(--c-primary-600);
  color: #ffffff;
  font-family: inherit;
  font-size: var(--fs-caption);
  font-weight: 500;
  border: none;
  border-radius: var(--r-sm);
  padding: var(--sp-2) var(--sp-4);
  cursor: pointer;
  white-space: nowrap;
  transition: background-color var(--dur-base) var(--ease-out),
    box-shadow var(--dur-base) var(--ease-out),
    transform var(--dur-fast) var(--ease-out);
}

.package-card__btn:hover {
  background-color: var(--c-primary-700);
  box-shadow: var(--sh-primary);
}

.package-card__btn:active {
  transform: scale(0.97);
}

.load-more-btn {
  display: block;
  margin: var(--sp-10) auto 0;
  padding: var(--sp-3) var(--sp-6);
  background-color: var(--c-bg);
  border: 1px solid var(--c-line);
  border-radius: var(--r-sm);
  color: var(--c-ink-2);
  font-family: inherit;
  font-size: var(--fs-body);
  font-weight: 500;
  cursor: pointer;
  transition: background-color var(--dur-base) var(--ease-out),
    border-color var(--dur-base) var(--ease-out),
    box-shadow var(--dur-base) var(--ease-out);
}

.load-more-btn:hover {
  background-color: var(--c-bg-mute);
  border-color: var(--c-primary-300);
  box-shadow: var(--sh-1);
}

/* 旅游攻略样式 */
.guides-section {
  padding: var(--sp-16) 0;
  background-color: var(--c-bg);
}

.guides-list {
  display: grid;
  grid-template-columns: 1fr;
  gap: var(--sp-6);
}

@media (min-width: 768px) {
  .guides-list {
    grid-template-columns: 2fr 1fr;
  }
}

.guide-card--featured {
  grid-column: 1 / -1;
}

@media (min-width: 768px) {
  .guide-card--featured {
    grid-row: 1 / 4;
  }
}

.guide-card__content--featured {
  background-color: var(--c-bg);
  border: 1px solid var(--c-line);
  border-radius: var(--r-lg);
  overflow: hidden;
  box-shadow: var(--sh-1);
  display: flex;
  flex-direction: column;
  height: 100%;
  will-change: transform;
  backface-visibility: hidden;
  transition: transform var(--dur-base) var(--ease-out),
    box-shadow var(--dur-base) var(--ease-out),
    border-color var(--dur-base) var(--ease-out);
}

.guide-card__content--featured:hover {
  transform: translateY(-4px);
  box-shadow: var(--sh-hover);
  border-color: transparent;
}

@media (min-width: 768px) {
  .guide-card__content--featured {
    flex-direction: row;
  }
}

.guide-card__image--featured {
  width: 100%;
  height: 16rem;
  object-fit: cover;
  transition: transform var(--dur-slower) var(--ease-out);
}

@media (min-width: 768px) {
  .guide-card__image--featured {
    width: 50%;
    height: auto;
  }
}

.guide-card__content--featured:hover .guide-card__image--featured {
  transform: scale(1.05);
}

.guide-card__text--featured {
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

@media (min-width: 768px) {
  .guide-card__text--featured {
    width: 50%;
  }
}

.guide-card__tags {
  display: flex;
  align-items: center;
  margin-bottom: var(--sp-3);
}

.guide-card__hot-tag {
  background-color: var(--c-accent-soft);
  color: var(--c-accent-strong);
  font-size: var(--fs-caption);
  font-weight: 500;
  padding: var(--sp-1) var(--sp-2);
  border-radius: var(--r-xs);
  margin-right: var(--sp-4);
}

.guide-card__views {
  display: flex;
  align-items: center;
  color: var(--c-ink-4);
  font-size: var(--fs-caption);
  margin-right: var(--sp-4);
}

.guide-card__views-icon {
  margin-right: var(--sp-1);
}

.guide-card__comments {
  display: flex;
  align-items: center;
  color: var(--c-ink-4);
  font-size: var(--fs-caption);
}

.guide-card__comments-icon {
  margin-right: var(--sp-1);
}

.guide-card__title--featured {
  font-size: var(--fs-h2);
  line-height: var(--lh-h2);
  font-weight: 600;
  color: var(--c-ink);
  margin-bottom: var(--sp-4);
  transition: color var(--dur-base) var(--ease-out);
}

.guide-card__content--featured:hover .guide-card__title--featured {
  color: var(--c-primary-600);
}

.guide-card__desc--featured {
  color: var(--c-ink-3);
  line-height: var(--lh-body-lg);
  margin-bottom: var(--sp-4);
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.guide-card__author {
  display: flex;
  align-items: center;
}

.guide-card__author-avatar {
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 50%;
  object-fit: cover;
  /* 父级 .guide-card__author 是 flex 容器，flex 项默认可收缩 —— 作者名一长，
     头像宽度就会被压到 2.5rem 以下（实测 25.2×40），height 固定则不变形变椭圆 */
  flex-shrink: 0;
}

.guide-card__author-info {
  margin-left: var(--sp-3);
}

.guide-card__author-name {
  font-weight: 500;
  color: var(--c-ink);
}

.guide-card__date {
  color: var(--c-ink-4);
  font-size: var(--fs-caption);
}

.guide-card--small {
  display: flex;
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

.guide-card--small:hover {
  transform: translateY(-4px);
  box-shadow: var(--sh-hover);
  border-color: transparent;
}

.guide-card__image--small {
  width: 33.333%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--dur-slower) var(--ease-out);
}

.guide-card--small:hover .guide-card__image--small {
  transform: scale(1.06);
}

.guide-card__text--small {
  width: 66.666%;
  padding: var(--sp-4);
}

.guide-card__title--small {
  font-weight: 600;
  color: var(--c-ink);
  margin-bottom: var(--sp-2);
  transition: color var(--dur-base) var(--ease-out);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.guide-card--small:hover .guide-card__title--small {
  color: var(--c-primary-600);
}

.guide-card__date--small {
  color: var(--c-ink-4);
  font-size: var(--fs-caption);
}

.guide-card__date-icon {
  margin-right: var(--sp-1);
}

/* 用户评价样式 */
.reviews-section {
  padding: var(--sp-16) 0;
  background-color: var(--c-bg-sub);
}

.reviews-title {
  font-size: var(--fs-h2);
  line-height: var(--lh-h2);
  font-weight: 600;
  letter-spacing: -0.01em;
  color: var(--c-ink);
  margin-bottom: var(--sp-8);
  text-align: center;
}

.reviews-list {
  display: grid;
  grid-template-columns: 1fr;
  gap: var(--sp-6);
}

@media (min-width: 768px) {
  .reviews-list {
    grid-template-columns: repeat(3, 1fr);
  }
}

.review-card {
  background-color: var(--c-bg);
  padding: var(--sp-6);
  border: 1px solid var(--c-line);
  border-radius: var(--r-lg);
  box-shadow: var(--sh-1);
  will-change: transform;
  backface-visibility: hidden;
  transition: transform var(--dur-base) var(--ease-out),
    box-shadow var(--dur-base) var(--ease-out),
    border-color var(--dur-base) var(--ease-out);
}

.review-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--sh-hover);
  border-color: transparent;
}

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
  /* 同上：.review-card__user 是 flex 容器，用户名过长时头像会被压扁（实测 14×48） */
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

.review-card__footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: var(--fs-caption);
  color: var(--c-ink-3);
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
  /* 原值 `max-width: 2xl` 为无效 CSS（Tailwind 类名误当 CSS 值），此处改为实际像素 */
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
