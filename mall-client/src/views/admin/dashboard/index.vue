<script setup>
import { ref, computed, onMounted, markRaw, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { httpGet } from '@/utils/request'
import apiConfig from '@/apis/api.config.js'
import * as echarts from 'echarts'
import {
  User,
  Goods,
  List,
  Reading,
  ArrowRight,
  Clock,
  ShoppingCart,
  Money
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const BASE_URL = import.meta.env.VITE_BASE_URL

const menuList = [
  { title: '用户管理', desc: '账户与角色管理', icon: markRaw(User), path: '/admin/user/account', color: 'primary' },
  { title: '商品管理', desc: '二手商品与分类', icon: markRaw(Goods), path: '/admin/commodity/list', color: 'success' },
  { title: '订单管理', desc: '处理订单和物流', icon: markRaw(List), path: '/admin/order', color: 'danger' },
  { title: '知识库管理', desc: '管理知识库内容', icon: markRaw(Reading), path: '/admin/knowledge', color: 'warning' }
]

const stats = ref([
  { label: '总用户数', value: '0', color: 'primary', icon: markRaw(User) },
  { label: '商品总数', value: '0', color: 'success', icon: markRaw(Goods) },
  { label: '订单总数', value: '0', color: 'warning', icon: markRaw(ShoppingCart) },
  { label: '今日交易金额', value: '¥0.00', color: 'danger', icon: markRaw(Money) }
])

const statsLoading = ref(false)

const formatNumber = (num) => {
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

const fetchStats = async () => {
  statsLoading.value = true
  try {
    const res = await httpGet(apiConfig.admin.dashboardStats)
    if (res.data && res.data.code === 200) {
      const data = res.data.data
      stats.value = [
        { label: '总用户数', value: formatNumber(data.totalUsers || 0), color: 'primary', icon: markRaw(User) },
        { label: '商品总数', value: formatNumber(data.totalCommodities || 0), color: 'success', icon: markRaw(Goods) },
        { label: '订单总数', value: formatNumber(data.totalOrders || 0), color: 'warning', icon: markRaw(ShoppingCart) },
        { label: '今日交易金额', value: '¥' + (data.todaySales || 0).toFixed(2), color: 'danger', icon: markRaw(Money) }
      ]
    }
  } catch (error) {
    console.error('获取统计数据失败', error)
  } finally {
    statsLoading.value = false
  }
}

onMounted(() => {
  fetchStats()
  fetchWeekOrders()
  fetchMonthCommodities()
})

onUnmounted(() => {
  if (chartInstance.value) {
    chartInstance.value.dispose()
    chartInstance.value = null
  }
  if (chartResizeHandler.value) {
    window.removeEventListener('resize', chartResizeHandler.value)
    chartResizeHandler.value = null
  }
})

// 本月新增商品折线图
const chartRef = ref(null)
const chartInstance = ref(null)
const chartResizeHandler = ref(null)
const monthCommodityData = ref([])
const monthCommodityLoading = ref(false)

const fetchMonthCommodities = async () => {
  monthCommodityLoading.value = true
  try {
    const res = await httpGet(apiConfig.admin.dashboardMonthCommodities)
    const data = res.data
    if (data.code === 200) {
      monthCommodityData.value = data.data || []
      initChart()
    }
  } catch (error) {
    console.error('获取本月新增商品失败', error)
  } finally {
    monthCommodityLoading.value = false
  }
}

const initChart = () => {
  if (!chartRef.value) return
  if (chartInstance.value) {
    chartInstance.value.dispose()
  }

  const instance = echarts.init(chartRef.value)
  const dates = monthCommodityData.value.map(item => item.date)
  const counts = monthCommodityData.value.map(item => item.count)

  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#ffe60f',
      borderWidth: 1,
      textStyle: { color: '#303133', fontSize: 13 },
      formatter: (params) => {
        const p = params[0]
        return `<div style="font-weight:600;margin-bottom:4px">${p.name}</div>
                <div style="display:flex;align-items:center;gap:6px">
                  <span style="display:inline-block;width:10px;height:10px;border-radius:50%;background:${p.color}"></span>
                  <span>新增商品：</span><span style="font-weight:600">${p.value} 件</span>
                </div>`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLine: { lineStyle: { color: '#e0e0e0' } },
      axisLabel: { color: '#909399', fontSize: 11 },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#909399', fontSize: 11 },
      splitLine: { lineStyle: { color: '#f0f0f0', type: 'dashed' } }
    },
    series: [
      {
        name: '新增商品',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        showSymbol: false,
        data: counts,
        lineStyle: {
          color: '#ffe60f',
          width: 3
        },
        itemStyle: {
          color: '#ffe60f',
          borderColor: '#fff',
          borderWidth: 2
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(255, 230, 15, 0.35)' },
              { offset: 1, color: 'rgba(255, 230, 15, 0.02)' }
            ]
          }
        },
        emphasis: {
          focus: 'series',
          itemStyle: {
            color: '#ffd700',
            borderColor: '#ffe60f',
            borderWidth: 3,
            shadowBlur: 10,
            shadowColor: 'rgba(255, 230, 15, 0.5)'
          }
        }
      }
    ]
  }

  instance.setOption(option)
  chartInstance.value = instance

  chartResizeHandler.value = () => instance.resize()
  window.addEventListener('resize', chartResizeHandler.value)
}

