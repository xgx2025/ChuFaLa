<template>
  <el-menu
    :default-active="activeIndex"
    class="sidebar-menu"
    router
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
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Ticket } from '@element-plus/icons-vue'
import { Hotel, Plane, Train } from '@/components/Icon.vue'

const route = useRoute()

// 原来写死 default-active="1"，任何子路由下都没有菜单项处于高亮态
const activeIndex = computed(() => route.path)
</script>

<style scoped>
/* 原实现用 active-text-color / background-color / text-color 三个属性写死颜色，
   其中 active-text-color 用的是 Element Plus 默认蓝，与项目主色冲突。
   改为覆写 Element Plus 的菜单变量，统一走设计令牌。 */
.sidebar-menu {
  border-right: none;
  --el-menu-active-color: var(--c-primary-600);
  --el-menu-bg-color: transparent;
  --el-menu-text-color: var(--c-ink-2);
  --el-menu-hover-bg-color: var(--c-primary-50);
  --el-menu-item-height: 48px;
}

.sidebar-menu :deep(.el-menu-item) {
  border-radius: var(--r-sm);
  margin: var(--sp-1) var(--sp-2);
  transition: background-color var(--dur-base) var(--ease-out),
    color var(--dur-base) var(--ease-out);
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background-color: var(--c-primary-50);
  font-weight: 500;
}
</style>
