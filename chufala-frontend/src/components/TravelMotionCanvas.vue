<template>
  <div ref="host" class="travel-motion" :class="{ 'travel-motion--ready': ready }" aria-hidden="true"></div>
</template>

<script setup>
import { onActivated, onDeactivated, onMounted, onUnmounted, ref, watch } from 'vue'

const props = defineProps({
  variant: { type: String, default: 'hero' },
  progress: { type: Number, default: null },
  running: { type: Boolean, default: false },
  active: { type: Boolean, default: true },
})
const emit = defineEmits(['ready'])
const host = ref(null)
const ready = ref(false)

let sketch = null
let loading = false
let requestId = 0
let visible = false
let activated = true
let desktopQuery
let reducedQuery
let intersectionObserver
let resizeObserver
const pointer = { x: 0, y: 0, smoothX: 0, smoothY: 0 }

const canDraw = () => host.value && props.active && activated && visible &&
  desktopQuery?.matches && !reducedQuery?.matches && !document.hidden &&
  host.value.clientWidth > 0 && host.value.clientHeight > 0

function cubicPoint(p, controls, t, width, height) {
  const x = p.bezierPoint(controls[0], controls[2], controls[4], controls[6], t) * width
  const y = p.bezierPoint(controls[1], controls[3], controls[5], controls[7], t) * height
  return { x, y }
}

const heroRoutes = [
  [0.54, 0.78, 0.68, 0.28, 0.78, 0.87, 0.96, 0.25],
  [0.62, 0.19, 0.78, 0.52, 0.79, 0.08, 1.04, 0.62],
  [0.47, 0.53, 0.61, 0.91, 0.84, 0.32, 0.98, 0.76],
]

function drawHero(p, time, stars) {
  const width = p.width
  const height = p.height
  pointer.smoothX = p.lerp(pointer.smoothX, pointer.x, 0.035)
  pointer.smoothY = p.lerp(pointer.smoothY, pointer.y, 0.035)
  p.push()
  p.translate(pointer.smoothX * 11, pointer.smoothY * 8)
  p.noFill()

  // Fine chart rings give the moving paths a quiet spatial reference.
  const centerX = width * 0.79
  const centerY = height * 0.49
  for (let i = 0; i < 3; i++) {
    p.stroke(218, 238, 247, 16 - i * 3)
    p.strokeWeight(1)
    const diameter = Math.min(width * 0.37, height * (0.65 + i * 0.28))
    p.circle(centerX, centerY, diameter)
  }

  stars.forEach((star, index) => {
    const shimmer = 0.65 + 0.35 * Math.sin(time * star.speed + index)
    p.noStroke()
    p.fill(232, 246, 250, 25 + 72 * shimmer)
    p.circle(star.x * width, star.y * height, star.size)
  })

  heroRoutes.forEach((route, routeIndex) => {
    p.noFill()
    p.stroke(223, 246, 255, routeIndex === 0 ? 91 : 48)
    p.strokeWeight(routeIndex === 0 ? 1.25 : 0.8)
    p.bezier(
      route[0] * width, route[1] * height,
      route[2] * width, route[3] * height,
      route[4] * width, route[5] * height,
      route[6] * width, route[7] * height,
    )

    const head = (time * (0.075 + routeIndex * 0.013) + routeIndex * 0.31) % 1
    for (let segment = 13; segment >= 0; segment--) {
      const t = head - segment * 0.006
      if (t < 0) continue
      const point = cubicPoint(p, route, t, width, height)
      p.noStroke()
      p.fill(247, 200, 117, Math.max(0, 165 - segment * 12))
      p.circle(point.x, point.y, Math.max(1, 4.4 - segment * 0.23))
    }
  })

  p.pop()
}

function routeControls(hasProgress) {
  return hasProgress
    ? [0.05, 0.68, 0.25, 0.08, 0.62, 0.96, 0.95, 0.32]
    : [0.16, 0.52, 0.32, 0.03, 0.57, 0.83, 0.73, 0.4]
}

function drawRoute(p, time, displayProgress) {
  const hasProgress = Number.isFinite(props.progress)
  const controls = routeControls(hasProgress)
  const width = p.width
  const height = p.height
  const segments = 100

  // The full route is always visible as a surveyed line; only verified stages fill it.
  p.noFill()
  p.stroke(59, 119, 139, hasProgress ? 43 : 54)
  p.strokeWeight(1.2)
  p.beginShape()
  for (let i = 0; i <= segments; i++) {
    const point = cubicPoint(p, controls, i / segments, width, height)
    p.vertex(point.x, point.y)
  }
  p.endShape()

  const traced = hasProgress ? displayProgress : 1
  p.stroke(51, 129, 146, hasProgress ? 185 : 102)
  p.strokeWeight(hasProgress ? 2.5 : 1.7)
  p.beginShape()
  for (let i = 0; i <= Math.ceil(segments * traced); i++) {
    const point = cubicPoint(p, controls, Math.min(traced, i / segments), width, height)
    p.vertex(point.x, point.y)
  }
  p.endShape()

  const markerCount = hasProgress ? 5 : 4
  for (let index = 0; index < markerCount; index++) {
    const t = hasProgress ? (index + 1) / markerCount : index / (markerCount - 1)
    const point = cubicPoint(p, controls, t, width, height)
    const completed = !hasProgress || displayProgress + 0.003 >= t
    p.noStroke()
    p.fill(completed ? 247 : 126, completed ? 200 : 165, completed ? 117 : 171, completed ? 60 : 65)
    p.circle(point.x, point.y, completed ? 17 : 12)
    p.fill(completed ? 197 : 255, completed ? 137 : 255, completed ? 63 : 255, 235)
    p.circle(point.x, point.y, completed ? 6 : 5)
  }

  if (!hasProgress || props.running) {
    const t = hasProgress ? Math.min(0.985, displayProgress + 0.035) : (time * 0.15) % 1
    const point = cubicPoint(p, controls, t, width, height)
    const pulse = 1 + 0.16 * Math.sin(time * 4)
    p.noStroke()
    p.fill(247, 200, 117, 35)
    p.circle(point.x, point.y, 25 * pulse)
    p.fill(247, 200, 117, 190)
    p.circle(point.x, point.y, 5)
  }
}

