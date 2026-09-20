<!-- Layout.vue（全局壳层：导航栏 + 内容区 + 页脚） -->
<template>
  <div class="app-container">
    <!-- 导航栏 -->
    <header
      class="header-nav"
      :class="scrolled ? 'header-nav--scrolled' : 'header-nav--unscrolled'"
    >
      <div class="header-nav__container">
        <div class="header-nav__logo">
          <!-- 这里原来是 <h1>。导航 logo 是站点标识而不是页面标题，
               用 h1 会让**每一个**页面都多出一个内容为"出发啦"的一级标题，
               与页面自身真正的 h1 冲突（Flight / Guide / Profile 等页面实测都是双 h1）。
               改为 div 后，全站每个页面只剩一个 h1。 -->
          <div class="header-nav__logo-text">
            <router-link to="/" class="header-nav__logo-link">
              <img src="@/assets/logo2.png" class="header-nav__logo-img" alt="出发啦" />
            </router-link>
            <span>出发啦</span>
          </div>
        </div>

        <!-- 导航链接 -->
        <nav class="header-nav__desktop-nav">
          <router-link to="/" class="header-nav__nav-link" exact-active-class="header-nav__nav-link--active" exact>首页</router-link>
          <router-link to="/attraction" class="header-nav__nav-link" exact-active-class="header-nav__nav-link--active">景点</router-link>
          <router-link to="/hotel" class="header-nav__nav-link" exact-active-class="header-nav__nav-link--active">酒店</router-link>
          <router-link to="/flight" class="header-nav__nav-link" exact-active-class="header-nav__nav-link--active">机票</router-link>
          <router-link to="/package" class="header-nav__nav-link" exact-active-class="header-nav__nav-link--active">旅游套餐</router-link>
          <router-link to="/guide" class="header-nav__nav-link" exact-active-class="header-nav__nav-link--active">智能规划</router-link>
        </nav>

        <!-- 用户功能 -->
        <div class="header-nav__user-actions">
          <template v-if="userInfoStore.info.id">
            <el-dropdown placement="bottom-end" trigger="hover">
              <div class="header-nav__login-btn">
                <!-- size 的合法值只有 "" / "default" / "small" / "large"（或数字），
                     原写法 "medium" 非法，实际渲染的是默认值 default（40px）。
                     显式写成 default，行为不变但不再刷校验警告。 -->
                <el-avatar :src="userInfoStore.info.avatar" size="default" class="user-avatar" />
                <div class="user-info-wrapper">
                  <span class="user-name">{{ userInfoStore.info.username }}</span>
                  <div
                    class="member-tag"
                    :class="`member-tag--${userInfoStore.info.vip === 1 ? 'diamond' : 'normal'}`"
                  >
                    <el-icon class="member-icon">
                      <template v-if="userInfoStore.info.vip === 1">
                        <SuperVip />
                      </template>
                      <template v-else>
                        <NormalVip />
                      </template>
                    </el-icon>
                    <span class="member-text">
                      {{ userInfoStore.info.vip === 1 ? '高级会员' : '普通会员' }}
                    </span>
                  </div>
                </div>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="">我的积分</el-dropdown-item>
                  <el-dropdown-item>我的收藏</el-dropdown-item>
                  <el-dropdown-item @click="router.push('/my/order/hotel')">我的订单</el-dropdown-item>
                  <el-dropdown-item @click="router.push('/my/subscription')">开通会员</el-dropdown-item>
                  <el-dropdown-item @click="router.push('/my/profile')">个人中心</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>

            <button type="button" class="logout-btn" title="退出登录" @click="logout">
              <el-icon><Logout /></el-icon>
            </button>
          </template>

          <template v-else>
            <button type="button" class="header-nav__login-btn" @click="router.push('/login')">
              <span class="user-name">登录 / 注册</span>
            </button>
          </template>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="main-content">
      <!--
        ⚠️ 约束：所有页面组件必须是「单根节点」。
        <transition mode="out-in"> 的机制是「等旧页 leave 过渡跑完再插入新页」，
        而 leave 过渡只能作用于真实元素。若某个页面模板有多个顶层节点
        （典型写法：根 <div> 旁边再放一个平级的 <el-backtop>），
        组件就会渲染成 Fragment 根 —— 过渡拿不到元素，状态机永远停在 leaving，
        结果是**离开该页后主内容区永久空白，必须手动刷新才恢复**。
        Vue 此时会在控制台警告：
          "Component inside <Transition> renders non-element root node that cannot be animated."
        新增页面时请把 <el-backtop> 之类的浮层放进根节点内部（它们多为 fixed 定位，不影响布局）。
      -->
      <router-view v-slot="{ Component, route }">
        <transition name="page" mode="out-in">
          <keep-alive include="Guide">
            <component :is="Component" :key="route.path" />
          </keep-alive>
        </transition>
      </router-view>
    </main>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="footer__container">
        <div class="footer__content">
          <div class="footer__brand">
            <h3 class="footer__logo">
              <el-icon class="footer__logo-icon"><Plane /></el-icon>
              <span>出发啦</span>
            </h3>
            <p class="footer__brand-desc">
              让每一次旅行都成为难忘的回忆，我们致力于为您提供最优质的旅游服务和体验。
            </p>
            <div class="footer__social">
              <a href="javascript:void(0)" class="footer__social-icon footer__social-icon--wechat" aria-label="微信公众号" @click="showQrCode('wechat')">
                <WeChat />
              </a>
              <a href="javascript:void(0)" class="footer__social-icon footer__social-icon--qq" aria-label="QQ 交流群" @click="showQrCode('qq')">
                <QQ />
              </a>
              <a href="javascript:void(0)" class="footer__social-icon footer__social-icon--weibo" aria-label="官方微博" @click="showQrCode('weibo')">
                <WeiBo />
              </a>
              <a href="javascript:void(0)" class="footer__social-icon footer__social-icon--tiktok" aria-label="抖音号" @click="showQrCode('tiktok')">
                <TikTok />
              </a>
            </div>
          </div>

          <div class="footer__column">
            <h4 class="footer__column-title">目的地</h4>
            <ul class="footer__list">
              <li><a href="#" class="footer__link">国内游</a></li>
              <li><a href="#" class="footer__link">国外游</a></li>
              <li><a href="#" class="footer__link">热门城市</a></li>
              <li><a href="#" class="footer__link">景点大全</a></li>
              <li><a href="#" class="footer__link">美食推荐</a></li>
            </ul>
          </div>

          <div class="footer__column">
            <h4 class="footer__column-title">关于我们</h4>
            <ul class="footer__list">
              <li><a href="#" class="footer__link">公司简介</a></li>
              <li><a href="#" class="footer__link">联系我们</a></li>
              <li><a href="#" class="footer__link">加入我们</a></li>
              <li><a href="#" class="footer__link">旅游保险</a></li>
              <li><a href="#" class="footer__link">服务条款</a></li>
            </ul>
          </div>

          <div class="footer__column">
            <h4 class="footer__column-title">客户服务</h4>
            <ul class="footer__list">
              <li class="footer__contact-item">
                <el-icon class="footer__contact-icon"><Phone /></el-icon>
                <span class="num">400-123-4567</span>
              </li>
              <li class="footer__contact-item">
                <el-icon class="footer__contact-icon"><Email /></el-icon>
                <span>service@chufala.com</span>
              </li>
              <li class="footer__contact-item">
                <el-icon class="footer__contact-icon"><Clock /></el-icon>
                <span>7:00-23:00 全年无休</span>
              </li>
            </ul>
          </div>
        </div>

        <div class="footer__copyright">
          <p>© 2025 XGX出发啦旅游网 版权所有 | 营业执照 | 旅行社资质</p>
        </div>
      </div>
    </footer>

    <!-- 二维码弹窗 -->
    <el-dialog
      v-model="qrDialogVisible"
      :title="qrTitle"
      width="360px"
      center
      align-center
      destroy-on-close
    >
      <div class="qr-code-container">
        <div class="qr-code-wrapper">
          <img :src="qrImage" alt="二维码" class="qr-code-img" />
        </div>
        <p class="qr-code-desc">{{ qrDesc }}</p>
        <p class="qr-code-tip">请使用手机扫一扫</p>
      </div>
    </el-dialog>

    <SmartAssistant />
  </div>
