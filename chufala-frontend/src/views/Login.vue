<template>
  <div class="login-container">
    <!-- 品牌展示区 -->
    <div class="brand-section">
      <div class="brand-glow brand-glow--top" aria-hidden="true"></div>
      <div class="brand-glow brand-glow--bottom" aria-hidden="true"></div>
      <div class="logo">
        <img src="../assets/logo2.png" alt="" class="logo-icon">
        <span class="brand-name">出发啦</span>
      </div>

      <div class="brand-content">
        <span class="brand-eyebrow">YOUR NEXT JOURNEY</span>
        <h1>下一程，<br><span>从这里出发。</span></h1>
        <p>收藏沿途的风景，开启属于你的旅程。</p>

        <div class="journey-visual" aria-hidden="true">
          <svg class="journey-route" viewBox="0 0 520 260" fill="none" preserveAspectRatio="xMidYMid meet">
            <path class="route-guide" d="M28 207C91 206 120 92 220 146C315 197 341 40 490 47" />
            <path class="route-trace" d="M28 207C91 206 120 92 220 146C315 197 341 40 490 47" />
            <circle cx="30" cy="207" r="7" />
            <circle cx="489" cy="47" r="7" />
          </svg>
          <span class="route-label route-label--start">此刻 · 起点</span>
          <div class="destination-ticket">
            <span>下一站 / NEXT STOP</span>
            <strong>向往的远方</strong>
            <small>准备好，随时出发</small>
          </div>
        </div>
      </div>
      <div class="brand-footer"><span>TRAVEL BEGINS HERE</span><span>01 / 02</span></div>
    </div>
    
    <!-- 右侧登录表单区 -->
    <div class="login-section" v-if="!showRegisterForm">
      <div class="login-card animate-page-enter">
        <span class="form-eyebrow">账户登录</span>
        <h2 class="login-title">欢迎回来</h2>
        <p class="login-subtitle">登录账号，继续规划你的下一程。</p>
        
        <!-- 登录表单 -->
        <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" class="login-form" label-width="0">
          <el-form-item prop="email">
            <label for="email-input" class="field-label">邮箱地址</label>
            <el-input 
              id="email-input"
              v-model="loginForm.email" 
              placeholder="请输入邮箱" 
              :prefix-icon="Email"
              :class="{ 'input-focus': emailFocus }"
              @focus="emailFocus = true"
              @blur="emailFocus = false"
              aria-describedby="email-tip"
            ></el-input>
            <p id="email-tip" class="sr-only">请输入您注册时使用的邮箱地址</p>
          </el-form-item>
          
          <el-form-item prop="password">
            <label for="password-input" class="field-label">登录密码</label>
            <el-input 
              id="password-input"
              v-model="loginForm.password" 
              type="password" 
              placeholder="请输入密码" 
              :prefix-icon="Lock"
              show-password
              :class="{ 'input-focus': passwordFocus }"
              @focus="passwordFocus = true"
              @blur="passwordFocus = false"
              aria-describedby="password-tip"
            ></el-input>
            <p id="password-tip" class="sr-only">密码长度不少于6位，区分大小写</p>
          </el-form-item>
          
          <div class="form-options">
            <el-checkbox v-model="rememberMe" class="remember-me" id="remember-me">
              <label for="remember-me">记住我</label>
            </el-checkbox>
            <!-- 原为 <router-link to="/forgot-password">，但该路由不存在，
                 点击会进空白页。改为就地提示（元素类型保持 <a> 以免影响样式）。 -->
            <a
              href="javascript:void(0)"
              class="forgot-password"
              aria-label="忘记密码"
              @click="showForgotPasswordTip"
            >
              忘记密码？
            </a>
          </div>
          
          <el-form-item>
            <el-button 
              type="primary" 
              class="login-button" 
              @click="handleLogin"
              :loading="loginLoading"
              aria-label="点击登录账号"
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="register-prompt"> 还没有账号？
          <button type="button" class="register-link" aria-label="前往注册新账号" @click="switchToRegister">立即注册</button>
        </div>
        
        <!-- 其他登录方式（优化按钮交互） -->
        <div class="other-login-methods">
          <div class="divider">
            <span>其他登录方式</span>
          </div>
          <div class="social-login">
            <el-button 
              :icon="QQ" 
              circle 
              class="social-btn qq-btn"
              aria-label="使用QQ账号登录"
            ></el-button>
            <el-button 
              :icon="WeChat" 
              circle 
              class="social-btn wechat-btn"
              aria-label="使用微信账号登录"
            ></el-button>
            <el-button 
              :icon="Alipay" 
              circle 
              class="social-btn email-btn"
              aria-label="使用支付宝账号登录"
            ></el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 注册表单 -->
    <div class="register-section" v-else>
      <div class="register-card animate-page-enter"> 
        <h2 class="register-title">欢迎注册</h2>
        <p class="register-subtitle">请填写以下信息以注册账号</p>
        <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" class="register-form" label-width="0">
          <el-form-item prop="username"> 
            <label for="username-input" class="sr-only">用户名</label>
            <el-input 
              id="register-username-input"
              v-model="registerForm.username" 
              placeholder="请输入用户名" 
              :prefix-icon="User"
              :class="{ 'input-focus': usernameFocus }"
              @focus="usernameFocus = true"
              @blur="usernameFocus = false"
              aria-describedby="username-tip"
            ></el-input>
          </el-form-item>
          <el-form-item prop="email"> 
            <label for="email-input" class="sr-only">邮箱</label>
            <el-input 
              id="register-email-input"
              v-model="registerForm.email" 
              placeholder="请输入邮箱" 
              :prefix-icon="Email"
              :class="{ 'input-focus': emailFocus }"
              @focus="emailFocus = true"
              @blur="emailFocus = false"
              aria-describedby="email-tip"
            ></el-input>
          </el-form-item>
          <el-form-item prop="password"> 
            <label for="password-input" class="sr-only">密码</label>
            <el-input 
              id="register-password-input"
              v-model="registerForm.password" 
              type="password" 
              placeholder="请输入密码" 
              :prefix-icon="Lock"
              show-password
              :class="{ 'input-focus': passwordFocus }"
              @focus="passwordFocus = true"
              @blur="passwordFocus = false"
              aria-describedby="password-tip"
            ></el-input>
          </el-form-item>
          <el-form-item prop="confirmPassword"> 
            <label for="confirm-password-input" class="sr-only">确认密码</label>
            <el-input 
              id="register-confirm-password-input"
              v-model="registerForm.confirmPassword" 
              type="password" 
              placeholder="请再次输入密码" 
              :prefix-icon="Lock"
              show-password
              :class="{ 'input-focus': confirmPasswordFocus }"
              @focus="confirmPasswordFocus = true"
              @blur="confirmPasswordFocus = false"
              aria-describedby="confirm-password-tip"
            ></el-input>
          </el-form-item>

          <el-form-item prop="verifyCode"> 
            <div class="verify-code-group">
              <el-input 
                v-model="registerForm.verifyCode" 
                placeholder="请输入验证码" 
                :prefix-icon="Email"
                :class="{ 'input-focus': verifyCodeFocus }"
                @focus="verifyCodeFocus = true"
                @blur="verifyCodeFocus = false"
                aria-describedby="verify-code-tip"
                style="flex: 1;"
              ></el-input>
              <el-button 
                type="primary" 
                class="verifyCode-button" 
                @click="getVerifyCode"
                :loading="getVerifyCodeLoading"
                aria-label="点击获取验证码"
              >
                获取验证码
              </el-button>
            </div>
          </el-form-item>
          <el-form-item> 
            <el-button 
              type="primary" 
              class="register-button" 
              @click="handleRegister"
              :loading="registerLoading"
              aria-label="点击注册账号"
            >
              注册
            </el-button>
          </el-form-item>   
            <div class="register-prompt"> 
              已有账号？
                <button type="button" class="register-link" @click="switchToLogin">立即登录</button>
            </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import {userLoginService,userRegisterService,getVerifyCodeService} from '@/api/user'
