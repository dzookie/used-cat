<script setup>
import Category from './components/Category.vue'
import CommodityList from './components/CommodityList.vue'
import ModelTitle from '@/components/ModelTitle.vue'
import Sidebar from '@/components/Sidebar.vue'
import { useCommodityStore } from '@/stores/commodity'

const commodityStore = useCommodityStore()

const handleRefresh = () => {
  commodityStore.refreshCommodityList()
}
</script>

<template>
  <div>
    <Sidebar />
    <Category />
    <div class="content">
      <ModelTitle title="热门推荐" @refresh="handleRefresh" />
      <CommodityList />
    </div>
  </div>
</template>



<style scoped>

.content {
  width: 80%;
  margin: 20px auto;
  background-color: #fff;
  padding: 20px;
  border-radius: 10px;
}
</style>