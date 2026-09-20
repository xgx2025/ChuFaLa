import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import router from '@/router'
import { createPinia } from 'pinia'
import { createPersistedState } from 'pinia-plugin-persistedstate'


const app = createApp(App)
const pinia = createPinia()
const persist = createPersistedState()

app.use(router)   //router的use放在第一个
pinia.use(persist)
app.use(pinia)
app.use(ElementPlus,{
  locale: zhCn,
  zIndex: 3000 })
app.mount('#app')


