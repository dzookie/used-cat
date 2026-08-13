<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { httpGet, httpPost } from '@/utils/request'
import apiConfig from '@/apis/api.config'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Star } from '@element-plus/icons-vue'
import Commodity from '@/components/Commodity.vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const favorites = ref([])

const fetchFavorites = async () => {
  loading.value = true
  try {
    const { data } = await httpGet(apiConfig.favorite.list)
    if (data.code === 200) {
      favorites.value = data.data || []
    } else {
      ElMessage.error(data.msg || '获取收藏列表失败')
    }
  } catch {
    ElMessage.error('获取收藏列表失败')
  } finally {
    loading.value = false
  }
}

const removeFavorite = async (commodityId) => {
  try {
    const { data } = await httpPost(apiConfig.favorite.toggle, null, {
      params: { commodityId }
    })
    if (data.code === 200) {
      favorites.value = favorites.value.filter(item => item.commodityId !== commodityId)
      ElMessage.success('已取消收藏')
    } else {
      ElMessage.error(data.msg || '操作失败')
    }
  } catch {
    ElMessage.error('操作失败')
  }
}

onMounted(async () => {
  if (!userStore.loginUser) {
    await userStore.getCurrLoginUser()
  }
  fetchFavorites()
})
</script>

<template>
  <div class="collection_container">
    <h2 class="collection_title">我的收藏</h2>

    <div v-if="loading" class="loading_wrapper">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else-if="favorites.length === 0" class="empty_wrapper">
      <el-empty description="暂无收藏商品" />
    </div>

    <div v-else class="commodity_grid">
      <div
        v-for="item in favorites"
        :key="item.commodityId"
        class="commodity_wrapper"
        @click="router.push(`/detail/${item.commodityId}`)"
      >
        <Commodity :commodity="item" />
        <div class="remove_btn" @click.stop="removeFavorite(item.commodityId)">
          <el-tooltip content="取消收藏" placement="top">
            <el-icon><Star /></el-icon>
          </el-tooltip>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.collection_container {
  padding: 24px;
  background: #fff;
  border-radius: 8px;
  min-height: calc(100vh - 100px);
}

.collection_title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 24px;
}

.commodity_grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.commodity_wrapper {
  position: relative;
}

.remove_btn {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.85);
  border-radius: 6px;
  color: #ff6700;
  font-size: 14px;
  cursor: pointer;
  z-index: 2;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.15);
}

.remove_btn:hover {
  background: #fff;
  color: #e55a00;
  transform: scale(1.1);
}

.loading_wrapper {
  text-align: center;
  padding: 80px 0;
}

.spinner {
  width: 36px;
  height: 36px;
  margin: 0 auto 16px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #ff6700;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading_wrapper p {
  color: #999;
  font-size: 14px;
}

.empty_wrapper {
  padding: 60px 0;
}
</style>