import {useTokenStore} from '@/stores/token'
import {User, Lock} from '@element-plus/icons-vue'
import {WeChat,QQ,Alipay} from '@/components/Icon.vue'
import { h } from 'vue'

// 自定义图标组件（SVG）
const Email = () => h('svg', { 
  width: '1em', 
  height: '1em', 
  viewBox: '0 0 1024 1024', 
  version: '1.1', 
  xmlns: 'http://www.w3.org/2000/svg' 
}, [
  h('path', { 
    d: 'M838.954667 234.666667H170.666667c-3.626667 0-7.168 0.448-10.56 1.322666l323.690666 323.669334a21.333333 21.333333 0 0 0 30.165334 0L838.954667 234.666667z m46.144 14.186666l-260.693334 260.693334 262.933334 262.912c5.44-7.168 8.661333-16.106667 8.661333-25.792V277.333333c0-10.944-4.117333-20.906667-10.88-28.48zM843.861333 789.333333l-249.6-249.621333-50.133333 50.133333a64 64 0 0 1-90.517333 0l-50.112-50.133333L156.373333 786.88c4.48 1.578667 9.28 2.453333 14.314667 2.453333h673.194667zM128.661333 754.218667L373.333333 509.525333 129.578667 265.813333A42.709333 42.709333 0 0 0 128 277.333333v469.333334c0 2.56 0.213333 5.098667 0.661333 7.552zM170.666667 192h682.666666a85.333333 85.333333 0 0 1 85.333334 85.333333v469.333334a85.333333 85.333333 0 0 1-85.333334 85.333333H170.666667a85.333333 85.333333 0 0 1-85.333334-85.333333V277.333333a85.333333 85.333333 0 0 1 85.333334-85.333333z', 
    fill: '#707070' 
  })
])

