<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import apiConfig from '@/apis/api.config'
import { httpGet } from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import Commodity from '@/components/Commodity.vue'

const router = useRouter()
const userStore = useUserStore()

const currUserCommunity = ref([])
const total = ref(0)
const loading = ref(false)
const requestParams = ref({
  pageSize: 8,
  pageIndex: 1
})

const hasMore = computed(() => {
  return currUserCommunity.value.length < total.value
})

/**
 * 获取该用户的商品列表
 */
const getCurrUserCommunityList = async () => {
  try {
    loading.value = true
    const { data } = await httpGet(apiConfig.commodity.getCommodityList, {
      ...requestParams.value,
      userId: userStore.loginUser.userId
    })
    if (data.code === 200) {
      currUserCommunity.value = [...currUserCommunity.value, ...data.data.items || []]
      total.value = data.data.total || 0
    } else {
      ElMessage.error(data.message || '获取商品列表失败')
    }
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

/**
 * 加载更多商品
 */
const loadMore = () => {
  if (loading.value || !hasMore.value) return
  requestParams.value.pageIndex++
  getCurrUserCommunityList()
}

const goDetail = (commodityId) => {
  router.push(`/detail/${commodityId}`)
}



onMounted(() => {
  getCurrUserCommunityList()
})

</script>

<template>
  <div v-if="currUserCommunity.length > 0" class="community_list">
    <Commodity v-for="item in currUserCommunity" :key="item.commodityId" :commodity="item" @click="goDetail(item.commodityId)" />
  </div>
  <el-empty v-else description="暂无商品" />
  <div v-if="currUserCommunity.length > 0" class="load_more">
    <button v-if="hasMore" class="load_more_btn" :disabled="loading" @click="loadMore">
      {{ loading ? '加载中...' : '加载更多' }}
    </button>
    <p v-else class="no_more">没有更多数据了</p>
  </div>
</template>


<style scoped>
.community_list {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  padding: 28px;
}

.load_more {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.load_more_btn {
  padding: 10px 40px;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 20px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s ease;
}

.load_more_btn:hover:not(:disabled) {
  background: #f7f7f7;
  color: #333;
}

.load_more_btn:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.no_more {
  color: #999;
  font-size: 14px;
}
</style>