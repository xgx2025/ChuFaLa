<template>
  <main class="payment-return">
    <el-card class="result-card" shadow="hover">
      <h1>已返回出发啦</h1>
      <p>支付结果正在确认，请到对应页面查看最终状态。</p>
      <p class="hint">如果刚完成付款但状态尚未更新，请稍后刷新订单页面。</p>
      <div class="actions">
        <el-button type="primary" @click="router.push('/my/order/hotel')">查看酒店订单</el-button>
        <el-button @click="router.push('/my/subscription')">查看会员权益</el-button>
      </div>
    </el-card>
  </main>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style scoped>
.payment-return {
  display: flex;
  justify-content: center;
  padding: 80px 24px;
}

.result-card {
  width: min(100%, 520px);
  text-align: center;
}

.hint {
  color: var(--c-text-muted, #64748b);
}

.actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 28px;
}
</style>
