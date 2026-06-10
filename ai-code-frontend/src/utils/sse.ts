/**
 * SSE 连接工具
 * 用于连接后端的 SSE 流式生成接口
 */

interface SSECallbacks {
  onMessage: (data: string) => void
  onError: (error: Event) => void
  onComplete: () => void
}

/**
 * 连接 SSE 流式接口
 * @param appId 应用 ID
 * @param message 用户消息
 * @param callbacks 回调函数
 * @returns 取消连接的函数
 */
export function connectSSE(
  appId: number | string,
  message: string,
  { onMessage, onError, onComplete }: SSECallbacks,
): () => void {
  const baseUrl = 'http://localhost:8445/api'
  const url = `${baseUrl}/app/chat/gen/code?appId=${appId}&message=${encodeURIComponent(message)}`
  // withCredentials: true 确保跨域时带上 session cookie
  const eventSource = new EventSource(url, { withCredentials: true })

  eventSource.onmessage = (event) => {
    const data = event.data
    // 检查结束标记（后端可能发送 [DONE] 或 done 事件）
    if (event.lastEventId === 'done' || data === '[DONE]') {
      eventSource.close()
      onComplete()
      return
    }
    onMessage(data)
  }

  eventSource.addEventListener('done', () => {
    eventSource.close()
    onComplete()
  })

  eventSource.onerror = (err) => {
    // EventSource 会在连接断开时自动重连
    // 如果 readyState 是 CLOSED，说明连接已完全断开
    if (eventSource.readyState === EventSource.CLOSED) {
      onError(err)
    }
  }

  // 返回取消函数
  return () => {
    eventSource.close()
  }
}
