<script setup>
import Commodity from '@/components/Commodity.vue';
import { onMounted, onBeforeUnmount, ref } from 'vue'
import { useCommodityStore } from '@/stores/commodity'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const commodityStore = useCommodityStore()

/**
 * 跳转详情页
 */
const goDetail = (commodityId) => {
  router.push(`/detail/${commodityId}`)
}

onMounted(() => {
  commodityStore.getCommodityList(null, route.params.id)
})

onBeforeUnmount(() => {
  commodityStore.resetCategoryPageParams()
})
</script>

<template>
  <div class="category_app">
    <div class="category_content">
      <div class="category_title">
        <p>{{ route.query.typeName }}</p>
        <p>{{ route.query.typeDesc }}</p>
      </div>

      <div class="commodity_container">
        <Commodity v-for="commodity in commodityStore.categoryCommodityList" :key="commodity.id" :commodity="commodity"
          @click="goDetail(commodity.commodityId)" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.category_app {
  background-color: var(--bj-color);

  .category_content {
    width: 80%;
    margin: 20px auto;
    background-color: #fff;
    border-radius: 10px;
    padding: 20px;

    .commodity_container {
      display: grid;
      grid-template-columns: repeat(5, 1fr);
      gap: 20px;
    }

    .category_title {
      display: flex;
      gap: 10px;
      align-items: center;
      margin-bottom: 20px;

      p:nth-child(1) {
        font-size: 19px;
        font-weight: bold;
      }

      p:nth-child(2) {
        font-size: 13px;
        color: #999;
      }
    }
  }
}
</style>