<script setup>
import { useRouter } from 'vue-router'
import { onMounted, ref } from 'vue'
import { httpGet } from '@/utils/request.js'
import apiConfig from '@/apis/api.config'
import CategorySkeleton from '@/components/CategorySkeleton.vue'

const router = useRouter()

const categoryList = ref([])
const loading = ref(true)
const baseUrl = import.meta.env.VITE_BASE_URL

/**
 * 获取商品分类列表
 */
const getCategoryList = async () => {
  try {
    const { data } = await httpGet(apiConfig.category.getCategoryList, null)
    if(data.code === 200) {
      categoryList.value = data.data
    }
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

/**
 * 跳转详情页
 */
const goDetail = (commodityId) => {
  router.push(`/detail/${commodityId}`)
}

/**
 * 跳转商品分类页
 */
const goCategory = (category) => {
  router.push({
    path: `/category/${category.typeId}`,
    query: {
      typeDesc: category.typeDesc,
      typeName: category.typeName
    }
  })
}

onMounted(() => {
  getCategoryList()
})
</script>

<template>
  <div class="category_container">
    <CategorySkeleton v-if="loading" />
    <template v-else>
      <div class="category_item" v-for="category in categoryList" :key="category.typeId"
        :style="`background-color: ${category.color};`" @click="goCategory(category)">
        <!-- 左边 -->
        <div class="category_left">
          <div class="category_left_title">
            <p :style="`--en-text: '${category.en}'`">{{ category.typeName }}</p>
            <img src="@/assets/icons/rightArrow.svg" alt="right arrow">
          </div>
          <p class="category_left_desc">{{ category.typeDesc }}</p>
          <!-- 装饰图 -->
          <div class="category_decorate">
            <img :src="baseUrl + category.img" :alt="category.typeName">
          </div>
        </div>

        <!-- 右边 -->
        <div class="category_right">
          <div class="category_product" v-for="item in category.recommendationList" :key="item.commodityId" @click.stop="goDetail(item.commodityId)">
            <img :src="baseUrl + item.albums[0].path" alt="xx">
            <p>￥<b>{{ item.price }}</b></p>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.category_container {
  width: 80%;
  padding: 20px;
  background-color: #ffffff;
  margin: 20px auto;
  border-radius: 10px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;

  .category_item {
    display: flex;
    border: 1px solid #f0f0f0;
    border-radius: 12px;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.3s ease;
    background-color: var(--primary-color);
    padding: 10px;

    &:hover>.category_left>.category_decorate>img:nth-child(1) {
      transform: scale(1.5);
      transition: all 1s ease;
    }

    &:hover .category_right {
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
    }

    .category_left {
      display: flex;
      flex-direction: column;
      gap: 8px;
      width: 200px;

      .category_decorate {

        img {
          transform-origin: top center;
          transform: translateX(5px);
          width: 60%;
          transition: all 1s ease;
        }

        img:nth-child(1) {
          z-index: 999;
          position: relative;
        }
      }

      .category_left_title {
        display: flex;
        align-items: center;
        gap: 6px;
        margin-top: 8px;
      }

      .category_left_title>p {
        font-size: 1rem;
        font-weight: 600;
        color: #1a1a1a;
        margin: 0;
        position: relative;
        z-index: 999;
      }

      .category_left_title>p::before {
        content: var(--en-text);
        position: absolute;
        top: -15px;
        left: 0;
        color: rgba(255, 255, 255, 0.4);
        z-index: -5;
        font-size: 20px;
        text-transform: uppercase;
      }


      .category_left_title img {
        width: 14px;
        height: 14px;
        z-index: 999;
      }

      .category_left_desc {
        font-size: 0.75rem;
        color: #666;
        margin: 0;
      }
    }

    .category_right {
      background-color: rgba(255, 255, 255, 0.8);
      display: flex;
      gap: 10px;
      width: 100%;
      justify-content: space-between;
      padding: 10px;
      border-radius: 10px;
      z-index: 999;
      transition: all 0.3s ease;

      .category_product {
        display: flex;
        flex-direction: column;
        gap: 3px;
        justify-content: center;
        align-items: center;
        overflow: hidden;

        img {
          width: 70px;
          height: 70px;
          border-radius: 10px;
          z-index: 999;

          &:hover {
            transform: scale(1.1);
            transition: all 0.3s ease;
          }
        }

        p {
          font-size: 8px;
          color: #ff0000;

          b {
            font-size: 12px;
            font-weight: 600;
          }
        }
      }

    }
  }
}
</style>