</template>

<script setup>
// 导航栏滚动效果（公共逻辑，放在根组件）
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { Logout, NormalVip, SuperVip, WeChat, QQ, WeiBo, Phone, Email, Clock, TikTok, Plane } from '@/components/Icon.vue';
import { useUserInfoStore } from '@/stores/userInfo';
import { useTokenStore } from '@/stores/token';
import { getUserInfoService } from '@/api/user';
import { ElMessageBox, ElNotification } from 'element-plus';
import router from '@/router';
import { useGeoStore } from '@/stores/geo';
import SmartAssistant from '@/components/SmartAssistant.vue';

const scrolled = ref(false);
const userInfoStore = useUserInfoStore();
const tokenStore = useTokenStore();
const geoStore = useGeoStore();

// 二维码弹窗逻辑
const qrDialogVisible = ref(false);
const currentQrType = ref('');

const qrMap = {
  wechat: {
    title: '关注微信公众号',
    image: 'https://chufala.oss-cn-shenzhen.aliyuncs.com/9eb8dde2-e493-4f05-addf-3f0b6c670e4c.jpg',
    desc: '扫码关注“出发啦”微信公众号，获取最新旅游资讯'
  },
  qq: {
    title: '加入QQ交流群',
    image: 'https://chufala.oss-cn-shenzhen.aliyuncs.com/1a5f4d3e-a0fb-4b4c-a462-adee6edc4f7e.jpg',
    desc: '扫码加入官方QQ交流群，与驴友畅聊'
  },
  weibo: {
    title: '关注官方微博',
    image: 'https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=https://weibo.com/',
    desc: '扫码关注“出发啦”官方微博，参与互动抽奖'
  },
  tiktok: {
    title: '关注抖音号',
    image: 'https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=https://www.douyin.com/',
    desc: '扫码关注“出发啦”抖音号，看遍世界美景'
  }
};

