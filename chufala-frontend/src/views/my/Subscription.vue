<template>
  <div class="subscription-container">
    <div class="subscription-header">
      <h1>升级会员，开启智能旅行新体验</h1>
      <p>解锁无限AI规划次数，享受更强大的智能助手模型</p>
    </div>

    <div class="plans-container">
      <!-- 免费版 -->
      <div class="plan-card">
        <div class="plan-header">
          <h2>普通用户</h2>
          <div class="price">免费</div>
        </div>
        <ul class="features-list">
          <li>
            <el-icon class="check-icon"><Check /></el-icon>
            <span>基础AI规划功能</span>
          </li>
          <li>
            <el-icon class="check-icon"><Check /></el-icon>
            <span>每日限 2 次智能规划</span>
          </li>
          <li>
            <el-icon class="check-icon"><Check /></el-icon>
            <span>标准智能助手模型 (Qwen)</span>
          </li>
          <li class="disabled">
            <el-icon class="close-icon"><Close /></el-icon>
            <span>专属客服支持</span>
          </li>
        </ul>
        <el-button class="action-btn" disabled>当前版本</el-button>
      </div>

      <!-- 会员版 -->
      <div class="plan-card pro">
        <div class="popular-tag">推荐</div>
        <div class="plan-header">
          <h2>高级会员</h2>
          <div class="price">¥19.9 <span class="period">/月</span></div>
        </div>
        <ul class="features-list">
          <li>
            <el-icon class="check-icon"><Check /></el-icon>
            <span>无限次AI规划</span>
          </li>
          <li>
            <el-icon class="check-icon"><Check /></el-icon>
            <span>解锁高级智能助手模型 (Deepseek)</span>
          </li>
          <li>
            <el-icon class="check-icon"><Check /></el-icon>
            <span>更精准的行程推荐</span>
          </li>
          <li>
            <el-icon class="check-icon"><Check /></el-icon>
            <span>优先客服支持</span>
          </li>
        </ul>
        <el-button 
          type="primary" 
          class="action-btn" 
          :loading="loading"
          @click="handleSubscribe"
          v-if="userInfoStore.info.vip !== 1"
        >
          立即升级
        </el-button>
        <el-button 
          type="success" 
          class="action-btn" 
          v-else
          disabled
        >
          尊贵会员
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useUserInfoStore } from '@/stores/userInfo'
import { ElMessage } from 'element-plus'
import { Check, Close } from '@element-plus/icons-vue'
import { updateUserInfoService } from '@/api/user'
import { createMembershipOrderService, createPayService } from '@/api/membership'

const userInfoStore = useUserInfoStore()
const loading = ref(false)

const handleSubscribe = async () => {
  if (!userInfoStore.info.id) {
    ElMessage.warning('请先登录')
    return
  }

  loading.value = true
  try {
    // 1. 创建会员订单
    const orderRes = await createMembershipOrderService()
    const orderId = orderRes.data

    // 2. 获取支付页面
    const payHtml = await createPayService('VIP', orderId)

    // 3. 提交支付表单
    const div = document.createElement('div')
    div.innerHTML = payHtml
    document.body.appendChild(div)
    const form = div.querySelector('form')
    if (form) {
      form.submit()
    } else {
      ElMessage.error('支付页面加载失败')
    }
    document.body.removeChild(div)
  } catch (error) {
    console.error(error)
    ElMessage.error('发起支付失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.subscription-container {
  max-width: 1200px;
  margin: 40px auto;
  padding: 0 20px;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
}

.subscription-header {
  text-align: center;
  margin-bottom: 60px;
}

.subscription-header h1 {
  font-size: 36px;
  color: #333;
  margin-bottom: 16px;
}

.subscription-header p {
  font-size: 18px;
  color: #666;
}

.plans-container {
  display: flex;
  justify-content: center;
  gap: 40px;
  flex-wrap: wrap;
}

.plan-card {
  background: #fff;
  border-radius: 16px;
  padding: 40px;
  width: 350px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  position: relative;
  border: 1px solid #eee;
}

.plan-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.plan-card.pro {
  border: 2px solid #68BF7B;
  background: linear-gradient(to bottom, #fcfdfc, #fff);
}

.popular-tag {
  position: absolute;
  top: -15px;
  left: 50%;
  transform: translateX(-50%);
  background: #68BF7B;
  color: white;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: bold;
}

.plan-header {
  text-align: center;
  margin-bottom: 30px;
  border-bottom: 1px solid #eee;
  padding-bottom: 20px;
}

.plan-header h2 {
  font-size: 24px;
  color: #333;
  margin-bottom: 10px;
}

.price {
  font-size: 36px;
  font-weight: bold;
  color: #333;
}

.plan-card.pro .price {
  color: #68BF7B;
}

.period {
  font-size: 16px;
  color: #999;
  font-weight: normal;
}

.features-list {
  list-style: none;
  padding: 0;
  margin: 0 0 40px 0;
}

.features-list li {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  color: #555;
  font-size: 16px;
}

.features-list li.disabled {
  color: #ccc;
}

.check-icon {
  color: #68BF7B;
  margin-right: 12px;
  font-size: 18px;
}

.close-icon {
  color: #ccc;
  margin-right: 12px;
  font-size: 18px;
}

.action-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 24px;
}
</style>
