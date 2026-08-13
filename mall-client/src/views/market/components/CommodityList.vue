<script setup name="CommodityList">
import Commodity from '@/components/Commodity.vue';
import { onMounted, ref, onUnmounted } from 'vue'
import { useCommodityStore } from '@/stores/commodity'
import { useRouter } from 'vue-router'

const router = useRouter()

const commodityStore = useCommodityStore()

/**
 * 滚动事件处理
 */
const handleScroll = () => {
  const scrollTop = window.scrollY || document.documentElement.scrollTop
  const clientHeight = document.documentElement.clientHeight
  const scrollHeight = document.documentElement.scrollHeight

  if (scrollTop + clientHeight >= scrollHeight - 100) {
    commodityStore.getCommodityList()
  }
}

/**
 * 跳转详情页
 */
const goDetail = (commodityId) => {
  router.push(`/detail/${commodityId}`)
}

onMounted(async () => {
  await commodityStore.getCommodityList()
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<template>
  <div class="commodity_container">
    <Commodity v-for="item in commodityStore.commodityList" :key="item.commodityId" :commodity="item" @click="goDetail(item.commodityId)"/>

    <div v-if="commodityStore.loading" class="loading_tip">
      <div class="loading_dots">
        <span class="dot"></span>
        <span class="dot"></span>
        <span class="dot"></span>
      </div>
      <span>正在加载更多...</span>
    </div>

    <div v-if="commodityStore.finished && commodityStore.commodityList.length > 0" class="loading_tip">
      <span>没有更多了</span>
    </div>
  </div>
</template>

<style scoped>
.commodity_container {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
}

.loading_tip {
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #999;
  font-size: 14px;
}

.loading_dots {
  display: flex;
  gap: 4px;
}

.dot {
  width: 6px;
  height: 6px;
  background-color: #999;
  border-radius: 50%;
  animation: bounce 1.4s ease-in-out infinite;
}

.dot:nth-child(1) {
  animation-delay: 0s;
}

.dot:nth-child(2) {
  animation-delay: 0.2s;
}

.dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes bounce {

  0%,
  80%,
  100% {
    transform: translateY(0);
  }

  40% {
    transform: translateY(-8px);
  }
}
</style>
