<script setup>
import LikeImg from '@/assets/icons/like.svg'
import ResetImg from '@/assets/icons/reset.svg'

defineProps({
  title: {
    type: String,
    default: '商品列表'
  }
})

const emit = defineEmits(['refresh'])

const handleRefresh = () => {
  emit('refresh')
}
</script>


<template>
  <div class="title_container">
    <div class="title_content">
      <img :src="LikeImg">
      <p>{{ title }}</p>
    </div>

    <button type="button" class="refresh_button" @click="handleRefresh">
      <img :src="ResetImg" alt="换一批">
      <span>换一批</span>
    </button>
  </div>
</template>

<style scoped>
.title_container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 20px;
  

  .title_content {
    display: flex;
    align-items: center;
    font-size: 14px;
    font-weight: bold;
    border-radius: 50px;
    border: 1px solid #e5e5e5;
    padding: 8px 16px;
    background-color: var(--primary-color);
    color: #000;
    /* border: 2px solid #e5e5e5; */

    img {
      width: 20px;
      height: 20px;
      margin-right: 8px;
      border: 2px solid #000;
      border-radius: 100%;
    }
  }

  .refresh_button {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 4px 12px;
    border-radius: 50px;
    border: 1px solid #e5e5e5;
    cursor: pointer;
    transition: all 0.2s ease;
    font-size: 12px;
    background-color: #fff;

    img {
      width: 14px;
      height: 14px;
      transition: transform 0.3s ease;
    }

    &:hover {
      color: #000;
      border-color: #000;
      background-color: #fff;

      img {
        transform: rotate(180deg);
      }
    }
  }
}
</style>
