<script setup lang="ts">
</script>

<template>
  <!--
    顶层路由过渡：只做 opacity，不做 transform。
    因为 Layout 内部包含 position: fixed 的 SmartAssistant，
    一旦祖先带 transform，fixed 元素会退化为相对该祖先定位而错位。
    页面级的位移过渡放在 Layout.vue 内层的 router-view 上。
  -->
  <router-view v-slot="{ Component, route }">
    <transition name="fade" mode="out-in">
      <component :is="Component" :key="route.matched[0]?.path || route.path" />
    </transition>
  </router-view>
</template>
