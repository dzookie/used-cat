<script setup>
import { ref, watch, nextTick, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { Setting, Paperclip, Picture, Sunny, Comment, Loading } from '@element-plus/icons-vue'
import { httpGet, httpPost, postFile } from '@/utils/request'
import apiConfig from '@/apis/api.config'
import avatarDefault from '@/assets/icons/avatar.svg'
import { createChatClient, onMessage, offMessage } from '@/utils/websocket'
import { formatDateTime } from '@/utils/date'
import { emojiList } from '@/assets/emoji.js'

const BASE_URL = import.meta.env.VITE_BASE_URL

const props = defineProps({
  conversation: {
    type: Object,
    default: null
  }
})

const messageInput = ref('')
const messages = ref([])
const messagesContainer = ref(null)
const inputRef = ref(null)
const cursorPos = ref(-1)
const imageInputRef = ref(null)
const uploadingImage = ref(false)

const isNearBottom = () => {
  const el = messagesContainer.value
  if (!el) return true
  return el.scrollHeight - el.scrollTop - el.clientHeight < 80
}

const scrollToBottom = () => {
  nextTick(() => {
    const el = messagesContainer.value
    if (!el) return
    el.scrollTop = el.scrollHeight
    requestAnimationFrame(() => {
      if (messagesContainer.value) {
        messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
      }
    })
  })
}

const loadMessages = async () => {
  if (!props.conversation) return
  try {
    const res = await httpGet(apiConfig.chat.messageList, {
      conversationId: props.conversation.id
    })
    if (res.data.code === 200) {
      messages.value = res.data.data
    }
  } catch (e) {
    console.error('获取消息列表失败', e)
  }
}

const insertEmoji = (emoji) => {
  const text = messageInput.value || ''
  const pos = cursorPos.value >= 0 ? cursorPos.value : text.length
  messageInput.value = text.slice(0, pos) + emoji + text.slice(pos)
  cursorPos.value = pos + emoji.length
  nextTick(() => {
    const textarea = inputRef.value?.$el?.querySelector('textarea')
    if (textarea) {
      textarea.focus()
      textarea.setSelectionRange(cursorPos.value, cursorPos.value)
    }
  })
}

const saveCursorPos = () => {
  const textarea = inputRef.value?.$el?.querySelector('textarea')
  if (textarea) {
    cursorPos.value = textarea.selectionStart
  }
}

const handleImageClick = () => {
  imageInputRef.value?.click()
}

const handleImageChange = async (e) => {
  const file = e.target.files?.[0]
  if (!file) return

  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.warning('只能发送图片文件')
    return
  }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.warning('图片大小不能超过5MB')
    return
  }

  uploadingImage.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    const { data } = await postFile(apiConfig.chat.uploadImage, formData)
    if (data.code === 200) {
      await sendImageMessage(data.data)
    } else {
      ElMessage.error(data.msg || '图片上传失败')
    }
  } catch (err) {
    ElMessage.error('图片上传失败')
  } finally {
    uploadingImage.value = false
    e.target.value = ''
  }
}

/**
 * 发送图片消息
 */
const sendImageMessage = async (imagePath) => {
  if (!props.conversation) return

  const tempId = 'temp_' + Date.now()
  const tempMsg = {
    messageId: tempId,
    conversationId: props.conversation.id,
    senderId: 0,
    receiverId: props.conversation.targetUserId,
    content: imagePath,
    messageType: 'image',
    isRead: 0,
    createTime: new Date().toISOString(),
    _sending: true
  }
  messages.value.push(tempMsg)
  props.conversation.content = '[图片]'
  scrollToBottom()

  try {
    const res = await httpPost(apiConfig.chat.sendMessage, {
      conversationId: props.conversation.id,
      receiverId: props.conversation.targetUserId,
      content: imagePath,
      messageType: 'image'
    })
    if (res.data.code === 200 && res.data.data) {
      const idx = messages.value.findIndex(m => m.messageId === tempId)
      if (idx !== -1) {
        messages.value[idx] = { ...res.data.data }
      }
    }
  } catch (e) {
    console.error('发送图片失败', e)
    const idx = messages.value.findIndex(m => m.messageId === tempId)
    if (idx !== -1) {
      messages.value[idx]._failed = true
    }
  }
}

/**
 * 发送消息
 */