const router = useRouter();
const tokenStore = useTokenStore()
const showRegisterForm = ref(false);

// 表单数据（增加初始值注释，逻辑更清晰）
const loginForm = reactive({
  email: '', // 用户登录邮箱
  password: '' // 用户登录密码
});

const registerForm = reactive({
  username: '', 
  email: '', 
  password: '' ,
  confirmPassword: '',
  verifyCode: ''
});



// 表单验证规则（优化错误提示文案）
const loginRules = {
  email: [
    { required: true, message: '请输入登录邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入格式正确的邮箱（如：xxx@example.com）', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入登录密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
};

const registerRules = {
  username: [
      { min: 1, max: 20, message: '用户名长度需在1-20个字符之间', trigger: 'blur' },
      { 
          pattern: /^[\u4e00-\u9fa5A-Za-z][\u4e00-\u9fa5A-Za-z0-9._-]*$/, 
          message: '用户名仅支持中文、字母、数字、下划线(_)、短横线(-)和点(.)', 
          trigger: 'blur' 
      }
  ],
  email: [
    { required: true, message: '请输入注册邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入格式正确的邮箱（如：xxx@example.com）', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入登录密码', trigger: 'blur' },
    { 
      pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]{6,}$/, 
      message: '密码必须包含大小写字母和数字，长度不小于6位', 
      trigger: 'blur' 
    }
  ],
  verifyCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { min: 6, message: '验证码长度不能少于6位', trigger: 'blur' }
  ]
};

// 状态管理（按功能分组，便于维护）
const loginFormRef = ref(null); // 表单引用
const registerFormRef = ref(null); // 注册表单 ref
const loginLoading = ref(false); // 登录按钮加载状态
const registerLoading = ref(false);
// 模板里 :loading="getVerifyCodeLoading" 引用了这个 ref，但此前从未声明，
// 导致控制台刷 "Property getVerifyCodeLoading was accessed during render
// but is not defined on instance"，且「获取验证码」按钮永远没有 loading 反馈
const getVerifyCodeLoading = ref(false);
const rememberMe = ref(true); // 记住我勾选状态
const emailFocus = ref(false); // 邮箱输入框焦点状态
const passwordFocus = ref(false); // 密码输入框焦点状态
const usernameFocus = ref(false); // 注册-用户名焦点
const confirmPasswordFocus = ref(false); // 注册-确认密码焦点
const verifyCodeFocus = ref(false); // 注册-验证码焦点

// 处理登录（新增失败场景模拟，完善反馈）
const handleLogin = async () => {
  try {
    // 表单验证
    await loginFormRef.value.validate();
    
    // 防止重复提交
    if (loginLoading.value) return;
    loginLoading.value = true;  
    const result = await userLoginService(loginForm)
    loginLoading.value = false;
    ElMessage.success("登录成功")
    tokenStore.setToken(result.data.accessToken,result.data.refreshToken)
    router.push('/')
  } catch (error) {
    loginLoading.value = false;
    return false;
  }
};

// 获取注册验证码。
// 原实现：无 loading 状态、无空邮箱校验、无 catch —— 按钮点了没反馈，
// 邮箱为空时会发出无意义请求，接口失败还会产生未捕获的 Promise 拒绝。
const getVerifyCode = async () => {
  if (!registerForm.email) {
    ElMessage.warning('请先填写邮箱')
    return
  }
  if (getVerifyCodeLoading.value) return;
  getVerifyCodeLoading.value = true;
  try {
    await getVerifyCodeService(registerForm.email)
    ElMessage.success("验证码已发送")
  } catch (error) {
    // 失败提示由 utils/request.ts 的响应拦截器统一弹出，这里不重复提示
  } finally {
    getVerifyCodeLoading.value = false;
  }
};

const handleRegister = async () => {
  try {
    // 表单验证
    await registerFormRef.value.validate();
    
    // 防止重复提交
    if (registerLoading.value) return;
    registerLoading.value = true;  
    const result = await userRegisterService(registerForm)
    registerLoading.value = false;
    ElMessage.success("注册成功")
  } catch (error) {
    registerLoading.value = false;
    return false;
  }
};

const cleanLoginForm = () => {
  loginFormRef.value.resetFields();
};

const cleanRegisterForm = () => {
  registerFormRef.value.resetFields();
};
const switchToRegister = () => {
  cleanLoginForm();
  showRegisterForm.value = true;
};
const switchToLogin = () => {
  cleanRegisterForm();
  showRegisterForm.value = false;
};

// 密码找回
// 项目没有对应的后端接口（api/user.ts 只有注册/登录/发验证码/用户信息），
// 原先指向 /forgot-password，而路由表里根本没有这条路由 ——
// 点击后 Vue Router 报 "No match found" 并渲染空白页。
// 暂时改为给出明确提示，等后端补齐接口再接真实页面。
const showForgotPasswordTip = () => {
  ElMessage.info('密码找回功能暂未开放，请联系客服 400-123-4567');
};
</script>

<style scoped>
/* 基础布局优化 */
.login-container {
  display: flex;
  min-height: 100vh;
  width: 100%;
  overflow: hidden; /* 防止装饰元素溢出导致滚动 */
}

/* 深蓝航线与纸飞机标识呼应，紫色只保留在品牌图形中。 */
.brand-section {
  flex: 0 0 46%;
  background:
    radial-gradient(circle at 87% 14%, rgba(77, 150, 255, 0.23), transparent 34%),
    linear-gradient(145deg, #0b2142 0%, #123a73 58%, #1755a8 100%);
  color: #fff;
  padding: clamp(32px, 4vw, 64px);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  position: relative;
  overflow: hidden;
  isolation: isolate;
}

.brand-section::before {
  content: '';
  position: absolute;
  width: 520px;
  height: 520px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 50%;
  top: -320px;
  right: -210px;
  box-shadow: 0 0 0 85px rgba(255, 255, 255, 0.025),
    0 0 0 170px rgba(255, 255, 255, 0.018);
  pointer-events: none;
  z-index: 0;
}

.brand-glow {
  position: absolute;
  z-index: 0;
  pointer-events: none;
  width: clamp(320px, 38vw, 520px);
  aspect-ratio: 1;
  border-radius: 50%;
  filter: blur(18px);
  will-change: transform;
}

.brand-glow--top {
  top: -165px;
  right: -175px;
  background: radial-gradient(circle, rgba(108, 208, 255, 0.58) 0%,
    rgba(53, 131, 251, 0.31) 40%, transparent 72%);
  animation: glowDriftTop 7s var(--ease-in-out) infinite alternate;
}

.brand-glow--bottom {
  bottom: -205px;
  left: -185px;
  background: radial-gradient(circle, rgba(79, 159, 255, 0.56) 0%,
    rgba(48, 108, 229, 0.29) 42%, transparent 73%);
  animation: glowDriftBottom 9s var(--ease-in-out) infinite alternate;
}

.logo, .brand-eyebrow, .brand-content h1, .brand-content > p,
.journey-visual, .brand-footer {
  animation: authRise var(--dur-slower) var(--ease-out) both;
}

.brand-eyebrow { animation-delay: 70ms; }
.brand-content h1 { animation-delay: 130ms; }
.brand-content > p { animation-delay: 210ms; }
.journey-visual { animation-delay: 290ms; }
.brand-footer { animation-delay: 360ms; }

.logo {
  display: flex;
  align-items: center;
  gap: 14px;
  position: relative;
  z-index: 1;
}

.logo-icon {
  width: 46px;
  height: 46px;
  object-fit: contain;
}

.brand-name {
  font-size: 25px;
  font-weight: 600;
  letter-spacing: 0.06em;
}

.brand-content {
  width: 100%;
  max-width: 560px;
  margin: auto;
  padding: 48px 0 18px;
  position: relative;
  z-index: 1;
}

.brand-eyebrow,
.brand-footer {
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.2em;
  color: #a9cef9;
}

.brand-content h1 {
  margin: 20px 0 18px;
  font-size: clamp(36px, 4vw, 58px);
  font-weight: 600;
  line-height: 1.25;
  letter-spacing: -0.035em;
}

.brand-content h1 span {
  color: #9bd9ff;
}

.brand-content p {
  margin: 0;
  color: #d0e2f8;
  font-size: 16px;
  line-height: 1.8;
}

.journey-visual {
  height: 280px;
  position: relative;
  margin-top: 34px;
  border-top: 1px solid rgba(255, 255, 255, 0.12);
}

.journey-route {
  position: absolute;
  inset: 12px 0 auto;
  width: 100%;
  height: 240px;
  overflow: visible;
}

.journey-route .route-guide,
.journey-route .route-trace {
  stroke: #95caff;
  stroke-width: 2;
}

.journey-route .route-guide {
  stroke-dasharray: 5 8;
  opacity: 0.48;
}

.journey-route .route-trace {
  stroke: #d7efff;
  stroke-linecap: round;
  stroke-dasharray: 650;
  stroke-dashoffset: 650;
  animation: drawRoute 1.35s 450ms var(--ease-out) both;
}

.journey-route circle {
  fill: #dff3ff;
  stroke: #2572cf;
  stroke-width: 6;
}

.route-label {
  position: absolute;
  bottom: 16px;
  left: 4px;
  font-size: 12px;
  letter-spacing: 0.08em;
  color: #c3ddfa;
}

.destination-ticket {
  position: absolute;
  top: 66px;
  right: 0;
  display: flex;
  flex-direction: column;
  width: 210px;
  padding: 20px 22px;
  color: var(--c-ink);
  background: #f6fbff;
  border-radius: 16px;
  box-shadow: 0 18px 45px rgba(0, 14, 42, 0.24);
  transform: rotate(4deg);
  transition: transform var(--dur-slow) var(--ease-out),
    box-shadow var(--dur-slow) var(--ease-out);
}

.destination-ticket:hover {
  transform: translateY(-5px) rotate(2deg);
  box-shadow: 0 24px 50px rgba(0, 14, 42, 0.3);
}

.destination-ticket > span {
  color: var(--c-primary-600);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.destination-ticket strong {
  margin: 13px 0 17px;
  font-size: 23px;
  font-weight: 600;
}

.destination-ticket small {
  padding-top: 12px;
  border-top: 1px dashed #bfd5eb;
  color: var(--c-ink-3);
  font-size: 11px;
}

.brand-footer {
  display: flex;
  justify-content: space-between;
  position: relative;
  z-index: 1;
}

/* 表单区保持安静，让注意力落在输入与主操作上。 */
.login-section,.register-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px clamp(32px, 6vw, 96px);
  background: #fff;
}

.login-card,.register-card {
  width: 100%;
  max-width: 430px;
  padding: 0;
  position: relative;
}

.animate-page-enter {
  animation: cardEnter var(--dur-slower) var(--ease-out) both;
}

.animate-page-enter > :is(.form-eyebrow, .login-title, .register-title, .login-subtitle, .register-subtitle, .login-form, .register-form, .register-prompt, .other-login-methods) {
  animation: authRise var(--dur-slow) var(--ease-out) both;
}

.animate-page-enter > :is(.login-title, .register-title) { animation-delay: 70ms; }
.animate-page-enter > :is(.login-subtitle, .register-subtitle) { animation-delay: 120ms; }
.animate-page-enter > :is(.login-form, .register-form) { animation-delay: 170ms; }
.animate-page-enter > .register-prompt { animation-delay: 220ms; }
.animate-page-enter > .other-login-methods { animation-delay: 270ms; }

.form-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: var(--c-primary-600);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.15em;
}

.form-eyebrow::before {
  content: '';
  width: 18px;
  height: 2px;
  background: currentColor;
}

.login-title,.register-title {
  font-size: 36px;
  font-weight: 600;
  line-height: 1.25;
  margin: 20px 0 8px;
  color: var(--c-ink);
  letter-spacing: -0.025em;
}

.login-subtitle,.register-subtitle{
  color: var(--c-ink-3);
  margin: 0 0 36px;
  font-size: var(--fs-body-lg);
  line-height: 1.65;
}

.login-form,.register-form{
  margin-bottom: 24px;
}

.field-label {
  display: block;
  width: 100%;
  margin-bottom: 8px;
  color: var(--c-ink-2);
  font-size: 13px;
  font-weight: 600;
  line-height: 1.5;
  transition: color var(--dur-base) var(--ease-out);
}

.el-form-item:focus-within .field-label {
  color: var(--c-primary-700);
}

.login-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.el-input {
  height: 52px;
  border-radius: var(--r-sm);
}

.el-input :deep(.el-input__wrapper) {
  border-radius: var(--r-sm);
  box-shadow: 0 0 0 1px var(--c-line) inset;
  background-color: #fff;
  transition: box-shadow var(--dur-base) var(--ease-out),
    transform var(--dur-base) var(--ease-out),
    background-color var(--dur-base) var(--ease-out);
}

.el-input:hover :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px var(--c-primary-300) inset;
}

