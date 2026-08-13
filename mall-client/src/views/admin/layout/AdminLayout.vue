<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  DataAnalysis,
  User,
  Goods,
  Reading,
  List,
  Fold,
  Expand,
  ArrowDown,
  ArrowRight,
  UserFilled,
  Search,
  Bell
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const BASE_URL = import.meta.env.VITE_BASE_URL

const avatarUrl = computed(() => {
  const avatar = userStore.loginUser?.avatar
  if (!avatar) return ''
  return avatar.startsWith('http') ? avatar : BASE_URL + avatar
})

const isCollapse = ref(false)

const activeMenu = computed(() => route.path)

const menuTitleMap = {
  '/admin/dashboard': '数据概览',
  '/admin/user/account': '账户管理',
  '/admin/user/role': '角色管理',
  '/admin/commodity/list': '二手商品',
  '/admin/commodity/category': '商品分类',
  '/admin/knowledge': '知识库管理',
  '/admin/order': '订单管理'
}

const breadcrumbItems = computed(() => {
  const path = route.path
  const items = [{ title: '首页', path: '/admin/dashboard' }]
  if (path.startsWith('/admin/user')) {
    items.push({ title: '用户管理' })
    if (path === '/admin/user/account') items.push({ title: '账户管理' })
    if (path === '/admin/user/role') items.push({ title: '角色管理' })
  } else if (path.startsWith('/admin/commodity')) {
    items.push({ title: '商品管理' })
    if (path === '/admin/commodity/list') items.push({ title: '二手商品' })
    if (path === '/admin/commodity/category') items.push({ title: '商品分类' })
  } else if (path === '/admin/knowledge') {
    items.push({ title: '知识库管理' })
  } else if (path === '/admin/order') {
    items.push({ title: '订单管理' })
  } else if (path === '/admin/dashboard') {
    items.push({ title: '数据概览' })
  }
  return items
})

const handleMenuSelect = (index) => {
  router.push(index)
}

const handleLogout = () => {
  userStore.loginOut()
}

const isSubMenuActive = (prefix) => {
  return route.path.startsWith(prefix)
}
</script>

<template>
  <el-container class="admin-layout">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="admin-aside">
      <div class="logo-area">
        <img src="@/assets/logo.svg" alt="logo" class="logo-img" />
        <span v-if="!isCollapse" class="logo-text">二手猫后台</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        class="admin-menu"
        background-color="#ffffff"
        text-color="#606266"
        active-text-color="#d4b800"
        :unique-opened="true"
        @select="handleMenuSelect"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>数据概览</template>
        </el-menu-item>

        <el-sub-menu index="/admin/user">
          <template #title>
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </template>
          <el-menu-item index="/admin/user/account">账户管理</el-menu-item>
          <el-menu-item index="/admin/user/role">角色管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/admin/commodity">
          <template #title>
            <el-icon><Goods /></el-icon>
            <span>商品管理</span>
          </template>
          <el-menu-item index="/admin/commodity/list">二手商品</el-menu-item>
          <el-menu-item index="/admin/commodity/category">商品分类</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/admin/order">
          <el-icon><List /></el-icon>
          <template #title>订单管理</template>
        </el-menu-item>

        <el-menu-item index="/admin/knowledge">
          <el-icon><Reading /></el-icon>
          <template #title>知识库管理</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="admin-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <div class="breadcrumb">
            <template v-for="(item, index) in breadcrumbItems" :key="index">
              <span
                :class="['breadcrumb-item', { 'breadcrumb-active': index === breadcrumbItems.length - 1 }]"
                @click="item.path && router.push(item.path)"
              >
                {{ item.title }}
              </span>
              <el-icon v-if="index < breadcrumbItems.length - 1" class="breadcrumb-sep">
                <ArrowRight />
              </el-icon>
            </template>
          </div>
        </div>
        <div class="header-right">
          <div class="search-box">
            <el-icon class="search-icon"><Search /></el-icon>
            <input type="text" class="search-input" placeholder="搜索..." />
          </div>
          <div class="notification-btn">
            <el-icon><Bell /></el-icon>
            <span class="notification-badge"></span>
          </div>
          <el-dropdown trigger="click">
            <div class="user-info">
              <img v-if="avatarUrl" :src="avatarUrl" alt="头像" class="user-avatar" />
              <el-icon v-else class="user-avatar-default"><UserFilled /></el-icon>
              <span class="user-name">{{ userStore.loginUser?.nickname }}</span>
              <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.admin-layout {
  height: 100vh;
}