const qrTitle = computed(() => qrMap[currentQrType.value]?.title || '');
const qrImage = computed(() => qrMap[currentQrType.value]?.image || '');
const qrDesc = computed(() => qrMap[currentQrType.value]?.desc || '');

const showQrCode = (type) => {
  currentQrType.value = type;
  qrDialogVisible.value = true;
};

/**
 * 滚动监听
 * - passive: true  避免滚动被事件回调阻塞
 * - rAF 节流       每帧最多计算一次，避免高频 setState 触发无谓渲染
 * 阈值从 50 降到 8：原来滚过 50px 之前导航栏一直是透明的，
 * 内容会从导航文字下方穿过，观感很差。
 */
let ticking = false;
const handleScroll = () => {
  if (ticking) return;
  ticking = true;
  requestAnimationFrame(() => {
    scrolled.value = window.scrollY > 8;
    ticking = false;
  });
};

onMounted(() => {
  window.addEventListener('scroll', handleScroll, { passive: true });
  handleScroll();
  getUserInfo();
  geoStore.getCityByBrowser();
});

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll);
});

const getUserInfo = async () => {
  const result = await getUserInfoService();
  userInfoStore.setInfo(result.data);
};

const logout = () => {
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
      tokenStore.removeToken();
      userInfoStore.removeInfo();
      router.push('/login');
      ElNotification.success({
        title: '提示',
        message: '您已成功退出登录！'
      });
    })
    .catch(() => {
      ElNotification.primary('您已取消 “退出登录” 操作');
    });
};
</script>

<style scoped>
/* ==================================================================
 * 说明：原文件有两个 <style scoped> 块，且包含大量「死代码」——
 * .root-container / .section-container / .hero-section* / .header-nav__mobile-*
 * 这些类名在本组件的模板中并未使用（它们真正生效的定义在各子页面自己的
 * scoped 样式里）。此处已全部清理，只保留本模板实际用到的类。
 * ================================================================== */

.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex-grow: 1;
  background-color: var(--c-bg);
}

/* ==================================================================
 * 导航栏
 * ================================================================== */
.header-nav {
  position: sticky;
  top: 0;
  z-index: var(--z-header);
  height: var(--header-h);
  display: flex;
  align-items: center;
  border-bottom: 1px solid transparent;
  /* 只过渡颜色类属性，不动 padding（原实现过渡 padding 会触发重排） */
  transition: background-color var(--dur-base) var(--ease-out),
    border-color var(--dur-base) var(--ease-out),
    box-shadow var(--dur-base) var(--ease-out);
}

