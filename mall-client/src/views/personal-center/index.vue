<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import Sidebar from './components/Sidebar.vue'

const userStore = useUserStore()

const activeMenu = ref('profile')

const handleMenuChange = (key) => {
  activeMenu.value = key
}

onMounted(() => {
  if (!userStore.loginUser) {
    userStore.getCurrLoginUser()
  }
})
</script>

<template>
  <div class="personal_center">
    <Sidebar @menu-change="handleMenuChange" />
    <div class="app_content">
      <RouterView />
    </div>
  </div>
</template>

<style scoped>
.personal_center {
  display: flex;
  /* min-height: calc(100vh - 180px); */
  max-width: 1200px;
  margin: 20px auto;
  gap: 24px;
  padding: 0 20px;

  .app_content {
    flex: 1;
    min-width: 0;
    align-self: flex-start;
    background-color: #fff;
    border-radius: 12px;
    overflow: hidden;
  }
}
</style>
