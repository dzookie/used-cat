const WS_BASE = import.meta.env.VITE_BASE_URL

let ws = null
let reconnectTimer = null
const listeners = new Map()

function getToken() {
  const token = localStorage.getItem('token')
  if (token) return token.replace('Bearer ', '')
  return ''
}

function getWsUrl() {
  const base = WS_BASE.replace(/^http/, 'ws')
  return `${base}/ws?token=${getToken()}`
}

function connect() {
  if (ws && ws.readyState === WebSocket.OPEN) return

  ws = new WebSocket(getWsUrl())

  ws.onopen = () => {
    console.log('[WS] 已连接')
  }

  ws.onmessage = event => {
    try {
      const data = JSON.parse(event.data)
      console.log('[WS] 收到消息:', data.content, 'conversationId:', data.conversationId, '监听数:', listeners.size)
      listeners.forEach((callbacks, key) => {
        callbacks.forEach(cb => {
          try {
            cb(data)
          } catch (e) {
            console.error('[WS] 回调异常:', key, e)
          }
        })
      })
    } catch (e) {
      console.error('[WS] 消息解析失败', e)
    }
  }

  ws.onclose = () => {
    console.log('[WS] 连接断开')
    ws = null
    reconnectTimer = setTimeout(connect, 3000)
  }

  ws.onerror = () => {
    ws?.close()
  }
}

export function createChatClient() {
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
  connect()
}

export function onMessage(key, callback) {
  if (!listeners.has(key)) {
    listeners.set(key, [])
  }
  listeners.get(key).push(callback)
  return callback
}

export function offMessage(key, callback) {
  if (callback) {
    const callbacks = listeners.get(key)
    if (callbacks) {
      const idx = callbacks.indexOf(callback)
      if (idx !== -1) callbacks.splice(idx, 1)
      if (callbacks.length === 0) listeners.delete(key)
    }
  } else {
    listeners.delete(key)
  }
}

export function disconnectChatClient() {
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
  listeners.clear()
  if (ws) {
    ws.close()
    ws = null
  }
}
