<script setup>
import { ref, onMounted, computed, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import { Bell, Setting, ChatDotRound, MoreFilled, Delete } from '@element-plus/icons-vue'
import ChatWindow from './components/ChatWindow.vue'
import { httpGet, httpPost } from '@/utils/request'
import apiConfig from '@/apis/api.config'
import avatarDefault from '@/assets/icons/avatar.svg'
import { createChatClient, onMessage, offMessage } from '@/utils/websocket'

const BASE_URL = import.meta.env.VITE_BASE_URL
const route = useRoute()
const conversations = ref([])
const selectedConversation = ref(null)

const totalUnreadCount = computed(() => {
  return conversations.value.reduce((sum, c) => sum + (c.unreadCount || 0), 0)
})

const handleWsMessage = (msg) => {
  console.log('[index] 收到WS消息:', msg)
  const conv = conversations.value.find(c => c.id === msg.conversationId)
  if (!conv) {
    console.log('[index] 未找到匹配会话, msg.conversationId=', msg.conversationId, '会话IDs=', conversations.value.map(c => c.id))
    return
  }
  conv.content = msg.messageType === 'image' ? '[图片]' : msg.content
  conv.time = msg.createTime
  if (selectedConversation.value?.id !== conv.id) {
    conv.unread = true
    conv.unreadCount = (conv.unreadCount || 0) + 1
  }
}

const loadConversations = async () => {
  try {
    const res = await httpGet(apiConfig.chat.conversationList)
    if (res.data.code === 200) {
      conversations.value = res.data.data.map(item => ({
        ...item,
        isNotice: false,
        unread: item.unread
      }))
      autoOpenConversation()
    }
  } catch (e) {
    console.error('获取会话列表失败', e)
  }
}

const autoOpenConversation = () => {
  const queryId = route.query.conversationId
  if (queryId) {
    const conv = conversations.value.find(c => c.id === Number(queryId))
    if (conv) {
      selectConversation(conv)
    }
  }
}

const selectConversation = async (conversation) => {
  selectedConversation.value = conversation
  if (conversation.unread) {
    try {
      await httpGet(apiConfig.chat.markRead, { conversationId: conversation.id })
      conversation.unread = false
      conversation.unreadCount = 0
    } catch (e) {
      console.error('标记已读失败', e)
    }
  }
}

/**
 * 删除会话
 * @param {*} item 会话项
 */
const deleteConversation = async (item) => {
  try {
    await httpPost(apiConfig.chat.deleteConversation, null, {
      params: { conversationId: item.id }
    })
    conversations.value = conversations.value.filter(c => c.id !== item.id)
    if (selectedConversation.value?.id === item.id) {
      selectedConversation.value = null
    }
  } catch (e) {
    console.error('删除会话失败', e)
  }
}

let wsCallback = null

onMounted(() => {
  createChatClient()
  wsCallback = onMessage('chatMessages', handleWsMessage)
  loadConversations()
})

onBeforeUnmount(() => {
  offMessage('chatMessages', wsCallback)
})
</script>

<template>
  <div class="chat_page">
    <!-- 左侧会话列表 -->
    <div class="conversation_sidebar">
      <div class="sidebar_header">
        <div class="header_title">
          <span class="title_text">消息</span>
          <span class="unread_badge" v-if="totalUnreadCount > 0">{{ totalUnreadCount }}</span>
        </div>
        <div class="header_actions">
          <!-- <el-icon class="action_icon">
            <Bell />
          </el-icon>
          <el-icon class="action_icon">
            <Setting />
          </el-icon> -->
        </div>
      </div>

      <div class="conversation_list">
        <el-empty v-if="conversations.length === 0" description="暂无消息" :image-size="80">
          <template #image>
            <el-icon :size="60" color="#c0c4cc">
              <ChatDotRound />
            </el-icon>
          </template>
        </el-empty>

        <div class="conversation_item" v-for="item in conversations" :key="item.id"
          :class="{ active: selectedConversation?.id === item.id, unread: item.unread, notice: item.isNotice }"
          @click="selectConversation(item)">
          <div class="conversation_avatar">
            <div v-if="item.isNotice" class="notice_avatar">
              <div class="notice_circle"></div>
            </div>
            <img v-else :src="item.avatar ? BASE_URL + item.avatar : avatarDefault" alt="avatar" class="user_avatar" />
          </div>

          <div class="conversation_content">
            <div class="conversation_top">
              <span class="conversation_title">{{ item.title }}</span>
              <el-popover
                placement="right"
                :width="120"
                trigger="click"
                popper-class="conv_action_popover"
                @click.stop
              >
                <template #reference>
                  <el-icon class="conv_more_icon" @click.stop>
                    <MoreFilled />
                  </el-icon>
                </template>
                <div class="conv_action_item" @click.stop="deleteConversation(item)">
                  <el-icon :size="14"><Delete /></el-icon>
                  <span>删除会话</span>
                </div>
              </el-popover>
            </div>
            <div class="conversation_bottom">
              <span class="conversation_text">{{ item.content }}</span>
              <div v-if="item.unread && !item.isNotice" class="unread_dot"></div>
            </div>
          </div>


        </div>
      </div>
    </div>

    <!-- 右侧聊天区域 -->
    <ChatWindow :conversation="selectedConversation" />
  </div>
</template>

<style scoped>
.chat_page {
  display: flex;
  max-width: 1200px;
  margin: 20px auto;
  gap: 24px;
  padding: 0 20px;
  height: calc(100vh - 180px);
  overflow: hidden;
}

/* 左侧会话列表 */
.conversation_sidebar {
  width: 320px;
  background-color: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
}

.sidebar_header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  background-color: #fafafa;
}

