<script setup>
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { ref, computed, onMounted } from 'vue'
import { ChatDotRound, Star, StarFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { httpGet, httpPost } from '@/utils/request'
import apiConfig from '@/apis/api.config'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const props = defineProps({
  commodity: {
    type: Object,
    default: () => ({})
  }
})

const baseUrl = import.meta.env.VITE_BASE_URL

const currentImageIndex = ref(0)

const albums = computed(() => {
  return props.commodity.albums || []
})

const showNavigation = computed(() => {
  return albums.value.length > 1
})

const selectImage = (index) => {
  currentImageIndex.value = index
}

const prevImage = () => {
  currentImageIndex.value = (currentImageIndex.value - 1 + albums.value.length) % albums.value.length
}

const nextImage = () => {
  currentImageIndex.value = (currentImageIndex.value + 1) % albums.value.length
}

const formattedDescription = computed(() => {
  if (!props.commodity.commodityDesc) return '暂无商品描述'
  return props.commodity.commodityDesc.replace(/\r\n/g, '<br>').replace(/\n/g, '<br>')
})

/**
 * 跳转到创建订单页面
 */
const handleBuyClick = () => {
  router.push({
    path: '/create-order',
    query: {
      id: props.commodity.commodityId,
      price: props.commodity.price,
      description: props.commodity.commodityDesc,
      image: props.commodity.albums?.[0]?.path || ''
    }
  })
}

/**
 * 点击聊一聊：创建会话并跳转到聊天页
 */
const handleChatClick = async () => {
  if (!userStore.loginUser) {
    router.push('/login')
    return
  }
  const sellerId = props.commodity.userId
  if (!sellerId) {
    ElMessage.warning('无法获取卖家信息')
    return
  }
  if (sellerId === userStore.loginUser.userId) {
    ElMessage.warning('不能和自己聊天哦')
    return
  }
  try {
    const res = await httpPost(apiConfig.chat.createConversation, null, {
      params: {
        targetUserId: sellerId,
        commodityId: props.commodity.commodityId
      }
    })
    if (res.data.code === 200) {
      const conv = res.data.data
      router.push({
        path: '/chat',
        query: {
          conversationId: conv.conversationId,
          targetUserId: sellerId,
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

const isFavorited = ref(false)
const favoriteCount = ref(0)

const fetchFavoriteCount = async () => {
  if (!props.commodity?.commodityId) return
  try {
    const res = await httpGet(apiConfig.favorite.count, {
      commodityId: props.commodity.commodityId
    })
    if (res.data.code === 200) {
      favoriteCount.value = res.data.data
    }
  } catch (e) {
    // ignore
  }
}

const checkFavoriteStatus = async () => {
  if (!userStore.loginUser || !props.commodity?.commodityId) return
  try {
    const res = await httpGet(apiConfig.favorite.isFavorited, {
      commodityId: props.commodity.commodityId
    })
    if (res.data.code === 200) {
      isFavorited.value = res.data.data.favorited
    }
  } catch (e) {
    // ignore
  }
}

const handleFavoriteClick = async () => {
  if (!userStore.loginUser) {
    router.push('/login')
    return
  }
  try {
    const res = await httpPost(apiConfig.favorite.toggle, null, {
      params: { commodityId: props.commodity.commodityId }
    })
    if (res.data.code === 200) {
      isFavorited.value = res.data.data.favorited
      ElMessage.success(res.data.data.message)
      fetchFavoriteCount()
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  checkFavoriteStatus()
  fetchFavoriteCount()
})

</script>

<template>
  <div class="detail-container">
    <div class="left-section">
      <div class="thumbnail-list">
        <div v-for="(img, index) in albums" :key="index" class="thumbnail-item"
          :class="{ active: currentImageIndex === index }" @click="selectImage(index)">
          <img :src="typeof img === 'string' ? img : (baseUrl + img.path)" alt="thumbnail" />
        </div>
      </div>
      <div class="main-image-section">
        <div class="main-image-wrapper">
          <button v-if="showNavigation" class="nav-btn prev" @click="prevImage">
            <el-icon>
              <ArrowLeft />
            </el-icon>
          </button>
          <img
            :src="typeof albums[currentImageIndex] === 'string' ? albums[currentImageIndex] : (baseUrl + albums[currentImageIndex]?.path)"
            alt="main" class="main-image" />
          <button v-if="showNavigation" class="nav-btn next" @click="nextImage">
            <el-icon>
              <ArrowRight />
            </el-icon>
          </button>
        </div>
      </div>
    </div>

    <div class="right-section">
      <div class="price-row">
        <span class="price">¥{{ commodity.price || 1300 }}</span>
        <span class="price-tag">{{ commodity.useStatus }}</span>
      </div>

      <div class="status-row">
        <span class="view-info"><b class="browse-count">{{ favoriteCount }}</b>人想要 ▪ <b class="browse-count">{{ commodity.browse }}</b>次浏览</span>
      </div>

      <div class="description" v-html="formattedDescription"></div>

      <div class="specs-grid">
        <div class="spec-item">
          <span class="spec-label">品牌：</span>
          <span class="spec-value">{{ commodity.brand }}</span>
        </div>

        <div class="spec-item">
          <span class="spec-label">使用情况：</span>
          <span class="spec-value">{{ commodity.useStatus }}</span>
        </div>

      </div>

      <div class="action-buttons">
        <template v-if="userStore.loginUser">
          <el-button type="primary" class="buy-btn" @click="handleBuyClick">
            立即购买
          </el-button>
          <el-button type="warning" class="chat-btn" :icon="ChatDotRound" @click="handleChatClick">
            聊一聊
          </el-button>
          <el-button
            class="favorite-btn"
            :class="{ 'is-favorited': isFavorited }"
            :icon="isFavorited ? StarFilled : Star"
            @click="handleFavoriteClick">
            {{ isFavorited ? '已收藏' : '收藏' }}
          </el-button>
        </template>

        <template v-else>
          <button class="not_login_btn" @click="$router.push('/login')">登录查看更多</button>
        </template>

      </div>

    </div>
  </div>
</template>

<style scoped>
.detail-container {
  display: flex;
  gap: 40px;
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  background-color: #fff;
  border-radius: 15px;
}

.left-section {
  display: flex;
  gap: 16px;
  flex: 1;
  align-items: flex-start;
}

.thumbnail-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  height: 500px;
  padding-right: 4px;
  box-sizing: border-box;
  overflow-y: auto;
  overflow-x: hidden;
}

.thumbnail-list::-webkit-scrollbar {
  width: 0;
  height: 0;
}

.thumbnail-list::-webkit-scrollbar-track {
  background: transparent;
}

.thumbnail-list::-webkit-scrollbar-thumb {
  background: transparent;
}

/* 隐藏Firefox滚动条 */
.thumbnail-list {
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.thumbnail-item {
  width: 60px;
  height: 60px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.3s;
  flex-shrink: 0;
  padding: 2px;
}

.thumbnail-item:hover {
  border-color: #000;
}

.thumbnail-item.active {
  border-color: #000;
}

.thumbnail-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.main-image-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  min-height: 400px;
}

.main-image-wrapper {
  position: relative;
  width: 100%;
  max-width: 500px;
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 12px;
  overflow: hidden;
}

.main-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.nav-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 40px;
  height: 40px;
  border: none;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: #666;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.nav-btn:hover {
  background: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.nav-btn.prev {
  left: 16px;
}

.nav-btn.next {
  right: 16px;
}

.right-section {
  flex: 1;
  max-width: 500px;
  display: flex;
  flex-direction: column;
}

.action-buttons {
  margin-top: auto;
  display: flex;
  gap: 12px;
}

.price-row {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 12px;
}

.price {
  font-size: 32px;
  font-weight: bold;
  color: #ff6b00;
}

.price-tag {
  font-size: 20px;
  font-weight: bold;
  color: #000000;
  font-style: italic;
  position: relative;
  z-index: 999;
}

.price-tag::after {
  content: '';
  font-size: 14px;
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 10px;
  background-color: var(--primary-color);
  z-index: -1;
  transform: skew(-15deg);
  transform-origin: left bottom;
}

.status-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.browse-count{
  color: #ff6b00;
}

.status-tag {
  font-size: 14px;
  color: #ff6b00;
  font-weight: 500;
}

.view-info {
  font-size: 12px;
  color: #999;
}

.description {
  font-size: 14px;
  line-height: 1.8;
  color: #333;
  margin-bottom: 20px;
  padding: 16px;
  border: 1px solid #eee;
  border-radius: 8px;
  max-height: 500px;
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: #ddd #f5f5f5;
}

.description::-webkit-scrollbar {
  width: 6px;
}

.description::-webkit-scrollbar-track {
  background: #f5f5f5;
  border-radius: 3px;
}

.description::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 3px;
}

.description::-webkit-scrollbar-thumb:hover {
  background: #ccc;
}

.pickup-info {
  margin-bottom: 20px;
}

.specs-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 24px;
  margin-bottom: 24px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.spec-item {
  display: flex;
  font-size: 13px;
}

.spec-label {
  color: #999;
  min-width: 70px;
}

.spec-value {
  color: #333;
  flex: 1;
}

.chat-btn {
  flex: 1;
  height: 44px;
  font-size: 15px;
  background: var(--primary-color);
  border-color: var(--primary-color);
  color: #333;
  transition: all 0.3s;
}

.chat-btn:hover {
  filter: brightness(1.05);
}

.buy-btn {
  flex: 1;
  height: 44px;
  font-size: 15px;
  background: #333;
  border-color: #333;
}

.buy-btn:hover {
  background: #000;
  border-color: #000;
}

.favorite-btn {
  width: 100px;
  height: 44px;
  font-size: 15px;
  border: 1px solid #ddd;
  background: #fff;
  color: #666;
}

.favorite-btn:hover {
  border-color: #ff6b00;
  color: #ff6b00;
}

.favorite-btn.is-favorited {
  border-color: #ff5000;
  background: #ff5000;
  color: #fff;
}

.not_login_btn{
  width: 100%;
  height: 44px;
  font-size: 15px;
  color: #000;
  background-color: var(--primary-color);
  border-radius: 5px;
}
</style>
