import type { Directive } from 'vue'

/**
 * v-lazy-img —— 图片进入视口附近时再加载，加载完成后淡入
 * ------------------------------------------------------------------
 * 用法（两种都支持）：
 *   1. 已有 src，只想加"淡入"效果：
 *        <img :src="url" v-lazy-img>
 *   2. 需要真正的懒加载（推荐，能显著改善首屏 LCP）：
 *        <img :data-src="url" v-lazy-img>
 *      指令会在图片接近视口时把 data-src 搬到 src。
 *
 * 相比原生 loading="lazy"，本指令额外提供：
 *   - 加载完成后的 opacity 淡入，避免图片"啪"地出现
 *   - 加载前的灰色占位底色，避免白块闪烁
 *   - rootMargin 提前 200px 预加载，滚动时基本无等待
 */

const LOADED = 'is-loaded'

type LazyImg = HTMLImageElement

let observer: IntersectionObserver | null = null

function load(el: LazyImg) {
  const finish = () => el.classList.add(LOADED)

  el.addEventListener('load', finish, { once: true })
  // 加载失败也要淡入，否则会永远停留在透明态
  el.addEventListener('error', finish, { once: true })

  const dataSrc = el.dataset.src
  if (dataSrc) {
    el.src = dataSrc
    delete el.dataset.src
  }

  // 命中缓存时 load 事件不会再触发，需要手动补一次
  if (el.complete) finish()
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
        const el = entry.target as LazyImg
        observer?.unobserve(el)
        load(el)
      })
    },
    { rootMargin: '200px 0px' }
  )
  return observer
}

export const vLazyImg: Directive<LazyImg> = {
  mounted(el: LazyImg) {
    el.decoding = 'async'
    el.classList.add('img-fade')

    const io = getObserver()

    // 降级：不支持 IntersectionObserver 时立即加载
    if (!io) {
      load(el)
      return
    }

    io.observe(el)
  },

  unmounted(el: LazyImg) {
    observer?.unobserve(el)
  },
}

export default vLazyImg