/* 未滚动：透明，让首屏更干净 */
.header-nav--unscrolled {
  background-color: transparent;
}

/* 已滚动：毛玻璃白（替换原来的薄荷绿 #a5f5e5，与蓝色主色系冲突） */
.header-nav--scrolled {
  background-color: rgba(255, 255, 255, 0.78);
  -webkit-backdrop-filter: blur(16px) saturate(180%);
  backdrop-filter: blur(16px) saturate(180%);
  border-bottom-color: var(--c-line);
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}

.header-nav__container {
  width: 100%;
  max-width: var(--container-max);
  margin: 0 auto;
  padding: 0 var(--sp-4);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--sp-6);
}

.header-nav__logo {
  flex-shrink: 0;
}

.header-nav__logo-text {
  margin: 0;
  font-size: var(--fs-h3);
  font-weight: 600;
  color: var(--c-primary-600);
  letter-spacing: -0.01em;
  display: flex;
  align-items: center;
  gap: var(--sp-2);
}

.header-nav__logo-link {
  display: flex;
  align-items: center;
}

/* 54px 在 64px 高的导航里只剩 5px 余量，过于局促；收到 40px */
.header-nav__logo-img {
  height: 40px;
  width: auto;
  display: block;
  transition: transform var(--dur-base) var(--ease-out);
}

.header-nav__logo-link:hover .header-nav__logo-img {
  transform: scale(1.06);
}

.header-nav__desktop-nav {
  display: none;
  align-items: center;
  gap: var(--sp-8);
}

@media (min-width: 768px) {
  .header-nav__desktop-nav {
    display: flex;
  }
}

.header-nav__nav-link {
  position: relative;
  padding: var(--sp-1) 0;
  font-size: var(--fs-body);
  font-weight: 500;
  color: var(--c-ink-2);
  text-decoration: none;
  white-space: nowrap;
  transition: color var(--dur-base) var(--ease-out);
}

/* 下划线滑出：用 transform: scaleX 而不是 width，避免重排 */
.header-nav__nav-link::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: -2px;
  height: 2px;
  border-radius: var(--r-full);
  background-color: var(--c-primary-600);
  transform: scaleX(0);
  transform-origin: left center;
  transition: transform var(--dur-base) var(--ease-out);
}

.header-nav__nav-link:hover {
  color: var(--c-primary-600);
}

.header-nav__nav-link:hover::after,
.header-nav__nav-link--active::after {
  transform: scaleX(1);
}

.header-nav__nav-link--active {
  color: var(--c-primary-600);
  font-weight: 600;
}

/* ---- 用户区域 ---- */
.header-nav__user-actions {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  flex-shrink: 0;
}

.header-nav__login-btn {
  display: none;
  align-items: center;
  gap: var(--sp-2);
  padding: var(--sp-1) var(--sp-2);
  background: none;
  border: none;
  border-radius: var(--r-sm);
  font-family: inherit;
  color: var(--c-ink-2);
  cursor: pointer;
  transition: color var(--dur-base) var(--ease-out),
    background-color var(--dur-base) var(--ease-out);
}

@media (min-width: 768px) {
  .header-nav__login-btn {
    display: flex;
  }
}

.header-nav__login-btn:hover {
  color: var(--c-primary-600);
  background-color: var(--c-primary-50);
}

.user-avatar {
  border: 1px solid var(--c-line);
}

.user-info-wrapper {
  display: flex;
  flex-direction: column;
  gap: var(--sp-1);
  align-items: flex-start;
}

.user-name {
  font-size: var(--fs-body);
  font-weight: 500;
  color: var(--c-ink);
  line-height: 1.2;
}

.member-tag {
  display: flex;
  align-items: center;
  gap: var(--sp-1);
  padding: 1px var(--sp-2);
  border-radius: var(--r-full);
  font-size: var(--fs-caption);
  line-height: 1.5;
}

.member-icon {
  font-size: var(--fs-body-lg);
}

.member-tag--normal {
  background-color: var(--c-bg-mute);
  color: var(--c-ink-3);
}

.member-tag--diamond {
  background-color: var(--c-primary-50);
  color: var(--c-primary-600);
}

.logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  padding: 0;
  background: none;
  border: none;
  border-radius: var(--r-sm);
  color: var(--c-ink-3);
  cursor: pointer;
  transition: color var(--dur-base) var(--ease-out),
    background-color var(--dur-base) var(--ease-out);
}

