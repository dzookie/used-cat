<script setup>
import { ref, onMounted } from 'vue'
import { httpGet } from '@/utils/request'
import { useRouter } from 'vue-router'
import apiConfig from '@/apis/api.config'
import { useUserStore } from '@/stores/user'
import { LiaoWindow, LiaoMessageList, LiaoInputArea, LiaoMessageBubble } from '@yuandezuohua/liaokit'
import '@yuandezuohua/liaokit/dist/liaokit.css'

const router = useRouter()
const userStore = useUserStore()
const BASE_URL = import.meta.env.VITE_BASE_URL
const FRONTEND_HOST = window.location.origin

const messages = ref([])
const inputText = ref('')
const loading = ref(false)
const sessionId = ref(null)
const notLoggedIn = ref(false)
const messageListRef = ref(null)

let messageIdCounter = 0
const generateId = () => `msg_${Date.now()}_${messageIdCounter++}`

const FRONTEND_PATHS = ['/product/', '/order/', '/user/', '/chat/']

const isInternalLink = (url) => {
  try {
    const urlObj = new URL(url)
    if (urlObj.origin === FRONTEND_HOST) return true
    return FRONTEND_PATHS.some(p => url.startsWith(p))
  } catch {
    return false
  }
}

const handleLinkClick = (e) => {
  const target = e.target.closest('a')
  if (!target) return
  const href = target.getAttribute('href')
  if (!href) return
  e.preventDefault()

  // 清理残留的链接 tooltip，避免跳转后 tooltip 残留在页面上
  document.querySelectorAll('.tooltip-element').forEach(el => el.remove())

  if (isInternalLink(href)) {
    if (href.startsWith('/')) {
      router.push(href)
    } else {
      const urlObj = new URL(href)
      router.push(urlObj.pathname + urlObj.search + urlObj.hash)
    }
  } else {
    window.open(href, '_blank')
  }
}

const goLogin = () => {
  router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
}

const loadHistory = async (sid) => {
  try {
    const { data } = await httpGet(apiConfig.ai.getHistory, { sessionId: sid })
    if (data.code === 200 && data.data) {
      messages.value = data.data.map(h => ({
        id: generateId(),
        content: h.content,
        isSelf: h.role === 'user',
        role: h.role,
        status: 'sent',
        timestamp: new Date(h.datetime)
      }))
      scrollToLatest()
    }
  } catch (e) {
    console.error('加载历史记录失败', e)
  }
}

const scrollToLatest = () => {
  // 弹窗打开时列表可能还在过渡动画中（尺寸为 0），直接滚动无效。
  // 轮询等待列表有实际高度且内容溢出后再滚到底部。
  let attempts = 0
  const tryScroll = () => {
    const listEl = document.querySelector('.liao-message-list')
    if (listEl && listEl.clientHeight > 0 && listEl.scrollHeight > listEl.clientHeight) {
      messageListRef.value?.scrollToBottom(false)
      return
    }
    if (++attempts < 30) {
      setTimeout(tryScroll, 100)
    } else {
      messageListRef.value?.scrollToBottom(false)
    }
  }
  tryScroll()
}

const initSession = async () => {
  if (!userStore.isLoggedIn()) {
    notLoggedIn.value = true
    return
  }
  try {
    const { data } = await httpGet(apiConfig.ai.customerServiceInit)
    if (data.code === 200 && data.data) {
      sessionId.value = data.data.sessionId
      await loadHistory(data.data.sessionId)
    }
  } catch (e) {
    console.error('初始化客服会话失败', e)
    if (e?.response?.status === 401 || e?.response?.status === 403) {
      notLoggedIn.value = true
    }
  }
}

const sendMessage = async () => {
  const text = inputText.value.trim()
  if (!text || loading.value) return

  if (!userStore.isLoggedIn()) {
    notLoggedIn.value = true
    return
  }

  messages.value.push({
    id: generateId(),
    content: text,
    isSelf: true,
    role: 'user',
    status: 'sent',
    timestamp: new Date()
  })

  inputText.value = ''
  loading.value = true

  const assistantId = generateId()
  messages.value.push({
    id: assistantId,
    content: '',
    isSelf: false,
    role: 'assistant',
    status: 'streaming',
    timestamp: new Date()
  })

  const token = localStorage.getItem('token')?.replace('Bearer ', '') || ''

  try {
    const response = await fetch(
      `${BASE_URL}${apiConfig.ai.customerServiceChat}?message=${encodeURIComponent(text)}`,
      { headers: { 'Authorization': token } }
    )

    if (!response.ok) {
      if (response.status === 401 || response.status === 403) {
        notLoggedIn.value = true
      }
      const failIdx = messages.value.findIndex(m => m.id === assistantId)
      if (failIdx !== -1) {
        messages.value[failIdx] = { ...messages.value[failIdx], content: '抱歉，提问失败，请稍后重试', status: 'sent' }
      }
      return
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      const idx = messages.value.findIndex(m => m.id === assistantId)
      if (idx !== -1) {
        messages.value[idx] = { ...messages.value[idx], content: buffer }
      }
    }

    const idx = messages.value.findIndex(m => m.id === assistantId)
    if (idx !== -1) {
      messages.value[idx] = { ...messages.value[idx], status: 'sent' }
    }

    if (!buffer) {
      const idx2 = messages.value.findIndex(m => m.id === assistantId)
      if (idx2 !== -1) {
        messages.value[idx2] = {
          ...messages.value[idx2],
          content: '抱歉，未收到回复，请重试',
          status: 'sent'
        }
      }
    }
  } catch (e) {
    const idx = messages.value.findIndex(m => m.id === assistantId)
    if (idx !== -1) {
      messages.value[idx] = {
        ...messages.value[idx],
        content: '抱歉，网络异常，请稍后重试',
        status: 'sent'
      }
    }
  } finally {
    loading.value = false
  }
}

