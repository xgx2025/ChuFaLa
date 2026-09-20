<template>
  <div class="root-container">
    <main class="main-content">
      <router-view></router-view>
      <!-- 英雄区域 -->
      <section class="hero-section">
        <!-- 轮播图 -->
       <div class="hero-section__slides">
          <el-carousel autoplay :interval="3000" indicator-position="bottom" arrow="hover" height="500px">
            <el-carousel-item v-for="(slide, index) in slides" :key="index">
              <img :src="slide.image" :alt="`轮播图${index+1}`" class="carousel-image" >
            </el-carousel-item>
          </el-carousel>
        </div>
        
        <!-- 搜索框 + 标题 -->
        <div class="hero-section__content">
          <div class="hero-section__text">
            <h2 class="hero-section__title">探索世界的每一个角落</h2>
            <p class="hero-section__desc">从热门景点到隐秘宝藏，让我们带你领略不一样的旅行体验</p>
            
            <!-- 搜索框 -->
            <form class="search-form" @submit.prevent="handleSearch">
              <div class="search-form__group">
                <i class="fa fa-search search-form__icon"></i>
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
                <i class="fa fa-tags search-form__icon"></i>
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
                <i class="fa fa-star search-form__icon"></i>
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
              <Ticket style="width: 25px;height: 25px;"/>
              </div>
              <span class="category-nav__text">景点门票</span>
            </div>
            
            <div class="category-nav__item">
              <div class="category-nav__icon-container" @click="router.push(`/flight`)">
                <Plane style="width: 25px;height: 25px;"/>
              </div>
              <span class="category-nav__text">机票</span>
            </div>
            
            <div class="category-nav__item">
              <div class="category-nav__icon-container" @click="router.push(`/hotel`)">
                <Hotel style="width: 25px;height: 25px;"/>
              </div>
              <span class="category-nav__text">酒店</span>
            </div>
            
            <div class="category-nav__item">
              <div class="category-nav__icon-container" @click="router.push(`/package`)">
                <TravelPackage style="width: 25px;height: 25px;"/>
              </div>
              <span class="category-nav__text">旅游套餐</span>
            </div>
            
            <div class="category-nav__item">
              <div class="category-nav__icon-container">
                <Taxi style="width: 25px;height: 25px;"/>
              </div>
              <span class="category-nav__text">租车</span>
            </div>
            
            <div class="category-nav__item">
              <div class="category-nav__icon-container">
                <More style="width: 25px;height: 25px;"/>
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
              <i class="fa fa-arrow-right section-more__icon"></i>
            </a>
          </div>
          
          <div class="destinations-list">
            <div 
              class="destination-card"
              v-for="(destination, index) in destinations" 
              :key="index"
            >
              <img 
                :src="destination.image" 
                :alt="`图片展示的是${destination.name}的风景`" 
                class="destination-card__image"
              >
              <div class="destination-card__overlay"></div>
              <div class="destination-card__content">
                <h3 class="destination-card__name">{{ destination.name }}</h3>
                <div class="destination-card__location">
                  <i class="fa fa-map-marker destination-card__location-icon"></i>
                  <span>{{ destination.location }}</span>
                </div>
                <div class="destination-card__rating">
                  <div class="rating-tag">
                    <i class="fa fa-star rating-tag__star"></i>
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
              <i class="fa fa-arrow-right section-more__icon"></i>
            </a>
          </div>
          
          <div class="deals-container">
            <div class="deals-scroll">
              <div class="deals-list">
                <div class="deal-card" v-for="(deal, index) in deals" :key="index">
                  <div class="deal-card__image-container">
                    <img :src="deal.image" class="deal-card__image">
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
            >
              <div class="package-card__image-container">
                <img 
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
                    <i class="fa fa-star"></i>
                    <i class="fa fa-star"></i>
                    <i class="fa fa-star"></i>
                    <i class="fa fa-star"></i>
                    <i class="fa fa-star-half-o"></i>
                    <span class="package-card__rating-text">{{ tourPackage.rating }}</span>
                  </div>
                </div>
                
                <h3 class="package-card__name">{{ tourPackage.name }}</h3>
                <p class="package-card__desc">{{ tourPackage.description }}</p>
                
                <div class="package-card__sales">
                  <i class="fa fa-users package-card__sales-icon"></i>
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
              <i class="fa fa-arrow-right section-more__icon"></i>
            </a>
          </div>
          
          <div class="guides-list">
            <!-- 主要攻略卡片 -->
            <div class="guide-card--featured">
              <div class="guide-card__content--featured">
                <img 
                  src="https://picsum.photos/600/400?random=10" 
                  alt="图片展示的是热门旅游攻略的相关风景" 
                  class="guide-card__image--featured"
                >
                <div class="guide-card__text--featured">
                  <div>
                    <div class="guide-card__tags">
                      <span class="guide-card__hot-tag">热门</span>
                      <span class="guide-card__views">
                        <i class="fa fa-eye guide-card__views-icon"></i> 2.5k 阅读
                      </span>
                      <span class="guide-card__comments">
                        <i class="fa fa-comment guide-card__comments-icon"></i> 128 评论
                      </span>
                    </div>
                    <h3 class="guide-card__title--featured">2023年夏季最值得去的10个海岛，你去过几个？</h3>
                    <p class="guide-card__desc--featured">
                      夏天来了，海岛游成为了许多人的首选。本文为你推荐10个性价比超高的海岛，从国内到国外，从热门到小众，总有一个适合你。带上防晒霜，一起去海边吧！
                    </p>
                  </div>
                  <div class="guide-card__author">
                    <img 
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
            <div class="guide-card--small">
              <img 
                src="https://picsum.photos/300/200?random=11" 
                alt="图片展示的是美食攻略的相关内容" 
                class="guide-card__image--small"
              >
              <div class="guide-card__text--small">
                <h3 class="guide-card__title--small">成都美食攻略：除了火锅，这些小吃也不能错过</h3>
                <p class="guide-card__date--small">
                  <i class="fa fa-clock-o guide-card__date-icon"></i> 5天前
                </p>
              </div>
            </div>
            
            <div class="guide-card--small">
              <img 
                src="https://picsum.photos/300/200?random=12" 
                alt="图片展示的是摄影攻略的相关风景" 
                class="guide-card__image--small"
              >
              <div class="guide-card__text--small">
                <h3 class="guide-card__title--small">青海湖摄影攻略：最佳拍摄地点和时间</h3>
                <p class="guide-card__date--small">
                  <i class="fa fa-clock-o guide-card__date-icon"></i> 1周前
                </p>
              </div>
            </div>
            
            <div class="guide-card--small">
              <img 
                src="https://picsum.photos/300/200?random=13" 
                alt="图片展示的是徒步攻略的相关路线" 
                class="guide-card__image--small"
              >
              <div class="guide-card__text--small">
                <h3 class="guide-card__title--small">徒步虎跳峡：新手也能完成的经典路线</h3>
                <p class="guide-card__date--small">
                  <i class="fa fa-clock-o guide-card__date-icon"></i> 2周前
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
          <i class="fa fa-paper-plane decor-icon--plane"></i>
          <i class="fa fa-sun-o decor-icon--sun"></i>
          <i class="fa fa-ship decor-icon--ship"></i>
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
    </main>
  </div>
  <el-backtop :right="100" :bottom="100" style="color:rgb(82, 233, 200);"/>
