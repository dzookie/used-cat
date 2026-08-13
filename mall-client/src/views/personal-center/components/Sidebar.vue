<script setup>
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, ShoppingBag, Star, Setting, ArrowDown, ArrowUp, Location, Box } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const activeMenu = ref('profile')

const emit = defineEmits(['menu-change'])

const menuData = [
  {
    key: 'profile',
    label: '个人中心',
    icon: User,
    path: '/personal-center/personal'
  },
  {
    key: 'order',
    label: '订单管理',
    icon: ShoppingBag,
    children: [
      { key: 'bought', label: '我的订单', path: '/personal-center/buy' },
      { key: 'shipment', label: '订单发货', path: '/personal-center/shipment' }
    ]
  },
  {
    key: 'idle',
    label: '闲置管理',
    icon: Star,
    children: [
      { key: 'release', label: '发布闲置', path: '/personal-center/release-idle' },
      { key: 'published', label: '我的闲置', path: '/personal-center/published' },
      { key: 'collection', label: '我的收藏', path: '/personal-center/collection' }
    ]
  },
  {
    key: 'address',
    label: '收货地址',
    icon: Location,
    path: '/personal-center/receiving-address'
  },
  {
    key: 'settings',
    label: '账户设置',
    icon: Setting,
    children: [
      { key: 'foundation', label: '信息设置', path: '/personal-center/setting/foundation' },
      { key: 'password', label: '重置密码', path: '/personal-center/setting/password' }
    ]
  }
]

const expandedMenus = ref(['order', 'idle', 'settings'])

const toggleExpand = (key) => {
  const index = expandedMenus.value.indexOf(key)
  if (index > -1) {
    expandedMenus.value.splice(index, 1)
  } else {
    expandedMenus.value.push(key)
  }
}

const isExpanded = (key) => {
  return expandedMenus.value.includes(key)
}

const handleMenuClick = (key, path) => {
  activeMenu.value = key
  if (path) {
    router.push(path)
  }
  emit('menu-change', key)
}

const updateActiveMenuByRoute = () => {
  const currentPath = route.path
  for (const item of menuData) {
    if (item.path === currentPath) {
      activeMenu.value = item.key
      return
    }
    if (item.children) {
      for (const sub of item.children) {
        if (sub.path === currentPath) {
          activeMenu.value = sub.key
          if (!expandedMenus.value.includes(item.key)) {
            expandedMenus.value.push(item.key)
          }
          return
        }
      }
    }
  }
}

watch(() => route.path, updateActiveMenuByRoute, { immediate: true })
</script>

<template>
  <aside class="sidebar">
    <nav class="menu">
      <template v-for="item in menuData" :key="item.key">
        <div v-if="!item.children" class="menu_item" :class="{ active: activeMenu === item.key }" @click="handleMenuClick(item.key, item.path)">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </div>

        <div v-else class="menu_item menu_group" :class="{ expanded: isExpanded(item.key) }">
          <div class="menu_item_header" @click="toggleExpand(item.key)">
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
            <el-icon class="arrow"><ArrowDown v-if="!isExpanded(item.key)" /><ArrowUp v-else /></el-icon>
          </div>
          <transition name="slide">
            <div class="sub_menu" v-show="isExpanded(item.key)">
              <div
                v-for="sub in item.children"
                :key="sub.key"
                class="sub_menu_item"
                :class="{ active: activeMenu === sub.key }"
                @click.stop="handleMenuClick(sub.key, sub.path)"
              >
                {{ sub.label }}
              </div>
            </div>
          </transition>
        </div>
      </template>
    </nav>
  </aside>
</template>

<style scoped>
.sidebar {
  width: 220px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 12px;
  padding: 16px 0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  height: fit-content;
}

.menu {
  display: flex;
  flex-direction: column;
}

.menu_item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 24px;
  font-size: 14px;
  color: #333;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.menu_item:hover {
  background: #f7f7f7;
  color: #000;
}

.menu_item.active {
  color: #ff5000;
  font-weight: 500;
}

.menu_item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 16px;
  background: #ff5000;
  border-radius: 0 3px 3px 0;
}

.menu_item .el-icon {
  font-size: 18px;
}

.menu_group {
  flex-direction: column;
  align-items: stretch;
  padding: 0;
}

.menu_item_header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 24px;
  cursor: pointer;
}

.menu_item_header .arrow {
  margin-left: auto;
  font-size: 12px;
  color: #999;
  transition: transform 0.2s ease;
}

.sub_menu {
  overflow: hidden;
}

.sub_menu_item {
  padding: 10px 24px 10px 52px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s ease;
}

.sub_menu_item:hover {
  color: #333;
  background: #f7f7f7;
}

.sub_menu_item.active {
  color: #ff5000;
}

.slide-enter-active,
.slide-leave-active {
  transition: all 0.25s ease;
}

.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
