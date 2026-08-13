<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { httpGet } from '@/utils/request'
import apiConfig from '@/apis/api.config'
import { ElMessage } from 'element-plus'

const router = useRouter()
const BASE_URL = import.meta.env.VITE_BASE_URL

const commodities = ref([])
const loading = ref(true)

const fetchBrowseHistory = async () => {
  loading.value = true
  try {
    const { data } = await httpGet(apiConfig.browseHistory.list)
    if (data.code === 200) {
      commodities.value = data.data || []
    } else {
      ElMessage.error(data.message || '获取浏览记录失败')
    }
  } catch (error) {
    ElMessage.error('获取浏览记录失败')
  } finally {
    loading.value = false
  }
}

const goDetail = (commodityId) => {
  router.push(`/detail/${commodityId}`)
}

const getMainImage = (commodity) => {
  if (commodity.albums && commodity.albums.length > 0) {
    return commodity.albums[0].path
  }
  return ''
}

onMounted(() => {
  fetchBrowseHistory()
})
</script>

<template>
  <div class="browse_container">
    <div v-loading="loading" class="browse_list">
      <el-empty v-if="!loading && commodities.length === 0" description="暂无浏览记录" />

      <div v-else class="commodity_grid">
        <div v-for="item in commodities" :key="item.commodityId" class="commodity_card" @click="goDetail(item.commodityId)">
          <div class="card_image">
            <img v-if="getMainImage(item)" :src="`${BASE_URL}${getMainImage(item)}`" alt="" />
            <div v-else class="no_image">暂无图片</div>
            <span v-if="item.status === 2" class="sold_tag">已售出</span>
          </div>
          <div class="card_info">
            <p class="card_name">{{ item.commodityName || '商品名称' }}</p>
            <p class="card_price">¥{{ item.price?.toFixed(2) }}</p>
            <p class="card_browse">{{ item.browse || 0 }}次浏览</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.browse_container{
  padding: 28px;
}
.browse_list {
  min-height: 200px;
}

.commodity_grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.commodity_card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.3s;
}

.commodity_card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card_image {
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;
  position: relative;
  background: #f5f5f5;
}

.card_image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no_image {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ccc;
  font-size: 14px;
}

.sold_tag {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.card_info {
  padding: 10px;
}

.card_name {
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 6px;
}

.card_price {
  font-size: 16px;
  font-weight: 600;
  color: #ff6700;
  margin-bottom: 4px;
}

.card_browse {
  font-size: 12px;
  color: #999;
}
</style>