</template>

<script setup>
import { ref, onMounted, onUnmounted} from 'vue';
import { getUserInfoService } from '@/api/user';
import {useUserInfoStore} from '@/stores/userInfo'
import router from '@/router'
import { useTokenStore } from '@/stores/token'
import {ElMessageBox,ElNotification} from 'element-plus'
import {Plane,Hotel,Taxi,TravelPackage,More} from '@/components/Icon.vue'
import {Ticket,Search} from '@element-plus/icons-vue'

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

.header-nav__logo-img {
  height: 54px; 
  width: auto;  
  vertical-align: middle; 
  margin-right: 10px; 
}

.root-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f9fafb;
}

.main-content {
  flex-grow: 1;
  background-color: #1890ff;
}

.section-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

/* 导航栏样式 */
.header-nav {
  position: sticky;
  top: 0;
  z-index: 50;
  transition: all 0.3s ease;
}

.header-nav--unscrolled {
  background-color: transparent;
  padding: 1rem 0;
}

.header-nav--scrolled {
  background-color: #ffffff;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  padding: 0.5rem 0;
}

.header-nav__container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-nav__logo-text {
  outline: none;
  font-size: 1.5rem;
  font-weight: bold;
  color: #2563eb;
  display: flex;
  align-items: center;
}

.header-nav__logo-icon {
  margin-right: 0.5rem;
}