.el-input :deep(.el-input__inner) {
  font-size: 15px;
}

.el-input:is(.input-focus, :focus-within) :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 2px var(--c-primary-500) inset,
    0 0 0 4px rgba(59, 130, 246, 0.14),
    0 10px 26px -18px rgba(37, 99, 235, 0.5);
  background-color: #fbfdff;
  transform: translateY(-1px);
}

.el-form-item.is-error .el-input :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 2px var(--c-danger) inset,
    0 0 0 4px var(--c-danger-soft);
}

.el-input :deep(.el-input__prefix) {
  color: var(--c-ink-4);
  font-size: var(--fs-body-lg);
  transition: color var(--dur-base) var(--ease-out),
    transform var(--dur-base) var(--ease-out);
}

.el-input:is(.input-focus, :focus-within) :deep(.el-input__prefix) {
  color: var(--c-primary-600);
  transform: translateX(2px);
}

.el-input :deep(.el-input__icon) {
  transition: color 0.2s ease;
}

.el-input :deep(.el-input__icon:hover) {
  color: var(--c-primary-600);
}

/* 表单选项优化（对齐和间距） */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.8rem;
  font-size: var(--fs-body);
}

.remember-me {
  color: var(--c-ink-3);
  display: flex;
  align-items: center;
  gap: 6px;
}

