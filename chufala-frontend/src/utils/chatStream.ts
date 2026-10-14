import { useTokenStore } from '@/stores/token'

export type ChatStreamEvent = 'delta' | 'complete' | 'failed'

export interface ChatStreamHandlers {
  onEvent: (event: ChatStreamEvent, data: string) => void
  onCursor?: (id: string) => void
}

const pause = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

async function readEvents(response: Response, handlers: ChatStreamHandlers): Promise<boolean> {
  if (!response.ok || !response.body) throw new Error(`聊天连接失败 (${response.status})`)
  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  let event = 'message'
  let id = ''
  let data: string[] = []
  let terminal = false

  const processLine = (raw: string) => {
    const line = raw.endsWith('\r') ? raw.slice(0, -1) : raw
    if (!line) {
      if (data.length) {
        const joined = data.join('\n')
        let value = joined
        try { value = JSON.parse(joined) } catch { /* Legacy plain SSE data. */ }
        if (event === 'delta' || event === 'complete' || event === 'failed') {
          handlers.onEvent(event, value)
        }
        if (id) handlers.onCursor?.(id)
        if (event === 'complete' || event === 'failed') terminal = true
      }
      event = 'message'
      id = ''
      data = []
      return
    }
    if (line.startsWith(':')) return
    const colon = line.indexOf(':')
    const field = colon < 0 ? line : line.slice(0, colon)
    const value = colon < 0 ? '' : line.slice(colon + 1).replace(/^ /, '')
    if (field === 'event') event = value
    else if (field === 'id') id = value
    else if (field === 'data') data.push(value)
  }

  try {
    while (!terminal) {
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
    if (data.length) processLine('')
  } finally {
    await reader.cancel().catch(() => {})
  }
  return terminal
}

/** Reopen the same assistant message; never resubmit the user's question. */
export async function followChatStream(
  messageId: string,
  initialResponse: Response | null,
  handlers: ChatStreamHandlers,
  initialCursor = ''
): Promise<void> {
  let response = initialResponse
  let cursor = initialCursor
  let retries = 0
  while (true) {
    try {
      const previousCursor = cursor
      if (!response) {
        const token = useTokenStore().accessToken
        response = await fetch(`/api/agent/chat/stream/${encodeURIComponent(messageId)}`, {
          headers: {
            Authorization: `Bearer ${token}`,
            Accept: 'text/event-stream',
            ...(cursor ? { 'Last-Event-ID': cursor } : {})
          }
        })
      }
      if ([401, 403, 404].includes(response.status)) {
        throw new Error(`聊天流不可用 (${response.status})`)
      }
      const completed = await readEvents(response, {
        ...handlers,
        onCursor: id => { cursor = id; handlers.onCursor?.(id) }
      })
      if (completed) return
      retries = cursor !== previousCursor ? 0 : retries + 1
      if (retries > 6) throw new Error('聊天连接反复中断')
    } catch (error) {
      if (error instanceof Error && /\((401|403|404)\)/.test(error.message)) throw error
      if (++retries > 6) throw error
    }
    response = null
    await pause(Math.min(1000 * 2 ** retries, 10000))
  }
}
