<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { httpGet, httpDelete } from '@/utils/request'
import apiConfig from '@/apis/api.config'

const BASE_URL = import.meta.env.VITE_BASE_URL

const keyword = ref('')
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const loading = ref(false)

const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return BASE_URL + path
}

const getStatusType = (status) => {
  switch (status) {
    case 0: return 'info'
    case 1: return 'success'
    case 2: return 'warning'
    default: return ''
  }
}

const getStatusText = (status) => {
  switch (status) {
    case 0: return '下架'
    case 1: return '上架'
    case 2: return '售出'
    default: return '未知'
  }
}

const fetchCommodityList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    if (keyword.value.trim()) {
      params.keyword = keyword.value.trim()
    }
    const res = await httpGet(apiConfig.admin.commodityList, params)
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
  fetchCommodityList()
}

const handleReset = () => {
  keyword.value = ''
  currentPage.value = 1
  fetchCommodityList()
}

const handleRefresh = () => {
  fetchCommodityList()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  fetchCommodityList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchCommodityList()
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除商品「${row.commodityName}」吗？此操作不可恢复。`,
    '删除商品',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'error'
    }
  ).then(async () => {
    try {
      const res = await httpDelete(`${apiConfig.admin.deleteCommodity}/${row.commodityId}`)
      const data = res.data
      if (data.code === 200) {
        ElMessage.success('删除成功')
        fetchCommodityList()
      } else {
        ElMessage.error(data.msg || '删除失败')
      }
    } catch (e) {
      console.error(e)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchCommodityList()
})
</script>

<template>
  <div class="commodity-page">
    <!-- 标题 + 筛选区 -->
    <div class="filter-card">
      <div class="page-title-wrap">
        <h1 class="page-title">二手商品管理</h1>
        <p class="page-desc">管理和查看平台所有二手商品信息</p>
      </div>
      <div class="filter-form">
        <div class="filter-item">
          <label class="filter-label">关键字</label>
          <el-input
            v-model="keyword"
            placeholder="搜索商品名称/描述"
            clearable
            class="filter-keyword-input"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
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
        <span class="toolbar-title">商品列表</span>
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
        <el-table-column prop="commodityId" label="ID" width="80">
          <template #default="{ row }">
            <span class="commodity-id">{{ row.commodityId }}</span>
          </template>
        </el-table-column>
        <el-table-column label="商品图片" width="100">
          <template #default="{ row }">
            <div class="image-wrap" v-if="row.albums && row.albums.length > 0">
              <el-image
                :src="getImageUrl(row.albums[0].path)"
                :preview-src-list="row.albums.map(a => getImageUrl(a.path))"
                preview-teleported
                fit="cover"
                class="commodity-image"
                :initial-index="0"
                hide-on-click-modal
              />
            </div>
            <span v-else class="no-image">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="commodityName" label="商品名称" min-width="200" />
        <el-table-column prop="commodityDesc" label="描述" min-width="200">
          <template #default="{ row }">
            <el-tooltip
              :content="row.commodityDesc || '-'"
              placement="top"
              :show-after="300"
              effect="dark"
            >
              <span class="desc-text">{{ row.commodityDesc }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="brand" label="品牌" width="100" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            <span class="price-text">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="plain" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="browse" label="浏览量" width="80" />
        <el-table-column label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button type="danger" link @click="handleDelete(row)">
                删除
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
.commodity-page {
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

.filter-actions {
  margin-left: auto;
  display: flex;
  gap: 12px;
}

.filter-keyword-input {
  width: 280px;
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

.commodity-id {
  font-family: 'Menlo', 'Monaco', 'Consolas', monospace;
  font-size: 13px;
  color: #909399;
}

.image-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 60px;
}

.commodity-image {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  cursor: pointer;
}

.no-image {
  color: #94a3b8;
  font-size: 12px;
}

.desc-text {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-size: 13px;
  color: #606266;
}

.price-text {
  color: #ef4444;
  font-weight: 600;
}

.action-btns {
  display: flex;
  gap: 4px;
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