.header-nav__desktop-nav {
  display: none;
  align-items: center;
  gap: 2rem;
}

@media (min-width: 768px) {
  .header-nav__desktop-nav {
    display: flex;
  }
}

.header-nav__nav-link {
  font-weight: 500;
  color: #4b5563;
  transition: color 0.2s ease;
  text-decoration: none;
}

.header-nav__nav-link:hover {
  color: #2563eb;
}

.header-nav__nav-link--active {
  color: #2563eb;
}

.header-nav__user-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}


.header-nav__login-btn {
  outline: none;
  display: none;
  align-items: center;
  color: #4b5563;
  background: none;
  border: none;
  cursor: pointer;
  transition: color 0.2s ease;
  gap: 0.5rem;
}

@media (min-width: 768px) {
  .header-nav__login-btn {
    display: flex;
  }
}

.header-nav__login-btn:hover {
  color: #2563eb;
}

.header-nav__mobile-btn {
  display: block;
  color: #4b5563;
  background: none;
  border: none;
  cursor: pointer;
}

@media (min-width: 768px) {
  .header-nav__mobile-btn {
    display: none;
  }
}

.header-nav__mobile-menu {
  display: block;
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background-color: #ffffff;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  transform: translateY(-100%);
  transition: transform 0.3s ease-in-out;
  z-index: -1;
}

@media (min-width: 768px) {
  .header-nav__mobile-menu {
    display: none;
  }
}

.mobile-menu--open {
  transform: translateY(0);
  z-index: 40;
}

.header-nav__mobile-menu-container {
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.header-nav__mobile-link {
  padding: 0.5rem 0;
  font-weight: 500;
  color: #4b5563;
  text-decoration: none;
  transition: color 0.2s ease;
}

.header-nav__mobile-link:hover {
  color: #2563eb;
}

.header-nav__mobile-link--active {
  color: #2563eb;
}

.header-nav__mobile-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding-top: 0.5rem;
  margin-top: 0.5rem;
  border-top: 1px solid #f3f4f6;
}

/* 英雄区域样式 */
.hero-section {
  position: relative;
  height: 500px; 
  overflow: hidden;
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
  background-color: #ffffff;
  border-radius: 0.75rem;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.2);
  padding: 0.5rem 1rem;
  display: grid;
  grid-template-columns: 1fr;
  gap: 0.5rem;
}

@media (min-width: 768px) {
  .search-form {
    grid-template-columns: repeat(4, 1fr);
    padding: 1rem;
  }
}

.search-form__group {
  display: flex;
  align-items: center;
  padding: 0.5rem;
  border: 1px solid #e5e7eb;
  border-radius: 0.5rem;
}

.search-form__icon {
  color: #2563eb;
  margin-right: 0.5rem;
}

.search-form__label {
  font-size: 0.75rem;
  color: #9ca3af;
}