// 本周订单
const weekOrders = ref([])
const weekTotal = ref(0)
const weekLoading = ref(false)

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

const fetchWeekOrders = async () => {
  weekLoading.value = true
  try {
    const res = await httpGet(apiConfig.admin.dashboardWeekOrders, { pageNum: 1, pageSize: 5 })
    const data = res.data
    if (data.code === 200) {
      weekOrders.value = data.data.items || []
      weekTotal.value = data.data.total || 0
    }
  } catch (error) {
    console.error('获取本周订单失败', error)
  } finally {
    weekLoading.value = false
  }
}

const today = computed(() => {
  return new Date().toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
})

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 12) return '早上好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})


</script>

<template>
  <div class="dashboard">
    <!-- 欢迎卡片 -->
    <div class="welcome-card">
      <div class="welcome-info">
        <div class="welcome-title">
          <span class="greeting-text">{{ greeting }}，</span>
          <span class="user-name">{{ userStore.loginUser?.nickname || '管理员' }}</span>
        </div>
        <div class="welcome-desc">
          <el-icon class="welcome-icon"><Clock /></el-icon>
          {{ today }}
        </div>
      </div>
      <div class="welcome-decor">
        <svg viewBox="0 0 200 200" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle cx="100" cy="100" r="80" fill="rgba(255,255,255,0.1)"/>
          <circle cx="100" cy="100" r="60" fill="rgba(255,255,255,0.1)"/>
          <circle cx="100" cy="100" r="40" fill="rgba(255,255,255,0.15)"/>
        </svg>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div
        v-for="(stat, index) in stats"
        :key="index"
        class="stat-card"
        v-loading="statsLoading"
      >
        <div :class="['stat-icon', `stat-icon-${stat.color}`]">
          <el-icon><component :is="stat.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </div>
    </div>

    <!-- 主要内容区 -->
    <div class="dashboard-grid">
      <!-- 本月新增商品 -->
      <div class="chart-card" v-loading="monthCommodityLoading">
        <div class="card-header">
          <h3 class="card-title">本月新增商品</h3>
          <div class="card-action">
            <span class="chart-total">
              总计: {{ monthCommodityData.reduce((a, b) => a + b.count, 0) }} 件
            </span>
          </div>
        </div>
        <div ref="chartRef" class="echart-container"></div>
      </div>

      <!-- 快捷入口 -->
      <div class="quick-actions-card">
        <div class="card-header">
          <h3 class="card-title">快捷入口</h3>
        </div>
        <div class="quick-grid">
          <div
            v-for="item in menuList"
            :key="item.path"
            class="quick-item"
            @click="router.push(item.path)"
          >
            <div :class="['quick-icon', `quick-icon-${item.color}`]">
              <el-icon><component :is="item.icon" /></el-icon>
            </div>
            <div class="quick-info">
              <div class="quick-title">{{ item.title }}</div>
              <div class="quick-desc">{{ item.desc }}</div>
            </div>
            <el-icon class="quick-arrow"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
    </div>

    <!-- 本周订单 -->
    <div class="week-orders-card">
      <div class="card-header">
        <h3 class="card-title">本周订单</h3>
        <button class="view-all-btn" @click="router.push('/admin/order')">
          查看全部
          <el-icon><ArrowRight /></el-icon>
        </button>
      </div>
      <div class="table-card">
        <el-table
          :data="weekOrders"
          v-loading="weekLoading"
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
        </el-table>
      </div>
      <div v-if="weekTotal > 0" class="week-orders-footer">
        <span class="week-orders-info">本周共 {{ weekTotal }} 条订单</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 欢迎卡片 */
