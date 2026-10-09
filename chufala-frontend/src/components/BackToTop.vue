<template>
  <el-backtop
    class="site-backtop"
    :right="36"
    :bottom="106"
    role="button"
    tabindex="0"
    aria-label="返回页面顶部"
    title="返回顶部"
    @keydown.enter.prevent="scrollToTop"
    @keydown.space.prevent="scrollToTop"
  >
    <svg class="site-backtop__arrow" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M12 19V5m0 0-6 6m6-6 6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
    </svg>
  </el-backtop>
</template>

<script setup lang="ts">
const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
</script>
