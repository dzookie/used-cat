import { defineStore } from 'pinia'
import { ref } from 'vue'
import apiConfig from '@/apis/api.config'
import { httpGet } from '@/utils/request'
import { ElMessage } from 'element-plus'

export const useCommodityStore = defineStore('commodity', () => {
  // 商品列表数据
  const commodityList = ref([])
  // 商品列表总条数
  const total = ref(0)
  // 加载状态
  const loading = ref(false)
  // 分页状态
  const finished = ref(false)
  // 请求参数
  const requestParams = ref({
    pageSize: 10,
    pageIndex: 1
  })
  // 分类商品列表
  const categoryCommodityList = ref([])
  // 分类请求参数
  const categoryRequestParams = ref({
    pageSize: 10,
    pageIndex: 1
  })

  /**
   * 重置分类商品页码
   */
  const resetCategoryPageParams = () => {
    categoryCommodityList.value = []
    categoryRequestParams.value.pageIndex = 1
  }

  /**
   * 获取商品列表数据
   * @param {*} userId 用户ID
   * @param {*} commodityType 商品类型
   */
  const getCommodityList = async (userId, commodityType, random = false) => {
    if (!commodityType) {
      if (loading.value || finished.value) return
    }

    loading.value = true
    try {
      let reqParams = commodityType ? categoryRequestParams.value : requestParams.value
      const { data } = await httpGet(apiConfig.commodity.getCommodityList, {
        ...reqParams,
        commodityType,
        userId,
        status: 1,
        random
      })
      if (data.code === 200) {
        if (commodityType) {
          categoryCommodityList.value = [...categoryCommodityList.value, ...data.data.items]
          categoryRequestParams.value.pageIndex++
        } else {
          commodityList.value = [...commodityList.value, ...data.data.items]
          total.value = data.data.total
          requestParams.value.pageIndex++

          if (commodityList.value.length >= total.value) {
            finished.value = true
          }
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

  /**
   * 刷新商品列表（换一批）
   */
  const refreshCommodityList = async () => {
    commodityList.value = []
    requestParams.value.pageIndex = 1
    finished.value = false
    await getCommodityList(null, null, true)
  }

  return {
    commodityList,
    total,
    loading,
    finished,
    categoryCommodityList,
    resetCategoryPageParams,
    getCommodityList,
    refreshCommodityList
  }
})