function dispose() {
  requestId++
  loading = false
  if (sketch) {
    sketch.remove()
    sketch = null
  }
  if (ready.value) {
    ready.value = false
    emit('ready', false)
  }
}

async function sync() {
  if (!canDraw()) {
    dispose()
    return
  }
  if (sketch || loading) return
  loading = true
  const currentRequest = ++requestId
  try {
    const module = await import('p5')
    if (currentRequest !== requestId || !canDraw()) return
    const P5 = module.default || module
    sketch = new P5((p) => {
      let stars = []
      let displayProgress = Number.isFinite(props.progress) ? props.progress : 0
      p.setup = () => {
        p.pixelDensity(Math.min(window.devicePixelRatio || 1, 1.5))
        p.createCanvas(host.value.clientWidth, host.value.clientHeight)
        p.frameRate(30)
        p.strokeCap(p.ROUND)
        p.randomSeed(20261017)
        p.noiseSeed(20261017)
        stars = Array.from({ length: 31 }, () => ({
          x: p.random(0.57, 0.97), y: p.random(0.07, 0.92),
          size: p.random(1.1, 2.6), speed: p.random(0.5, 1.3),
        }))
        ready.value = true
        emit('ready', true)
      }
      p.draw = () => {
        p.clear()
        const time = p.millis() / 1000
        if (props.variant === 'hero') {
          drawHero(p, time, stars)
        } else {
          const target = Number.isFinite(props.progress) ? Math.max(0, Math.min(1, props.progress)) : 0
          displayProgress += (target - displayProgress) * 0.095
          drawRoute(p, time, displayProgress)
          if (Number.isFinite(props.progress) && !props.running && Math.abs(target - displayProgress) < 0.002) p.noLoop()
        }
      }
    }, host.value)
  } catch (error) {
    console.warn('Travel motion unavailable:', error)
  } finally {
    if (currentRequest === requestId) loading = false
  }
}

function resize() {
  if (!sketch || !host.value) return
  const width = Math.round(host.value.clientWidth)
  const height = Math.round(host.value.clientHeight)
  if (width > 0 && height > 0 && (sketch.width !== width || sketch.height !== height)) {
    sketch.resizeCanvas(width, height)
  }
}

function onPointerMove(event) {
  if (!host.value || props.variant !== 'hero') return
  const bounds = host.value.getBoundingClientRect()
  pointer.x = Math.max(-1, Math.min(1, (event.clientX - bounds.left) / bounds.width * 2 - 1))
  pointer.y = Math.max(-1, Math.min(1, (event.clientY - bounds.top) / bounds.height * 2 - 1))
}

watch(() => props.active, sync)
watch(() => [props.progress, props.running], () => {
  if (sketch && !sketch.isLooping()) sketch.loop()
})
onActivated(() => { activated = true; sync() })
onDeactivated(() => { activated = false; dispose() })

onMounted(() => {
  desktopQuery = window.matchMedia('(min-width: 1024px)')
  reducedQuery = window.matchMedia('(prefers-reduced-motion: reduce)')
  desktopQuery.addEventListener('change', sync)
  reducedQuery.addEventListener('change', sync)
  document.addEventListener('visibilitychange', sync)
  window.addEventListener('pointermove', onPointerMove, { passive: true })
  intersectionObserver = new IntersectionObserver(([entry]) => {
    visible = entry.isIntersecting
    sync()
  })
  intersectionObserver.observe(host.value)
  resizeObserver = new ResizeObserver(() => { resize(); sync() })
  resizeObserver.observe(host.value)
})

onUnmounted(() => {
  intersectionObserver?.disconnect()
  resizeObserver?.disconnect()
  desktopQuery?.removeEventListener('change', sync)
  reducedQuery?.removeEventListener('change', sync)
  document.removeEventListener('visibilitychange', sync)
  window.removeEventListener('pointermove', onPointerMove)
  dispose()
})
</script>

<style scoped>
.travel-motion {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.travel-motion :deep(canvas) {
  display: block;
  width: 100%;
  height: 100%;
}

@media (max-width: 1023px), (prefers-reduced-motion: reduce) {
  .travel-motion { display: none; }
}
</style>
