// utils/authSse.ts
import { useTokenStore } from '@/stores/token'

interface SseOptions {
  onMessage: (data: string, event?: string) => void
  onError?: (error: Error) => void
  onOpen?: () => void
  onClose?: () => void
}

export class SseHttpError extends Error {
  constructor(public readonly status: number) {
    super(`SSE 连接失败: ${status}`)
  }
}

/**
 * 支持 Bearer Token 的 SSE 客户端（基于 fetch + ReadableStream）
 */
export function authSse(url: string, options: SseOptions) {
  const tokenStore = useTokenStore()
  const { onMessage, onError, onOpen, onClose } = options

  // 获取 token（和 axios 拦截器逻辑一致）
  if (!tokenStore.accessToken) {
    onError?.(new SseHttpError(401))
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
        throw new SseHttpError(response.status)
      }

      onOpen?.()

      const reader = response.body.getReader()
      const decoder = new TextDecoder('utf-8')
      let buffer = ''
      let event = 'message'
      let dataLines: string[] = []

      const processLine = (rawLine: string) => {
        const line = rawLine.endsWith('\r') ? rawLine.slice(0, -1) : rawLine
        if (line === '') {
          if (dataLines.length > 0) onMessage(dataLines.join('\n'), event)
          event = 'message'
          dataLines = []
          return
        }
        if (line.startsWith(':')) return
        const colon = line.indexOf(':')
        const field = colon < 0 ? line : line.slice(0, colon)
        const value = colon < 0 ? '' : line.slice(colon + 1).replace(/^ /, '')
        if (field === 'event') event = value
        else if (field === 'data') dataLines.push(value)
      }

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })
        let newline = buffer.indexOf('\n')
        while (newline >= 0) {
          processLine(buffer.slice(0, newline))
          buffer = buffer.slice(newline + 1)
          newline = buffer.indexOf('\n')
        }
      }
      buffer += decoder.decode()
      if (buffer) processLine(buffer)
      if (dataLines.length > 0) onMessage(dataLines.join('\n'), event)
      if (!signal.aborted) onClose?.()
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
