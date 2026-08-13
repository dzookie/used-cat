<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import Navigation from '@/components/Navigation.vue'
import Fotter from '@/components/Fotter.vue'

const isSticky = ref(false)
const navHeight = 64

const handleScroll = () => {
  isSticky.value = window.scrollY > 250
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<template>
  <div class="home_container">
    <div class="nav_wrapper" :class="{ sticky: isSticky }">
      <Navigation />
    </div>
    <div v-show="isSticky" :style="{ height: navHeight + 'px' }"></div>
    <RouterView />
    <Fotter />
  </div>
</template>

<style>
.home_container{
  background-color: var(--bj-color);
}

.nav_wrapper {
  z-index: 9999;
}

.nav_wrapper.sticky {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
}

.content{
  width: 80%;
  margin: 20px auto;
  background-color: #fff;
  padding: 20px;
  border-radius: 10px;
}
</style>