.header_title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.title_text {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.unread_badge {
  background-color: #ff4d4f;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 10px;
  min-width: 18px;
  text-align: center;
}

.header_actions {
  display: flex;
  gap: 12px;
}

.action_icon {
  font-size: 18px;
  color: #666;
  cursor: pointer;
  transition: color 0.2s;
}

.action_icon:hover {
  color: #1a1a1a;
}

.conversation_list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.conversation_item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
  margin-bottom: 4px;
  border-left: 4px solid #ffffff;
}

.conversation_item:hover {
  background-color: #f5f5f5;
}

.conversation_item.active {
  background-color: #fffff6;
  border-left: 4px solid var(--primary-color);
}

.conversation_item.unread {
  background-color: #fffbe6;
}

.conversation_avatar {
  width: 48px;
  height: 48px;
  flex-shrink: 0;
  position: relative;
}

.notice_avatar {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #ff6b6b 0%, #feca57 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.notice_circle {
  width: 20px;
  height: 20px;
  border: 3px solid #fff;
  border-radius: 50%;
}

.user_avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.conversation_content {
  flex: 1;
  min-width: 0;
}

.conversation_top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.conversation_title {
  font-size: 15px;
  font-weight: 500;
  color: #1a1a1a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.conversation_bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.conversation_text {
  font-size: 13px;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}

.unread_dot {
  width: 8px;
  height: 8px;
  background-color: #ff4d4f;
  border-radius: 50%;
  flex-shrink: 0;
}
</style>

<style>
.conv_more_icon {
  font-size: 16px;
  color: #bbb;
  cursor: pointer;
  flex-shrink: 0;
  padding: 2px;
  border-radius: 4px;
  transition: all 0.2s;
}

.conv_more_icon:hover {
  color: #666;
  background-color: #f0f0f0;
}

.conv_action_popover {
  padding: 4px 0 !important;
}

.conv_action_item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  font-size: 13px;
  color: #ff4d4f;
  cursor: pointer;
  transition: background-color 0.15s;
}

.conv_action_item:hover {
  background-color: #fff1f0;
}
</style>