.search-form__hint {
  font-weight: 500;
  color: #1f2937;
}

.search-form__btn {
  background-color: #2563eb;
  color: #ffffff;
  border: none;
  border-radius: 0.5rem;
  padding: 0.75rem 1rem;
  cursor: pointer;
  transition: background-color 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-form__btn:hover {
  background-color: #1d4ed8;
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
  color: #1f2937;
  font-size: 0.95rem;
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

/* 分类导航样式 */
.category-nav {
  padding: 2rem 0;
  background-color: #ffffff;
}

.category-nav__list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
}

@media (min-width: 640px) {
  .category-nav__list {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (min-width: 768px) {
  .category-nav__list {
    grid-template-columns: repeat(6, 1fr);
  }
}

.category-nav__item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0.75rem;
  border-radius: 0.5rem;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.category-nav__item:hover {
  background-color: #f9fafb;
}

.category-nav__icon-container {
  width: 3rem;
  height: 3rem;
  background-color: #dbeafe;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 0.5rem;
}

.category-nav__icon {
  color: #2563eb;
  font-size: 1.5rem;
}

.category-nav__text {
  font-size: 0.875rem;
  font-weight: 500;
  color: #1f2937;
}

/* 通用区域标题样式 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.section-title {
  font-size: 1.5rem;
  font-weight: bold;
  color: #1f2937;
}

.section-more {
  color: #2563eb;
  font-weight: 500;
  text-decoration: none;
  display: flex;
  align-items: center;
  transition: color 0.2s ease;
}

.section-more:hover {
  color: #1d4ed8;
}

.section-more__icon {
  margin-left: 0.25rem;
  font-size: 0.875rem;
}

/* 热门目的地样式 */
.destinations-section {
  padding: 3rem 0;
  background-color: #def5e9;
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
  border-radius: 0.75rem;
  overflow: hidden;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  height: 16rem;
}

.destination-card:hover {
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.2);
}

.destination-card__image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.7s ease;
}

.destination-card:hover .destination-card__image {
  transform: scale(1.1);
}

.destination-card__overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.7), rgba(0, 0, 0, 0.2), transparent);
}

.destination-card__content {
  position: absolute;
  bottom: 0;
  left: 0;
  padding: 1rem;
  width: 100%;
}

.destination-card__name {
  font-size: 1.25rem;
  font-weight: bold;
  color: #ffffff;
  margin-bottom: 0.25rem;
}

.destination-card__location {
  display: flex;
  align-items: center;
  color: #f3f4f6;
  font-size: 0.875rem;
}

.destination-card__location-icon {
  margin-right: 0.25rem;
  font-size: 0.75rem;
}

.destination-card__rating {
  display: flex;
  align-items: center;
  margin-top: 0.5rem;
}

.rating-tag {
  background-color: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(4px);
  border-radius: 9999px;
  padding: 0.25rem 0.5rem;
  color: #ffffff;
  font-size: 0.875rem;
  display: flex;
  align-items: center;
}

.rating-tag__star {
  color: #facc15;
  margin-right: 0.25rem;
  font-size: 0.75rem;
}

.destination-card__reviews {
  margin-left: 0.5rem;
  color: #ffffff;
  font-size: 0.875rem;
}

/* 特惠活动样式 */
.deals-section {
  padding: 3rem 0;
  background-color: #f9eed7;
}

.deals-container {
  position: relative;
}

.deals-scroll {
  overflow-x: auto;
  padding-bottom: 1rem;
}

.deals-list {
  display: flex;
  gap: 1rem;
  width: max-content;
}

.deal-card {
  width: 18rem;
  background-color: #f9fafb;
  border-radius: 0.75rem;
  overflow: hidden;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
  transition: box-shadow 0.2s ease;
}