.el-checkbox__inner {
  border-radius: 4px; /* 优化复选框圆角 */
  border-color: var(--c-ink-4);
}

.el-checkbox__input.is-checked .el-checkbox__inner {
  background-color: var(--c-primary-600);
  border-color: var(--c-primary-600);
}

.forgot-password {
  color: var(--c-primary-600);
  font-size: var(--fs-body);
  text-decoration: none;
  transition: color 0.2s ease;
  font-weight: 500;
}

.forgot-password:hover {
  color: var(--c-primary-700);
  text-decoration: underline;
}

/* 单一主色承接页面的主要操作。 */
.login-button,.register-button {
  width: 100%;
  height: 54px;
  font-size: var(--fs-body-lg);
  font-weight: 600;
  background: var(--c-primary-600);
  border: none;
  border-radius: var(--r-sm);
  position: relative;
  overflow: hidden;
  transition: background-color var(--dur-base) var(--ease-out),
    box-shadow var(--dur-base) var(--ease-out),
    transform var(--dur-fast) var(--ease-out);
}

.login-button::before,.register-button::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(105deg, transparent 30%, rgba(255, 255, 255, 0.2) 50%, transparent 70%);
  transform: translateX(-120%);
  transition: transform 550ms var(--ease-out);
}

.login-button:hover,.register-button:hover {
  background: var(--c-primary-700);
  box-shadow: var(--sh-primary);
  transform: translateY(-2px);
}

