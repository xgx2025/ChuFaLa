<template>
  <el-menu
    default-active="1"
    class="sidebar-menu"
    router
    active-text-color="#409EFF" 
    background-color="#f8f9fa"
    text-color="#333"
  >
    <el-menu-item index="/my/order/hotel">
      <el-icon><Hotel /></el-icon>
      <span>酒店订单</span>
    </el-menu-item>
    <el-menu-item index="/my/order/ticket">
      <el-icon><Ticket /></el-icon>
      <span>门票订单</span>
    </el-menu-item>
    <el-menu-item index="/my/order/flight">
      <el-icon><Plane /></el-icon>
      <span>机票订单</span>
    </el-menu-item>
    <el-menu-item index="/my/order/train">
      <el-icon><Train /></el-icon>
      <span>火车票订单</span>
    </el-menu-item>
  </el-menu>
</template>

<script setup>
import {Ticket} from '@element-plus/icons-vue'
import {Hotel, Plane,Train} from '@/components/Icon.vue'
</script>

<style scoped>
.sidebar-menu {
  border-right: none; /* 去掉默认右侧边框，保持简约 */
}
</style>