.deal-card:hover {
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.deal-card__image-container {
  position: relative;
  height: 12rem;
}

.deal-card__image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.deal-card:hover .deal-card__image {
  transform: scale(1.05);
}

.deal-card__discount {
  position: absolute;
  top: 0.75rem;
  left: 0.75rem;
  background-color: #ef4444;
  color: #ffffff;
  font-size: 0.75rem;
  font-weight: bold;
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
}

.deal-card__content {
  padding: 1rem;
}

.deal-card__name {
  font-weight: bold;
  color: #1f2937;
  margin-bottom: 0.25rem;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.deal-card__desc {
  color: #6b7280;
  font-size: 0.875rem;
  margin-bottom: 0.75rem;
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
  color: #ef4444;
  font-weight: bold;
  font-size: 1.25rem;
}

.deal-card__original-price {
  color: #9ca3af;
  font-size: 0.875rem;
  text-decoration: line-through;
  margin-left: 0.25rem;
}

.deal-card__btn {
  background-color: #2563eb;
  color: #ffffff;
  font-size: 0.875rem;
  border: none;
  border-radius: 9999px;
  padding: 0.25rem 0.75rem;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.deal-card__btn:hover {
  background-color: #1d4ed8;
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
  background-color: #d1d5db;
}

.deal-indicator--active {
  background-color: #2563eb;
}

/* 精选旅游套餐样式 */
.packages-section {
  padding: 3rem 0;
  background-color: #f5ecfb;
}

.packages-filter {
  display: none;
  gap: 0.5rem;
}

@media (min-width: 768px) {
  .packages-filter {
    display: flex;
  }
}

.filter-btn {
  padding: 0.5rem 1rem;
  border-radius: 9999px;
  font-size: 0.875rem;
  font-weight: 500;
  background-color: #ffffff;
  color: #4b5563;
  border: 1px solid #e5e7eb;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.filter-btn:hover {
  background-color: #f3f4f6;
}

.filter-btn--active {
  background-color: #2563eb;
  color: #ffffff;
  border-color: #2563eb;
}

.packages-list {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1.5rem;
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
  background-color: #ffffff;
  border-radius: 0.75rem;
  overflow: hidden;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  transition: box-shadow 0.2s ease;
}

.package-card:hover {
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
}

.package-card__image-container {
  position: relative;
  height: 12rem;
}

.package-card__image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.package-card:hover .package-card__image {
  transform: scale(1.05);
}

.package-card__days-tag {
  position: absolute;
  top: 0.75rem;
  right: 0.75rem;
  background-color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(4px);
  color: #2563eb;
  font-size: 0.75rem;
  font-weight: 500;
  padding: 0.25rem 0.5rem;
  border-radius: 9999px;
}

.package-card__content {
  padding: 1rem;
}

.package-card__header {
  display: flex;
  align-items: center;
  margin-bottom: 0.5rem;
}

.package-card__type-tag {
  background-color: #dbeafe;
  color: #2563eb;
  font-size: 0.75rem;
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
  margin-right: 0.5rem;
}

.package-card__rating {
  display: flex;
  align-items: center;
  color: #facc15;
  font-size: 0.75rem;
}

.package-card__rating-text {
  color: #6b7280;
  margin-left: 0.25rem;
}

.package-card__name {
  font-weight: bold;
  color: #1f2937;
  margin-bottom: 0.5rem;
  transition: color 0.2s ease;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.package-card:hover .package-card__name {
  color: #2563eb;
}

.package-card__desc {
  color: #6b7280;
  font-size: 0.875rem;
  margin-bottom: 0.75rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.package-card__sales {
  display: flex;
  align-items: center;
  color: #6b7280;
  font-size: 0.875rem;
  margin-bottom: 1rem;
}

.package-card__sales-icon {
  margin-right: 0.25rem;
}

.package-card__price-area {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.package-card__price {
  color: #ef4444;
  font-weight: bold;
  font-size: 1.25rem;
}

.package-card__price-unit {
  color: #6b7280;
  font-size: 0.875rem;
}

.package-card__btn {
  background-color: #2563eb;
  color: #ffffff;
  border: none;
  border-radius: 0.5rem;
  padding: 0.5rem 1rem;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.package-card__btn:hover {
  background-color: #1d4ed8;
}

.load-more-btn {
  display: block;
  margin: 2rem auto 0;
  padding: 0.75rem 1.5rem;
  background-color: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 0.5rem;
  color: #4b5563;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.load-more-btn:hover {
  background-color: #f3f4f6;
}

/* 旅游攻略样式 */
.guides-section {
  padding: 3rem 0;
  background-color: #ffffff;
}

.guides-list {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1.5rem;
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
  background-color: #f9fafb;
  border-radius: 0.75rem;
  overflow: hidden;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
  transition: box-shadow 0.2s ease;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.guide-card__content--featured:hover {
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
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
  transition: transform 0.5s ease;
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
  margin-bottom: 0.75rem;
}

.guide-card__hot-tag {
  background-color: #fef3c7;
  color: #d97706;
  font-size: 0.75rem;
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
  margin-right: 1rem;
}

.guide-card__views {
  color: #9ca3af;
  font-size: 0.875rem;
  margin-right: 1rem;
}

.guide-card__views-icon {
  margin-right: 0.25rem;
}

.guide-card__comments {
  color: #9ca3af;
  font-size: 0.875rem;
}

.guide-card__comments-icon {
  margin-right: 0.25rem;
}

.guide-card__title--featured {
  font-size: 1.5rem;
  font-weight: bold;
  color: #1f2937;
  margin-bottom: 1rem;
  transition: color 0.2s ease;
}

.guide-card__content--featured:hover .guide-card__title--featured {
  color: #2563eb;
}

.guide-card__desc--featured {
  color: #6b7280;
  margin-bottom: 1rem;
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
}

.guide-card__author-info {
  margin-left: 0.75rem;
}

.guide-card__author-name {
  font-weight: 500;
  color: #1f2937;
}

.guide-card__date {
  color: #9ca3af;
  font-size: 0.875rem;
}

.guide-card--small {
  display: flex;
  background-color: #f9fafb;
  border-radius: 0.75rem;
  overflow: hidden;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
  transition: box-shadow 0.2s ease;
}

.guide-card--small:hover {
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.guide-card__image--small {
  width: 33.333%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.guide-card--small:hover .guide-card__image--small {
  transform: scale(1.05);
}

.guide-card__text--small {
  width: 66.666%;
  padding: 1rem;
}

.guide-card__title--small {
  font-weight: bold;
  color: #1f2937;
  margin-bottom: 0.5rem;
  transition: color 0.2s ease;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.guide-card--small:hover .guide-card__title--small {
  color: #2563eb;
}

.guide-card__date--small {
  color: #9ca3af;
  font-size: 0.875rem;
}

.guide-card__date-icon {
  margin-right: 0.25rem;
}

/* 用户评价样式 */
.reviews-section {
  padding: 3rem 0;
  background-color: #f9fafb;
}

.reviews-title {
  font-size: 1.5rem;
  font-weight: bold;
  color: #1f2937;
  margin-bottom: 2rem;
  text-align: center;
}

.reviews-list {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1.5rem;
}

@media (min-width: 768px) {
  .reviews-list {
    grid-template-columns: repeat(3, 1fr);
  }
}

.review-card {
  background-color: #ffffff;
  padding: 1.5rem;
  border-radius: 0.75rem;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
  transition: box-shadow 0.2s ease;
}

.review-card:hover {
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.review-card__user {
  display: flex;
  align-items: center;
  margin-bottom: 1rem;
}

.review-card__avatar {
  width: 3rem;
  height: 3rem;
  border-radius: 50%;
  object-fit: cover;
}

.review-card__user-info {
  margin-left: 1rem;
}

.review-card__user-name {
  font-weight: bold;
  color: #1f2937;
}

.review-card__stars {
  display: flex;
  color: #facc15;
  font-size: 0.75rem;
  margin-top: 0.25rem;
}

.review-card__star--empty {
  color: #e5e7eb;
}

.review-card__content {
  color: #6b7280;
  font-style: italic;
  margin-bottom: 1rem;
}

.review-card__footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.875rem;
  color: #6b7280;
}

/* 订阅区域样式 */
.subscribe-section {
  padding: 4rem 0;
  background-color: #2563eb;
  position: relative;
  overflow: hidden;
}

.subscribe-section__decor {
  position: absolute;
  inset: 0;
  opacity: 0.1;
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
  max-width: 2xl;
  margin: 0 auto;
  text-align: center;
}

.subscribe-section__title {
  font-size: 1.875rem;
  font-weight: bold;
  color: #ffffff;
  margin-bottom: 1rem;
}

.subscribe-section__desc {
  color: #dbeafe;
  margin-bottom: 2rem;
}

.subscribe-form {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

@media (min-width: 640px) {
  .subscribe-form {
    flex-direction: row;
  }
}

.subscribe-form__input {
  flex-grow: 1;
  padding: 0.75rem 1rem;
  border-radius: 0.5rem;
  border: none;
  outline: none;
  transition: box-shadow 0.2s ease;
}

.subscribe-form__input:focus {
  box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.5);
}

.subscribe-form__btn {
  background-color: #facc15;
  color: #1f2937;
  font-weight: bold;
  border: none;
  border-radius: 0.5rem;
  padding: 0.75rem 1.5rem;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.subscribe-form__btn:hover {
  background-color: #fbbf24;
}

.subscribe-section__privacy {
  color: #bfdbfe;
  font-size: 0.875rem;
  margin-top: 1rem;
}


/* 滚动条样式优化 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}


/* 3. 核心样式：调整用户区域布局+会员标签美观度 */
.header-nav__user-actions {
  display: flex;
  align-items: center;
  gap: 16px; /* 退出图标与用户区域的间距 */
}

/* 下拉触发区：头像与用户信息横向对齐 */
.header-nav__login-btn {
  display: flex;
  align-items: center;
  gap: 8px; /* 头像与文字的间距 */
  padding: 4px 8px;
  cursor: pointer;
}

/* 用户名+会员等级：垂直居中排列 */
.user-info-wrapper {
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: center; /* 文字与会员标签居中对齐 */
}

/* 用户名样式：简洁清晰 */
.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

/* 会员标签基础样式：圆角+内边距+紧凑布局 */
.member-tag {
  display: flex;
  align-items: center;
  gap: 4px; /* 图标与文字的间距 */
  padding: 1px 6px;
  border-radius: 12px; /* 大圆角更显柔和 */
  font-size: 12px;
  font-weight: 400;
}

/* 会员图标大小控制 */
.member-icon {
  font-size: 20px;
}

/* 4. 不同会员等级的颜色方案（美观且区分度高） */
/* 普通会员：浅灰 */
.member-tag--normal {
  background-color: #f5f7fa;
  color: #666;
}

/* 白银会员：浅蓝 */
.member-tag--silver {
  background-color: #e6f4ff;
  color: #1890ff;
}

/* 黄金会员：浅金 */
.member-tag--gold {
  background: linear-gradient(120deg, #fffbe6, #fff1cc); /* 渐变更美观 */
  color: #fa8c16;
}

/* 钻石会员：深蓝渐变 */
.member-tag--diamond {
  background: linear-gradient(120deg, #e6f7ff, #bae7ff);
  color: #1890ff;
}

/* 退出图标样式：与整体协调 */
.logout-icon {
  font-size: 18px;
  color: #666;
  cursor: pointer;
  transition: color 0.2s;
}

.logout-icon:hover {
  color: #1890ff;
}
</style>
