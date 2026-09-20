<template>
  <div class="location-container">
    <h3>用户当前地理位置</h3>
    <!-- 加载状态 -->
    <div v-if="isLoading" class="loading">定位中...</div>
    <!-- 定位成功：显示经纬度和城市 -->
    <div v-else-if="(lat && lng) || cityName" class="success">
      <template v-if="cityName">当前城市：{{ cityName }}<br></template>
      <template v-if="lat && lng">
        纬度（Lat）：{{ lat }}<br>
        经度（Lng）：{{ lng }}
      </template>
    </div>
    <!-- 定位失败：显示错误信息 -->
    <div v-else class="error">{{ errorMsg }}</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';

// 1. 响应式数据：存储经纬度、城市、加载状态、错误信息
const lat = ref(''); // 纬度
const lng = ref(''); // 经度
const cityName = ref(''); // 城市名称
const isLoading = ref(false); // 定位加载中
const errorMsg = ref(''); // 错误提示

// 2. 配置百度地图AK（从环境变量注入，禁止硬编码；模板见 .env.example）
const BAIDU_MAP_AK = import.meta.env.VITE_BAIDU_MAP_AK;

// 3. 动态加载百度地图API脚本
const loadBaiduMapScript = () => {
  return new Promise((resolve, reject) => {
    // 检查脚本是否已加载（避免重复加载）
    if (window.BMapGL) {
      resolve(window.BMapGL);
      return;
    }

    // 创建script标签，引入百度地图API
    const script = document.createElement('script');
    script.type = 'text/javascript';
    // 回调函数名称要与下面定义的一致
    script.src = `https://api.map.baidu.com/api?v=1.0&type=webgl&ak=${BAIDU_MAP_AK}&callback=initialize`;
    script.onerror = () => reject(new Error('百度地图API加载失败'));
    document.body.appendChild(script);

    // 定义全局回调函数（与script中的callback参数一致）
    window.initialize = () => {
      resolve(window.BMapGL);
    };
  });
};

// 4. 调用百度地图IP定位接口，获取城市信息
const getCityByIP = async () => {
  try {
    isLoading.value = true;
    errorMsg.value = '';

    // 第一步：加载百度地图API
    const BMapGL = await loadBaiduMapScript();

    // 第二步：初始化IP定位对象
    const myCity = new BMapGL.LocalCity();
    
    // 第三步：获取城市信息
    myCity.get((result) => {
      isLoading.value = false;
      if (result) {
        cityName.value = result.name;
        // 同时获取城市中心点坐标
        lat.value = result.center.lat;
        lng.value = result.center.lng;
        console.log('IP定位成功，城市：', cityName.value, '，经纬度：', lat.value, lng.value);
      } else {
        errorMsg.value = '无法获取城市信息';
      }
    });
  } catch (err) {
    isLoading.value = false;
    errorMsg.value = err.message;
  }
};

// 5. 组件挂载后执行定位
onMounted(() => {
  getCityByIP();
});
</script>

<style scoped>
.location-container {
  padding: 20px;
  font-size: 16px;
  max-width: 600px;
  margin: 0 auto;
}
.loading {
  color: #666;
  padding: 10px;
  background-color: #f5f5f5;
  border-radius: 4px;
}
.success {
  color: #2ecc71;
  margin-top: 10px;
  padding: 10px;
  background-color: #f0fff4;
  border-radius: 4px;
  border: 1px solid #c3e6c3;
}
.error {
  color: #e74c3c;
  margin-top: 10px;
  padding: 10px;
  background-color: #fff5f5;
  border-radius: 4px;
  border: 1px solid #ffe3e3;
}
h3 {
  color: #333;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}
</style>