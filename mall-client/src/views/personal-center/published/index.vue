<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { httpGet, httpPost } from '@/utils/request'
import apiConfig from '@/apis/api.config'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Top, Bottom, Delete } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const BASE_URL = import.meta.env.VITE_BASE_URL

const commodities = ref([])
const loading = ref(false)
const statusFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(5)
const total = ref(0)

const statusMap = {
  0: { text: '下架', type: 'info' },
  1: { text: '上架', type: 'success' },
  2: { text: '售出', type: 'danger' }
}

const getStatusInfo = (status) => {
  return statusMap[status] || { text: '未知', type: 'info' }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return dateStr.replace('T', ' ').substring(0, 16)
}

const getMainImage = (commodity) => {
  if (commodity.albums && commodity.albums.length > 0) {
    return commodity.albums[0].path
  }
  return ''
}

/**
 * 获取用户发布的商品列表
 */
const fetchCommodities = async () => {
  const userId = userStore.loginUser?.userId
  if (!userId) {
    ElMessage.warning('请先登录')
    return
  }

  loading.value = true
  try {
    const { data } = await httpGet(apiConfig.commodity.getCommodityList, {
      pageSize: pageSize.value,
      pageIndex: currentPage.value,
      userId,
      status: statusFilter.value || undefined
    })
    if (data.code === 200) {
      commodities.value = data.data?.items || []
      total.value = data.data?.total || 0
    } else {
      ElMessage.error(data.message || '获取商品列表失败')
    }
  } catch (error) {
    console.error('获取商品列表失败', error)
    ElMessage.error('获取商品列表失败')
  } finally {
    loading.value = false
  }
}

const goDetail = (commodityId) => {
  router.push(`/detail/${commodityId}`)
}

const toggleStatus = async (commodity) => {
  const targetStatus = commodity.status === 1 ? 0 : 1
  const actionText = targetStatus === 1 ? '上架' : '下架'
  try {
    await ElMessageBox.confirm(`确定要${actionText}该商品吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      customClass: 'confirm-box-custom'
    })
    const { data } = await httpPost(apiConfig.commodity.updateStatus, {
      commodityId: commodity.commodityId,
      status: targetStatus
    })
    if (data.code === 200) {
      commodity.status = targetStatus
      ElMessage.success(`${actionText}成功`)
    } else {
      ElMessage.error(data.message || `${actionText}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(`${actionText}失败`, error)
    }
  }
}

const deleteCommodity = async (commodity) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？删除后无法恢复', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      customClass: 'confirm-box-custom'
    })
    const { data } = await httpPost(apiConfig.commodity.delete, {
      commodityId: commodity.commodityId
    })
    if (data.code === 200) {
      ElMessage.success('删除成功')
      fetchCommodities()
    } else {
      ElMessage.error(data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
    }
  }
}

const handleFilterChange = () => {
  currentPage.value = 1
  fetchCommodities()
}

const handlePageChange = (page) => {
  currentPage.value = page
  fetchCommodities()
}

onMounted(async () => {
  if (!userStore.loginUser) {
    await userStore.getCurrLoginUser()
  }
  fetchCommodities()
})
</script>

<template>
  <div class="published_container">
    <div class="published_header">
      <h2>我的闲置</h2>
      <el-select v-model="statusFilter" placeholder="全部状态" clearable size="small" style="width: 130px"
        @change="handleFilterChange">
        <el-option label="全部" value="" />
        <el-option label="上架" value="1" />
        <el-option label="下架" value="0" />
        <el-option label="售出" value="2" />
      </el-select>
    </div>

    <div v-loading="loading" class="table_wrapper">
      <el-empty v-if="!loading && commodities.length === 0" description="暂无发布的商品" />

      <el-table v-else :data="commodities" stripe class="rounded_table" style="width: 100%"
        :header-cell-style="{ background: 'var(--primary-color)', color: '#333' }">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="商品图片" width="90">
          <template #default="{ row }">
            <img v-if="getMainImage(row)" :src="`${BASE_URL}${getMainImage(row)}`" class="commodity_thumb" />
          </template>
        </el-table-column>

        <el-table-column label="商品名称" min-width="180">
          <template #default="{ row }">
            <span class="commodity_name" @click="goDetail(row.commodityId)">
              {{ row.commodityName }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="价格" width="100" align="right">
          <template #default="{ row }">
            <span class="price_text">¥{{ row.price?.toFixed(2) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusInfo(row.status).type" size="small">
              {{ getStatusInfo(row.status).text }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="发布时间" width="160">
          <template #default="{ row }">
            <span class="time_text">{{ formatDate(row.createTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="160" align="center">
          <template #default="{ row }">
            <div class="action_btns">
              <el-tooltip v-if="row.status === 0" content="上架" placement="top">
                <el-button type="success" :icon="Top" circle size="small" @click="toggleStatus(row)" />
              </el-tooltip>
              <el-tooltip v-if="row.status === 1" content="下架" placement="top">
                <el-button type="danger" :icon="Bottom" circle size="small" @click="toggleStatus(row)" />
              </el-tooltip>
              <el-tooltip content="删除" placement="top">
                <el-button type="danger" :icon="Delete" circle size="small" @click="deleteCommodity(row)" />
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="total > pageSize" class="pagination_wrapper">
        <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[5, 10, 20, 50]"
          :total="total" layout="total, sizes, prev, pager, next, jumper" background @current-change="handlePageChange"
          @size-change="handlePageChange" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.published_container {
  width: 100%;
  box-sizing: border-box;
  padding: 20px;
}

.published_header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.published_header h2 {
  font-size: 18px;
  color: #333;
  margin: 0;
}

.table_wrapper {
  min-height: 300px;
}

.rounded_table {
  border-radius: 8px;
  overflow: hidden;
}

.pagination_wrapper {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.commodity_thumb {
  width: 56px;
  height: 56px;
  object-fit: cover;
  border-radius: 6px;
}

.commodity_name {
  color: #333;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: block;
  max-width: 260px;
}

.commodity_name:hover {
  color: #ff6700;
}

.price_text {
  font-weight: 500;
  color: #ff6700;
}

.time_text {
  font-size: 13px;
  color: #999;
}

.action_btns {
  display: flex;
  justify-content: center;
  gap: 8px;
}
</style>

<style>
.confirm-box-custom .el-button--primary {
   background-color: var(--primary-color);
   border-color: #000;
   color: #000;
 }
</style>
