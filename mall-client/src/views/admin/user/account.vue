<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  Plus
} from '@element-plus/icons-vue'
import { httpGet, httpDelete, httpPut } from '@/utils/request'
import apiConfig from '@/apis/api.config'

const BASE_URL = import.meta.env.VITE_BASE_URL

const keyword = ref('')
const role = ref(null)
const roleList = ref([])
const userList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const loading = ref(false)

const avatarColors = [
  'linear-gradient(135deg, #ffe60f, #d4b800)',
  'linear-gradient(135deg, #67c23a, #529b2e)',
  'linear-gradient(135deg, #e6a23c, #c0842c)',
  'linear-gradient(135deg, #f56c6c, #d44e4e)',
  'linear-gradient(135deg, #909399, #606266)',
  'linear-gradient(135deg, #722ed1, #531dab)',
  'linear-gradient(135deg, #13c2c2, #08979c)',
  'linear-gradient(135deg, #ff7a45, #d4380d)'
]

const getAvatarStyle = (userId) => {
  const index = userId % avatarColors.length
  return {
    background: avatarColors[index]
  }
}

const getAvatarChar = (nickname) => {
  if (!nickname) return '用'
  return nickname.charAt(0)
}

const getAvatarUrl = (avatar) => {
  if (!avatar) return ''
  return avatar.startsWith('http') ? avatar : BASE_URL + avatar
}

const getRoleText = (roleId) => {
  const found = roleList.value.find(r => r.roleId === roleId)
  return found ? found.roleName : '未知'
}

const getRoleTagType = (roleId) => {
  return roleId === 1 ? 'warning' : 'info'
}

const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

const fetchRoleList = async () => {
  try {
    const res = await httpGet(apiConfig.admin.userRoles)
    const data = res.data
    if (data.code === 200) {
      roleList.value = data.data || []
    }
  } catch (e) {
    console.error(e)
  }
}

const fetchUserList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    if (keyword.value.trim()) {
      params.keyword = keyword.value.trim()
    }
    if (role.value != null) {
      params.role = role.value
    }
    const res = await httpGet(apiConfig.admin.userList, params)
    const data = res.data
    if (data.code === 200) {
      userList.value = data.data.items || []
      total.value = data.data.total || 0
    } else {
      ElMessage.error(data.message || '获取用户列表失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchUserList()
}

const handleReset = () => {
  keyword.value = ''
  role.value = null
  currentPage.value = 1
  fetchUserList()
}

const handleRefresh = () => {
  fetchUserList()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  fetchUserList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchUserList()
}

const handleResetPassword = async (row) => {
  ElMessageBox.confirm(
    `确定要将用户「${row.nickname}」的密码重置为 usedcat 吗？`,
    '重置密码',
    {
      confirmButtonText: '确定重置',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await httpPut(`${apiConfig.admin.resetPassword}/${row.userId}`)
      const data = res.data
      if (data.code === 200) {
        ElMessage.success('密码重置成功')
      } else {
        ElMessage.error(data.message || '重置失败')
      }
    } catch (e) {
      console.error(e)
      ElMessage.error('重置失败')
    }
  }).catch(() => {})
}

const handleDelete = async (row) => {
  if (row.role === 1) {
    ElMessage.warning('不能删除管理员账户')
    return
  }
  ElMessageBox.confirm(
    `确定要删除用户「${row.nickname}」吗？此操作不可恢复。`,
    '删除用户',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'error'
    }
  ).then(async () => {
    try {
      const res = await httpDelete(`${apiConfig.admin.deleteUser}/${row.userId}`)
      const data = res.data
      if (data.code === 200) {
        ElMessage.success('删除成功')
        fetchUserList()
      } else {
        ElMessage.error(data.message || '删除失败')
      }
    } catch (e) {
      console.error(e)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchRoleList()
  fetchUserList()
})
</script>

<template>
  <div class="user-account-page">
    <!-- 标题 + 筛选区 -->
    <div class="filter-card">
      <div class="page-title-wrap">
        <h1 class="page-title">账户管理</h1>
        <p class="page-desc">管理和查看系统中所有用户信息</p>
      </div>
      <div class="filter-form">
        <div class="filter-item">
          <label class="filter-label">关键字</label>
          <el-input
            v-model="keyword"
            placeholder="搜索昵称/邮箱"
            clearable
            class="filter-keyword-input"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
        <div class="filter-item">
          <label class="filter-label">角色</label>
          <el-select v-model="role" placeholder="全部角色" class="filter-select" clearable>
            <el-option
              v-for="item in roleList"
              :key="item.roleId"
              :label="item.roleName"
              :value="item.roleId"
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
        <el-button type="primary">
          <el-icon><Plus /></el-icon>
          新增用户
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
      :data="userList"
      v-loading="loading"
      stripe
      style="width: 100%"
    >
      <el-table-column prop="userId" label="ID" width="80">
        <template #default="{ row }">
          <span class="user-id">{{ row.userId }}</span>
        </template>
      </el-table-column>
      <el-table-column label="用户信息" min-width="240">
        <template #default="{ row }">
          <div class="user-cell">
            <el-avatar v-if="getAvatarUrl(row.avatar)" :src="getAvatarUrl(row.avatar)" :size="36" />
            <div v-else class="user-avatar-sm" :style="getAvatarStyle(row.userId)">
              {{ getAvatarChar(row.nickname) }}
            </div>
            <div class="user-cell-info">
              <div class="user-name">{{ row.nickname }}</div>
              <div class="user-email">{{ row.email }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="角色" width="120">
        <template #default="{ row }">
          <el-tag :type="getRoleTagType(row.role)" effect="plain" size="small">
            {{ getRoleText(row.role) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="信誉分" width="100">
        <template #default="{ row }">
          <span class="credit-text">{{ row.credit || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="注册时间" width="180">
        <template #default="{ row }">
          {{ formatTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <div class="action-btns">
            <el-button type="primary" link @click="handleResetPassword(row)">
              重置密码
            </el-button>
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
.user-account-page {
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

.filter-select {
  width: 180px;
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

:deep(.filter-form .el-button--default) {
  background: #fffbeb;
  border-color: #ffe60f;
  color: #d4b800;
}

:deep(.filter-form .el-button--default:hover) {
  background: #ffe60f;
  border-color: #ffe60f;
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

.user-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar-sm {
  width: 36px;
  height: 36px;
  border-radius: 9999px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 500;
  color: #ffffff;
  flex-shrink: 0;
}

.user-cell-info {
  min-width: 0;
}

.user-name {
  font-weight: 500;
  color: #303133;
}

.user-email {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.user-id {
  font-family: 'Menlo', 'Monaco', 'Consolas', monospace;
  font-size: 13px;
  color: #909399;
}

.credit-text {
  color: #d4b800;
  font-weight: 500;
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
</style>
