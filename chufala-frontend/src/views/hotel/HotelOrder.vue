<template>
  <div class="hotel-booking-page">
    <!-- 顶部步骤条 -->
    <el-steps :active="1" align-center finish-status="success" style="margin-bottom: 24px;">
      <el-step title="已选"></el-step>
      <el-step title="个人信息"></el-step>
      <el-step title="最后一步"></el-step>
    </el-steps>

    <!-- 主体布局：左侧表单 + 右侧费用明细 -->
    <el-row :gutter="24">
      <!-- 左侧表单区域 -->
      <el-col :span="16">
        <!-- 酒店基础信息卡片 -->
        <div class="hotel-info-card">
          <h2>上海古北福玥酒店</h2>
          <p class="hotel-location"><el-icon style="color: #104287;"><Locate/></el-icon>中国，上海，长宁区，红宝石路188号B幢</p>
          <p class="room-type">
            经典大床房
            <span>2人入住</span>
            <span>1张1.8米大床</span>
            <span>无早餐</span>
            <el-link type="text" @click="showMoreFacilities">显示更多设施</el-link>
          </p>
          <div class="policy">
            <p>订房必读</p>
            <el-link type="text" @click="viewAllPolicy">查看全部</el-link>
            <p class="policy-desc">
              为贯彻落实《上海市生活垃圾管理条例》相关规定，推进生活垃圾源头减量，上海市文化和旅游局特制定《关于本市旅游住宿业不主动提供客房一次性日用品的实施意见》，2019年7月1日起，上海市旅游住宿业将不再主动提供牙刷、梳子、浴擦、剃须刀、指甲锉、鞋擦这些一次性日用品。若需要可咨询酒店。
            </p>
          </div>
        </div>

        <!-- 预订表单 -->
        <el-form :model="bookingForm" label-width="120px" class="booking-form">
          <!-- 日期与房间数 -->
          <div class="date-wrapper">
          <el-form-item label="日期选择" class="date-picker">
            <el-date-picker
              ref="datePickerRef"
              v-model="dateRange"
              type="daterange"
              :picker-options="pickerOptions"
              :disabled-date="disabledDate"
              class="custom-date-picker hide-input-text" 
            />
          </el-form-item >
           <el-tooltip
            class="box-item"
            effect="dark"
            content="提交信息后自动计算"
            placement="top"
          >
          <span class="time-tag">{{ nightNum }}晚</span>
          </el-tooltip>
          </div>
          <el-form-item label="房间数">
             <el-select v-model="bookingForm.roomCount" placeholder="请选择房间数量" :disabled="roomStock <= 0">
                <template v-if="roomStock <= 0">
                  <el-option label="暂无可用房间" value="" disabled></el-option>
                </template>
                <template v-else>
                  <el-option 
                    v-for="num in roomOptions" 
                    :key="num" 
                    :label="num.toString()" 
                    :value="num.toString()"
                  ></el-option>
                </template>
              </el-select>
          </el-form-item>

          <!-- 住客资料 -->
          <el-form-item label="住客姓名">
            <el-input v-model="bookingForm.guestName" placeholder="每间只需填1人" />
          </el-form-item>
          <el-form-item label="电子邮件（选填）">
            <el-input v-model="bookingForm.email" placeholder="电子邮件" />
          </el-form-item>
          <el-form-item label="电话号码">
            <el-input
              v-model="bookingForm.phone"
              placeholder="电话号码"
            />
          </el-form-item>

          <!-- 预计到店 -->
          <el-form-item label="到达时间">
            <el-select v-model="bookingForm.arrivalTime" placeholder="请选择">
              <el-option label="18:30" value="18:30"></el-option>
              <el-option label="19:00" value="19:00"></el-option>
              <el-option label="其他时间" value="other"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="房间整晚保留">
            <el-checkbox v-model="bookingForm.roomKeepAllNight" />
          </el-form-item>

          <!-- 特别要求 -->
          <el-form-item label="特别要求（选填）">
            <div>
              <p class="special-request-desc">我们会将您的需求转达给酒店，但无法确保一定能够满足。</p>
              <el-link type="text" @click="toggleSpecialRequest" style="color: #0065f2;">
                {{ specialRequestExpanded ? '收起' : '展开' }}
              </el-link>
              <el-input
                type="textarea"
                v-model="bookingForm.specialRequest"
                v-show="specialRequestExpanded"
                placeholder="请输入特别要求"
              />
            </div>
          </el-form-item>

          <!-- 发票信息 -->
          <el-form-item label="发票信息">
            <p class="invoice-desc">下单后在订单页开具，发票由赫程国际旅行社或其分公司开具（可开普票，专票）</p>
          </el-form-item>
          <el-button type="primary"  @click="confirmDate" >提交订单信息</el-button>
          <!-- 协议与支付 -->
          <div class="agreement">
            <el-link type="primary" @click="viewTerms" style="color: #0065f2;">预订条款</el-link>
            <span>，</span>
            <el-link type="primary" @click="viewAuthAgreement" style="color: #0065f2;">个人信息授权协议</el-link>
            <span>，前往下一步即代表已阅读并同意上述条件</span>
          </div>
          <div class="payment-bar">
            <span style="font-size: 14px;">在线付 <span style="font-size: 24px; font-weight: bold; color: #0065f2;">¥{{ totalAmount }}</span></span>
            <el-button type="primary" size="large" @click="goToPay" style="background-color: #ff9500;border-color: #ff9500;">立即预定</el-button>
          </div>
          <el-link type="text" @click="changeSelection">
            <i class="el-icon-back"></i> 更改我的选择
          </el-link>
        </el-form>
      </el-col>

      <!-- 右侧费用明细区域 -->
      <el-col :span="8">
        <div class="cost-detail-card">
          <h2>费用明细</h2>
          <div class="cost-item">
            <span>在线支付</span>
            <span style="font-weight:bold; color: #006ff6;">¥{{ onlinePayment }}</span>
          </div>
          <div class="cost-item">
            <span>{{ costDates.checkIn }}</span>
            <span>¥{{ dailyAmount }}</span>
          </div>
          <div class="cost-item">
            <span>{{ costDates.checkOut }} (离店日)</span>
          </div>
          <div class="divider"></div>
          <div class="total-cost">
            <span style="margin-top: 10px;">应付总额</span>
            <span>在线支付<span style="font-size: 24px;font-weight: bold;color: #006ff6;"> ¥{{ totalAmount }}</span></span>
          </div>
          <el-divider border-style="dashed" />
          <div class="policy-desc">
            <h4 style="color: #06aebd;font-size: 14px;">限时取消</h4>
            <p>
              入住当天18:00前可免费取消。若未入住将收取您 ¥{{ totalAmount }}（如用优惠券、积分则以使用后的支付价为准）。订单需等酒店或供应商确认后生效，订单确认结果以携程短信、邮件或app通知为准，如订单不确认将全额退款至您的付款账户。
            </p>
          </div>

          <div class="description">
            <h4>说明</h4>
            <p>预订服务由出发啦旗下上海赫程国际旅行社有限公司及其分公司提供、住宿服务由酒店提供。</p>
          </div>

          <el-link type="primary" @click="viewServiceGuarantee">出发啦专业服务 全程保障</el-link>
        </div>
      </el-col>
    </el-row>
    <!-- 支付表单容器 -->
    <div id="alipay-form-container" style="display: none;"></div>
    <!-- 底部版权 -->
    <div class="footer">
      <p>Copyright© 2025, chufala.com  All rights reserved. </p>
    </div>
  </div>
