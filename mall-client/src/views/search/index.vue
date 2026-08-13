<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { httpGet } from '@/utils/request'
import apiConfig from '@/apis/api.config'
import { ElMessage } from 'element-plus'
import Commodity from '@/components/Commodity.vue'

const route = useRoute()
const router = useRouter()

const searchKeyword = ref('')
const commodityList = ref([])
const loading = ref(false)

const goDetail = (commodityId) => {
  router.push(`/detail/${commodityId}`)
}

const fetchSearchResults = async () => {
  const keyword = route.query.keyword
  if (!keyword) return

  searchKeyword.value = keyword
  loading.value = true
  try {
    const { data } = await httpGet(apiConfig.commodity.search, { keyword })
    if (data.code === 200) {
      commodityList.value = data.data || []
    } else {
      ElMessage.error(data.message || '搜索失败')
    }
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchSearchResults()
})

watch(() => route.query.keyword, () => {
  fetchSearchResults()
})
</script>

<template>
  <div class="search_app">
    <div class="search_content">
      <div class="search_title">
        <p v-if="commodityList.length > 0">共找到 <span class="count">{{ commodityList.length }}</span> 件与 "<span class="keyword">{{ searchKeyword }}</span>" 相关的商品</p>
        <p v-else-if="!loading">未找到与 "<span class="keyword">{{ searchKeyword }}</span>" 相关的商品</p>
      </div>

      <div v-if="loading" class="loading_wrapper">
        <span>搜索中...</span>
      </div>

      <el-empty v-else-if="commodityList.length === 0" description="暂无搜索结果" />

      <div v-else class="commodity_container">
        <Commodity v-for="item in commodityList" :key="item.commodityId" :commodity="item" @click="goDetail(item.commodityId)" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.search_app {
  background-color: var(--bj-color);
}

.search_content {
  width: 80%;
  margin: 20px auto;
  background-color: #fff;
  border-radius: 10px;
  padding: 20px;
}

.commodity_container {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
}

.search_title {
  margin-bottom: 20px;
  font-size: 15px;
  color: #666;
}

.search_title .count {
  color: #ff5000;
  font-weight: 600;
}

.search_title .keyword {
  color: #333;
  font-weight: 600;
}

.loading_wrapper {
  text-align: center;
  padding: 60px 0;
  color: #999;
  font-size: 14px;
}
</style>
