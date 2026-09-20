<template>
  <div class="login-container">
    <!-- 左侧品牌展示区（增强视觉层次） -->
    <div class="brand-section">
      <!-- Logo区域（优化图标协调性） -->
      <div class="logo">
        <img src="../assets/logo.png" alt="logo" style="width: 100px; height: 100px"></img>
        <h1 class="brand-name">出发啦</h1>
      </div>
      
      <!-- 品牌标语和装饰元素（新增太阳装饰） -->
      <div class="brand-content">
        <h2>探索世界，从这里开始</h2>
        <p>轻松规划，即刻出发，让每一次旅行都完美无缺</p>
        
        <div class="decorative-elements">
          <!-- 新增太阳装饰增强场景感 -->
          <div class="sun"></div>
          <div class="cloud cloud-1"></div>
          <div class="cloud cloud-2"></div>
          <div class="mountain mountain-1"></div>
          <div class="mountain mountain-2"></div>
        </div>
      </div>
    </div>
    
    <!-- 右侧登录表单区（增加入场动画） -->
    <div class="login-section" v-show="!showRegisterForm">
      <div class="login-card animate-page-enter">
        <h2 class="login-title">欢迎回来</h2>
        <p class="login-subtitle">请登录您的账号继续探索</p>
        
        <!-- 登录表单 -->
        <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" class="login-form" label-width="0">
          <el-form-item prop="email">
            <!-- 视觉隐藏label，屏幕阅读器可识别 -->
            <label for="email-input" class="sr-only">邮箱</label>
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
            <label for="password-input" class="sr-only">密码</label>
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
            <router-link to="/forgot-password" class="forgot-password" aria-label="忘记密码，前往找回">
              忘记密码？
            </router-link>
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
          <span class="register-link" aria-label="前往注册新账号" @click="switchToRegister">立即注册</span>
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
    <div class="register-section" v-show="showRegisterForm"> 
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
                <span class="register-link" @click="switchToLogin">立即登录</span>
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

const getVerifyCode = () => {
  getVerifyCodeService(registerForm.email).then(res => {
  ElMessage.success("验证码已发送")
  })

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
</script>

<style scoped>
/* 基础布局优化 */
.login-container {
  display: flex;
  min-height: 100vh;
  width: 100%;
  overflow: hidden; /* 防止装饰元素溢出导致滚动 */
}

/* 品牌展示区（增强视觉层次和动画） */
.brand-section {
  flex: 1;
  background: linear-gradient(135deg, #4285f4 0%, #7b55d3 50%, #9c27b0 100%);
  color: white;
  padding: 2.5rem;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  position: relative;
  overflow: hidden;
}

.logo {
  display: flex;
  align-items: center;
  gap: 1rem;
  z-index: 1; /* 确保logo在装饰元素之上 */
}

.logo-icon {
  width: 52px;
  height: 52px;
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1); /* 优化动画曲线 */
}

.logo:hover .logo-icon {
  transform: scale(1.12) rotate(6deg);
}

.brand-name {
  font-size: 2.1rem;
  font-weight: 700;
  margin: 0;
  letter-spacing: 0.5px; /* 优化文字间距 */
}

.brand-content {
  max-width: 550px;
  margin: 0 auto;
  text-align: center;
  padding: 2rem 0;
  z-index: 1;
}

.brand-content h2 {
  font-size: 2.6rem;
  margin-bottom: 1.2rem;
  font-weight: 600;
  animation: fadeInUp 1s ease;
}

.brand-content p {
  font-size: 1.25rem;
  opacity: 0.92;
  line-height: 1.6;
  animation: fadeInUp 1s ease 0.3s forwards;
  opacity: 0;
}

/* 装饰元素优化（增加层次和动画差异） */
.decorative-elements {
  position: relative;
  height: 220px;
  margin-top: 3rem;
}

/* 太阳装饰 */
.sun {
  position: absolute;
  top: 30px;
  right: 80px;
  width: 60px;
  height: 60px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  box-shadow: 0 0 30px rgba(255, 255, 255, 0.8);
  animation: sunPulse 4s ease-in-out infinite;
}

/* 云朵装饰（差异化动画） */
.cloud {
  position: absolute;
  width: 140px;
  height: 55px;
  background: rgba(255, 255, 255, 0.22);
  border-radius: 50%;
  box-shadow: 
    90px 25px 0 8px rgba(255, 255, 255, 0.22),
    -15px 18px 0 15px rgba(255, 255, 255, 0.22);
}

.cloud-1 {
  top: 60px;
  left: 40px;
  animation: float 9s ease-in-out infinite;
}

.cloud-2 {
  top: 90px;
  right: 60px;
  animation: float 11s ease-in-out infinite 1.5s; /* 延迟动画，增加层次感 */
  transform: scale(0.85);
}

/* 山峰装饰（差异化大小和位置） */
.mountain {
  position: absolute;
  bottom: 0;
  width: 0;
  height: 0;
  border-left: 100px solid transparent;
  border-right: 100px solid transparent;
  border-bottom: 150px solid rgba(255, 255, 255, 0.18);
  animation: fadeIn 2s ease forwards;
  opacity: 0;
}

.mountain-1 {
  left: 30%;
  transform: translateX(-50%);
  animation-delay: 0.6s;
}

.mountain-2 {
  left: 70%;
  transform: translateX(-50%);
  border-left: 80px solid transparent;
  border-right: 80px solid transparent;
  border-bottom: 120px solid rgba(255, 255, 255, 0.15);
  animation-delay: 0.9s;
}

.mountain::after {
  content: '';
  position: absolute;
  top: 30px;
  left: -120px;
  width: 0;
  height: 0;
  border-left: 120px solid transparent;
  border-right: 120px solid transparent;
  border-bottom: 180px solid rgba(255, 255, 255, 0.12);
}

.mountain-2::after {
  left: -90px;
  border-left: 90px solid transparent;
  border-right: 90px solid transparent;
  border-bottom: 140px solid rgba(255, 255, 255, 0.1);
}

/* 登录表单区（优化卡片质感） */
.login-section,.register-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  background-color: #f8fafc; /* 优化背景色，更柔和 */
}


