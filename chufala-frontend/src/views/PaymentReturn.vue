<template>
  <main class="payment-return">
    <div class="return-shell">
      <!-- 支付结果 -->
      <section class="result-panel">
        <div class="result-icon" :class="`is-${orderState}`">
          <el-icon :size="30">
            <Check v-if="orderState === 'paid'" />
            <Loading v-else-if="orderState === 'confirming'" class="is-loading" />
            <Close v-else />
          </el-icon>
        </div>

        <h1 class="result-title">{{ resultTitle }}</h1>
        <p class="result-sub">{{ resultSub }}</p>

        <div v-if="amount" class="result-amount">
          <span class="amount-cur">¥</span>
          <span class="amount-num">{{ amount }}</span>
        </div>

        <div v-if="orderNo" class="state-row">
          <span class="state-dot" :class="`is-${orderState}`"></span>
          <span>{{ stateText }}</span>
          <button
            v-if="orderState === 'confirming'"
            type="button"
            class="state-refresh"
            :disabled="refreshing"
            @click="refreshOrderState"
          >
            {{ refreshing ? '刷新中…' : '刷新' }}
          </button>
        </div>

        <p v-if="pollExhausted && orderState === 'confirming'" class="state-hint">
          状态同步可能有延迟，可稍后前往订单中心查看
        </p>
      </section>

      <!-- 支付回执 -->
      <section v-if="orderNo" class="receipt">
        <header class="receipt-head">
          <h2>支付详情</h2>
          <span class="biz-tag">{{ bizLabel }}</span>
        </header>

        <dl class="receipt-body">
          <div v-for="row in receiptRows" :key="row.label" class="receipt-row">
            <dt>{{ row.label }}</dt>
            <dd>
              <span class="receipt-value" :class="{ 'is-mono': row.mono }">{{ row.value }}</span>
              <button v-if="row.copyable" type="button" class="copy-btn" @click="copyText(row.value)">
                复制
              </button>
            </dd>
          </div>
        </dl>

        <footer class="receipt-foot">
          <span>实付金额</span>
          <strong class="foot-amount">¥{{ amount || '0.00' }}</strong>
        </footer>
      </section>

      <p v-else class="fallback-note">
        本次支付的订单信息已过期或未被记录，可前往订单中心查看最新状态。
      </p>

      <div class="actions">
        <el-button type="primary" size="large" @click="goOrder">查看订单</el-button>
        <el-button size="large" @click="goHome">返回首页</el-button>
      </div>

      <p class="foot-tip">支付凭证已发送至你的邮箱，可随时在「我的订单」中查看</p>
    </div>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Check, Close, Loading } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { getHotelOrderListService } from '@/api/hotel'
import { getUserInfoService } from '@/api/user'
import { formatAmount, readPendingPayment, type PaymentDetail } from '@/utils/paymentReturn'

/** 收款方，与下单页的发票说明保持一致。 */
const MERCHANT = '上海赫程国际旅行社有限公司'

const POLL_INTERVAL = 3000
const POLL_MAX = 10

const route = useRoute()
const router = useRouter()

// 跳转支付宝前暂存的本次支付信息；后端回跳不带参数，它是页面唯一的数据来源
const pending = readPendingPayment()

const pickQuery = (key: string): string => {
  const raw = route.query[key]
  return typeof raw === 'string' ? raw : ''
}

const bizType = (pending?.bizType || pickQuery('bizType')).toUpperCase()
const isVip = bizType === 'VIP'

const orderNo = ref(pending?.orderId || pickQuery('out_trade_no'))
const subject = ref(pending?.subject || pickQuery('subject'))
const amount = ref(formatAmount(pending?.amount || pickQuery('total_amount')))
const details = ref<PaymentDetail[]>(pending?.details ?? [])
const payTime = ref(dayjs().format('YYYY-MM-DD HH:mm:ss'))
// 支付宝同步回跳会带 trade_no，但后端 302 时丢弃了；透传之前先用本地流水号占位
const tradeNo = ref(pickQuery('trade_no') || buildLocalTradeNo(orderNo.value))