const handleSend = (content) => {
  const text = typeof content === 'string' ? content : content?.text || content?.content || ''
  if (text.trim()) {
    inputText.value = text.trim()
    sendMessage()
  }
}

onMounted(async () => {
  if (!userStore.loginUser) {
    await userStore.getCurrLoginUser()
  }
  if (userStore.isLoggedIn()) {
    await initSession()
  } else {
    notLoggedIn.value = true
  }
})
</script>

<template>
  <div class="ai_service_wrapper" @click="handleLinkClick">
    <LiaoWindow
      v-if="!notLoggedIn"
      title="二手猫智能助手"
      :show-close="false"
      :show-minimize="false"
      :show-maximize="false"
      width="100%"
      height="100%"
      :min-height="0"
    >
      <LiaoMessageList
        ref="messageListRef"
        :messages="messages"
        :loading="loading"
        :use-ai-adapter="false"
        :show-name="false"
        :show-time="false"
        class="ai_msg_list"
      >
        <template #empty>
          <div class="ai_welcome">
            <div class="ai_welcome_icon">😸</div>
            <p class="ai_welcome_title">你好！我是二手猫智能助手</p>
            <p class="ai_welcome_subtitle">有什么可以帮你的吗？</p>
            <div class="ai_welcome_guides">
              <span class="guide_tag" @click="inputText = '帮我查一下我的订单'">查订单</span>
              <span class="guide_tag" @click="inputText = '我的快递到哪了'">物流查询</span>
              <span class="guide_tag" @click="inputText = '怎么发布闲置'">发布闲置</span>
              <span class="guide_tag" @click="inputText = '如何支付'">如何支付</span>
            </div>
          </div>
        </template>
        <template #message="{ message }">
          <LiaoMessageBubble
            :content="message.content"
            :type="message.isSelf ? 'self' : 'other'"
            :status="message.status"
            show-avatar
          >
            <template #avatar>
              <span class="ai_avatar_emoji">{{ message.isSelf ? '👤' : '😸' }}</span>
            </template>
          </LiaoMessageBubble>
        </template>
      </LiaoMessageList>
      <template #footer>
        <LiaoInputArea
          v-model="inputText"
          :disabled="loading"
          :enable-file-upload="false"
          :enable-emoji-input="false"
          placeholder="输入消息，按 Enter 发送"
          @send="handleSend"
        />
      </template>
    </LiaoWindow>
    <div v-else class="ai_login_prompt">
      <div class="ai_login_icon">🔐</div>
      <p class="ai_login_title">请先登录</p>
      <p class="ai_login_subtitle">登录后即可使用智能客服服务</p>
      <el-button type="primary" round @click="goLogin">去登录</el-button>
    </div>
  </div>
</template>

<style scoped>
.ai_service_wrapper {
  height: 500px;
  overflow: hidden;
}

.ai_service_wrapper :deep(.liao-window) {
  height: 100%;
}

.ai_service_wrapper :deep(.liao-window-body) {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
  padding-bottom: 16px;
}

.ai_service_wrapper :deep(.liao-window-header) {
  padding: 0px 12px;
  background: var(--primary-color, #ffe60f);
}

.ai_service_wrapper :deep(.liao-window-header) .liao-window-header-title {
  font-size: 14px;
  color: #000;
}

.ai_service_wrapper :deep(.liao-window-footer) {
  padding: 8px 12px 6px;
  border-top: 1px solid #eee;
  margin-top: 4px;
}

.ai_service_wrapper :deep(.liao-input-area) {
  padding: 0;
}

.ai_service_wrapper :deep(.liao-input-area-input) {
  padding: 6px 10px;
  font-size: 13px;
}

.ai_msg_list {
  height: 100%;
}

.ai_service_wrapper :deep(.liao-message-list-container) {
  padding-bottom: 12px;
}

.ai_service_wrapper :deep(.liao-message-bubble-self .liao-message-bubble-text) {
  background-color: var(--primary-color, #ffe60f) !important;
  color: #000 !important;
}

.ai_service_wrapper :deep(a) {
  color: #ff6700;
  text-decoration: underline;
  cursor: pointer;
  word-break: break-all;
}

.ai_service_wrapper :deep(a:hover) {
  color: #e55a00;
}

.ai_avatar_emoji {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  background: #f0f0f0;
  border-radius: 50%;
  flex-shrink: 0;
}

.ai_welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  text-align: center;
}

.ai_welcome_icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.ai_welcome_title {
  font-size: 15px;
  font-weight: 500;
  color: #333;
  margin: 0 0 6px;
}

.ai_welcome_subtitle {
  font-size: 13px;
  color: #999;
  margin: 0 0 20px;
}

.ai_welcome_guides {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.guide_tag {
  padding: 6px 14px;
  font-size: 12px;
  color: #666;
  background: #f5f5f5;
  border: 1px solid #eee;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.2s;
}

.guide_tag:hover {
  color: #333;
  background: #ebebeb;
  border-color: #ddd;
}

.ai_login_prompt {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 40px 20px;
  text-align: center;
}

.ai_login_icon {
  font-size: 48px;
}

.ai_login_title {
  font-size: 15px;
  font-weight: 500;
  color: #333;
  margin: 0;
}

.ai_login_subtitle {
  font-size: 13px;
  color: #999;
  margin: 0 0 8px;
}
</style>