</template>

<script setup>
import { ref, watch,onMounted } from 'vue';
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus';
import { useRoute } from 'vue-router';
import { Locate } from '@/components/Icon.vue';
import { getRoomInfoService, getRoomTotalPriceService, bookRoomService, payOrderService } from '@/api/hotel';
// 引入dayjs核心库（若需要处理时区，可额外引入dayjs/plugin/timezone等插件）
import dayjs from 'dayjs';


// 接收路由参数（房间ID）
const route = useRoute();
const roomId = route.params.id; // 从路由中获取ID
ElNotification.primary("请完成相关信息的填写");

// 房间库存与选项
const roomOptions = ref([]);
const roomStock = ref(0);
const nightNum = ref(1);

// 表单响应式数据
const bookingForm = ref({
  checkInDate: '',
  checkOutDate: '', 
  roomCount: '1',
  guestName: '',
  email: '',
  phone: '',
  arrivalTime: '18:30',
  roomKeepAllNight: false,
  specialRequest: '',
});

// 费用明细中的动态日期（MM-dd格式）
const costDates = ref({
  checkIn: '',
  checkOut: ''
});

const priceInfo = ref({
  checkIn: '',
  checkOut: '',
  nightNum: 1,
  roomCount: 0,
  totalPrice: 0,
})
const rawData = ref('');
const priceSignature = ref('');

