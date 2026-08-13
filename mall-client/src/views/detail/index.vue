<script setup>
import Detail from './components/Detail.vue'
import Seller from './components/Seller.vue'
import { useRoute } from 'vue-router'
import { ref, onMounted } from 'vue'
import { httpGet, httpPost } from '@/utils/request'
import apiConfig from '@/apis/api.config'
import { ElMessage } from 'element-plus'

const route = useRoute()
const commodity = ref({})
const loading = ref(true)

const getCommodityById = async () => {
  const id = route.params.id
  if (!id) return
  try {
    const { data } = await httpGet(apiConfig.commodity.getCommodityById, { commodityId: id })
    if (data.code === 200) {
      commodity.value = data.data
      // 同一会话内不重复计数
      const key = `visited_${id}`
      if (!sessionStorage.getItem(key)) {
        sessionStorage.setItem(key, '1')
        httpPost(apiConfig.browseHistory.record, { commodityId: Number(id) })
        httpPost(apiConfig.commodity.incrementBrowse, { commodityId: Number(id) })
      }
    } else {
      ElMessage.error(data.msg)
    }
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  getCommodityById()
})
</script>

<template>
  <div class="detail-page">
    <template v-if="!loading">
      <Seller :userId="commodity.userId" :commodityId="commodity.commodityId" />
      <Detail :commodity="commodity" />
    </template>
  </div>
</template>

<style scoped>
.detail-page {
  background: #f5f5f5;
}
</style>
