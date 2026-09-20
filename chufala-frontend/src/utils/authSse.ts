// utils/authSse.ts
import { useTokenStore } from '@/stores/token'

interface SseOptions {
  onMessage: (data: string, event?: string) => void
  onError?: (error: Error) => void
  onOpen?: () => void
}

/**
 * 支持 Bearer Token 的 SSE 客户端（基于 fetch + ReadableStream）
 */
export function authSse(url: string, options: SseOptions) {
  const tokenStore = useTokenStore()
  const { onMessage, onError, onOpen } = options

  // 获取 token（和 axios 拦截器逻辑一致）
  if (!tokenStore.accessToken) {
    onError?.(new Error('未登录，无法建立 SSE 连接'))
    return { close: () => {} }
  }

  const abortController = new AbortController()
  const { signal } = abortController

  // 构造带 Token 的请求
  fetch(url, {
    method: 'GET',
    headers: {
      'Accept': 'text/event-stream',
      'Cache-Control': 'no-cache',
      'Connection': 'keep-alive',
      'Authorization': `Bearer ${tokenStore.accessToken}`,
    },
    signal,
  })
    .then(async (response) => {
      if (!response.ok || !response.body) {
        throw new Error(`SSE 连接失败: ${response.status} ${response.statusText}`)
      }

      onOpen?.()

      const reader = response.body.getReader()
      const decoder = new TextDecoder('utf-8')
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })

        // 按行处理 SSE 数据（SSE 协议以 \n\n 分隔事件）
        const lines = buffer.split('\n')
        buffer = lines.pop() || '' // 保留不完整行

        let event = 'message' // 默认事件名
        let data = ''

        for (const line of lines) {
          if (line.startsWith('event:')) {
            event = line.slice(7).trim()
          } else if (line.startsWith('data:')) {
            data = line.slice(5).trim()
          } else if (line === '') {
            // 空行表示事件结束
            if (data) {
              onMessage(data, event)
              data = ''
              event = 'message'
            }
          }
        }
      }
    })
    .catch((err) => {
      if (signal.aborted) {
        console.log('SSE 连接已手动关闭')
      } else {
        console.error('SSE 连接错误:', err)
        onError?.(err)
      }
    })

  return {
    close: () => abortController.abort(),
  }
}