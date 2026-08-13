<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { httpGet, httpPost, httpPut, httpDelete } from '@/utils/request'
import apiConfig from '@/apis/api.config'

const BASE_URL = import.meta.env.VITE_BASE_URL

const keyword = ref('')
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const loading = ref(false)

const dialogVisible = ref(false)
const dialogTitle = ref('新增分类')
const formRef = ref(null)

const formData = reactive({
  typeId: null,
  typeName: '',
  typeDesc: '',
  img: '',
  color: '',
  en: ''
})

const rules = {
  typeName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return BASE_URL + path
}

const convertToHex = (color) => {
  if (!color) return color
  if (color.startsWith('#')) return color
  const rgbMatch = color.match(/rgba?\((\d+),\s*(\d+),\s*(\d+)/)
  if (rgbMatch) {
    const r = parseInt(rgbMatch[1])
    const g = parseInt(rgbMatch[2])
    const b = parseInt(rgbMatch[3])
    return '#' + [r, g, b].map(x => x.toString(16).padStart(2, '0')).join('')
  }
  return color
}

const fetchCategoryList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    if (keyword.value.trim()) {
      params.keyword = keyword.value.trim()
    }
    const res = await httpGet(apiConfig.admin.categoryList, params)
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
  fetchCategoryList()
}

const handleReset = () => {
  keyword.value = ''
  currentPage.value = 1
  fetchCategoryList()
}

const handleRefresh = () => {
  fetchCategoryList()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  fetchCategoryList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchCategoryList()
}

const handleAdd = () => {
  dialogTitle.value = '新增分类'
  formData.typeId = null
  formData.typeName = ''
  formData.typeDesc = ''
  formData.img = ''
  formData.color = ''
  formData.en = ''
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑分类'
  formData.typeId = row.typeId
  formData.typeName = row.typeName
  formData.typeDesc = row.typeDesc || ''
  formData.img = row.img || ''
  formData.color = convertToHex(row.color) || ''
  formData.en = row.en || ''
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    formData.color = convertToHex(formData.color) || ''
    const isAdd = formData.typeId == null
    const res = isAdd
      ? await httpPost(apiConfig.admin.addCategory, formData)
      : await httpPut(apiConfig.admin.updateCategory, formData)
    const data = res.data
    if (data.code === 200) {
      ElMessage.success(isAdd ? '新增成功' : '更新成功')
      dialogVisible.value = false
      fetchCategoryList()
    } else {
      ElMessage.error(data.msg || '操作失败')
    }
  } catch (e) {
    if (e !== false) {
      console.error(e)
      ElMessage.error('操作失败')
    }
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除分类「${row.typeName}」吗？此操作不可恢复。`,
    '删除分类',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'error'
    }
  ).then(async () => {
    try {
      const res = await httpDelete(`${apiConfig.admin.deleteCategory}/${row.typeId}`)
      const data = res.data
      if (data.code === 200) {
        ElMessage.success('删除成功')
        fetchCategoryList()
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
  fetchCategoryList()
})
</script>

<template>
  <div class="category-page">
    <!-- 标题 + 筛选区 -->
    <div class="filter-card">
      <div class="page-title-wrap">
        <h1 class="page-title">商品分类管理</h1>
        <p class="page-desc">管理和查看平台所有商品分类信息</p>
      </div>
      <div class="filter-form">
        <div class="filter-item">
          <label class="filter-label">关键字</label>
          <el-input
            v-model="keyword"
            placeholder="搜索分类名称"
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
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增分类
        </el-button>
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
        <el-table-column prop="typeId" label="ID" width="80">
          <template #default="{ row }">
            <span class="category-id">{{ row.typeId }}</span>
          </template>
        </el-table-column>
        <el-table-column label="图标" width="100">
          <template #default="{ row }">
            <div class="image-wrap" v-if="row.img">
              <img :src="getImageUrl(row.img)" alt="分类图标" class="category-image" />
            </div>
            <span v-else class="no-image">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="typeName" label="分类名称" min-width="140" />
        <el-table-column prop="typeDesc" label="描述" min-width="180">
          <template #default="{ row }">
            <span class="desc-text">{{ row.typeDesc || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="主题色" width="120">
          <template #default="{ row }">
            <div class="color-wrap" v-if="row.color">
              <span class="color-dot" :style="{ background: row.color }"></span>
              <span class="color-value">{{ row.color }}</span>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="en" label="英文标识" width="120">
          <template #default="{ row }">
            {{ row.en || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
              <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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

    <!-- 新增/编辑 对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="分类名称" prop="typeName">
          <el-input v-model="formData.typeName" placeholder="请输入分类名称" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="分类描述">
          <el-input v-model="formData.typeDesc" placeholder="请输入分类描述" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="图标路径">
          <el-input v-model="formData.img" placeholder="请输入图标图片路径" />
        </el-form-item>
        <el-form-item label="主题色">
          <el-color-picker v-model="formData.color" color-format="hex" />
        </el-form-item>
        <el-form-item label="英文标识">
          <el-input v-model="formData.en" placeholder="请输入英文标识" maxlength="20" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.category-page {
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

.toolbar-right {
  display: flex;
  gap: 8px;
  align-items: center;
}

:deep(.toolbar .el-button--primary) {
  background: #ffe60f;
  border-color: #ffe60f;
  color: #1a1a2e;
}

:deep(.toolbar .el-button--primary:hover) {
  background: #ffd700;
  border-color: #ffd700;
  color: #1a1a2e;
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

.category-id {
  font-family: 'Menlo', 'Monaco', 'Consolas', monospace;
  font-size: 13px;
  color: #909399;
}

.image-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 50px;
}

.category-image {
  width: 50px;
  height: 50px;
  object-fit: contain;
}

.no-image {
  color: #94a3b8;
  font-size: 12px;
}

.desc-text {
  font-size: 13px;
  color: #606266;
}

.color-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}

.color-dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 1px solid #e5e7eb;
}

.color-value {
  font-family: 'Menlo', 'Monaco', 'Consolas', monospace;
  font-size: 12px;
  color: #606266;
}

.action-btns {
  display: flex;
  gap: 4px;
}

:deep(.el-button--primary.is-link) {
  color: #d4b800;
}

:deep(.el-button--primary.is-link:hover) {
  color: #e6c800;
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

/* 对话框 */
:deep(.el-dialog) {
  border-radius: 12px;
}

:deep(.el-dialog__header) {
  padding: 20px 24px 16px;
}

:deep(.el-dialog__title) {
  font-weight: 600;
  color: #303133;
}

:deep(.el-dialog__body) {
  padding: 0 24px 24px;
}

:deep(.el-dialog__footer) {
  padding: 16px 24px 20px;
  border-top: 1px solid #f2f6fc;
}

:deep(.el-dialog .el-button--primary) {
  background: #ffe60f;
  border-color: #ffe60f;
  color: #1a1a2e;
}

:deep(.el-dialog .el-button--primary:hover) {
  background: #ffd700;
  border-color: #ffd700;
  color: #1a1a2e;
}
</style>
