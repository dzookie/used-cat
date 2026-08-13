<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { httpGet, httpPut } from '@/utils/request'
import apiConfig from '@/apis/api.config'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import MapContainer from '@/components/MapContainer.vue'

const router = useRouter()
const userStore = useUserStore()
const BASE_URL = import.meta.env.VITE_BASE_URL

const orders = ref([])
const loading = ref(false)
const filterStatus = ref(null)

const filteredOrders = computed(() => {
  if (filterStatus.value === null || filterStatus.value === '') {
    return orders.value
  }
  return orders.value.filter(o => o.status === Number(filterStatus.value))
})

const statusMap = {
  0: { text: '待支付', class: 'status_pending' },
  1: { text: '待发货', class: 'status_paid' },
  2: { text: '已发货', class: 'status_shipped' },
  3: { text: '已取消', class: 'status_cancelled' }
}

const getStatusInfo = (status) => {
  return statusMap[status] || { text: '未知', class: '' }
}

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
    const { data } = await httpGet(apiConfig.order.list, { userId })
    if (data.code === 200) {
      orders.value = data.data || []
    } else {
      ElMessage.error(data.message || '获取订单失败')
    }
  } catch (error) {
    console.error('获取订单列表失败', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

const goDetail = (commodityId) => {
  if (commodityId) {
    router.push(`/detail/${commodityId}`)
  }
}

const goPay = (order) => {
  router.push({
    path: '/payment',
    query: {
      orderId: order.id,
      orderNo: order.orderNo,
      totalAmount: order.totalAmount,
      commodityDesc: order.commodityDesc,
      commodityImage: order.commodityImage
    }
  })
}

const logisticsDrawerVisible = ref(false)
const currentLogisticsOrder = ref(null)
const logisticsTraces = ref([])
const logisticsFromCoord = ref(null)
const logisticsToCoord = ref(null)
const logisticsInfo = ref(null)
const logisticsLoading = ref(false)

const goLogistics = async (order) => {
  logisticsLoading.value = true

  try {
    const { data } = await httpGet(apiConfig.order.logistics, { orderId: order.id })
    if (data.code === 200) {
      const info = data.data
      logisticsInfo.value = info
      logisticsTraces.value = info.traces || []
      logisticsFromCoord.value = info.fromCoord || null
      logisticsToCoord.value = info.toCoord || null
      currentLogisticsOrder.value = order
      logisticsDrawerVisible.value = true
    } else {
      ElMessage.warning(data.msg || '暂无物流信息')
    }
  } catch (error) {
    ElMessage.warning('获取物流信息失败')
  } finally {
    logisticsLoading.value = false
  }
}

const closeLogisticsDrawer = () => {
  logisticsDrawerVisible.value = false
  currentLogisticsOrder.value = null
  logisticsTraces.value = []
  logisticsInfo.value = null
  logisticsFromCoord.value = null
  logisticsToCoord.value = null
}

const handleCancelOrder = async (order) => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '返回',
      type: 'warning',
      customClass: 'confirm-box-custom'
    })
    const { data } = await httpPut(apiConfig.order.cancel, {
      orderId: order.id,
      userId: userStore.loginUser.userId
    })
    if (data.code === 200) {
      ElMessage.success('订单已取消')
      fetchOrders()
    } else {
      ElMessage.error(data.message || '取消订单失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消订单失败')
    }
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
  <div class="buy_container">
    <div class="buy_header">
      <h2>我的订单</h2>
      <el-select v-model="filterStatus" placeholder="全部状态" clearable size="small" style="width: 130px">
        <el-option label="全部" value="" />
        <el-option label="待支付" value="0" />
        <el-option label="待发货" value="1" />
        <el-option label="已发货" value="2" />
        <el-option label="已取消" value="3" />
      </el-select>
    </div>

    <div v-if="loading" class="loading_wrapper">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else-if="filteredOrders.length === 0" class="empty_wrapper">
      <el-empty :description="orders.length === 0 ? '暂无购买记录' : '暂无匹配的订单'" />
    </div>

    <div v-else class="order_list">
      <div v-for="order in filteredOrders" :key="order.id" class="order_card">
        <div class="order_header">
          <span class="order_no">订单号：{{ order.orderNo }}</span>
          <span :class="['order_status', getStatusInfo(order.status).class]">
            {{ getStatusInfo(order.status).text }}
          </span>
        </div>

        <div class="order_body" @click="goDetail(order.commodityId)">
          <img v-if="order.commodityImage" :src="`${BASE_URL}${order.commodityImage}`" alt="商品图片"
            class="commodity_image" />
          <div class="commodity_info">
            <p class="commodity_name">{{ order.commodityName }}</p>
            <p class="commodity_desc">{{ order.commodityDesc }}</p>
          </div>
          <div class="price_section">
            <span class="price">¥{{ order.price?.toFixed(2) }}</span>
            <span class="quantity">x{{ order.quantity }}</span>
          </div>
        </div>

        <div class="order_footer">
          <div class="footer_left">
            <span class="create_time">
              下单时间：{{ formatDate(order.createTime) }}
            </span>
          </div>
          <div class="footer_right">
            <button v-if="order.status === 0" class="pay_btn" @click.stop="goPay(order)">前往支付</button>
            <button v-if="order.status === 0" class="cancel_btn" @click.stop="handleCancelOrder(order)">取消订单</button>
            <template v-else-if="order.status !== 3">
              <button class="logistics_btn" @click.stop="goLogistics(order)">查看物流</button>
              <span class="pay_time_text">
                支付时间：{{ formatDate(order.payTime) }}
              </span>
            </template>
            <span class="total_label">合计：</span>
            <span class="total_amount">¥{{ order.totalAmount?.toFixed(2) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>

  <el-drawer v-model="logisticsDrawerVisible" title="物流信息" direction="rtl" size="50%" @close="closeLogisticsDrawer">
    <template v-if="currentLogisticsOrder">
      <div class="logistics_order_info">
        <p><strong>订单号：</strong>{{ currentLogisticsOrder.orderNo }}</p>
        <p><strong>商品：</strong>{{ currentLogisticsOrder.commodityName }}</p>
        <p v-if="logisticsInfo"><strong>快递：</strong>{{ logisticsInfo.companyName }}</p>
        <p v-if="logisticsInfo && logisticsInfo.fromCity">
          <strong>路线：</strong>{{ logisticsInfo.fromCity }} → {{ logisticsInfo.toCity }}
        </p>
        <p v-if="logisticsInfo && logisticsInfo.estimatedDeliveryTime">
          <strong>预计送达：</strong>{{ logisticsInfo.estimatedDeliveryTime }}
        </p>
      </div>

      <div v-if="logisticsLoading" class="logistics_loading">加载物流信息中...</div>

      <template v-else>
        <MapContainer :traces="logisticsTraces" :fromCoord="logisticsFromCoord" :toCoord="logisticsToCoord" />

        <div v-if="logisticsTraces.length > 0" class="logistics_timeline">
          <div v-for="(trace, i) in logisticsTraces" :key="i" class="timeline_item" :class="{ active: i === 0 }">
            <div class="timeline_dot"></div>
            <div class="timeline_content">
              <div class="timeline_time">{{ trace.time }}</div>
              <div class="timeline_context">{{ trace.context }}</div>
              <div class="timeline_location">{{ trace.location }}</div>
            </div>
          </div>
        </div>

        <div v-if="!logisticsLoading && logisticsTraces.length === 0" class="logistics_empty">
          暂无物流轨迹，请接入快递API后重试
        </div>
      </template>
    </template>
  </el-drawer>
</template>

<style scoped>
.buy_container {
  width: 100%;
  box-sizing: border-box;
  margin: 0 auto;
  padding: 20px;
}

.buy_header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.buy_header h2 {
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
  0% {
    transform: rotate(0deg);
  }

  100% {
    transform: rotate(360deg);
  }
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

.status_pending {
  color: #ff6700;
}

.status_paid {
  color: #52c41a;
}

.status_shipped {
  color: #1677ff;
}

.status_cancelled {
  color: #999;
}

.order_body {
  display: flex;
  align-items: center;
  padding: 16px;
  gap: 12px;
  cursor: pointer;
}

.order_body:hover {
  background: #f9f9f9;
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

.order_footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  border-top: 1px solid #f0f0f0;
}

.footer_left {
  font-size: 12px;
  color: #999;
}

.footer_right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.pay_btn {
  padding: 5px 14px;
  font-size: 13px;
  color: #fff;
  background: #ff6700;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.pay_btn:hover {
  background: #e55f00;
}

.cancel_btn {
  padding: 5px 14px;
  font-size: 13px;
  color: #999;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
}

.cancel_btn:hover {
  color: #666;
  border-color: #bbb;
}

.logistics_btn {
  padding: 5px 14px;
  font-size: 13px;
  color: #ff6700;
  background: #fff;
  border: 1px solid #ff6700;
  border-radius: 4px;
  cursor: pointer;
}

.logistics_btn:hover {
  background: #fff7f0;
}

.pay_time_text {
  font-size: 12px;
  color: #999;
}

.total_label {
  font-size: 13px;
  color: #666;
}

.total_amount {
  font-size: 16px;
  font-weight: 600;
  color: #ff6700;
}

.logistics_order_info {
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #fafafa;
  border-radius: 8px;
}

.logistics_order_info p {
  margin: 0 0 6px 0;
  font-size: 14px;
  color: #333;
}

.logistics_order_info p:last-child {
  margin-bottom: 0;
}

.logistics_loading {
  text-align: center;
  padding: 40px 0;
  color: #999;
  font-size: 14px;
}

.logistics_empty {
  text-align: center;
  padding: 40px 0;
  color: #999;
  font-size: 14px;
}

.logistics_timeline {
  margin-top: 20px;
  padding-left: 8px;
}

.timeline_item {
  display: flex;
  gap: 12px;
  position: relative;
  padding-bottom: 18px;
}

.timeline_item:last-child {
  padding-bottom: 0;
}

.timeline_item::before {
  content: '';
  position: absolute;
  left: 6px;
  top: 14px;
  bottom: 0;
  width: 2px;
  background: #e8e8e8;
}

.timeline_item:last-child::before {
  display: none;
}

.timeline_dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: #d9d9d9;
  flex-shrink: 0;
  margin-top: 3px;
  position: relative;
  z-index: 1;
}

.timeline_item.active .timeline_dot {
  background: #1677ff;
  box-shadow: 0 0 0 4px rgba(22, 119, 255, 0.2);
}

.timeline_content {
  flex: 1;
  min-width: 0;
}

.timeline_time {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.timeline_context {
  font-size: 14px;
  color: #333;
  margin-bottom: 2px;
}

.timeline_location {
  font-size: 12px;
  color: #666;
}
</style>
