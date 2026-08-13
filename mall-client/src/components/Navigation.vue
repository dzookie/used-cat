<script setup>
import { ref } from 'vue'
import searchIcon from '@/assets/icons/search.svg'
import goRightIcon from '@/assets/icons/goRight.svg'
import userIcon from '@/assets/icons/user.svg'
import orderIcon from '@/assets/icons/order.svg'
import logoutIcon from '@/assets/icons/logout.svg'
import { ElMessage } from 'element-plus'
import logoIcon from '@/assets/logo.svg'
import { useUserStore } from '@/stores/user'
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { animate, scrambleText } from 'animejs';

const router = useRouter()

const BASE_URL = import.meta.env.VITE_BASE_URL
const userStore = useUserStore()
const logoTextRef = ref(null)
const logoText = ['二手猫', 'USED CAT']
const searchKeyword = ref('')

const handleSearch = () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) return
  router.push(`/search?keyword=${encodeURIComponent(keyword)}`)
  searchKeyword.value = ''
}

const handleLoginOut = () => {
  userStore.loginOut()
  ElMessage.success('退出登录成功')
}

let i = 0;
const logoAnimePlay = () => {
  i = (i + 1) % logoText.length;
  animate(logoTextRef.value, {
    innerHTML: scrambleText({ text: logoText[i] }),
  });
}

/**
 * 跳转用户中心
 */
const handlePersonalCenter = () => {
  router.push('/personal-center')
}

/**
 * 跳转订单中心
 */
const handleOrderCenter = () => {
  router.push('/personal-center/buy')
}

onMounted(async () => {
  await userStore.getCurrLoginUser()

  setInterval(logoAnimePlay, 10000)
})
</script>

<template>
  <div class="nav_container">
    <div class="logo" @click="$router.push('/')">
      <img :src="logoIcon" alt="">
      <p ref="logoTextRef">二手猫</p>
    </div>

    <div class="search_container">
      <img :src="searchIcon" alt="搜索" class="search_icon" @click="handleSearch">
      <input v-model="searchKeyword" type="text" placeholder="搜索商品..." @keyup.enter="handleSearch">
    </div>

    <div class="user_container">
      <template v-if="userStore.loginUser">
        <el-popover placement="bottom" width="200px">
          <template #reference>
            <div class="user_info" @click="handlePersonalCenter">
              <img :src="BASE_URL + userStore.loginUser.avatar" alt="用户" class="avatar">
              <span>{{ userStore.loginUser.nickname }}</span>
            </div>
          </template>

          <template #default>
            <div class="user_menu">
              <div class="user_header" @click="handlePersonalCenter">
                <img :src="BASE_URL + userStore.loginUser.avatar" alt="用户" class="avatar">
                <div class="user_info_text">
                  <span class="nickname">{{ userStore.loginUser.nickname }}</span>
                  <span class="email">{{ userStore.loginUser.email }}</span>
                </div>
              </div>

              <ul class="menu_list">
                <li @click="handlePersonalCenter">
                  <div class="menu_item_left">
                    <img :src="userIcon" alt="用户中心">
                    <span>用户中心</span>
                  </div>
                  <img :src="goRightIcon" alt="前往" class="arrow">
                </li>
                <li @click="handleOrderCenter">
                  <div class="menu_item_left">
                    <img :src="orderIcon" alt="我的订单">
                    <span>我的订单</span>
                  </div>
                  <img :src="goRightIcon" alt="前往" class="arrow">
                </li>
              </ul>

              <button class="login_out_btn" @click="handleLoginOut">
                <img :src="logoutIcon" alt="退出">
                退出登录
              </button>
            </div>
          </template>
        </el-popover>
      </template>

      <template v-else>
        <span @click="$router.push('/login')">登录/注册</span>
      </template>

    </div>
  </div>
</template>

<style scoped>
.nav_container {
  width: 100%;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 3rem;
  box-sizing: border-box;
  background: var(--primary-color);
  border-bottom: 1px solid #e5e5e5;
}

.logo {
  font-size: 20px;
  color: #1a1a1a;
  letter-spacing: 1px;
  cursor: pointer;
  transition: color 0.2s ease;
  font-family: 'CustomFont', sans-serif;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;

  img {
    height: 40px;
  }

  p{
    width: 150px;
  }
}

.search_container {
  flex: 0 1 360px;
  display: flex;
  align-items: center;
  gap: 12px;
  background: #f5f5f5;
  border-radius: 8px;
  padding: 0 16px;
  height: 40px;
  transition: background 0.2s ease;
}

.search_container:focus-within {
  background: #f0f0f0;
  outline: 2px solid #1a1a1a;
  outline-offset: -2px;
}

.search_icon {
  width: 18px;
  height: 18px;
  opacity: 0.5;
  flex-shrink: 0;
}

.search_container input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 0.9rem;
  color: #1a1a1a;
  outline: none;
}

.search_container input::placeholder {
  color: #999999;
}

.user_container {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 8px;
  transition: background 0.2s ease;

  &:hover span {
    color: #575757;
  }

  .user_info {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.user_container .avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
}

.user_container span {
  color: #1a1a1a;
  font-size: 0.9rem;
  font-weight: 500;
}

.user_menu {
  margin: 0;
  padding: 0;

  .login_out_btn {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 12px 0;
    margin-top: 8px;
    border: none;
    background: var(--primary-color);
    font-size: 14px;
    color: #000;
    cursor: pointer;
    border-radius: 8px;
    transition: all 0.2s ease;

    img {
      width: 16px;
      height: 16px;
    }

    &:hover {
      filter: brightness(1.05);
    }
  }

  .user_header {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px;
    margin: -12px -12px 0 -12px;
    background: linear-gradient(135deg, #fff9f0 0%, #fff5e6 100%);
    cursor: pointer;
    transition: background 1s ease;

    &:hover {
      background: linear-gradient(135deg, #fff5e6 0%, #ffe8cc 100%);
    }

    img.avatar {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      object-fit: cover;
      border: 2px solid #fff;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }

    .user_info_text {
      display: flex;
      flex-direction: column;
      gap: 2px;
      overflow: hidden;

      .nickname {
        font-size: 16px;
        font-weight: 600;
        color: #1a1a1a;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .email {
        font-size: 12px;
        color: #888;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }
  }

  .menu_list {
    display: flex;
    flex-direction: column;
    gap: 4px;
    margin-top: 12px;
    padding: 0 4px;

    & li {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 12px;
      padding: 10px 12px;
      cursor: pointer;
      border-radius: 8px;
      transition: all 0.2s ease;

      &:hover {
        background: #f7f7f7;

        .arrow {
          transform: translateX(4px);
        }
      }

      .menu_item_left {
        display: flex;
        align-items: center;
        gap: 12px;

        img {
          width: 18px;
          height: 18px;
          opacity: 0.7;
        }

        span {
          font-size: 14px;
          color: #333;
        }
      }

      .arrow {
        width: 14px;
        height: 14px;
        opacity: 0.4;
        transition: transform 0.2s ease;
      }
    }
  }
}
</style>