<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox, ElForm, ElFormItem, ElInput } from 'element-plus'
import {
  Refresh,
  Plus,
  Edit,
  Delete
} from '@element-plus/icons-vue'
import { httpGet, httpPost, httpDelete, httpPut } from '@/utils/request'
import apiConfig from '@/apis/api.config'

const roleList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const loading = ref(false)

const showAddDialog = ref(false)
const showEditDialog = ref(false)
const formRef = ref(null)
const editFormRef = ref(null)

const addForm = ref({
  roleName: ''
})

const editForm = ref({
  roleId: null,
  roleName: ''
})

const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

const fetchRoleList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    const res = await httpGet(apiConfig.admin.roleList, params)
    const data = res.data
    if (data.code === 200) {
      roleList.value = data.data.items || []
      total.value = data.data.total || 0
    } else {
      ElMessage.error(data.message || '获取角色列表失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('获取角色列表失败')
  } finally {
    loading.value = false
  }
}

const handleRefresh = () => {
  fetchRoleList()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  fetchRoleList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchRoleList()
}

const handleAdd = () => {
  addForm.value = { roleName: '' }
  showAddDialog.value = true
}

const handleEdit = (row) => {
  editForm.value = {
    roleId: row.roleId,
    roleName: row.roleName
  }
  showEditDialog.value = true
}

const handleDelete = async (row) => {
  if (row.roleId === 1 || row.roleId === 2) {
    ElMessage.warning('系统默认角色不能删除')
    return
  }
  ElMessageBox.confirm(
    `确定要删除角色「${row.roleName}」吗？此操作不可恢复。`,
    '删除角色',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'error'
    }
  ).then(async () => {
    try {
      const res = await httpDelete(`${apiConfig.admin.deleteRole}/${row.roleId}`)
      const data = res.data
      if (data.code === 200) {
        ElMessage.success('删除成功')
        fetchRoleList()
      } else {
        ElMessage.error(data.message || '删除失败')
      }
    } catch (e) {
      console.error(e)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const handleAddSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await httpPost(apiConfig.admin.addRole, addForm.value)
        const data = res.data
        if (data.code === 200) {
          ElMessage.success('新增成功')
          showAddDialog.value = false
          fetchRoleList()
        } else {
          ElMessage.error(data.message || '新增失败')
        }
      } catch (e) {
        console.error(e)
        ElMessage.error('新增失败')
      }
    }
  })
}

const handleEditSubmit = async () => {
  if (!editFormRef.value) return
  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await httpPut(`${apiConfig.admin.updateRole}/${editForm.value.roleId}`, editForm.value)
        const data = res.data
        if (data.code === 200) {
          ElMessage.success('更新成功')
          showEditDialog.value = false
          fetchRoleList()
        } else {
          ElMessage.error(data.message || '更新失败')
        }
      } catch (e) {
        console.error(e)
        ElMessage.error('更新失败')
      }
    }
  })
}

onMounted(() => {
  fetchRoleList()
})
</script>

<template>
  <div class="role-page">
    <div class="filter-card">
      <div class="page-title-wrap">
        <h1 class="page-title">角色管理</h1>
        <p class="page-desc">管理和查看系统中所有角色信息</p>
      </div>
    </div>

    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增角色
        </el-button>
      </div>
      <div class="toolbar-right">
        <el-button class="btn-refresh" circle @click="handleRefresh" title="刷新">
          <el-icon><Refresh /></el-icon>
        </el-button>
      </div>
    </div>

    <div class="table-card">
      <el-table
        :data="roleList"
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="roleId" label="ID" width="80">
          <template #default="{ row }">
            <span class="role-id">{{ row.roleId }}</span>
          </template>
        </el-table-column>
        <el-table-column label="角色名称">
          <template #default="{ row }">
            <div class="role-name-wrap">
              <span class="role-name">{{ row.roleName }}</span>
              <el-tag v-if="row.roleId === 1" type="warning" size="small" class="default-tag">系统管理员</el-tag>
              <el-tag v-else-if="row.roleId === 2" type="info" size="small" class="default-tag">普通用户</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="创建人" width="250">
          <template #default="{ row }">
            {{ row.creator || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="250">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
              <el-button type="danger" link @click="handleDelete(row)" :disabled="row.roleId === 1 || row.roleId === 2">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

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

    <el-dialog v-model="showAddDialog" title="新增角色" width="400px" destroy-on-close>
      <el-form ref="formRef" :model="addForm" :rules="{
        roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
      }">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="addForm.roleName" placeholder="请输入角色名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAddSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showEditDialog" title="编辑角色" width="400px" destroy-on-close>
      <el-form ref="editFormRef" :model="editForm" :rules="{
        roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
      }">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="editForm.roleName" placeholder="请输入角色名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="handleEditSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.role-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.page-title-wrap {
  margin-bottom: 0;
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

.role-id {
  font-family: 'Menlo', 'Monaco', 'Consolas', monospace;
  font-size: 13px;
  color: #909399;
}

.role-name-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}

.role-name {
  font-weight: 500;
  color: #303133;
}

.default-tag {
  flex-shrink: 0;
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

:deep(.el-button--danger.is-link.is-disabled) {
  color: #c0c4cc;
  cursor: not-allowed;
}

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

:deep(.el-dialog__header) {
  border-bottom: 1px solid #f2f6fc;
}

:deep(.el-dialog__body) {
  padding: 24px;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid #f2f6fc;
}

:deep(.el-dialog__footer .el-button--primary) {
  background: #ffe60f;
  border-color: #ffe60f;
  color: #1a1a2e;
}

:deep(.el-dialog__footer .el-button--primary:hover) {
  background: #ffd700;
  border-color: #ffd700;
  color: #1a1a2e;
}
</style>