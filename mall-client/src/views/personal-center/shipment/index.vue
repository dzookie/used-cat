<script setup>
import { ref, onMounted } from 'vue'
import { httpGet, httpPut } from '@/utils/request'
import apiConfig from '@/apis/api.config'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const BASE_URL = import.meta.env.VITE_BASE_URL

const orders = ref([])
const loading = ref(false)

const expressCompanies = [
  { code: 'zhongtong', name: '中通快递' },
  { code: 'yuantong', name: '圆通快递' },
  { code: 'shentong', name: '申通快递' },
  { code: 'yunda', name: '韵达快递' },
  { code: 'shunfeng', name: '顺丰速运' },
  { code: 'ems', name: 'EMS' },
  { code: 'jd', name: '京东物流' },
  { code: 'jtexpress', name: '极兔速递' }
]

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return dateStr.replace('T', ' ').substring(0, 19)
}

const fetchOrders = async () => {
  const userId = userStore.loginUser?.userId
  if (!userId) {
    ElMessage.warning('请先登录')
    return
  }

  loading.value = true
  try {
    const { data } = await httpGet(apiConfig.order.sellerList, { userId })
    if (data.code === 200) {
      orders.value = data.data || []
    } else {
      ElMessage.error(data.msg || '获取订单失败')
    }
  } catch (error) {
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

const shipDialogVisible = ref(false)
const currentOrder = ref(null)
const shipForm = ref({
  expressCompany: 'zhongtong',
  expressNo: '79106899724817'
})

const formRef = ref(null)
const formRules = {
  expressCompany: [{ required: true, message: '请选择快递公司', trigger: 'change' }],
  expressNo: [
    { required: true, message: '请输入快递单号', trigger: 'blur' },
    { min: 5, max: 32, message: '快递单号长度在 5 到 32 位之间', trigger: 'blur' }
  ]
}

const submitting = ref(false)

const openShipDialog = (order) => {
  currentOrder.value = order
  shipForm.value = {
    expressCompany: 'zhongtong',
    expressNo: '79106899724817'
  }
  shipDialogVisible.value = true
}

const closeShipDialog = () => {
  shipDialogVisible.value = false
  currentOrder.value = null
  formRef.value?.resetFields()
}

const handleShip = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    const { data } = await httpPut(apiConfig.order.ship, {
      orderId: currentOrder.value.id,
      expressNo: shipForm.value.expressNo.trim(),
      expressCompany: shipForm.value.expressCompany
    })
    if (data.code === 200) {
      ElMessage.success('发货成功')
      closeShipDialog()
      await fetchOrders()
    } else {
      ElMessage.error(data.msg || '发货失败')
    }
  } catch (error) {
    ElMessage.error('发货失败')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  if (!userStore.loginUser) {
    await userStore.getCurrLoginUser()
  }
  fetchOrders()
})
</script>

