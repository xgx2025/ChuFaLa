<template>
  <div class="orders-container">
    <el-container>
      <!-- 左侧侧边栏 -->
      <el-aside width="200px" class="sidebar">
        <Sidebar />
      </el-aside>
      <!-- 右侧主内容（路由切换区域） -->
      <el-main>
        <router-view></router-view>
      </el-main>
    </el-container>
  </div>
  <el-backtop :right="100" :bottom="100" />
</template>

<script setup>
import Sidebar from '@/components/Sidebar.vue'
</script>

<style scoped>
.orders-container {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.sidebar {
  background-color: var(--c-bg-sub);
  border-right: 1px solid var(--c-line);
}

.el-main {
  flex: 1;
  padding: var(--sp-5);
  overflow: hidden;
}
</style>
