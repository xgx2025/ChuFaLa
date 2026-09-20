<template>
  <div class="app-container">
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
  <el-backtop :right="100" :bottom="100" style="color:rgb(82, 233, 200);"/>
</template>

<script setup>
import Sidebar from '@/components/Sidebar.vue'
</script>

<style scoped>
.app-container {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.sidebar {
  background-color: #f8f9fa;
  box-shadow: 2px 0 6px rgba(0, 0, 0, 0.1);
}

.el-main {
  flex: 1;
  padding: 20px;
  overflow: hidden;
}
</style>