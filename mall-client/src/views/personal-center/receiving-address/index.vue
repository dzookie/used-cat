<script setup>
import { ref, onMounted, reactive, nextTick } from 'vue'
import { httpGet, httpPost, httpPut, httpDelete } from '@/utils/request'
import apiConfig from '@/apis/api.config'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import AddressCard from '@/components/AddressCard.vue'

const userStore = useUserStore()
const addressList = ref([])
const loading = ref(false)

const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const submitting = ref(false)

const formData = reactive({
  consignee: '',
  phone: '',
  region: '',
  address: ''
})

const rules = {
  consignee: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  region: [
    { required: true, message: '请输入所在地区', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入详细地址', trigger: 'blur' }
  ]
}

/**
 * 获取收货地址列表
 */
const fetchAddressList = async () => {
  const userId = userStore.loginUser?.userId
  if (!userId) return
  
  loading.value = true
  try {
    const { data } = await httpGet(apiConfig.receivingAddress.getReceivingAddressList, { userId })
    if (data.code === 200) {
      addressList.value = data.data || []
    } else {
      ElMessage.error(data.message || '获取收货地址失败')
    }
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

const resetFormData = () => {
  formData.consignee = ''
  formData.phone = ''
  formData.region = ''
  formData.address = ''
}

const handleOpenDialog = () => {
  resetFormData()
  editingId.value = null
  dialogVisible.value = true
}

const handleEdit = (address) => {
  resetFormData()
  editingId.value = address.id
  formData.consignee = address.consignee
  formData.phone = address.phone
  formData.region = address.region
  formData.address = address.address
  dialogVisible.value = true
}

const handleCloseDialog = () => {
  dialogVisible.value = false
  editingId.value = null
  nextTick(() => {
    formData.consignee = ''
    formData.phone = ''
    formData.region = ''
    formData.address = ''
    formRef.value?.resetFields()
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      let res
      if (editingId.value) {
        res = await httpPut(apiConfig.receivingAddress.updateReceivingAddress, {
          id: editingId.value,
          userId: userStore.loginUser.userId,
          consignee: formData.consignee,
          phone: formData.phone,
          region: formData.region,
          address: formData.address
        })
      } else {
        res = await httpPost(apiConfig.receivingAddress.addReceivingAddress, {
          userId: userStore.loginUser.userId,
          consignee: formData.consignee,
          phone: formData.phone,
          region: formData.region,
          address: formData.address
        })
      }
      
      if (res.data.code === 200) {
        ElMessage.success(editingId.value ? '修改地址成功' : '新增地址成功')
        handleCloseDialog()
        fetchAddressList()
      } else {
        ElMessage.error(res.data.message || '操作失败')
      }
    } catch (error) {
      ElMessage.error(error.message)
    } finally {
      submitting.value = false
    }
  })
}

const handleDelete = async (address) => {
  try {
    await ElMessageBox.confirm('确定要删除该收货地址吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const { data } = await httpDelete(apiConfig.receivingAddress.deleteReceivingAddress, { id: address.id })
    if (data.code === 200) {
      ElMessage.success('删除成功')
      fetchAddressList()
    } else {
      ElMessage.error(data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

onMounted(() => {
  fetchAddressList()
})
</script>

<template>
  <div class="address_container">
    <div class="address_header">
      <h2 class="address_title">收货地址管理</h2>
      <a href="#" class="manage_link" @click.prevent="handleOpenDialog">新增地址</a>
    </div>

    <div v-if="loading" class="loading_wrapper">
      <span>加载中...</span>
    </div>

    <el-empty v-else-if="addressList.length === 0" description="暂无收货地址" />

    <div v-else class="address_list">
      <AddressCard
        v-for="item in addressList"
        :key="item.id"
        :address="item"
        :show-delete="true"
        :show-edit="true"
        @edit="handleEdit"
        @delete="handleDelete"
      />
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '修改收货地址' : '新增收货地址'"
      width="500px"
      :close-on-click-modal="false"
      @close="handleCloseDialog"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="收货人" prop="consignee">
          <el-input v-model="formData.consignee" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号码" />
        </el-form-item>
        <el-form-item label="所在地区" prop="region">
          <el-input v-model="formData.region" placeholder="请输入所在地区" />
        </el-form-item>
        <el-form-item label="详细地址" prop="address">
          <el-input
            v-model="formData.address"
            type="textarea"
            :rows="3"
            placeholder="请输入详细地址"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleCloseDialog">取消</el-button>
        <el-button class="submit_btn" type="primary" :loading="submitting" @click="handleSubmit">
          提交
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.address_container {
  padding: 24px;
}

.submit_btn{
  background-color: var(--primary-color);
  color: #000;
  border: 1px solid #000;
}

.address_header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.address_title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.manage_link {
  color: #333;
  text-decoration: none;
  font-size: 14px;
}

.manage_link:hover {
  color: #ff6700;
}

.loading_wrapper {
  padding: 40px;
  text-align: center;
  color: #999;
  font-size: 14px;
}

.address_list {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}
</style>
