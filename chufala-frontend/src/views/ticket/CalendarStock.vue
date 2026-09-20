<template>
  <div class="calendar-stock-container">
    <!-- 1. 顶部控制区 -->
    <el-card class="control-bar">
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="门票产品">
          <el-select v-model="queryForm.productId" placeholder="请选择门票产品" style="width: 200px">
            <el-option v-for="product in products" :key="product.id" :label="product.name" :value="product.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="queryForm.dateRange"
            type="month"
            placeholder="选择月份"
            format="YYYY-MM"
            value-format="YYYY-MM"
          />
        </el-form-item>
        <el-form-item>
          <el-button :icon="Search" type="primary" @click="handleQuery">查询</el-button>
        </el-form-item>
      </el-form>
      <div class="action-buttons">
        <el-button type="success" :icon="Coin" class="batch-price-btn" @click="openBatchPriceDialog">批量设置价格</el-button>
        <el-button type="warning" :icon="Box" class="batch-stock-btn" @click="openBatchStockDialog">批量设置库存</el-button>
        <el-button :icon="SyncYesterdaySetting" @click="syncYesterday">同步昨日设置</el-button>
      </div>
    </el-card>

    <!-- 2. 日历视图区 -->
    <el-card class="calendar-view">
      <div class="calendar-header">
        <el-button :icon="ArrowLeft" circle @click="changeMonth(-1)" />
        <h2>{{ currentYear }}年 {{ currentMonth }}月</h2>
        <el-button :icon="ArrowRight" circle @click="changeMonth(1)" />
      </div>
      <el-calendar v-model="currentDate">
        <template #date-cell="{ data }">
          <CalendarDayItem
            :day="data.day"
            :stock-data="stockMap[data.day]"
            :is-past="isPastDay(data.day)"
            @edit="openQuickEditDialog"
          />
        </template>
      </el-calendar>
    </el-card>

    <!-- 3. 快速编辑弹窗 -->
    <QuickEditDialog
      v-model:visible="quickEditDialogVisible"
      :day="selectedDay"
      :initial-data="selectedDayData"
      @confirm="handleQuickEditConfirm"
    />

    <!-- 4. 批量设置价格弹窗 -->
    <BatchPriceDialog
      v-model:visible="batchPriceDialogVisible"
      @confirm="handleBatchPriceConfirm"
    />

    <!-- 5. 批量设置库存弹窗 -->
    <BatchStockDialog
      v-model:visible="batchStockDialogVisible"
      @confirm="handleBatchStockConfirm"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search, ArrowLeft,ArrowRight} from '@element-plus/icons-vue';
import dayjs from 'dayjs';
import isSameOrBefore from 'dayjs/plugin/isSameOrBefore';
import isSameOrAfter from 'dayjs/plugin/isSameOrAfter';

// 模拟子组件，实际项目中应拆分为单独的 .vue 文件
import CalendarDayItem from './components/CalendarDayItem.vue';
import QuickEditDialog from './components/QuickEditDialog.vue';
import BatchPriceDialog from './components/BatchPriceDialog.vue';
import BatchStockDialog from './components/BatchStockDialog.vue';
import {Coin,Box,SyncYesterdaySetting} from '@/components/Icon.vue';

dayjs.extend(isSameOrBefore);
dayjs.extend(isSameOrAfter);

// --- 数据与状态 ---
const queryForm = reactive({
  productId: 1, // 默认选中第一个产品
  dateRange: dayjs().format('YYYY-MM'),
});

const currentDate = ref(new Date());
const products = ref([
  { id: 1, name: '成人票' },
  { id: 2, name: '儿童票' },
  { id: 3, name: '学生票' },
]);

// 模拟从后端获取的库存数据，格式为 { 'YYYY-MM-DD': { price, stock, status } }
const stockData = ref({});
const stockMap = computed(() => stockData.value);

// --- 弹窗状态 ---
const quickEditDialogVisible = ref(false);
const batchPriceDialogVisible = ref(false);
const batchStockDialogVisible = ref(false);
const selectedDay = ref('');
const selectedDayData = ref(null);

// --- 计算属性 ---
const currentYear = computed(() => dayjs(currentDate.value).year());
const currentMonth = computed(() => dayjs(currentDate.value).month() + 1);

