<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { httpGet, httpPut } from '@/utils/request'
import apiConfig from '@/apis/api.config'

const BASE_URL = import.meta.env.VITE_BASE_URL

const orderNo = ref('')
const status = ref(null)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const loading = ref(false)

const statusOptions = [
  { value: 0, label: '待支付' },
  { value: 1, label: '已支付' },
  { value: 2, label: '已发货' },
  { value: 3, label: '已取消' },
  { value: 4, label: '已完成' }
]

const getStatusType = (status) => {
  switch (status) {
    case 0: return 'warning'
    case 1: return 'success'
    case 2: return 'primary'
    case 3: return 'info'
    case 4: return 'success'
    default: return ''
  }
}

const getStatusText = (status) => {
  const found = statusOptions.find(s => s.value === status)
  return found ? found.label : '未知'
}

const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return BASE_URL + path
}

const fetchOrderList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    if (orderNo.value.trim()) {
      params.orderNo = orderNo.value.trim()
    }
    if (status.value !== null) {
      params.status = status.value
    }
    const res = await httpGet(apiConfig.admin.orderList, params)
    const data = res.data
    if (data.code === 200) {
      tableData.value = data.data.items || []
      total.value = data.data.total || 0
    } else {
      ElMessage.error(data.msg || '查询失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchOrderList()
}

const handleReset = () => {
  orderNo.value = ''
  status.value = null
  currentPage.value = 1
  fetchOrderList()
}

const handleRefresh = () => {
  fetchOrderList()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  fetchOrderList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchOrderList()
}

const handleUpdateStatus = (row, newStatus) => {
  const statusText = getStatusText(newStatus)
  ElMessageBox.confirm(
    `确定将订单「${row.orderNo}」状态更改为「${statusText}」吗？`,
    '更新订单状态',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await httpPut(`${apiConfig.admin.updateOrderStatus}/${row.id}?status=${newStatus}`)
      const data = res.data
      if (data.code === 200) {
        ElMessage.success('状态更新成功')
        fetchOrderList()
      } else {
        ElMessage.error(data.msg || '更新失败')
      }
    } catch (e) {
      console.error(e)
      ElMessage.error('更新失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchOrderList()
})
</script>

<template>
  <div class="order-page">
    <!-- 标题 + 筛选区 -->
    <div class="filter-card">
      <div class="page-title-wrap">
        <h1 class="page-title">订单管理</h1>
        <p class="page-desc">管理和查看平台所有订单信息</p>
      </div>
      <div class="filter-form">
        <div class="filter-item">
          <label class="filter-label">订单号</label>
          <el-input
            v-model="orderNo"
            placeholder="搜索订单号"
            clearable
            class="filter-input"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
        <div class="filter-item">
          <label class="filter-label">订单状态</label>
          <el-select v-model="status" placeholder="全部状态" class="filter-input" clearable>
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </div>
        <div class="filter-actions">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button class="btn-reset" @click="handleReset">重置</el-button>
        </div>
      </div>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <span class="toolbar-title">订单列表</span>
      </div>
      <div class="toolbar-right">
        <el-button class="btn-refresh" circle @click="handleRefresh" title="刷新">
          <el-icon><Refresh /></el-icon>
        </el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="table-card">
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="70">
          <template #default="{ row }">
            <span class="order-id">{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="订单号" width="180">
          <template #default="{ row }">
            <span class="order-no">{{ row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="商品信息" min-width="220">
          <template #default="{ row }">
            <div class="commodity-cell">
              <img
                v-if="row.commodityImage"
                :src="getImageUrl(row.commodityImage)"
                alt="商品图片"
                class="commodity-image"
              />
              <div class="commodity-info">
                <div class="commodity-name">{{ row.commodityName }}</div>
                <div class="commodity-price">¥{{ row.price }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="买家信息" min-width="180">
          <template #default="{ row }">
            <div class="buyer-info">
              <div class="buyer-name">{{ row.consignee || '-' }}</div>
              <div class="buyer-phone">{{ row.phone || '-' }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="订单金额" width="100">
          <template #default="{ row }">
            <span class="amount-text">¥{{ row.totalAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="订单状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="plain" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button
                v-if="row.status === 0"
                type="success"
                link
                @click="handleUpdateStatus(row, 1)"
              >
                确认支付
              </el-button>
              <el-button
                v-if="row.status === 1"
                type="primary"
                link
                @click="handleUpdateStatus(row, 2)"
              >
                确认发货
              </el-button>
              <el-button
                v-if="row.status === 2"
                type="success"
                link
                @click="handleUpdateStatus(row, 4)"
              >
                确认完成
              </el-button>
              <el-button
                v-if="row.status === 0"
                type="danger"
                link
                @click="handleUpdateStatus(row, 3)"
              >
                取消订单
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-area">
      <div class="pagination-info">共 {{ total }} 条数据</div>
      <el-pagination
        background
        layout="prev, pager, next, sizes, jumper"
        :total="total"
        :current-page="currentPage"
        :page-size="pageSize"
        :page-sizes="[20, 50, 100]"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<style scoped>
.order-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 筛选卡片 */
.filter-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.page-title-wrap {
  margin-bottom: 20px;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 6px 0;
}

.page-desc {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

.filter-form {
  display: flex;
  gap: 16px;
  align-items: flex-end;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.filter-label {
  font-size: 13px;
  color: #909399;
  font-weight: 500;
}

.filter-input {
  width: 200px;
}

.filter-actions {
  margin-left: auto;
  display: flex;
  gap: 12px;
}

:deep(.filter-form .el-button--primary) {
  background: #ffe60f;
  border-color: #ffe60f;
  color: #1a1a2e;
}

:deep(.filter-form .el-button--primary:hover) {
  background: #ffd700;
  border-color: #ffd700;
  color: #1a1a2e;
}

:deep(.btn-reset) {
  background: #fffbeb !important;
  border-color: #ffe60f !important;
  color: #d4b800 !important;
}

:deep(.btn-reset:hover) {
  background: #fff3b0 !important;
  border-color: #ffd700 !important;
  color: #b89a00 !important;
}

:deep(.btn-refresh) {
  background: #fffbeb !important;
  border-color: #ffe60f !important;
  color: #d4b800 !important;
}

:deep(.btn-refresh:hover) {
  background: #fff3b0 !important;
  border-color: #ffd700 !important;
  color: #b89a00 !important;
}

/* 工具栏 */
.toolbar {
  background: #ffffff;
  border-radius: 12px;
  padding: 16px 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.toolbar-left {
  display: flex;
  gap: 12px;
}

.toolbar-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.toolbar-right {
  display: flex;
  gap: 8px;
  align-items: center;
}

/* 表格卡片 */
.table-card {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

:deep(.el-table) {
  --el-table-tr-bg-color: #ffffff;
  --el-table-header-bg-color: #fafafa;
  --el-table-border-color: #f2f6fc;
  --el-table-tr-hover-bg-color: #fffbeb;
  --el-table-tr-stripe-bg-color: #fafafa;
  --el-table-text-color: #606266;
  --el-table-header-text-color: #303133;
}

:deep(.el-table th.el-table__cell) {
  background: #fafafa;
}

:deep(.el-table td.el-table__cell) {
  border-bottom: 1px solid #f2f6fc;
}

:deep(.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell) {
  background: #fafafa;
}

:deep(.el-table__body tr:hover > td.el-table__cell) {
  background: #fffbeb !important;
}

:deep(.el-table .cell) {
  padding-left: 16px;
  padding-right: 16px;
}

.order-id {
  font-family: 'Menlo', 'Monaco', 'Consolas', monospace;
  font-size: 13px;
  color: #909399;
}

.order-no {
  font-family: 'Menlo', 'Monaco', 'Consolas', monospace;
  font-size: 13px;
  color: #303133;
}

.commodity-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.commodity-image {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
  flex-shrink: 0;
}

.commodity-info {
  min-width: 0;
}

.commodity-name {
  font-weight: 500;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.commodity-price {
  font-size: 13px;
  color: #ef4444;
  margin-top: 4px;
}

.buyer-info {
  min-width: 0;
}

.buyer-name {
  color: #303133;
}

.buyer-phone {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.amount-text {
  color: #ef4444;
  font-weight: 600;
}

.action-btns {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

:deep(.el-button--primary.is-link) {
  color: #d4b800;
}

:deep(.el-button--primary.is-link:hover) {
  color: #e6c800;
}

:deep(.el-button--success.is-link) {
  color: #67c23a;
}

:deep(.el-button--danger.is-link) {
  color: #f56c6c;
}

/* 分页 */
.pagination-area {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
}

.pagination-info {
  font-size: 14px;
  color: #909399;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background-color: #ffe60f !important;
  color: #1a1a2e !important;
  border-color: #ffe60f !important;
}

:deep(.el-pagination.is-background .btn-prev),
:deep(.el-pagination.is-background .btn-next),
:deep(.el-pagination.is-background .el-pager li) {
  border-radius: 8px;
  margin: 0 4px;
  color: #606266;
}

:deep(.el-pagination.is-background .el-pager li:hover) {
  color: #d4b800;
}

:deep(.el-pagination__sizes .el-input .el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-pagination__jump .el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-pagination__jump .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(255, 230, 15, 0.1);
  border-color: #ffe60f;
}
</style>