.logout-btn:hover {
  color: var(--c-primary-600);
  background-color: var(--c-primary-50);
}

/* ==================================================================
 * 页脚
 * ================================================================== */
.footer {
  background-color: var(--c-ink);
  color: var(--c-ink-4);
  padding: var(--sp-16) 0 var(--sp-8);
}

.footer__container {
  max-width: var(--container-max);
  margin: 0 auto;
  padding: 0 var(--sp-4);
}

.footer__content {
  display: grid;
  grid-template-columns: 1fr;
  gap: var(--sp-10);
}

@media (min-width: 768px) {
  .footer__content {
    grid-template-columns: 2fr 1fr 1fr 1.2fr;
    gap: var(--sp-8);
  }
}

.footer__brand {
  grid-column: 1 / -1;
}

@media (min-width: 768px) {
  .footer__brand {
    grid-column: auto;
  }
}

.footer__logo {
  margin: 0 0 var(--sp-4);
  font-size: var(--fs-h3);
  font-weight: 600;
  color: #ffffff;
  display: flex;
  align-items: center;
  gap: var(--sp-2);
}

.footer__logo-icon {
  font-size: var(--fs-h3);
  color: var(--c-primary-400);
}

.footer__brand-desc {
  margin: 0 0 var(--sp-5);
  max-width: 320px;
  font-size: var(--fs-body);
  line-height: var(--lh-body-lg);
  color: var(--c-ink-4);
}

.footer__social {
  display: flex;
  gap: var(--sp-3);
}

.footer__social-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: var(--r-sm);
  background-color: rgba(255, 255, 255, 0.06);
  font-size: var(--fs-body-lg);
  color: var(--c-ink-4);
  text-decoration: none;
  transition: transform var(--dur-base) var(--ease-out),
    background-color var(--dur-base) var(--ease-out),
    color var(--dur-base) var(--ease-out);
}

.footer__social-icon:hover {
  transform: translateY(-2px);
  background-color: rgba(255, 255, 255, 0.12);
}

.footer__social-icon--wechat:hover {
  color: var(--c-brand-wechat);
}
.footer__social-icon--qq:hover {
  color: var(--c-brand-qq);
}
.footer__social-icon--weibo:hover {
  color: var(--c-brand-weibo);
}
.footer__social-icon--tiktok:hover {
  color: #ffffff;
}

.footer__column-title {
  margin: 0 0 var(--sp-4);
  font-size: var(--fs-caption);
  font-weight: 500;
  letter-spacing: 0.02em;
  color: var(--c-ink-4);
  text-transform: uppercase;
}

.footer__list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: var(--sp-3);
}

.footer__link {
  font-size: var(--fs-body);
  color: var(--c-ink-4);
  text-decoration: none;
  transition: color var(--dur-base) var(--ease-out);
}

.footer__link:hover {
  color: #ffffff;
}

.footer__contact-item {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  font-size: var(--fs-body);
  color: var(--c-ink-4);
}

.footer__contact-icon {
  font-size: var(--fs-body-lg);
  color: var(--c-ink-4);
}

.footer__copyright {
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  margin-top: var(--sp-12);
  padding-top: var(--sp-6);
  font-size: var(--fs-caption);
  text-align: center;
  color: var(--c-ink-4);
}

.footer__copyright p {
  margin: 0;
}

/* ==================================================================
 * 二维码弹窗内容（弹窗本体样式由全局 element-override.css 统一处理）
 * ================================================================== */
.qr-code-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--sp-2);
}

.qr-code-wrapper {
  background: #fff;
  padding: var(--sp-2);
  border-radius: var(--r-sm);
  box-shadow: var(--sh-2);
  margin-bottom: var(--sp-4);
}

.qr-code-img {
  width: 200px;
  height: 200px;
  display: block;
  border-radius: var(--r-xs);
}

.qr-code-desc {
  margin: 0 0 var(--sp-2);
  font-size: var(--fs-body);
  line-height: var(--lh-body);
  color: var(--c-ink);
  text-align: center;
}

.qr-code-tip {
  margin: 0;
  font-size: var(--fs-caption);
  color: var(--c-ink-3);
}
</style>