.admin-aside {
  background: #ffffff;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  position: relative;
  border-right: 1px solid #ebeef5;
}

.logo-area {
  height: 60px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 20px;
  border-bottom: 1px solid #f2f6fc;
  position: relative;
  z-index: 1;
}

.logo-img {
  width: 28px;
  height: 28px;
  flex-shrink: 0;
}

.logo-text {
  color: #d4b800;
  font-size: 18px;
  font-weight: 700;
  white-space: nowrap;
}

.admin-menu {
  border-right: none;
  position: relative;
  z-index: 1;
  padding: 12px 0;
}

.admin-menu:not(.el-menu--collapse) {
  width: 220px;
}

:deep(.el-menu-item) {
  height: 48px;
  line-height: 48px;
  margin: 2px 0;
  transition: all 0.25s ease;
  border-left: 3px solid transparent;
  border-radius: 0;
  padding: 0 20px !important;
}

:deep(.el-menu-item:hover) {
  background: #fafafa !important;
  color: #303133 !important;
}

:deep(.el-menu-item.is-active) {
  background: #fffbeb !important;
  color: #d4b800 !important;
  border-left-color: #ffe60f;
  font-weight: 500;
  box-shadow: none;
  transform: none;
}

:deep(.el-sub-menu__title) {
  height: 48px;
  line-height: 48px;
  margin: 2px 0;
  transition: all 0.25s ease;
  border-left: 3px solid transparent;
  border-radius: 0;
  padding: 0 20px !important;
}

:deep(.el-sub-menu__title:hover) {
  background: #fafafa !important;
  color: #303133 !important;
}

:deep(.el-sub-menu .el-menu-item) {
  margin: 0;
  min-width: auto;
  padding: 0 20px 0 48px !important;
  height: 44px;
  line-height: 44px;
  border-left: 3px solid transparent;
}

:deep(.el-sub-menu .el-menu-item.is-active) {
  background: #fffbeb !important;
  color: #d4b800 !important;
  border-left-color: #ffe60f;
}

:deep(.el-sub-menu .el-menu) {
  background: #fafafa !important;
}

:deep(.el-menu--popup) {
  background: #ffffff !important;
  border-radius: 8px;
  padding: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.admin-header {
  height: 60px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-bottom: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: none;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #909399;
  transition: color 0.2s;
}

.collapse-btn:hover {
  color: #d4b800;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}

.breadcrumb-item {
  color: #909399;
  cursor: pointer;
  transition: color 0.2s;
}

.breadcrumb-item:hover {
  color: #d4b800;
}

.breadcrumb-active {
  color: #303133;
  font-weight: 500;
  cursor: default;
}

.breadcrumb-active:hover {
  color: #303133;
}

.breadcrumb-sep {
  font-size: 12px;
  color: #c0c4cc;
  font-weight: 300;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 14px;
  color: #909399;
  font-size: 14px;
}

.search-input {
  width: 240px;
  height: 36px;
  border-radius: 9999px;
  border: 1px solid #ebeef5;
  background: #f5f7fa;
  padding: 0 16px 0 40px;
  font-size: 13px;
  color: #303133;
  outline: none;
  transition: all 0.2s ease;
}

.search-input:focus {
  border-color: #ffe60f;
  background: #ffffff;
  box-shadow: 0 0 0 2px rgba(255, 230, 15, 0.15);
}

.search-input::placeholder {
  color: #c0c4cc;
}

.notification-btn {
  position: relative;
  width: 36px;
  height: 36px;
  border-radius: 9999px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  cursor: pointer;
  transition: all 0.2s ease;
}

.notification-btn:hover {
  background: #f5f7fa;
  color: #303133;
}

.notification-badge {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 8px;
  height: 8px;
  border-radius: 9999px;
  background: #f56c6c;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 14px 6px 6px;
  border-radius: 9999px;
  transition: all 0.25s ease;
  background: transparent;
}

.user-info:hover {
  background: #f5f7fa;
  transform: none;
  box-shadow: none;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 9999px;
  object-fit: cover;
  border: 2px solid #ebeef5;
}

.user-avatar-default {
  width: 32px;
  height: 32px;
  border-radius: 9999px;
  background: linear-gradient(135deg, #ffe60f, #d4b800);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  border: none;
}

.user-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.dropdown-arrow {
  font-size: 12px;
  color: #909399;
}

.admin-main {
  background: #f5f7fa;
  padding: 24px;
  overflow-y: auto;
}
</style>
