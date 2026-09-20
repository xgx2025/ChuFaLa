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

    <!-- el-backtop 必须放在根节点**内部**，否则组件渲染成 Fragment 根，
         Layout.vue 的 <transition mode="out-in"> 无法对其执行 leave 过渡，
         离开本页后主内容区会永久空白（必须刷新才恢复）。 -->
    <el-backtop :right="100" :bottom="100" />
  </div>
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
