<script setup>
import { onMounted, ref, computed } from 'vue'
import { ChatDotRound } from '@element-plus/icons-vue'
import avatar from '@/assets/avatar.png'
import { httpGet, httpPost } from '@/utils/request'
import apiConfig from '@/apis/api.config'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { formatTimeDifference } from '@/utils/date'

const router = useRouter()
const userStore = useUserStore()

const merchantData = ref({})
const BASE_URL = import.meta.env.VITE_BASE_URL

const creditLabel = computed(() => {
  const c = merchantData.value.credit || 0
  if (c >= 8) return '信誉良好'
  if (c >= 6) return '信誉一般'
  return '无良商家'
})

const creditClass = computed(() => {
  const c = merchantData.value.credit || 0
  if (c >= 8) return 'tag_good'
  if (c >= 6) return 'tag_normal'
  return 'tag_bad'
})

const props = defineProps({
  userId: {
    type: Number,
    default: 0
  },
  commodityId: {
    type: Number,
    default: 0
  }
})

const sendMessage = async () => {
  if (!userStore.loginUser) {
    router.push('/login')
    return
  }
  if (!props.userId) {
    ElMessage.warning('无法获取卖家信息')
    return
  }
  if (props.userId === userStore.loginUser.userId) {
    ElMessage.warning('不能和自己聊天哦')
    return
  }
  try {
    const res = await httpPost(apiConfig.chat.createConversation, null, {
      params: {
        targetUserId: props.userId,
        commodityId: props.commodityId
      }
    })
    if (res.data.code === 200) {
      const conv = res.data.data
      router.push({
        path: '/chat',
        query: {
          conversationId: conv.conversationId,
          targetUserId: props.userId,
          title: userStore.loginUser.nickname
        }
      })
    } else {
      ElMessage.error(res.data.msg || '创建会话失败')
    }
  } catch (e) {
    ElMessage.error('创建会话失败')
  }
}

const getMerchantInfo = async () => {
  if (!props.userId) return
  try {
    const { data } = await httpGet(apiConfig.user.getUserByUserId, {
      userId: props.userId
    })
    if (data.code === 200) {
      merchantData.value = data.data
    } else {
      ElMessage.error(data.msg)
    }
  } catch (error) {
    ElMessage.error(error.message)
  }

}

onMounted(() => {
  getMerchantInfo()
})
</script>

<template>
  <div class="seller-card">
    <div class="seller-info">
      <div class="avatar">
        <img :src="BASE_URL + merchantData.avatar" alt="卖家头像" />
      </div>
      <div class="seller-details">
        <div class="top-row">
          <h3 class="nickname">{{ merchantData.nickname }}</h3>
          <span class="credit_tag" :class="creditClass">{{ creditLabel }}</span>
        </div>
        <div class="bottom-row">
          <span class="info-item">来二手猫{{ formatTimeDifference(merchantData.createTime) }}</span>
        </div>
      </div>
      <el-button type="primary" class="message-btn" :icon="ChatDotRound" @click="sendMessage">
        私信
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.seller-card {
  background: #fff;
  padding: 16px;
  border-radius: 12px;
  margin-bottom: 16px;
  max-width: 1200px;
  margin: 20px auto;
}

.seller-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.seller-details {
  flex: 1;
  min-width: 0;
}

.top-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.nickname {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0;
  flex-shrink: 0;
}

.tags {
  display: flex;
  gap: 8px;
  flex-shrink: 1;
  overflow: hidden;
}

.tag {
  font-size: 12px;
  color: #ff6b00;
  background: #fff3e0;
  padding: 2px 8px;
  border-radius: 4px;
  white-space: nowrap;
}

.bottom-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.info-item {
  font-size: 12px;
  color: #666;
  white-space: nowrap;
}

.credit_tag {
  font-size: 11px;
  padding: 1px 8px !important;
  border-radius: 10px;
  font-weight: 500;
  white-space: nowrap;
}

.tag_good {
  background: #e8f5e9;
  color: #2e7d32;
  border: 1px solid #a5d6a7;
}

.tag_normal {
  background: #fff8e1;
  color: #f57f17;
  border: 1px solid #ffe082;
}

.tag_bad {
  background: #fce4ec;
  color: #c62828;
  border: 1px solid #ef9a9a;
}

.message-btn {
  flex-shrink: 0;
  height: 32px;
  font-size: 13px;
  background: var(--primary-color);
  border-color: var(--primary-color);
  color: #000;
  transition: all 0.3s;
}

.message-btn:hover {
  filter: brightness(1.05);
}

@media (max-width: 768px) {
  .seller-info {
    flex-wrap: wrap;
  }

  .message-btn {
    width: 100%;
    margin-top: 12px;
  }
}
</style>