// --- 方法 ---
const fetchStockData = async () => {
  console.log('Fetching data for:', queryForm);
  // 在这里调用你的后端API
  // const res = await api.getCalendarStock(queryForm.productId, queryForm.dateRange);
  // stockData.value = res.data;

  // --- 模拟数据 ---
  const mockData = {};
  const startOfMonth = dayjs(queryForm.dateRange).startOf('month');
  const endOfMonth = dayjs(queryForm.dateRange).endOf('month');
  let day = startOfMonth;
  while (day.isSameOrBefore(endOfMonth)) {
    const dayStr = day.format('YYYY-MM-DD');
    const isWeekend = day.day() === 0 || day.day() === 6;
    mockData[dayStr] = {
      price: isWeekend ? 200.0 : 150.0,
      stock: Math.floor(Math.random() * 1000),
      status: Math.random() > 0.1 ? 1 : 0, // 90%概率可售
    };
    day = day.add(1, 'day');
  }
  mockData[dayjs().format('YYYY-MM-DD')].stock = 0; // 模拟今日售罄
  stockData.value = mockData;
};

const handleQuery = () => {
  currentDate.value = dayjs(queryForm.dateRange + '-01').toDate();
  fetchStockData();
};

const changeMonth = (direction) => {
  currentDate.value = dayjs(currentDate.value).add(direction, 'month').toDate();
  queryForm.dateRange = dayjs(currentDate.value).format('YYYY-MM');
  fetchStockData();
};

const isPastDay = (day) => {
  return dayjs(day).isBefore(dayjs(), 'day');
};

// --- 快速编辑 ---
const openQuickEditDialog = (day, data) => {
  selectedDay.value = day;
  selectedDayData.value = { ...data };
  quickEditDialogVisible.value = true;
};

const handleQuickEditConfirm = (formData) => {
  console.log('Quick Edit Confirm:', formData);
  // 调用API更新单日数据
  // await api.updateSingleDayStock(queryForm.productId, selectedDay.value, formData);
  ElMessage.success('更新成功！');
  fetchStockData(); // 刷新数据
  quickEditDialogVisible.value = false;
};

// --- 批量操作 ---
const openBatchPriceDialog = () => {
  batchPriceDialogVisible.value = true;
};

const handleBatchPriceConfirm = (formData) => {
  console.log('Batch Price Confirm:', formData);
  // 调用API批量更新价格
  // await api.batchUpdatePrice(queryForm.productId, formData);
  ElMessage.success('批量设置价格成功！');
  fetchStockData();
  batchPriceDialogVisible.value = false;
};

const openBatchStockDialog = () => {
  batchStockDialogVisible.value = true;
};

const handleBatchStockConfirm = (formData) => {
  console.log('Batch Stock Confirm:', formData);
  // 调用API批量更新库存
  // await api.batchUpdateStock(queryForm.productId, formData);
  ElMessage.success('批量设置库存成功！');
  fetchStockData();
  batchStockDialogVisible.value = false;
};

const syncYesterday = () => {
  ElMessageBox.confirm('此操作将用昨天的设置覆盖今天的设置，是否继续？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    // 调用API同步
    // await api.syncYesterday(queryForm.productId);
    ElMessage.success('同步成功！');
    fetchStockData();
  }).catch(() => {});
};

// --- 生命周期 ---
onMounted(() => {
  fetchStockData();
});
</script>

<style scoped>

:deep(.batch-price-btn .el-icon) {
  font-size: var(--fs-h3); /* 核心：调节图标大小（默认1em，约16px） */
  width: 20px;     /* 兜底：部分 SVG 需显式设置宽高 */
  height: 20px;
}

:deep(.batch-stock-btn .el-icon) {
  font-size: var(--fs-h3); /* 核心：调节图标大小（默认1em，约16px） */
  width: 20px;     /* 兜底：部分 SVG 需显式设置宽高 */
  height: 20px;
}


.calendar-stock-container {
  padding: 20px;
}
.control-bar {
  margin-bottom: 20px;
}
.query-form {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}
.action-buttons {
  margin-top: 15px;
}
.calendar-view {
  padding: 20px;
}
.calendar-header {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 20px;
}
.calendar-header h2 {
  margin: 0 20px;
  font-weight: bold;
}

/* 覆盖 Element Plus 日历默认样式 */
:deep(.el-calendar-table .el-calendar-day) {
  height: 120px;
  padding: 4px;
}
:deep(.el-calendar-table .el-calendar-day:hover) {
  background-color: var(--c-bg-sub);
  cursor: pointer;
}
</style>
