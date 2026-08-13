<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { httpGet, httpPost } from '@/utils/request'
import apiConfig from '@/apis/api.config'
import { useUserStore } from '@/stores/user'
import AddressCard from '@/components/AddressCard.vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const BASE_URL = import.meta.env.VITE_BASE_URL
const addressList = ref([])
const selectedAddressId = ref(null)
const deliveryMethod = ref(true)

const commodity = computed(() => ({
  id: route.query.id,
  price: Number(route.query.price) || 0,
  description: route.query.description || '',
  image: route.query.image || ''
}))

const totalPrice = computed(() => {
  return commodity.value.price || 0
})

const freight = computed(() => 0)

const totalAmount = computed(() => {
  return totalPrice.value + freight.value
})

/**
 * 获取收货地址列表
 */
const fetchAddressList = async () => {
  const userId = userStore.loginUser?.userId
  if (!userId) return

  try {
    const { data } = await httpGet(apiConfig.receivingAddress.getReceivingAddressList, { userId })
    if (data.code === 200) {
      addressList.value = data.data || []
      if (addressList.value.length > 0 && !selectedAddressId.value) {
        selectedAddressId.value = addressList.value[0].id
      }
    }
  } catch (error) {
    ElMessage.error('获取收货地址失败', error)
  }
}

/**
 * 选择收货地址
 */
const selectAddress = (address) => {
  selectedAddressId.value = address.id
}

/**
 * 确认订单
 */
const handleConfirmOrder = async () => {
  if (!selectedAddressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }

  try {
    const userId = userStore.loginUser?.userId
    if (!userId) {
      ElMessage.warning('请先登录')
      return
    }

    const { data } = await httpPost(apiConfig.order.create, {
      userId,
      commodityId: commodity.value.id,
      commodityName: commodity.value.description?.substring(0, 20) || '商品',
      commodityDesc: commodity.value.description,
      commodityImage: commodity.value.image,
      price: totalPrice.value,
      quantity: 1,
      totalAmount: totalAmount.value,
      addressId: selectedAddressId.value
    })

    if (data.code === 200) {
      const order = data.data
      router.push({
        path: '/payment',
        query: {
          orderId: order.id,
          orderNo: order.orderNo,
          totalAmount: totalAmount.value,
          commodityDesc: commodity.value.description,
          commodityImage: commodity.value.image
        }
      })
    } else {
      ElMessage.error(data.message || '订单创建失败')
    }
  } catch (error) {
    console.error('订单创建失败', error)
    ElMessage.error('订单创建失败，请重试')
  }
}

onMounted(() => {
  fetchAddressList()
})
</script>

<template>
  <div class="order_container">
    <div class="order_main">
      <div class="address_section">
        <div class="section_header">
          <h3 class="section_title">收货地址</h3>
          <a href="/personal-center/receiving-address" class="manage_link">管理地址</a>
        </div>

        <div v-if="addressList.length === 0" class="empty_address">
          暂无收货地址，请先添加地址
        </div>

        <div v-else class="address_list">
          <AddressCard v-for="item in addressList" :key="item.id" :address="item"
            :selected="selectedAddressId === item.id" @click="selectAddress" />
        </div>
      </div>

      <div class="order_info_section">
        <h3 class="section_title">订单信息</h3>
        <div class="commodity_card">
          <img :src="`${BASE_URL}${commodity.image}`" alt="商品图片" class="commodity_image" />
          <div class="commodity_detail">
            <p class="commodity_desc">{{ commodity.description }}</p>
            <p class="commodity_price">¥{{ totalPrice.toFixed(2) }}</p>
          </div>
        </div>
      </div>
    </div>

    <div class="order_sidebar">
      <div class="price_section">
        <h3 class="section_title">价格明细</h3>
        <div class="price_total">
          <span class="label">合计:</span>
          <span class="total_amount">¥{{ totalAmount.toFixed(2) }}</span>
        </div>
        <button class="confirm_btn" @click="handleConfirmOrder">确认购买</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.order_container {
  width: 1240px;
  margin: 20px auto;
  display: flex;
  gap: 24px;
}

.order_main {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.order_sidebar {
  width: 320px;
  flex-shrink: 0;
}

.section_header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section_title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 10px;
}

.manage_link {
  color: #666;
  text-decoration: none;
  font-size: 14px;
}

.manage_link:hover {
  color: #ff6700;
}

.delivery_option {
  margin-bottom: 16px;
}

.delivery_text {
  font-size: 14px;
  color: #333;
}

:deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #ff6700;
  border-color: #ff6700;
}

:deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
  color: #333;
}

.empty_address {
  padding: 40px;
  text-align: center;
  color: #999;
  background: #f9f9f9;
  border-radius: 8px;
}

.address_list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  flex-wrap: wrap;
}

.address_section {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.order_info_section {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.commodity_card {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #f9f9f9;
  border-radius: 8px;
}

.commodity_image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
}

.commodity_detail {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.commodity_desc {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.commodity_price {
  font-size: 18px;
  font-weight: 600;
  color: #ff6700;
}

.price_section {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  position: sticky;
  top: 20px;
}

.price_item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
}

.price_item .label {
  color: #666;
}

.price_item .value {
  color: #999;
  font-size: 12px;
  margin-right: auto;
  margin-left: 12px;
}

.price_item .amount {
  color: #333;
  font-weight: 500;
}

.price_total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0 20px;
  font-size: 15px;
}

.price_total .label {
  color: #333;
  font-weight: 500;
}

.total_amount {
  font-size: 22px;
  font-weight: 600;
  color: #ff6700;
}

.confirm_btn {
  width: 100%;
  padding: 14px;
  background: var(--primary-color);
  color: #000;
  border: 1px solid #000;
  border-radius: 25px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.confirm_btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(124, 124, 124, 0.3);
}

.confirm_btn:active {
  transform: translateY(0);
}
</style>
