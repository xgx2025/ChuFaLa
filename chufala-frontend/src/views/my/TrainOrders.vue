<template>
  <div class="orders-page">
    <h1 class="page-title">火车票订单</h1>

    <!-- 筛选栏 -->
    <el-card shadow="never" class="filter-card">
      <el-select v-model="status" placeholder="订单类型" class="filter-card__select">
        <el-option label="全部订单" value="all" />
        <el-option label="已取消" value="已取消" />
        <el-option label="待支付" value="待支付" />
        <el-option label="已支付" value="已支付" />
      </el-select>
      <el-button type="primary" @click="handleQuery">查询</el-button>
    </el-card>

    <!-- 加载态 -->
    <div v-if="loading" class="orders-skeleton">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 空态：原实现只有筛选栏，下方是空白，看起来像页面坏了 -->
    <el-empty v-else :image-size="120" description="暂无火车票订单">
      <el-button type="primary" @click="router.push('/flight')">去看看出行票务</el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import router from '@/router'

const status = ref('all')
const loading = ref(false)

const handleQuery = () => {
  // 火车票订单接口尚未接入，先保留交互入口
}
</script>

<style scoped>
.orders-page {
  padding: var(--sp-1) 0;
}

.filter-card {
  margin-bottom: var(--sp-6);
}

.filter-card__select {
  width: 180px;
  margin-right: var(--sp-5);
}

.orders-skeleton {
  padding: var(--sp-4) 0;
}
</style>
