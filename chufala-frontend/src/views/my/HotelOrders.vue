<template>
  <div class="hotel-orders">
    <h1 class="page-title">酒店订单</h1>

    <!-- 筛选栏 -->
    <el-card shadow="hover" class="filter-card">
      <el-select v-model="orderStatus" placeholder="订单类型" style="width: 180px; margin-right: 20px;">
        <el-option label="全部订单" value="all"></el-option>
        <el-option label="已取消" value="已取消"></el-option>
        <el-option label="待支付" value="待支付"></el-option>
        <el-option label="已支付" value="已支付"></el-option>
      </el-select>
      <el-button type="primary"  size="small" @click="fetchHotelOrders">查询</el-button>
    </el-card>

    <!-- 订单列表（每个订单用卡片包裹） -->
    <el-card 
      shadow="hover" 
      v-for="order in orderList" 
      :key="order.orderId" 
      class="order-card"
    >
      <div class="order-header">
        <span class="order-id">订单号: <span style="color: #06c;">{{ order.orderId }}</span></span>
        <span class="order-date">预订日期: {{ order.bookTime }}</span>
        <el-button type="text" size="14px" class="delete-btn" @click="deleteOrder(order.orderId)">删除订单</el-button>
      </div>
      <div class="order-info">
        <div class="hotel-info">
          <h3>{{ order.hotelName }}</h3>
          <p>{{ order.address }}</p>
          <div style="display: inline-flex;"><p>房型：</p><p style="font-weight: bold;">{{ order.roomType }}</p></div>
          <p>入住日期: {{ order.checkIn }} 至 {{ order.checkOut }} &nbsp;&nbsp;&nbsp;{{ order.nightNum }} 晚 / {{ order.roomCount }}间</p>
          <p>入住人: {{ order.guestName }}</p>
        </div>
        <div class="order-status">
          <span class="status-tag">{{ order.orderStatus }}</span>
          <span class="price">¥{{ order.actualPrice }}</span>
        </div>
      </div>
      <div class="order-actions">
        <div v-if="order.orderStatus === '已支付' || order.orderStatus === '已取消'">
          <el-button type="text" style="height: 35px;width: 98px; font-size: 14px; border-color: #609eef; color: #2477e3;" @click="goToHotelDetail(order.hotelId)">酒店详情</el-button>
          <el-button type="text" style="height: 35px;width: 98px; font-size: 14px; border-color: #609eef;color: #2477e3;;" @click="router.push(`/hotel/order/${order.roomTypeId}`)">再次预订</el-button>
        </div>
        <div v-else>
          <el-button type="text" style="height: 35px;width: 98px; font-size: 14px; border-color: #609eef; color: #2477e3;" @click="goToHotelDetail(order.hotelId)">酒店详情</el-button>
          <el-button type="text" style="height: 35px;width: 98px; font-size: 14px; border-color: #609eef; color: #2477e3;" @click="cancelHotelOrder(order.orderId)">取消订单</el-button>
          <el-button type="primary" style="height: 35px;width: 98px; font-size: 14px; background-color: #ff9500;border-color: #ff9500;" @click="pay(order.orderId)">支付订单</el-button>
        </div>
      </div>
    </el-card>

    <!-- 分页 -->
    <el-pagination
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :current-page="currentPage"
      :page-sizes="[5, 8, 10, 12]"
      :page-size="pageSize"
      layout="total, sizes, prev, pager, next, jumper"
      :total="total"
    >
    </el-pagination>

    <!-- 支付表单容器。必须放在根节点**内部**：
         与根 <div> 平级会让组件渲染成 Fragment 根，
         破坏 Layout.vue 的 <transition mode="out-in"> 页面过渡。
         用 getElementById 取用，层级变化不影响。 -->
    <div id="alipay-form-container" style="display: none;"></div>
  </div>
</template>