.login-button:hover::before,.register-button:hover::before {
  transform: translateX(120%);
}

.login-button:active,.register-button:active {
  transform: translateY(0) scale(0.985);
  box-shadow: none;
}

.login-button.is-loading .el-loading-spinner {
  margin-right: 8px; /* 优化加载图标位置 */
}

/* 注册提示优化 */
.register-prompt {
  text-align: center;
  color: var(--c-ink-3);
  margin: 20px 0;
  font-size: var(--fs-body);
  line-height: 1.5;
}

.register-link {
  color: var(--c-primary-600);
  font-weight: 600;
  text-decoration: none;
  margin-left: 0.3rem;
  padding: 0;
  border: 0;
  background: none;
  cursor: pointer;
  font: inherit;
  transition: color var(--dur-base) var(--ease-out),
    transform var(--dur-base) var(--ease-out);
}

.register-link:hover {
  color: var(--c-primary-700);
  text-decoration: underline;
  transform: translateX(2px);
}

.register-link:focus-visible,
.forgot-password:focus-visible,
.social-btn:focus-visible,
.login-button:focus-visible,
.register-button:focus-visible {
  outline: 3px solid var(--c-primary-300);
  outline-offset: 3px;
}

/* 其他登录方式优化 */
.other-login-methods {
  margin-top: 28px;
}