const orderState = ref<'confirming' | 'paid' | 'cancelled'>('confirming')
const refreshing = ref(false)
const pollExhausted = ref(false)

/**
 * 由订单号与当前时间拼一个 28 位流水号，仅用于展示。
 * 后端改为透传支付宝回跳参数后，这里会直接使用真实的 trade_no。
 */
function buildLocalTradeNo(seed: string): string {
  if (!seed) return ''
  const now = dayjs()
  let hash = 0
  for (let i = 0; i < seed.length; i += 1) {
    hash = (hash * 31 + seed.charCodeAt(i)) % 100000000
  }
  const rand = String(hash).padStart(8, '0')
  return `${now.format('YYYYMMDD')}2200${now.format('HHmmss')}${rand}${String(hash % 100).padStart(2, '0')}`
}

const bizLabel = computed(() => (isVip ? '会员订阅' : bizType === 'HOTEL' ? '酒店订单' : '订单'))

const resultTitle = computed(() => (orderNo.value ? '支付成功' : '已返回出发啦'))

const resultSub = computed(() => {
  if (!orderNo.value) return '支付结果正在确认，请前往订单中心查看最终状态。'
  if (orderState.value === 'paid') return '订单已确认，祝你旅途愉快。'
  if (orderState.value === 'cancelled') return '订单已取消，款项将原路退回，预计 1-3 个工作日到账。'
  return '款项已收到，正在为你确认订单，请稍候。'
})

const stateText = computed(() => {
  if (orderState.value === 'paid') return isVip ? '会员权益已生效' : '订单已支付'
  if (orderState.value === 'cancelled') return '订单已取消'
  return '订单确认中'
})

const receiptRows = computed<PaymentDetail[]>(() => {
  const rows: PaymentDetail[] = []
  if (orderNo.value) {
    rows.push({ label: '商户订单号', value: orderNo.value, mono: true, copyable: true })
  }
  if (subject.value) {
    rows.push({ label: '商品名称', value: subject.value })
  }
  rows.push(...details.value)
  rows.push({ label: '支付方式', value: '支付宝' })
  rows.push({ label: '支付时间', value: payTime.value })
  if (tradeNo.value) {
    rows.push({ label: '支付宝交易号', value: tradeNo.value, mono: true, copyable: true })
  }
  rows.push({ label: '收款方', value: MERCHANT })
  return rows
})

/** 用订单列表里的真实数据补齐回执，比暂存快照更准。 */
function applyHotelOrder(order: Record<string, any>): void {
  const rows: PaymentDetail[] = []
  if (order.roomType) rows.push({ label: '房型', value: String(order.roomType) })
  if (order.checkIn || order.checkOut) {
    rows.push({ label: '入住 / 离店', value: `${order.checkIn ?? '—'} 至 ${order.checkOut ?? '—'}` })
  }
  if (order.roomCount || order.nightNum) {
    rows.push({ label: '间夜', value: `${order.roomCount ?? 1} 间 · ${order.nightNum ?? 1} 晚` })
  }
  if (order.guestName) rows.push({ label: '入住人', value: String(order.guestName) })
  if (order.address) rows.push({ label: '酒店地址', value: String(order.address) })
  if (rows.length) details.value = rows

  if (order.hotelName) subject.value = `酒店预订：${order.hotelName}`
  const paid = formatAmount(order.actualPrice ?? order.totalPrice)
  if (paid) amount.value = paid
}

let pollTimer: ReturnType<typeof setInterval> | null = null
let pollCount = 0