.welcome-card {
  background: linear-gradient(135deg, #ffe60f 0%, #ffd700 50%, #f5c400 100%);
  border-radius: 12px;
  padding: 16px 24px;
  position: relative;
  overflow: hidden;
  display: flex;
  justify-content: space-between;
  align-items: center;
  min-height: 80px;
}

.welcome-info {
  position: relative;
  z-index: 1;
}

.welcome-title {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 6px;
}

.greeting-text {
  font-weight: 600;
}

.user-name {
  color: #1a1a2e;
}

.welcome-desc {
  font-size: 13px;
  color: rgba(26, 26, 46, 0.75);
  display: flex;
  align-items: center;
  gap: 6px;
}

.welcome-icon {
  font-size: 14px;
}

.welcome-decor {
  width: 80px;
  height: 80px;
  opacity: 0.5;
}

.welcome-decor svg {
  width: 100%;
  height: 100%;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 20px 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease-in-out;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: default;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 24px;
}

.stat-icon-primary {
  background: rgba(255, 230, 15, 0.15);
  color: #d4b800;
}

.stat-icon-success {
  background: rgba(103, 194, 58, 0.1);
  color: #67c23a;
}

.stat-icon-warning {
  background: rgba(230, 162, 60, 0.1);
  color: #e6a23c;
}

.stat-icon-danger {
  background: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1.25;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

/* 内容网格 */
.dashboard-grid {
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: 16px;
  align-items: stretch;
}

.chart-card,
.quick-actions-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.chart-card {
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.chart-total {
  font-size: 13px;
  color: #909399;
}

/* ECharts */
.echart-container {
  flex: 1;
  min-height: 0;
}

/* 快捷入口 */
.quick-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.quick-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s ease;
  border: 1px solid transparent;
}

.quick-item:hover {
  background: #fafafa;
  border-color: #f0f0f0;
  transform: translateX(4px);
}

.quick-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.quick-icon-primary {
  background: rgba(255, 230, 15, 0.15);
  color: #d4b800;
}

.quick-icon-success {
  background: rgba(103, 194, 58, 0.1);
  color: #67c23a;
}

.quick-icon-warning {
  background: rgba(230, 162, 60, 0.1);
  color: #e6a23c;
}

.quick-icon-danger {
  background: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
}

.quick-info {
  flex: 1;
  min-width: 0;
}

.quick-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 2px;
}

.quick-desc {
  font-size: 12px;
  color: #909399;
}

.quick-arrow {
  color: #c0c4cc;
  font-size: 16px;
  transition: transform 0.25s ease;
}

.quick-item:hover .quick-arrow {
  color: #d4b800;
  transform: translateX(4px);
}

/* 本周订单 */
.week-orders-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.view-all-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  color: #d4b800;
  font-size: 13px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s ease;
  padding: 4px 8px;
  border-radius: 6px;
}

.view-all-btn:hover {
  background: rgba(255, 230, 15, 0.1);
}

.table-card {
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

.week-orders-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 12px 0 0;
}

.week-orders-info {
  font-size: 13px;
  color: #909399;
}
</style>