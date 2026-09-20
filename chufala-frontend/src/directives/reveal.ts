import type { Directive, DirectiveBinding } from 'vue'

/**
 * v-reveal —— 元素进入视口时淡入上移
 * ------------------------------------------------------------------
 * 用法：
 *   <div v-for="(item, i) in list" :key="item.id" v-reveal="i * 40">
 *
 * 参数为可选的延迟毫秒数，用于实现 stagger（依次入场）效果。
 * 只播放一次，播放后立即 unobserve。
 *
 * ── 为什么用内联 transition 而不是纯 CSS 类 ──
 * 卡片类元素（.card 等）自身带有 `transition: transform/box-shadow ...` 声明，
 * 与 `.reveal.is-in { transition: ... }` 同权重，而组件样式后加载 → 组件样式胜出，
 * 结果是 opacity 没有过渡、瞬间闪现，只有 transform 在动。
 * 因此这里改为在播放期间写内联 transition（内联样式优先级最高），
 * 播完后立刻移除，把 transform 的控制权还给组件自己的 hover 样式。
 */

const REVEALED = 'is-in'
const DURATION = 320
const EASING = 'cubic-bezier(.22, 1, .36, 1)'
const DELAY_KEY = 'revealDelay'

let observer: IntersectionObserver | null = null

function play(el: HTMLElement) {
  const delay = Number(el.dataset[DELAY_KEY] || 0)

  el.style.transition = `opacity ${DURATION}ms ${EASING}, transform ${DURATION}ms ${EASING}`
  if (delay > 0) {
    el.style.transitionDelay = `${delay}ms`
  }

  // 等一帧，确保初始态（opacity:0 / translateY）已经渲染，否则不会产生过渡
  requestAnimationFrame(() => {
    el.classList.add(REVEALED)
  })

  let cleaned = false
  const cleanup = () => {
    if (cleaned) return
    cleaned = true
    // 必须撤掉内联 transition，否则会一直压住卡片自己的 hover 过渡
    el.style.removeProperty('transition')
    el.style.removeProperty('transition-delay')
  }

  el.addEventListener('transitionend', cleanup, { once: true })
  // 兜底：transitionend 在某些情况下不会触发（元素被隐藏、被提前卸载等）
  window.setTimeout(cleanup, DURATION + delay + 200)
}

function getObserver(): IntersectionObserver | null {
  if (typeof window === 'undefined' || !('IntersectionObserver' in window)) {
    return null
  }
  if (observer) return observer

  observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (!entry.isIntersecting) return
        observer?.unobserve(entry.target)
        play(entry.target as HTMLElement)
      })
    },
    {
      threshold: 0.12,
      rootMargin: '0px 0px -8% 0px',
    }
  )
  return observer
}

export const vReveal: Directive<HTMLElement, number | undefined> = {
  mounted(el: HTMLElement, binding: DirectiveBinding<number | undefined>) {
    const io = getObserver()

    // 降级：不支持 IntersectionObserver 时直接显示，避免内容永久不可见
    if (!io) {
      el.classList.add(REVEALED)
      return
    }

    const delay = Number(binding.value)
    if (Number.isFinite(delay) && delay > 0) {
      el.dataset[DELAY_KEY] = String(delay)
    }

    el.classList.add('reveal')
    io.observe(el)
  },

  unmounted(el: HTMLElement) {
    observer?.unobserve(el)
  },
}

export default vReveal
