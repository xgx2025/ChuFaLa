/**
 * 地理位置状态管理模块（改为浏览器定位 + GCJ-02）
 * 不再依赖百度地图，使用 WGS-84 → GCJ-02 转换
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import gcoord from 'gcoord';

//如果你仍想根据坐标反查城市，可配置高德 key
const AMAP_API_KEY = import.meta.env.VITE_AMAP_API_KEY

export const useGeoStore = defineStore('geo', () => {
  const lat = ref<number | null>(null)   // GCJ-02 纬度
  const lng = ref<number | null>(null)   // GCJ-02 经度
  const cityName = ref('')
  const isLoading = ref(false)
  const errorMsg = ref('')

  /**
   * 通过浏览器 Geolocation API 获取位置并转换为 GCJ-02
   */
  const getCurrentPosition = (): Promise<{ lat: number; lng: number }> => {
    return new Promise((resolve, reject) => {
      if (!navigator.geolocation) {
        reject(new Error('当前浏览器不支持地理位置功能'))
        return
      }
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const { latitude, longitude } = position.coords
          // 转换 WGS-84 → GCJ-02
          const [gcjLng, gcjLat] = gcoord.transform(
            [longitude, latitude],
            gcoord.WGS84,
            gcoord.GCJ02
          )
          resolve({ lat: gcjLat, lng: gcjLng })
        },
        (error) => {
          let msg = '定位被拒绝或不可用'
          if (error.code === error.TIMEOUT) msg = '定位超时'
          reject(new Error(msg))
        },
        {
          enableHighAccuracy: true,
          timeout: 10000,
          maximumAge: 300000
        }
      )
    })
  }

  /**
   * 通过高德 Web 服务 API 反查城市
   */
  const reverseGeocode = async (lng: number, lat: number): Promise<string> => {
    const res = await fetch(
      `https://restapi.amap.com/v3/geocode/regeo?location=${lng},${lat}&key=${AMAP_API_KEY}`
    )
    const data = await res.json()
    console.log('高德逆地理编码响应：', data)

    if (data.status === '0') {
      console.error('高德API报错:', data.info)
      return '定位失败'
    }

    const addressComponent = data.regeocode?.addressComponent
    if (!addressComponent) return '未知城市'

    // 高德API特性：如果是直辖市，city字段可能为空或空数组，此时应取province
    const city = addressComponent.city
    const province = addressComponent.province

    // 判断 city 是否有效 (非空字符串且非空数组)
    const isValidCity = city && typeof city === 'string' && city.length > 0

    return isValidCity ? city : (province || '未知城市')
  }

  /**
   * 主方法：获取用户位置（GCJ-02）并尝试获取城市
   */
  const getCityByBrowser = async () => {
    try {
      isLoading.value = true
      errorMsg.value = ''

      const {lng: gcjLng,lat: gcjLat} = await getCurrentPosition()
      lng.value = gcjLng
      lat.value = gcjLat

      // 反查城市
      cityName.value = await reverseGeocode(gcjLng, gcjLat)
      console.log(`城市: ${cityName.value}`)
      console.log(`定位成功（GCJ-02）: ${gcjLng}, ${gcjLat}`)
    } catch (err) {
      errorMsg.value = (err as Error).message
      // 「用户拒绝授权 / 浏览器不支持定位」都是正常情况而不是程序缺陷，
      // 用 console.error 会让每次进页面都刷一条红色报错，干扰真实问题的排查。
      // 降级为 warn：errorMsg 目前没有任何 UI 消费，行为不变。
      console.warn('定位未成功，已降级为手动选择城市：', errorMsg.value)
    } finally {
      isLoading.value = false
    }
  }

  return {
    lat,
    lng,
    cityName,
    isLoading,
    errorMsg,
    getCityByBrowser 
  }
}, {
  persist: {
    key: 'geoInfo',
    storage: localStorage,
    // ⚠️ 选项名是 pick，不是 paths。
    // pinia-plugin-persistedstate v4 已把 paths 改名为 pick，
    // 且运行时**完全不认** paths（dist 里 paths 出现 0 次）——
    // 原写法被静默忽略，等于把整个 state（含 isLoading / errorMsg）都写进了 localStorage。
    pick: ['lng', 'lat', 'cityName']
  }
})