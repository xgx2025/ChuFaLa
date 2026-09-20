import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// ---- 全局样式（顺序不可调换）----
// 1. element-plus 基础样式
// 2. tokens.css           设计令牌，必须最先定义变量
// 3. base.css             全局基础样式，依赖 tokens 的变量
// 4. element-override.css 覆写 Element Plus，必须排在 element-plus 之后
// 5. motion.css           动效，供路由过渡与指令使用
import '@/styles/tokens.css'
import '@/styles/base.css'
import '@/styles/element-override.css'
import '@/styles/motion.css'

import zhCn from 'element-plus/es/locale/lang/zh-cn'
import router from '@/router'
import { createPinia } from 'pinia'
import { createPersistedState } from 'pinia-plugin-persistedstate'
import { vReveal } from '@/directives/reveal'
import { vLazyImg } from '@/directives/lazy-img'

const app = createApp(App)
const pinia = createPinia()
const persist = createPersistedState()

app.use(router)   //router的use放在第一个
pinia.use(persist)
app.use(pinia)
app.use(ElementPlus,{
  locale: zhCn,
  zIndex: 3000 })

// 全局指令
app.directive('reveal', vReveal)
app.directive('lazy-img', vLazyImg)

app.mount('#app')
