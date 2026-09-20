import {createRouter,createWebHistory} from 'vue-router'
//导入组件
import LoginVue from '@/views/Login.vue'
import LayoutVue from '@/views/Layout.vue'
import HomeVue from '@/views/Home.vue'
import HotelVue from '@/views/Hotel.vue'
import AttractionVue from '@/views/Attraction.vue'
import FlightVue from '@/views/Flight.vue'
import PackageVue from '@/views/Package.vue'
import GuideVue from '@/views/Guide.vue'
import TestVue from '@/views/Test.vue'
// 注意目录名大小写：实际目录是 views/hotel、views/attraction（小写）。
// 原来写成 @/views/Hotel/... 在 Windows 上能跑（文件系统大小写不敏感），
// 但在 Linux / Docker / CI 上 Vite 会直接报 "Failed to resolve import"。
import HotelDetailVue from '@/views/hotel/HotelDetail.vue'
import HotelOrderVue from '@/views/hotel/HotelOrder.vue'
import OrdersVue from '@/views/my/Orders.vue'
import HotelOrdersVue from '@/views/my/HotelOrders.vue'
import TicketOrdersVue from '@/views/my/TicketOrders.vue'
import FlightOrdersVue from '@/views/my/FlightOrders.vue'
import TrainOrdersVue from '@/views/my/TrainOrders.vue'
import ProfileVue from '@/views/my/ProfileVue.vue'
import AttractionDetailVue from '@/views/attraction/AttractionDetail.vue'
import CalendarStockVue from '@/views/ticket/CalendarStock.vue'
import SubscriptionVue from '@/views/my/Subscription.vue'
import ShareTripVue from '@/views/ShareTrip.vue'




//定义路由关系

const routes = [
    {
        path:'/',
        component:LayoutVue,
        children:[
            {path:'/',component:HomeVue},
            {path:'/attraction',component:AttractionVue},
            {path:'/hotel',component:HotelVue},
            {path:'/flight',component:FlightVue},
            {path:'/package',component:PackageVue},
            {path:'/guide',component:GuideVue},
            {path:'/hotel/detail/:id',component:HotelDetailVue},
            {path:'/attraction/detail/:id',component:AttractionDetailVue},
            {path:'/test',component:TestVue},
            {path:'/my/profile',component:ProfileVue},
            {path:'/my/subscription',component:SubscriptionVue},
            {path:'/calendarStock',component:CalendarStockVue}, //商家端测试
            {
                path:'/my/order/',
                component:OrdersVue,
                children:[
                    { path: 'hotel', component: HotelOrdersVue },
                    { path: 'ticket', component: TicketOrdersVue },
                    { path: 'flight', component: FlightOrdersVue },
                    { path: 'train', component: TrainOrdersVue }
                ]
            },
        ]
    },
    {path:'/login',component:LoginVue},
    {path:'/hotel/order/:id',component:HotelOrderVue},
    {path:'/share',component:ShareTripVue},
]

//创建路由器 
const router = createRouter({
    history:createWebHistory(),
    routes:routes,
    // 滚动行为
    scrollBehavior(to, from, savedPosition) {
        // 1. 浏览器前进/后退：恢复离开时的滚动位置
        if (savedPosition) {
            return savedPosition
        }
        // 2. 带锚点的跳转：滚到锚点处（配合 html { scroll-padding-top } 让开吸顶导航）
        if (to.hash) {
            return { el: to.hash, behavior: 'smooth' }
        }
        // 3. 其余情况回到顶部
        return { top: 0, left: 0 }
    }
})


//导出路由
export default router