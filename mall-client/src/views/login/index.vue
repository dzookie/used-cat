<script setup>
import { useRouter } from 'vue-router'
import logoIcon from '@/assets/logo.svg'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<template>
  <div class="login_page">
    <div class="top_logo" @click="goHome">
      <img :src="logoIcon" alt="logo" class="logo_icon" />
      <span class="logo_text">USED CAT</span>
    </div>
    <div class="left_section">
      <div class="logo">USED CAT<br />二手猫</div>
      <div class="illustration_container">
        <img src="@/assets/logo.svg" alt="二手猫" class="logo_img" />
      </div>
      <p class="slogan">登录发现更多好物</p>
    </div>

    <div class="right_section">
      <router-view />
    </div>
  </div>
</template>

<style scoped>
.login_page {
  display: flex;
  min-height: 100vh;
  background: #ffffff;
  position: relative;
}

.top_logo {
  position: absolute;
  top: 20px;
  left: 28px;
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  z-index: 10;
}

.top_logo .logo_icon {
  width: 32px;
  height: 32px;
}

.top_logo .logo_text {
  font-size: 1.1rem;
  font-weight: 600;
  color: #1a1a1a;
  letter-spacing: 1px;
  font-family: 'CustomFont', sans-serif;
}

.left_section {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #fafafa;
  padding: 2rem;
  border-right: 1px solid #e5e5e5;
}

.left_section .logo {
  font-size: 2rem;
  font-weight: 600;
  color: #1a1a1a;
  letter-spacing: 1px;
  margin-bottom: 2rem;
  text-align: center;
}

.illustration_container {
  width: 200px;
  height: 200px;
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo_img {
  width: 180px;
  height: 180px;
  animation: logoFloat 3s ease-in-out infinite;
}

@keyframes logoFloat {

  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-8px);
  }
}

.slogan {
  font-size: 1.1rem;
  color: #666666;
  font-weight: 500;
}

.right_section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}
</style>