.login-card,.register-card {
  width: 100%;
  max-width: 420px;
  background: white;
  padding: 2.8rem;
  border-radius: 16px; /* 增大圆角，更现代 */
  box-shadow: 0 12px 36px rgba(0, 0, 0, 0.09);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  position: relative;
}


/* 登录卡入场动画 */
.animate-page-enter{
  animation: cardEnter 0.6s ease-out forwards;
  opacity: 0;
  transform: translateY(20px);
}

.login-card:hover,.register-card:hover{
  transform: translateY(-6px);
  box-shadow: 0 20px 45px rgba(0, 0, 0, 0.13);
}

.login-title,.register-title {
  font-size: 1.9rem;
  font-weight: 600;
  margin-bottom: 0.6rem;
  color: #1e293b;
  letter-spacing: 0.3px;
}

.login-subtitle,.register-subtitle{
  color: #64748b;
  margin-bottom: 2.2rem;
  font-size: 1rem;
  line-height: 1.5;
}

.login-form,.register-form{
  margin-bottom: 1.8rem;
}

/* 输入框优化（增强焦点反馈） */
.el-input {
  height: 52px;
  border-radius: 10px; /* 增大圆角 */
  margin-bottom: 1.2rem;
  transition: all 0.3s ease;
  border-color: #e2e8f0;
}

.el-input__inner {
  border-radius: 10px !important; /* 覆盖Element默认样式 */
  font-size: 1rem;
  padding: 0 16px;
}

.el-input.input-focus {
  transform: translateY(-2px); /* 焦点时轻微上浮 */
}

.el-input.input-focus .el-input__inner {
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.2); /* 增强焦点阴影 */
}

.el-input__prefix {
  color: #94a3b8;
  font-size: 1.1rem;
}

/* 密码显示图标优化 */
.el-input__icon {
  transition: color 0.2s ease;
}

.el-input__icon:hover {
  color: #6366f1; /*  hover时变色，提示可点击 */
}

/* 表单选项优化（对齐和间距） */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.8rem;
  font-size: 0.95rem;
}

.remember-me {
  color: #64748b;
  display: flex;
  align-items: center;
  gap: 6px;
}

.el-checkbox__inner {
  border-radius: 4px; /* 优化复选框圆角 */
  border-color: #cbd5e1;
}

.el-checkbox__input.is-checked .el-checkbox__inner {
  background-color: #6366f1;
  border-color: #6366f1;
}

.forgot-password {
  color: #6366f1;
  font-size: 0.95rem;
  text-decoration: none;
  transition: all 0.2s ease;
  font-weight: 500;
}

.forgot-password:hover {
  color: #4f46e5;
  text-decoration: underline;
  transform: translateY(-1px);
}