const sendMessage = async () => {
  if (!messageInput.value.trim() || !props.conversation) return
  const tempContent = messageInput.value
  messageInput.value = ''

  const tempId = 'temp_' + Date.now()
  const tempMsg = {
    messageId: tempId,
    conversationId: props.conversation.id,
    senderId: 0,
    receiverId: props.conversation.targetUserId,
    content: tempContent,
    messageType: 'text',
    isRead: 0,
    createTime: new Date().toISOString(),
    _sending: true
  }
  messages.value.push(tempMsg)
  props.conversation.content = tempContent
  scrollToBottom()

  try {
    const res = await httpPost(apiConfig.chat.sendMessage, {
      conversationId: props.conversation.id,
      receiverId: props.conversation.targetUserId,
      content: tempContent
    })
    if (res.data.code === 200 && res.data.data) {
      const idx = messages.value.findIndex(m => m.messageId === tempId)
      if (idx !== -1) {
        const wsExists = messages.value.some(
          m => m.messageId === res.data.data.messageId && m.messageId !== tempId
        )
        if (wsExists) {
          messages.value.splice(idx, 1)
        } else {
          messages.value[idx] = { ...res.data.data }
        }
      }
    }
  } catch (e) {
    console.error('发送消息失败', e)
    const idx = messages.value.findIndex(m => m.messageId === tempId)
    if (idx !== -1) {
      messages.value[idx]._failed = true
    }
  }
}

const handleWsMessage = (msg) => {
  console.log('[ChatWindow] 收到WS消息:', msg)
  if (!props.conversation) return
  if (msg.conversationId !== props.conversation.id) {
    console.log('[ChatWindow] conversationId 不匹配, 忽略. msg.conversationId=', msg.conversationId, 'props.conversation.id=', props.conversation.id)
    return
  }
  const exists = messages.value.some(m => m.messageId === msg.messageId)
  if (!exists) {
    const tempIdx = messages.value.findIndex(m => m._sending && m.content === msg.content)
    if (tempIdx !== -1) {
      messages.value[tempIdx] = { ...msg }
    } else {
      messages.value.push(msg)
    }
    props.conversation.content = msg.messageType === 'image' ? '[图片]' : msg.content
    scrollToBottom()
    console.log('[ChatWindow] 消息已添加:', msg.content)
  }
}

let wsCallback = null
let pollTimer = null

const poll = () => {
  loadMessages().then(() => {
    if (isNearBottom()) {
      scrollToBottom()
    }
  })
}

watch(() => props.conversation?.id, () => {
  if (wsCallback) offMessage('chatMessages', wsCallback)
  if (pollTimer) clearInterval(pollTimer)

  createChatClient()
  wsCallback = onMessage('chatMessages', handleWsMessage)
  loadMessages().then(() => scrollToBottom())

  pollTimer = setInterval(poll, 3000)
}, { immediate: true })