// 用ref声明容器变量
const payFormContainer = ref(null);

// 在DOM挂载后获取容器
onMounted(() => {
  payFormContainer.value = document.getElementById('alipay-form-container');
});
// 从后端获取房间库存
const fetchRoomInfo = async () => {
  try {
    const result = await getRoomInfoService(roomId);
    roomStock.value = result.data.stock;
    if(roomStock.value == 0){
      ElMessage.error('当前房间已售罄，请选择其他房间');
    }
  } catch (error) {
    console.error('获取房间库存失败：', error);
    roomStock.value = 0;
  }
};

// 监听库存变化，动态生成房间数量选项
watch(
  () => roomStock.value,
  (newStock) => {
    if (newStock > 0) {
      roomOptions.value = Array.from({ length: newStock }, (_, i) => i + 1);
      // 若当前选中值超过库存，重置为最大可用值
      if (Number(bookingForm.roomCount) > newStock) {
        bookingForm.roomCount = newStock.toString();
      }
    } else {
      roomOptions.value = [];
    }
  },
  { immediate: true }
);

fetchRoomInfo();

// 日期范围
const dateRange = ref([]);
const disabledDate = (time) => time.getTime() < Date.now() - 8.64e7
// 日期选择器配置：禁用过去的日期（含昨天及之前）
const pickerOptions = ref({
  disabledDate(time) {
    const yesterdayEnd = dayjs().subtract(1, 'day').endOf('day').valueOf();
    return time.getTime() < yesterdayEnd;
  },
});

// 初始化默认日期：今天入住，明天退房
const initDefaultDate = () => {
  const today = dayjs();
  const tomorrow = today.add(1, 'day');
  // 直接用dayjs格式化，无需手动拼接
  dateRange.value = [
    today.format('YYYY-MM-DD'),
    tomorrow.format('YYYY-MM-DD')
  ];
  // 更新表单和费用明细的日期显示
  updateDateDisplay(today, tomorrow);
};

// 更新费用明细中的短日期
const updateDateDisplay = (startDayjs, endDayjs) => {
  bookingForm.value.checkInDate = startDayjs.format('YYYY-MM-DD');
  bookingForm.value.checkOutDate = endDayjs.format('YYYY-MM-DD');
  costDates.value.checkIn = startDayjs.format('MM-DD');
  costDates.value.checkOut = endDayjs.format('MM-DD');
};

// 监听日期选择变化，同步更新显示
watch(dateRange, (newRange) => {
  if (newRange.length!== 2 || !newRange[0] || !newRange[1]) return;
  
  const startDayjs = dayjs(newRange[0]);
  const endDayjs = dayjs(newRange[1]);
  
  if (!startDayjs.isValid() || !endDayjs.isValid()) {
    ElMessage.warning('日期格式无效，请重新选择');
    return;
  }
  
  // 校验退房日期不能早于入住日期
  if (endDayjs.isBefore(startDayjs)) {
    ElMessage.warning('退房日期不能早于入住日期');
    return;
  }
  
  // 更新日期显示
  updateDateDisplay(startDayjs, endDayjs);
});