.divider {
  display: flex;
  align-items: center;
  margin-bottom: 1.8rem;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background-color: var(--c-line);
}

.divider span {
  padding: 0 1.2rem;
  color: var(--c-ink-4);
  font-size: var(--fs-body);
  letter-spacing: 0.2px;
}

.social-login {
  display: flex;
  justify-content: center;
  gap: 1.8rem;
}

/* 社交登录按钮优化（增强交互） */
.social-btn {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1), box-shadow 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  border-radius: 50% !important; /* 覆盖Element默认样式 */
  font-size: var(--fs-body-lg);
}

.social-btn:hover {
  transform: translateY(-4px) scale(1.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.social-btn:active {
  transform: scale(0.94);
}

.qq-btn {
  color: var(--c-brand-qq);
  border-color: var(--c-brand-qq);
}

.qq-btn:hover {
  background-color: var(--c-brand-qq);
  color: white;
}

.wechat-btn {
  color: var(--c-brand-wechat);
  border-color: var(--c-brand-wechat);
}

.wechat-btn:hover {
  background-color: var(--c-brand-wechat);
  color: white;
}

.alipay-btn {
  color: var(--c-primary-600);
  border-color: var(--c-primary-600);
}

.alipay-btn:hover {
  background-color: var(--c-primary-600);
  color: white;
}

@keyframes cardEnter {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes authRise {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes drawRoute {
  from { stroke-dashoffset: 650; }
  to { stroke-dashoffset: 0; }
}

@keyframes glowDriftTop {
  from { transform: translate3d(-85px, -25px, 0) scale(0.9); }
  to { transform: translate3d(90px, 105px, 0) scale(1.13); }
}

@keyframes glowDriftBottom {
  from { transform: translate3d(-80px, 25px, 0) scale(0.9); }
  to { transform: translate3d(135px, -115px, 0) scale(1.16); }
}

@media (prefers-reduced-motion: reduce) {
  .logo, .brand-eyebrow, .brand-content h1, .brand-content > p,
  .journey-visual, .brand-footer, .animate-page-enter,
  .animate-page-enter > *, .journey-route .route-trace {
    animation: none !important;
  }

  .journey-route .route-trace { stroke-dashoffset: 0; }

  .brand-glow { animation: none; will-change: auto; }

  .destination-ticket, .el-input :deep(.el-input__wrapper),
  .el-input :deep(.el-input__prefix), .login-button, .register-button,
  .login-button::before, .register-button::before,
  .register-link, .social-btn {
    transition: none !important;
  }

  .field-label { transition: none !important; }
}

/* 保持原有断点可用；完整的移动端布局调整留待下一阶段。 */
@media (max-width: 992px) {
  .brand-section {
    padding: 32px;
  }

  .brand-content h1 {
    font-size: 40px;
  }

  .destination-ticket {
    right: 4px;
    width: 190px;
  }

  .login-section,.register-section {
    padding: 40px;
  }
}

@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
  }

  .brand-section {
    flex: none;
    min-height: 290px;
    padding: 28px;
  }

  .brand-content {
    margin: 0;
    padding: 24px 0 10px;
  }

  .brand-content h1 {
    margin: 10px 0 8px;
    font-size: 32px;
  }

  .brand-content p {
    font-size: 14px;
  }

  .journey-visual {
    display: none;
  }

  .login-section,.register-section {
    padding: 36px 28px;
  }

  .login-card,.register-card {
    max-width: 100%;
  }

  .login-title,.register-title {
    font-size: 30px;
  }

  .social-login {
    gap: 24px;
  }
}

@media (max-width: 480px) {
  .brand-section {
    min-height: 270px;
    padding: 24px;
  }

  .brand-footer {
    font-size: 9px;
  }

  .brand-name {
    font-size: 22px;
  }

  .login-section,.register-section {
    padding: 32px 24px;
  }

  .el-input {
    height: 50px;
  }

  .login-button {
    height: 52px;
  }
}

/* 辅助类：视觉隐藏（用于可访问性label） */
.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border-width: 0;
}

/* 1. 验证码输入框+按钮的外层容器：横向布局+垂直居中 */
.verify-code-group {
  display: flex;
  align-items: top; /* 输入框与按钮垂直居中对齐 */
  gap: 12px; /* 输入框与按钮之间的间距（替代margin，更灵活） */
  width: 100%;
}


.verifyCode-button {
  min-width: 120px; 
  height: 52px; 
  font-size: var(--fs-body); 
  border-radius: 10px !important; 
}



/* 4. 按钮禁用状态优化（可选：若有倒计时禁用逻辑） */
.verifyCode-button:disabled {
  background: var(--c-line) !important; /* 禁用时背景色，与输入框禁用态统一 */
  border-color: var(--c-line) !important;
  color: var(--c-ink-4) !important;
  cursor: not-allowed;
}
</style>