onBeforeUnmount(() => {
  if (wsCallback) offMessage('chatMessages', wsCallback)
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<template>
  <div class="chat_area">
    <div v-if="!conversation" class="empty_chat">
      <el-icon class="empty_icon">
        <Comment />
      </el-icon>
      <div class="empty_text">尚未选择任何联系人</div>
      <div class="empty_subtext">快点左侧列表聊起来吧~</div>
    </div>

    <div v-else class="chat_container">
      <div class="chat_header">
        <img :src="conversation.avatar ? BASE_URL + conversation.avatar : avatarDefault" alt="avatar"
          class="chat_header_avatar" />
        <div class="chat_title">{{ conversation.title }}</div>
        <div class="chat_actions">
          <!-- <el-icon class="action_icon">
            <Setting />
          </el-icon> -->
        </div>
      </div>

      <!-- 消息区域 -->
      <div class="chat_messages" ref="messagesContainer">
        <div v-for="msg in messages" :key="msg.messageId" class="message_item"
          :class="msg.senderId !== conversation?.targetUserId ? 'sent' : 'received'">
          <div v-if="msg.senderId === conversation?.targetUserId" class="message_avatar">
            <img :src="conversation.avatar ? BASE_URL + conversation.avatar : avatarDefault" alt="avatar" />
          </div>
          <div class="message_content">
            <div class="message_row">
              <el-icon v-if="msg._sending" class="sending_spinner" :size="14">
                <Loading />
              </el-icon>
              <img
                v-if="msg.messageType === 'image'"
                :src="BASE_URL + msg.content"
                class="message_image"
                alt="图片"
              />
              <div v-else class="message_text">{{ msg.content }}</div>
            </div>
            <div class="message_time">{{ formatDateTime(msg.createTime) }}</div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat_input_area">
        <div class="input_toolbar">
          <!-- <el-icon class="toolbar_icon">
            <Paperclip />
          </el-icon> -->
          <el-icon class="toolbar_icon" @click="handleImageClick">
            <Picture />
          </el-icon>
          <input
            ref="imageInputRef"
            type="file"
            accept="image/*"
            style="display: none"
            @change="handleImageChange"
          />
          <el-popover placement="top" :width="400" trigger="hover" popper-class="emoji_popover">
            <template #reference>
              <el-icon class="toolbar_icon">
                <Sunny />
              </el-icon>
            </template>
            <div class="emoji_grid">
              <span v-for="emoji in emojiList" :key="emoji" class="emoji_item" @click="insertEmoji(emoji)">{{ emoji
                }}</span>
            </div>
          </el-popover>
        </div>
        <div class="input_container">
          <el-input ref="inputRef" v-model="messageInput" placeholder="输入消息..." type="textarea" :rows="1"
            class="message_input" @keydown.enter.prevent="sendMessage" @blur="saveCursorPos" @mouseup="saveCursorPos"
            @keyup="saveCursorPos" />
          <button class="send_btn" @click="sendMessage">发送</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chat_area {
  flex: 1;
  background-color: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
}

.empty_chat {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.empty_icon {
  font-size: 80px;
  color: #ddd;
  margin-bottom: 20px;
}

.empty_text {
  font-size: 18px;
  font-weight: 500;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.empty_subtext {
  font-size: 14px;
  color: #999;
}

.chat_container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chat_header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  background-color: #fafafa;
}

.chat_header_avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 12px;
}

.chat_title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  flex: 1;
}

.chat_messages {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chat_messages::-webkit-scrollbar {
  width: 0;
  background: transparent;
}

.chat_messages {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.message_item {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.message_item.received {
  align-self: flex-start;
}

.message_item.sent {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message_avatar {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
}

.message_avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.message_content {
  max-width: 90%;
  width: fit-content;
  display: flex;
  flex-direction: column;
}

.message_item.sent .message_content {
  align-items: flex-end;
  width: auto;
  max-width: 90%;
}

.message_item.received .message_content {
  align-items: flex-start;
}

.message_text {
  padding: 10px 14px;
  border-radius: 18px;
  font-size: 14px;
  line-height: 1.4;
  margin-bottom: 4px;
}

.message_row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.message_image {
  max-width: 200px;
  max-height: 200px;
  border-radius: 12px;
  object-fit: cover;
  cursor: pointer;
  transition: opacity 0.15s;
}

.message_image:hover {
  opacity: 0.9;
}

.message_image._sending {
  opacity: 0.6;
}

.message_item.received .message_text {
  background-color: #fff;
  color: #1a1a1a;
  border-bottom-left-radius: 4px;
  overflow-wrap: break-word;
}

.message_item.sent .message_text {
  background-color: #1890ff;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.message_time {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
  width: fit-content;
}

.sending_spinner {
  color: #1890ff;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}

.chat_input_area {
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
  background-color: #fff;
}

.input_toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.toolbar_icon {
  font-size: 20px;
  color: #666;
  cursor: pointer;
  transition: color 0.2s;
}

.toolbar_icon:hover {
  color: #1890ff;
}

.input_container {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: center;
}

.message_input {
  flex: 1;
  border-radius: 8px;
}

.message_input :deep(.el-textarea__inner) {
  border-radius: 8px;
  resize: none;
  padding: 12px 16px;
  font-size: 14px;
}

.message_input :deep(.el-textarea__inner:focus) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 1px #000 inset;
}

.send_btn {
  padding: 12px 24px;
  background-color: var(--primary-color);
  color: #000000;
  border: 1px solid #000;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
  align-self: flex-end;
}

.send_btn:hover {
  filter: brightness(1.05);
}

.send_btn:active {
  background-color: #096dd9;
}
</style>

<style>
.emoji_popover {
  padding: 8px;
}

.emoji_grid {
  display: grid;
  grid-template-columns: repeat(10, 1fr);
  gap: 4px;
  max-height: 260px;
  overflow-y: auto;
  overflow-x: hidden;
}

.emoji_item {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  font-size: 20px;
  cursor: pointer;
  border-radius: 6px;
  transition: background-color 0.15s;
  user-select: none;
}

.emoji_item:hover {
  background-color: #f0f0f0;
}

.emoji_item:active {
  background-color: #e0e0e0;
}
</style>