/* 登录按钮优化（增强质感） */
.login-button,.register-button {
  width: 100%;
  height: 54px;
  font-size: 1.05rem;
  font-weight: 500;
  background: linear-gradient(90deg, #4285f4 0%, #7b55d3 50%, #9c27b0 100%);
  border: none;
  border-radius: 10px;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  letter-spacing: 0.5px;
}

.login-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 18px rgba(156, 39, 176, 0.35);
  background: linear-gradient(90deg, #3367d6 0%, #6a46c1 50%, #8e24aa 100%);
}

.login-button.is-loading .el-loading-spinner {
  margin-right: 8px; /* 优化加载图标位置 */
}

/* 注册提示优化 */
.register-prompt {
  text-align: center;
  color: #64748b;
  margin: 2rem 0;
  font-size: 0.95rem;
  line-height: 1.5;
}

.register-link {
  color: #6366f1;
  font-weight: 500;
  text-decoration: none;
  margin-left: 0.3rem;
  transition: all 0.2s ease;
}

.register-link:hover {
  color: #4f46e5;
  text-decoration: underline;
  transform: translateY(-1px);
}

/* 其他登录方式优化 */
.other-login-methods {
  margin-top: 2.5rem;
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
  background-color: #e2e8f0;
}

.divider span {
  padding: 0 1.2rem;
  color: #94a3b8;
  font-size: 0.9rem;
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
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  border-radius: 50% !important; /* 覆盖Element默认样式 */
  font-size: 1.1rem;
}

.social-btn:hover {
  transform: translateY(-4px) scale(1.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.qq-btn {
  color: #12b7f5;
  border-color: #12b7f5;
}

.qq-btn:hover {
  background-color: #12b7f5;
  color: white;
}

.wechat-btn {
  color: #07c160;
  border-color: #07c160;
}

.wechat-btn:hover {
  background-color: #07c160;
  color: white;
}

.alipay-btn {
  color: #1677ff;
  border-color: #1677ff;
}

.alipay-btn:hover {
  background-color: #1677ff;
  color: white;
}

/* 新增动画（丰富视觉体验） */
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

@keyframes sunPulse {
  0%, 100% {
    box-shadow: 0 0 30px rgba(255, 255, 255, 0.8);
  }
  50% {
    box-shadow: 0 0 45px rgba(255, 255, 255, 0.95);
  }
}

/* 原有动画优化 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(25px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes float {
  0% {
    transform: translateY(0) translateX(0);
  }
  50% {
    transform: translateY(-18px) translateX(18px);
  }
  100% {
    transform: translateY(0) translateX(0);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateX(-50%) scale(0.85);
  }
  to {
    opacity: 1;
    transform: translateX(-50%) scale(1);
  }
}

/* 响应式优化（更精细的适配） */
@media (max-width: 992px) {
  .brand-section {
    padding: 2rem;
  }
  
  .brand-content h2 {
    font-size: 2.3rem;
  }
  
  .login-card {
    padding: 2.5rem;
    max-width: 380px;
  }
}

@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
  }
  
  .brand-section {
    padding: 1.8rem;
    min-height: 280px; /* 优化小屏高度 */
  }
  
  .brand-content {
    padding: 1.5rem 0;
  }
  
  .brand-content h2 {
    font-size: 2rem;
    margin-bottom: 1rem;
  }
  
  .brand-content p {
    font-size: 1.15rem;
  }
  
  .decorative-elements {
    height: 180px;
    margin-top: 2rem;
  }
  
  .login-section {
    padding: 1.5rem;
    padding-top: 0;
  }
  
  .login-card {
    padding: 2rem;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.07);
    max-width: 100%;
    margin-top: -30px; /* 向上偏移，与品牌区衔接更自然 */
    z-index: 2;
    background: rgba(255, 255, 255, 0.98);
    backdrop-filter: blur(4px); /* 增加毛玻璃效果 */
  }
  
  .login-title {
    font-size: 1.7rem;
  }
  
  .social-login {
    gap: 1.5rem;
  }
}

@media (max-width: 480px) {
  .brand-section {
    min-height: 250px;
  }
  
  .logo-icon {
    width: 48px;
    height: 48px;
  }
  
  .brand-name {
    font-size: 1.9rem;
  }
  
  .login-card {
    padding: 1.8rem;
    margin-top: -20px;
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
  font-size: 0.95rem; 
  border-radius: 10px !important; 
}



/* 4. 按钮禁用状态优化（可选：若有倒计时禁用逻辑） */
.verifyCode-button:disabled {
  background: #e2e8f0 !important; /* 禁用时背景色，与输入框禁用态统一 */
  border-color: #e2e8f0 !important;
  color: #94a3b8 !important;
  cursor: not-allowed;
}
</style>