<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { EditPen } from '@element-plus/icons-vue'

const BASE_URL = import.meta.env.VITE_BASE_URL
const props = defineProps({
  activeMenu: {
    type: String,
    default: 'personal'
  }
})

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const activeTab = ref('items')

const userInfo = computed(() => {
  return userStore.loginUser || {}
})

const creditLabel = computed(() => {
  const c = userInfo.value.credit || 0
  if (c >= 8) return '信誉良好'
  if (c >= 6) return '信誉一般'
  return '无良商家'
})

const creditClass = computed(() => {
  const c = userInfo.value.credit || 0
  if (c >= 8) return 'tag_good'
  if (c >= 6) return 'tag_normal'
  return 'tag_bad'
})

const tabRoutes = {
  items: '/personal-center/personal/my-community',
  credit: '/personal-center/personal/browse-history'
}

const handleTabClick = (tab) => {
  activeTab.value = tab
  router.push(tabRoutes[tab])
}

watch(() => route.path, (path) => {
  if (path.includes('my-community')) {
    activeTab.value = 'items'
  } else if (path.includes('browse-history')) {
    activeTab.value = 'credit'
  }
}, { immediate: true })
</script>

<template>
  <div class="profile_header">
    <div class="user_info">
      <div class="avatar">
        <img v-if="userInfo.avatar" :src="BASE_URL + userInfo.avatar" alt="头像" />
        <div v-else class="avatar_placeholder">
          {{ (userInfo.username || '用户').charAt(0) }}
        </div>
      </div>
      <div class="info_text">
        <h2 class="username">{{ userInfo.nickname }} <span class="credit_tag" :class="creditClass">{{ creditLabel
            }}</span></h2>
        <p class="location">
          {{ userInfo.email || '未设置邮箱' }}
        </p>
      </div>
    </div>
    <button class="edit_btn" @click="router.push('/personal-center/setting')">
      <el-icon>
        <EditPen />
      </el-icon>
      编辑资料
    </button>
  </div>

  <div class="content_tabs">
    <div class="tab_header">
      <button class="tab_btn" :class="{ active: activeTab === 'items' }" @click="handleTabClick('items')">宝贝</button>
      <button class="tab_btn" :class="{ active: activeTab === 'credit' }"
        @click="handleTabClick('credit')">浏览历史</button>
    </div>

    <div class="tab_content">
      <RouterView />
    </div>
  </div>
</template>

<style scoped>
.profile_header {
  background: linear-gradient(135deg, #fff9f0 0%, #fff5e6 50%, #ffe8cc 100%);
  padding: 32px 36px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  position: relative;
  overflow: hidden;
}

.profile_header::before {
  content: '';
  position: absolute;
  right: -40px;
  top: -40px;
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(255, 229, 94, 0.3) 0%, transparent 70%);
  border-radius: 50%;
}

.profile_header::after {
  content: '';
  position: absolute;
  right: 80px;
  bottom: -30px;
  width: 120px;
  height: 120px;
  background: radial-gradient(circle, rgba(255, 183, 77, 0.2) 0%, transparent 70%);
  border-radius: 50%;
}

.user_info {
  display: flex;
  align-items: center;
  gap: 20px;
  position: relative;
  z-index: 1;
}

.avatar {
  width: 76px;
  height: 76px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  flex-shrink: 0;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar_placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 28px;
  font-weight: 600;
}

.info_text {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.username {
  font-size: 22px;
  font-weight: 600;
  color: #1a1a1a;
  line-height: 1.2;
}

.location {
  font-size: 13px;
  color: #888;
  display: flex;
  align-items: center;
  gap: 8px;
}

.credit_tag {
  display: inline-block;
  font-size: 11px;
  padding: 1px 8px;
  border-radius: 10px;
  font-weight: 500;
}

.tag_good {
  background: #e8f5e9;
  color: #2e7d32;
  border: 1px solid #a5d6a7;
}

.tag_normal {
  background: #fff8e1;
  color: #f57f17;
  border: 1px solid #ffe082;
}

.tag_bad {
  background: #fce4ec;
  color: #c62828;
  border: 1px solid #ef9a9a;
}

.edit_btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  background: #333;
  color: #fff;
  border: none;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  z-index: 1;
}

.edit_btn:hover {
  background: #555;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.edit_btn .el-icon {
  font-size: 14px;
}

.content_tabs {
  margin-top: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.tab_header {
  display: flex;
  border-bottom: 1px solid #f0f0f0;
  padding: 0 28px;
}

.tab_btn {
  padding: 16px 4px;
  margin-right: 32px;
  font-size: 15px;
  color: #999;
  background: none;
  border: none;
  cursor: pointer;
  position: relative;
  transition: all 0.2s ease;
  font-weight: 500;
}

.tab_btn:hover {
  color: #333;
}

.tab_btn.active {
  color: #1a1a1a;
  font-weight: 600;
}

.tab_btn.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: #1a1a1a;
  border-radius: 3px 3px 0 0;
}

.empty_state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.empty_illustration {
  width: 180px;
  height: 144px;
  margin-bottom: 20px;
}

.empty_text {
  font-size: 15px;
  color: #999;
}
</style>