<template>
  <div class="shipment_container">
    <div class="shipment_header">
      <h2>订单发货</h2>
    </div>

    <div v-if="loading" class="loading_wrapper">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else-if="orders.length === 0" class="empty_wrapper">
      <el-empty description="暂无待发货订单" />
    </div>

    <div v-else class="order_list">
      <div v-for="order in orders" :key="order.id" class="order_card">
        <div class="order_header">
          <span class="order_no">订单号：{{ order.orderNo }}</span>
          <span class="order_status status_paid">待发货</span>
        </div>

        <div class="order_body">
          <img v-if="order.commodityImage" :src="`${BASE_URL}${order.commodityImage}`" alt="商品图片" class="commodity_image" />
          <div class="commodity_info">
            <p class="commodity_name">{{ order.commodityName }}</p>
            <p class="commodity_desc">{{ order.commodityDesc }}</p>
          </div>
          <div class="price_section">
            <span class="price">¥{{ order.price?.toFixed(2) }}</span>
            <span class="quantity">x{{ order.quantity }}</span>
          </div>
        </div>

        <div class="buyer_info">
          <span class="buyer_label">收货人：</span>
          <span>{{ order.consignee }}</span>
          <span class="buyer_phone">{{ order.phone }}</span>
          <span class="buyer_address">{{ order.address }}</span>
        </div>

        <div class="order_footer">
          <div class="footer_left">
            <span class="pay_time">支付时间：{{ formatDate(order.payTime) }}</span>
          </div>
          <div class="footer_right">
            <span class="total_label">合计：</span>
            <span class="total_amount">¥{{ order.totalAmount?.toFixed(2) }}</span>
            <button class="ship_btn" @click="openShipDialog(order)">填写物流</button>
          </div>
        </div>
      </div>
    </div>
  </div>

  <el-dialog v-model="shipDialogVisible" title="填写物流信息" width="460px" :close-on-click-modal="false" @close="closeShipDialog">
    <el-form ref="formRef" :model="shipForm" :rules="formRules" class="ship_form">
      <el-form-item label="快递公司" prop="expressCompany">
        <el-select v-model="shipForm.expressCompany" placeholder="请选择快递公司" style="width: 100%">
          <el-option v-for="item in expressCompanies" :key="item.code" :label="item.name" :value="item.code" />
        </el-select>
      </el-form-item>
      <el-form-item label="快递单号" prop="expressNo">
        <el-input v-model="shipForm.expressNo" placeholder="请输入快递单号" maxlength="32" clearable />
      </el-form-item>
      <div class="confirm_section" v-if="shipForm.expressNo">
        <label class="form_label">确认订单信息</label>
        <div class="confirm_info">
          <p><span>商品：</span>{{ currentOrder?.commodityName }}</p>
          <p><span>收货人：</span>{{ currentOrder?.consignee }} {{ currentOrder?.phone }}</p>
          <p><span>地址：</span>{{ currentOrder?.address }}</p>
        </div>
      </div>
    </el-form>

    <template #footer>
      <el-button @click="closeShipDialog">取消</el-button>
      <el-button class="confirm_btn" type="primary" :loading="submitting" @click="handleShip">确认发货</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.shipment_container {
  width: 100%;
  box-sizing: border-box;
  margin: 0 auto;
  padding: 20px;
}

.confirm_btn{
  background-color: var(--primary-color);
  border: 1px solid #000;
  color: #000;
}

.shipment_header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.shipment_header h2 {
  font-size: 18px;
  color: #333;
  margin: 0;
}

.loading_wrapper {
  text-align: center;
  padding: 80px 0;
}

.spinner {
  width: 36px;
  height: 36px;
  margin: 0 auto 16px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #ff6700;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading_wrapper p {
  color: #999;
  font-size: 14px;
}

.empty_wrapper {
  padding: 60px 0;
}

.order_list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order_card {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
}

.order_header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
}

.order_no {
  font-size: 13px;
  color: #666;
}

.order_status {
  font-size: 13px;
  font-weight: 500;
}

.status_paid {
  color: #ff6700;
}

.order_body {
  display: flex;
  align-items: center;
  padding: 16px;
  gap: 12px;
}

.commodity_image {
  width: 72px;
  height: 72px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
}

.commodity_info {
  flex: 1;
  min-width: 0;
}

.commodity_name {
  font-size: 15px;
  color: #333;
  margin: 0 0 6px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.commodity_desc {
  font-size: 12px;
  color: #999;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.price_section {
  text-align: right;
  flex-shrink: 0;
}

.price {
  font-size: 15px;
  color: #333;
  font-weight: 500;
}

.quantity {
  font-size: 12px;
  color: #999;
  display: block;
  margin-top: 4px;
}

.buyer_info {
  padding: 0 16px 12px;
  font-size: 13px;
  color: #666;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
}

.buyer_label {
  color: #999;
}

.buyer_phone {
  color: #999;
  margin-left: 8px;
}

.buyer_address {
  width: 100%;
  margin-top: 2px;
  color: #999;
}

.order_footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  border-top: 1px solid #f0f0f0;
}

.footer_left {
  font-size: 13px;
  color: #999;
}

.footer_right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.total_label {
  font-size: 13px;
  color: #666;
}

.total_amount {
  font-size: 16px;
  font-weight: bold;
  color: #ff6700;
}

.ship_btn {
  margin-left: 12px;
  padding: 6px 20px;
  border: none;
  border-radius: 4px;
  background: #ff6700;
  color: #fff;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s ease;
}

.ship_btn:hover {
  background: #e55a00;
}

.ship_form {
  padding: 8px 0;
}

.form_label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.confirm_info {
  background: #fafafa;
  border-radius: 6px;
  padding: 12px 16px;
  font-size: 13px;
  color: #666;
  line-height: 1.8;
}

.confirm_info p {
  margin: 0;
}

.confirm_info span {
  color: #999;
}
</style>