// 初始化默认日期
initDefaultDate();

// 工具函数1：解析 rawData 字符串（处理 & 和 , 混合分隔符）
const parseRawData = (rawData) => {
  const params = {};
  // 第一步：先按 & 分割成大段（如 ["roomId=123", "checkIn=2024-09-18", ..., "timestamp=1726646400000"]）
  const bigSegments = rawData.split('&');
  
  bigSegments.forEach(segment => {
    // 第二步：对每个大段，再按 , 分割（处理 nightNum=2,roomCount=1 这种情况）
    const smallSegments = segment.split(',');
    
    smallSegments.forEach(smallSeg => {
      // 第三步：按 = 分割 key 和 value（跳过格式错误的段）
      const [key, value] = smallSeg.split('=');
      if (key && value !== undefined) {
        // 可选：转换数据类型（后端是 int/double，前端默认是字符串）
        if (key === 'nightNum' || key === 'roomCount' || key === 'timestamp') {
          params[key] = parseInt(value, 10); // 转整数
        } else if (key === 'totalPrice') {
          params[key] = parseFloat(value); // 转浮点数
        } else {
          params[key] = value; // 字符串类型（如 roomId、日期）
        }
      }
    });
  });
  
  return params;
};
const confirmDate = async () => { 
   if (!bookingForm.value.guestName) {
    ElMessage.warning('请填写住客姓名');
    return;
  }
  if (!bookingForm.value.phone) {
    ElMessage.warning('请填写电话号码');
    return;
  }
  const checkInFormatted = dayjs(dateRange.value[0]).format('YYYY-MM-DD');
  const checkOutFormatted = dayjs(dateRange.value[1]).format('YYYY-MM-DD');
  const result = await getRoomTotalPriceService({ id: roomId, checkIn:checkInFormatted, checkOut: checkOutFormatted,roomCount: bookingForm.value.roomCount }) 
  rawData.value = result.data.data;
  const rawDataStr = rawData.value;
  priceSignature.value = result.data.signature;
  const parsedParams = parseRawData(rawDataStr);
  console.log(parsedParams);
  const totalPrice = parsedParams.totalPrice;
  nightNum.value = parsedParams.nightNum;
  totalAmount.value = totalPrice.toFixed(2); // 保留两位小数
  onlinePayment.value = totalPrice.toFixed(2); // 在线支付金额同步
  ElNotification.success("信息提交成功！")

};



// 费用相关数据
const onlinePayment = ref('0.00');
const dailyAmount = ref('0.00');
const totalAmount = ref('0.00');

// 特别要求展开状态
const specialRequestExpanded = ref(false);

// 显示更多设施
const showMoreFacilities = () => {
  ElMessage.info('显示更多设施');
};

// 查看全部订房政策
const viewAllPolicy = () => {
  ElMessage.info('查看全部订房政策');
};

// 切换特别要求展开/收起
const toggleSpecialRequest = () => {
  specialRequestExpanded.value = !specialRequestExpanded.value;
};

// 查看预订条款
const viewTerms = () => {
  ElMessage.info('查看预订条款');
};

// 查看个人信息授权协议
const viewAuthAgreement = () => {
  ElMessage.info('查看个人信息授权协议');
};