<script setup>
import { ref,onMounted } from 'vue'
import { cancelHotelOrderService, getHotelOrderListService, payOrderService } from '@/api/hotel'
import dayjs from 'dayjs'
import { deleteHotelOrderService } from '@/api/hotel'
import { ElMessage, ElNotification,ElMessageBox } from 'element-plus'
import router from '@/router'

// 订单类型筛选
const orderStatus = ref("all")
// 分页参数
const currentPage = ref(1)
const pageSize = ref(5)
const total = ref(0) // 总订单数

// 模拟订单数据
const orderList = ref(null)
const payFormContainer = ref(null);
onMounted(() => {
  payFormContainer.value = document.getElementById('alipay-form-container');
});
// 分页事件
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchHotelOrders()
}
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchHotelOrders()
}
const fetchHotelOrders = async() => { 
  const result = await getHotelOrderListService({
    currentPage: currentPage.value,
    pageSize: pageSize.value,
    orderStatus: orderStatus.value
  })
  const rawOrders = result.data.data || [];
  const formattedOrders = rawOrders.map((order) => {
    return {...order,bookTime: order.bookTime ? dayjs(order.bookTime).format("YYYY-MM-DD HH:mm:ss") : "", };
  });

  orderList.value = formattedOrders
  total.value = result.data.total
}
fetchHotelOrders()

const deleteOrder = (orderId) => {
  ElMessageBox.confirm(
    '是否要删除该订单？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
  .then(async () => { 
    await deleteHotelOrderService(orderId)
    ElNotification.success("订单删除成功")
    fetchHotelOrders()
  })
  .catch(() => {
    ElNotification.info('已取消删除')
  })
}
const goToHotelDetail = (id) => {
  router.push(`/hotel/detail/${id}`)
}
const pay = async (id) => {
  console.log('发起支付请求，订单ID：', id);
  try {
    // 1. 调用支付接口，获取HTML响应
    const result = await payOrderService({
      orderId: id,
      bizType: 'hotel'
    });
    console.log('后端返回的HTML：', result); 

    // 2. 校验容器
    if (!payFormContainer.value) {
      ElMessage.error('支付容器初始化失败');
      return;
    }

    // 3. 插入正确的HTML
    payFormContainer.value.innerHTML = result;

    // 4. 延迟100ms，确保DOM更新后再提交表单
    setTimeout(() => {
      // 查找表单
      const form = payFormContainer.value.querySelector('form[name="punchout_form"]');
      if (form) {
        console.log('找到支付表单，手动提交');
        form.submit(); // 手动触发跳转
      } else {
        ElMessage.error('未找到支付表单');
        console.error('插入的HTML内容：', payFormContainer.value.innerHTML); // 排查HTML是否正确
      }
    }, 100);

  } catch (error) {
    ElMessage.error('支付请求失败，请稍后再试');
    console.error('支付错误详情：', error);
  }
};

const cancelHotelOrder = async (id) => { 
  ElMessageBox.confirm(
    '是否要取消该订单？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
  .then(async () => { 
    await cancelHotelOrderService(id)
    ElNotification.success("订单取消成功")
    fetchHotelOrders()
  })
}

</script>

<style scoped>
.hotel-orders {
  display: flex;
  flex-direction: column;
  gap: 20px; /* 组件间间距 */
  height: 100%;
  overflow: auto; 
}
.filter-card {
  display: flex !important;
  align-items: center;
  padding: 8px;
}
.order-card {
  padding: 2px;
}
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.order-id, .order-date {
  margin-right: 5px;
  font-size: var(--fs-body);
}
.delete-btn {
  color: var(--c-primary-600);
}
.order-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 2px;
}
.hotel-info {
  display: flex;
  flex-direction: column;
  gap: 3px;
  font-size: var(--fs-body);
}
.hotel-info h3 {
  margin: 0;
  font-size: var(--fs-body-lg);
}
.order-status {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 80px;
}
.status-tag {
  color: var(--c-ink-4);
  padding: 2px 6px;
  border-radius: 4px;
}
.price {
  font-size: var(--fs-h2);
  font-weight: bold;
  color: var(--c-ink);
}
.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>