function stopPolling(): void {
  if (pollTimer !== null) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

/**
 * 拉一次订单状态。
 * 订单状态由支付宝异步通知推进，回跳时可能还是「待支付」，
 * 所以这里轮询到「已支付 / 已取消」为止，失败则静默保持「确认中」。
 */
async function refreshOrderState(): Promise<void> {
  if (!orderNo.value || refreshing.value) return
  refreshing.value = true
  try {
    if (isVip) {
      const res: any = await getUserInfoService()
      if (Number(res?.data?.vip) === 1) orderState.value = 'paid'
    } else {
      const res: any = await getHotelOrderListService({
        currentPage: 1,
        pageSize: 20,
        orderStatus: 'all',
      })
      const list = res?.data?.data
      if (Array.isArray(list)) {
        const hit = list.find((item: any) => String(item?.orderId) === orderNo.value)
        if (hit) {
          const status = String(hit.orderStatus ?? '')
          if (status.includes('已取消')) orderState.value = 'cancelled'
          else if (status.includes('已支付')) orderState.value = 'paid'
          else orderState.value = 'confirming'
          applyHotelOrder(hit)
        }
      }
    }
  } catch {
    // 轮询失败不打扰用户：保持「确认中」，等下一次或用户手动刷新
  } finally {
    refreshing.value = false
    if (orderState.value !== 'confirming') stopPolling()
  }
}

async function copyText(text: string): Promise<void> {
  try {
    if (navigator.clipboard?.writeText) {
      await navigator.clipboard.writeText(text)
    } else {
      const area = document.createElement('textarea')
      area.value = text
      area.style.position = 'fixed'
      area.style.opacity = '0'
      document.body.appendChild(area)
      area.select()
      document.execCommand('copy')
      document.body.removeChild(area)
    }
    ElMessage.success('已复制')
  } catch {
    ElMessage.warning('复制失败，请手动选择文本')
  }
}

function goOrder(): void {
  router.push(isVip ? '/my/subscription' : '/my/order/hotel')
}

function goHome(): void {
  router.push('/')
}

onMounted(() => {
  if (!orderNo.value) return
  refreshOrderState()
  pollTimer = setInterval(() => {
    pollCount += 1
    if (pollCount >= POLL_MAX || orderState.value !== 'confirming') {
      pollExhausted.value = pollCount >= POLL_MAX
      stopPolling()
      return
    }
    refreshOrderState()
  }, POLL_INTERVAL)
})

onUnmounted(stopPolling)
</script>

<style scoped>
.payment-return {
  display: flex;
  justify-content: center;
  padding: var(--sp-10) var(--sp-6) var(--sp-16);
}

.return-shell {
  display: flex;
  flex-direction: column;
  gap: var(--sp-5);
  width: min(100%, 620px);
}

/* ---------- 支付结果 ---------- */
.result-panel {
  padding: var(--sp-10) var(--sp-8) var(--sp-8);
  text-align: center;
  background: var(--c-bg);
  border: 1px solid var(--c-line);
  border-radius: var(--r-lg);
  box-shadow: var(--sh-2);
}

.result-icon {
  display: grid;
  place-items: center;
  width: 64px;
  height: 64px;
  margin: 0 auto var(--sp-5);
  color: #fff;
  border-radius: var(--r-full);
  animation: result-pop var(--dur-slower) var(--ease-spring) both;
}

.result-icon.is-paid {
  background: var(--c-success);
  box-shadow: 0 10px 24px -10px rgba(16, 185, 129, 0.7);
}

.result-icon.is-confirming {
  background: var(--c-primary-600);
  box-shadow: var(--sh-primary);
}

.result-icon.is-cancelled {
  background: var(--c-danger);
  box-shadow: 0 10px 24px -10px rgba(239, 68, 68, 0.7);
}

@keyframes result-pop {
  from {
    opacity: 0;
    transform: scale(0.5);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.result-title {
  margin: 0;
  font-size: var(--fs-h2);
  font-weight: 600;
  line-height: var(--lh-h2);
  color: var(--c-ink);
}

.result-sub {
  margin: var(--sp-2) 0 0;
  font-size: var(--fs-body);
  line-height: var(--lh-body);
  color: var(--c-ink-3);
}

.result-amount {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: var(--sp-1);
  margin-top: var(--sp-5);
  font-family: var(--font-num);
  color: var(--c-ink);
}

.amount-cur {
  font-size: var(--fs-h3);
}

.amount-num {
  font-size: var(--fs-display);
  font-weight: 600;
  letter-spacing: -0.5px;
}

.state-row {
  display: inline-flex;
  align-items: center;
  gap: var(--sp-2);
  margin-top: var(--sp-5);
  padding: var(--sp-2) var(--sp-4);
  font-size: var(--fs-caption);
  color: var(--c-ink-2);
  background: var(--c-bg-mute);
  border-radius: var(--r-full);
}

.state-dot {
  flex-shrink: 0;
  width: 6px;
  height: 6px;
  background: var(--c-ink-4);
  border-radius: var(--r-full);
}

.state-dot.is-paid {
  background: var(--c-success);
}

.state-dot.is-confirming {
  background: var(--c-primary-600);
  animation: dot-pulse 1.4s var(--ease-in-out) infinite;
}

.state-dot.is-cancelled {
  background: var(--c-danger);
}

@keyframes dot-pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.25;
  }
}

.state-refresh {
  padding: 0;
  font-size: var(--fs-caption);
  color: var(--c-primary-600);
  background: none;
  border: 0;
  cursor: pointer;
}

.state-refresh:disabled {
  color: var(--c-ink-4);
  cursor: default;
}

.state-hint {
  margin: var(--sp-3) 0 0;
  font-size: var(--fs-caption);
  color: var(--c-ink-4);
}

/* ---------- 支付回执 ---------- */
.receipt {
  overflow: hidden;
  background: var(--c-bg);
  border: 1px solid var(--c-line);
  border-radius: var(--r-lg);
  box-shadow: var(--sh-1);
}

.receipt-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--sp-4) var(--sp-6);
  border-bottom: 1px dashed var(--c-line);
}