const pay = async (id) => {
  ElMessage.success('正在前往支付页面...');
  console.log('发起支付请求，订单ID：', id);
  try {
    // 1. 调用支付接口，获取HTML响应（注意：result.data才是HTML）
    const result = await payOrderService({
      orderId: id,
      bizType: 'hotel'
    });
    console.log('后端返回的HTML：', result); // 调试用：确认是完整的<form>标签

    // 2. 校验容器（ref变量必须用 .value 访问）
    if (!payFormContainer.value) {
      ElMessage.error('支付容器初始化失败');
      return;
    }

    // 3. 插入正确的HTML（用 result.data，而非 result）
    payFormContainer.value.innerHTML = result;

    // 4. 延迟100ms，确保DOM更新后再提交表单
    setTimeout(() => {
      // 查找表单（按name精准定位）
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

// 去支付（表单验证+跳转）
const goToPay =  async () =>{
if (!rawData.value || !priceSignature.value) {
    ElMessage.warning('请先提交订单信息');
    return;
  }

  const result = await bookRoomService({
    guestName:bookingForm.value.guestName,
    guestPhone:bookingForm.value.phone,
    guestEmail:bookingForm.value.email,
    arrivalTime:bookingForm.value.arrivalTime,
    specialRequest:bookingForm.value.specialRequest,
    rawData:rawData.value,
    signature:priceSignature.value,
  });
  const orderId = result.data;
  ElMessageBox.confirm('是否确认支付？', '提示', {
    confirmButtonText: '立即支付',
    cancelButtonText: '稍后支付',
    type: 'primary',
    }).then(() => { 
       pay(orderId);
    }).catch(() => { 
    ElMessage.primary('请在30分钟内完成支付,否则订单将自动取消')
    // 刷新页面
    window.location.reload();
  });
};


// 更改选择（返回上一步）
const changeSelection = () => {
  route.back();
};

// 查看服务保障
const viewServiceGuarantee = () => {
  ElMessage.info('查看出发啦专业服务全程保障');
};
</script>

<style scoped>
.hotel-booking-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

/* 酒店信息卡片 */
.hotel-info-card {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 16px;
  margin-bottom: 20px;
}

.hotel-info-card h2 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
}

.hotel-location {
  color: #606266;
  margin-bottom: 8px;
}

.room-type {
  margin-bottom: 8px;
}

.policy {
  margin-top: 12px;
}

.policy-desc {
  color: #606266;
  font-size: 13px;
  line-height: 1.5;
}

/* 预订表单 */
.booking-form {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 16px;
  margin-bottom: 20px;
}

.special-request-desc {
  color: #606266;
  font-size: 13px;
  margin-bottom: 8px;
}

.invoice-desc {
  color: #606266;
  font-size: 13px;
}

.agreement {
  margin: 16px 0;
  font-size: 13px;
  line-height: 1.5;
}

.payment-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 16px 0;
}

.payment-bar .el-button {
  width: 120px;
}

.phone-prefix {
  display: inline-block;
  margin-right: 8px;
  color: #606266;
}

/* 费用明细卡片 */
.cost-detail-card {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 16px;
  height: fit-content;
}

.cost-detail-card h3 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
}

.cost-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
}

.divider {
  height: 1px;
  background-color: #ebeef5;
  margin: 16px 0;
}

.total-cost {
  display: flex;
  justify-content: space-between;
  font-weight: 600;
  margin-bottom: 10px;
  font-size: 15px;
}

.policy-desc,
.description {
  margin-bottom: 16px;
  font-size: 13px;
  line-height: 1.5;
}

.policy-desc h4,
.description h4 {
  font-weight: 600;
  margin-bottom: 8px;
}

/* 底部版权 */
.footer {
  text-align: center;
  color: #606266;
  margin-top: 40px;
  font-size: 13px;
}

.date-wrapper {
  display: flex; /* 核心：让 el-form-item 和 晚 字同行 */
  align-items: center; /* 垂直居中，更美观 */
  gap: 12px; /* 两者之间的间距（替代 margin） */
  margin-bottom: 20px; /* 与下方表单项保持一致间距 */
  width: auto; /* 移除固定 800px 宽度，自适应内容 */
}

/* 调整表单项样式：清除默认块级特性和边距 */
.date-picker {
  display: inline-flex !important; /* 强制行内 flex，避免独占一行 */
  width: auto !important; /* 移除 150px 限制，让日期选择器正常显示 */
  margin-bottom: 0 !important; /* 清除 el-form-item 默认的底部外边距 */
}

/* 日期选择器本身：设置合适宽度（按需调整） */
.custom-date-picker {
  width: 220px !important; 
}

/* “晚”字标签样式不变，可微调 */
.time-tag {
  font-size: 14px;
  color: #333;
  margin-top: 0 !important; /* 清除可能的默认边距 */
}
</style>