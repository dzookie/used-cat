<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { httpGet, httpPost } from '@/utils/request'
import apiConfig from '@/apis/api.config'
import { ElMessage } from 'element-plus'
import QRCode from 'qrcode'

const route = useRoute()
const router = useRouter()
const BASE_URL = import.meta.env.VITE_BASE_URL

const orderId = ref(route.query.orderId)
const orderNo = ref(route.query.orderNo)
const totalAmount = ref(Number(route.query.totalAmount) || 0)
const commodityDesc = ref(route.query.commodityDesc || '')
const commodityImage = ref(route.query.commodityImage || '')
const qrCodeDataUrl = ref('')
const loading = ref(false)
const poling = ref(null)
const payStatus = ref(0)

/**
 * 生成支付二维码
 */
const generateQrCode = async () => {
  loading.value = true
  try {
    const { data } = await httpGet(apiConfig.alipay.payQrCode, {
      outTradeNo: orderNo.value,
      totalAmount: totalAmount.value.toString(),
      subject: commodityDesc.value?.substring(0, 50) || '商品购买'
    })

    if (data.code === 200) {
      const qrUrl = data.data
      const dataUrl = await QRCode.toDataURL(qrUrl, {
        width: 220,
        margin: 2,
        color: {
          dark: '#000000',
          light: '#ffffff'
        }
      })
      qrCodeDataUrl.value = dataUrl
      startPolling()
    } else {
      ElMessage.error(data.message || '二维码生成失败')
    }
  } catch (error) {
    console.error('二维码生成失败', error)
    ElMessage.error('二维码生成失败，请重试')
  } finally {
    loading.value = false
  }
}

/**
 * 开始轮询查询支付状态
 */
const startPolling = () => {
  let paid = false
  poling.value = setInterval(async () => {
    if (paid) return
    try {
      const { data } = await httpGet(apiConfig.alipay.query, {
        outTradeNo: orderNo.value
      })
      if (paid) return
      
      if (data.code === 200 && data.data === 'SUCCESS') {
        paid = true
        clearInterval(poling.value)
        payStatus.value = 1
        ElMessage.success('支付成功')
        
        setTimeout(() => {
          router.push('/personal-center/buy')
        }, 2000)
      }
    } catch (error) {
      console.error('查询支付状态失败', error)
    }
  }, 2000)
}

/**
 * 取消支付
 */
const handleCancel = () => {
  clearInterval(poling.value)
  router.push('/personal-center/buy')
}

/**
 * 刷新支付二维码
 */
const handleRefresh = () => {
  qrCodeDataUrl.value = ''
  generateQrCode()
}

onMounted(() => {
  if (!orderId.value || !orderNo.value) {
    ElMessage.error('订单信息不完整')
    router.push('/')
    return
  }
  generateQrCode()
})

onUnmounted(() => {
  if (poling.value) {
    clearInterval(poling.value)
  }
})
</script>

<template>
  <div class="payment_container">
    <div class="payment_card">
      <div class="payment_header">
        <h2>支付宝扫码支付</h2>
        <span class="order_no">订单号: {{ orderNo }}</span>
      </div>

      <div class="payment_body">
        <div class="commodity_info">
          <img 
            v-if="commodityImage" 
            :src="`${BASE_URL}${commodityImage}`" 
            alt="商品图片" 
            class="commodity_image"
          />
          <div class="commodity_detail">
            <p class="commodity_desc">{{ commodityDesc }}</p>
            <p class="price">¥{{ totalAmount.toFixed(2) }}</p>
          </div>
        </div>

        <div class="qrcode_section">
          <div v-if="loading" class="loading">
            <div class="spinner"></div>
            <p>正在生成二维码...</p>
          </div>
          
          <div v-else-if="payStatus === 1" class="success">
            <div class="success_icon">✓</div>
            <p>支付成功</p>
            <p class="redirect">正在跳转...</p>
          </div>
          
          <div v-else-if="qrCodeDataUrl" class="qrcode_wrapper">
            <img :src="qrCodeDataUrl" alt="支付二维码" class="qrcode" />
            <p class="hint">请使用支付宝扫描上方二维码完成支付</p>
            <button class="refresh_btn" @click="handleRefresh">刷新二维码</button>
          </div>
          
          <div v-else class="error">
            <p>二维码生成失败</p>
            <button class="refresh_btn" @click="handleRefresh">重试</button>
          </div>
        </div>
      </div>

      <div class="payment_footer">
        <button class="cancel_btn" @click="handleCancel" :disabled="payStatus === 1">取消</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.payment_container {
  width: 100%;
  min-height: calc(100vh - 200px);
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 20px 0;
}

.payment_card {
  width: 500px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.payment_header {
  padding: 24px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.payment_header h2 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.order_no {
  font-size: 12px;
  color: #999;
}

.payment_body {
  padding: 24px;
}

.commodity_info {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 24px;
}

.commodity_image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
}

.commodity_detail {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.commodity_desc {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin: 0 0 8px 0;
}

.price {
  font-size: 18px;
  font-weight: 600;
  color: #ff6700;
  margin: 0;
}

.qrcode_section {
  text-align: center;
  padding: 20px 0;
}

.loading {
  padding: 40px 0;
}

.spinner {
  width: 40px;
  height: 40px;
  margin: 0 auto 16px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #ff6700;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading p {
  color: #666;
  font-size: 14px;
}

.success {
  padding: 40px 0;
}

.success_icon {
  width: 60px;
  height: 60px;
  margin: 0 auto 16px;
  background: #52c41a;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: #fff;
}

.success p {
  color: #52c41a;
  font-size: 18px;
  font-weight: 500;
  margin: 0;
}

.redirect {
  color: #999 !important;
  font-size: 14px !important;
  font-weight: 400 !important;
  margin-top: 8px !important;
}

.qrcode_wrapper {
  padding: 20px 0;
}

.qrcode {
  width: 200px;
  height: 200px;
  margin: 0 auto;
  display: block;
}

.hint {
  color: #666;
  font-size: 14px;
  margin: 16px 0;
}

.refresh_btn {
  padding: 8px 24px;
  background: #fff;
  border: 1px solid #ff6700;
  border-radius: 20px;
  color: #ff6700;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.refresh_btn:hover {
  background: #ff6700;
  color: #fff;
}

.error {
  padding: 40px 0;
}

.error p {
  color: #ff4d4f;
  margin: 0 0 16px;
}

.payment_footer {
  display: flex;
  justify-content: center;
  padding: 24px;
  border-top: 1px solid #f0f0f0;
}

.cancel_btn {
  padding: 12px 48px;
  background: #fff;
  border: 1px solid #e5e5e5;
  border-radius: 25px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: all 0.3s;
}

.cancel_btn:hover:not(:disabled) {
  border-color: #ff6700;
  color: #ff6700;
}

.cancel_btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