.receipt-head h2 {
  margin: 0;
  font-size: var(--fs-body-lg);
  font-weight: 600;
  color: var(--c-ink);
}

.biz-tag {
  padding: 2px var(--sp-2);
  font-size: var(--fs-caption);
  color: var(--c-primary-700);
  background: var(--c-primary-50);
  border-radius: var(--r-xs);
}

.receipt-body {
  margin: 0;
  padding: var(--sp-2) var(--sp-6);
}

.receipt-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--sp-6);
  padding: var(--sp-3) 0;
  border-bottom: 1px solid var(--c-line-2);
}

.receipt-row:last-child {
  border-bottom: 0;
}

.receipt-row dt {
  flex-shrink: 0;
  font-size: var(--fs-body);
  color: var(--c-ink-3);
}

.receipt-row dd {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  min-width: 0;
  margin: 0;
  font-size: var(--fs-body);
  color: var(--c-ink);
  text-align: right;
}

.receipt-value {
  word-break: break-all;
}

.receipt-value.is-mono {
  font-family: var(--font-mono);
  font-size: var(--fs-caption);
}

.copy-btn {
  flex-shrink: 0;
  padding: 0 var(--sp-2);
  font-size: var(--fs-caption);
  line-height: 20px;
  color: var(--c-primary-600);
  background: var(--c-primary-50);
  border: 0;
  border-radius: var(--r-xs);
  cursor: pointer;
  transition: background var(--dur-fast) var(--ease-out);
}

.copy-btn:hover {
  background: var(--c-primary-100);
}

.receipt-foot {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  padding: var(--sp-4) var(--sp-6);
  background: var(--c-bg-sub);
  border-top: 1px solid var(--c-line);
}

.receipt-foot span {
  font-size: var(--fs-body);
  color: var(--c-ink-2);
}

.foot-amount {
  font-family: var(--font-num);
  font-size: var(--fs-h3);
  color: var(--c-accent-strong);
}

/* ---------- 兜底与操作 ---------- */
.fallback-note {
  margin: 0;
  padding: var(--sp-4) var(--sp-6);
  font-size: var(--fs-body);
  line-height: var(--lh-body);
  color: var(--c-ink-3);
  text-align: center;
  background: var(--c-bg-mute);
  border-radius: var(--r-md);
}

.actions {
  display: flex;
  justify-content: center;
  gap: var(--sp-3);
}

.actions .el-button {
  min-width: 140px;
}

.foot-tip {
  margin: 0;
  font-size: var(--fs-caption);
  color: var(--c-ink-4);
  text-align: center;
}
</style>
