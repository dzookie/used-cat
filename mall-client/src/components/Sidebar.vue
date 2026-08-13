<script setup>
import { CirclePlus, ChatLineRound, Service, Upload } from '@element-plus/icons-vue'
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { httpGet } from '@/utils/request'
import apiConfig from '@/apis/api.config'
import { createChatClient, onMessage, offMessage } from '@/utils/websocket'
import AiService from '@/components/AiService.vue'

const router = useRouter()
const showBackToTop = ref(false)
const unreadCount = ref(0)
const serviceButtonRef = ref(null)

const handleScroll = () => {
  showBackToTop.value = window.scrollY > 100
}

const backToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}

const goToRelease = () => {
  router.push('/personal-center/release-idle')
}

const goToChat = () => {
  router.push('/chat')
}

const fetchUnreadCount = async () => {
  const token = localStorage.getItem('token')
  if (!token) return
  try {
    const res = await httpGet(apiConfig.chat.conversationList)
    if (res.data.code === 200) {
      unreadCount.value = res.data.data.reduce((sum, item) => sum + (item.unreadCount || 0), 0)
    }
  } catch (e) {
    console.error('获取未读消息数失败', e)
  }
}

const handleWsMessage = (msg) => {
  if (router.currentRoute.value.path !== '/chat') {
    unreadCount.value++
  }
}

let wsCallback = null

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  handleScroll()
  fetchUnreadCount()
  createChatClient()
  wsCallback = onMessage('chatMessages', handleWsMessage)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  offMessage('chatMessages', wsCallback)
})
</script>

<template>
  <div class="sidebar_container">
    <div class="sidebar_item" @click="goToRelease">
      <el-icon style="font-size: 26px;background-color: var(--primary-color);border-radius: 100%;">
        <CirclePlus />
      </el-icon>
      <p>卖闲置</p>
    </div>

    <div class="sidebar_item" @click="goToChat">
      <el-badge :value="unreadCount" :max="99" :offset="[-5, 0]" :show-zero="false">
        <el-icon style="font-size: 26px;">
          <ChatLineRound />
        </el-icon>
      </el-badge>
      <p>消息</p>
    </div>

    <div class="sidebar_item" ref="serviceButtonRef">
      <el-icon style="font-size: 26px;">
        <Service />
      </el-icon>
      <p>客服</p>
    </div>

    <el-popover
      ref="popoverRef"
      :virtual-ref="serviceButtonRef"
      trigger="click"
      virtual-triggering
      placement="left"
      :width="500"
      :offset="25"
    >
      <AiService />
    </el-popover>

    <!-- 返回顶部 -->
    <div class="sidebar_item" v-if="showBackToTop" @click="backToTop">
      <el-icon style="font-size: 26px;">
        <Upload />
      </el-icon>
      <p>回顶部</p>
    </div>
  </div>
</template>

<style scoped>
.sidebar_container {
  padding: 10px 15px;
  border: 50%;
  position: fixed;
  top: 50%;
  right: 20px;
  transform: translateY(-50%);
  z-index: 9999;
  box-shadow: 0 6px 12px 0 rgba(0, 0, 0, .08);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  background-color: #fff;
  border-radius: 50px;
  transition: all 0.3s;


  .sidebar_item {
    display: flex;
    flex-direction: column;
    align-items: center;
    width: 100%;
    justify-content: center;
    gap: 5px;
    font-size: 12px;
    border-bottom: 1px solid var(--border-color);
    padding: 12px 0;

    &:hover>p {
      color: #252525;
    }

  }

  & .sidebar_item:last-child {
    border-bottom: none;
  }
}
</style>