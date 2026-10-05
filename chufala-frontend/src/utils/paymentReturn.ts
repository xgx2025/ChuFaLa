/**
 * 支付回跳上下文。
 *
 * <p>支付宝同步回跳会先打到后端 {@code /alipay/return}，该接口只做 302 重定向到
 * {@code alipay.frontendReturnUrl}，不携带任何业务参数，因此前端返回页原本拿不到
 * 订单号、金额、商品名。这里在「提交支付宝表单之前」把本次支付的关键信息写进
 * sessionStorage，返回页再读出来渲染订单回执。
 *
 * <p>返回页同时也会读 URL 上的 {@code out_trade_no} / {@code total_amount} /
 * {@code trade_no} 等支付宝回跳参数：将来若后端改成透传这些参数，页面无需改动即可
 * 直接使用真实数据，sessionStorage 只作为兜底。
 */

/** 一条回执明细（标签 / 值）。 */
export interface PaymentDetail {
  /** 明细标签，如「入住日期」 */
  label: string
  /** 明细内容 */
  value: string
  /** 是否用等宽字体展示（订单号、流水号） */
  mono?: boolean
  /** 是否提供「复制」按钮 */
  copyable?: boolean
}

/** 一次待确认支付。 */
export interface PendingPayment {
  /** 业务类型：hotel / TICKET / VIP，大小写不敏感 */
  bizType: string
  /** 商户订单号 */
  orderId: string
  /** 实付金额，已格式化为两位小数字符串 */
  amount: string
  /** 商品标题，如「酒店预订：上海古北福玥酒店」 */
  subject: string
  /** 附加展示明细，按数组顺序渲染 */
  details: PaymentDetail[]
  /** 写入时间戳，用于过期判断 */
  createdAt: number
}

const STORAGE_KEY = 'chufala:payment:pending'

/** 超过 2 小时的记录视为过期，避免翻出上一次支付的残留信息。 */
const TTL = 2 * 60 * 60 * 1000

/**
 * 暂存一次待确认支付。
 *
 * @param ctx 除 createdAt 外的支付上下文
 */
export function savePendingPayment(ctx: Omit<PendingPayment, 'createdAt'>): void {
  try {
    const payload: PendingPayment = { ...ctx, createdAt: Date.now() }
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(payload))
  } catch {
    // 隐私模式 / 存储被禁用时写不进去，返回页会退化为通用提示，不影响支付主流程
  }
}

/**
 * 读取最近一次待确认支付。
 *
 * @returns 支付上下文；不存在、已过期或内容损坏时返回 null
 */
export function readPendingPayment(): PendingPayment | null {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY)
    if (!raw) return null
    const parsed = JSON.parse(raw) as Partial<PendingPayment> | null
    if (!parsed || typeof parsed !== 'object') return null
    if (typeof parsed.createdAt === 'number' && Date.now() - parsed.createdAt > TTL) {
      return null
    }
    return {
      bizType: typeof parsed.bizType === 'string' ? parsed.bizType : '',
      orderId: parsed.orderId == null ? '' : String(parsed.orderId),
      amount: typeof parsed.amount === 'string' ? parsed.amount : '',
      subject: typeof parsed.subject === 'string' ? parsed.subject : '',
      details: Array.isArray(parsed.details) ? parsed.details.filter((d) => d && d.label) : [],
      createdAt: typeof parsed.createdAt === 'number' ? parsed.createdAt : Date.now(),
    }
  } catch {
    return null
  }
}

/**
 * 金额格式化为两位小数。
 *
 * <p>空串、null、undefined 一律返回空串：`Number('') === 0`，不先拦住会把
 * 「没有金额」误渲染成 ¥0.00。
 *
 * @param value 金额，字符串或数字
 * @returns 形如 "199.00" 的字符串；无法解析时返回空串
 */
export function formatAmount(value: unknown): string {
  if (value === null || value === undefined || value === '') return ''
  const num = Number(value)
  if (!Number.isFinite(num)) return ''
  return num.toFixed(2)
}
