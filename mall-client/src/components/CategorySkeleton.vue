<script setup>

</script>

<template>
  <div class="category_skeleton_container" v-for="value in 4">
    <div class="category_skeleton_item">
      <div class="category_skeleton_left">
        <div class="skeleton_title"></div>
        <div class="skeleton_desc"></div>
        <div class="skeleton_image"></div>
      </div>
      <div class="category_skeleton_right">
        <div class="skeleton_product" v-for="j in 2" :key="j">
          <div class="skeleton_product_image"></div>
          <div class="skeleton_product_price"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.category_skeleton_container {
  width: 100%;
  padding: 20px;
  background-color: #ffffff;
  margin: 20px auto;
  border-radius: 10px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.category_skeleton_item {
  grid-column: 1 / -1;
  display: flex;
  gap: 20px;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  overflow: hidden;
  background-color: #f5f5f5;
  padding: 10px;
}

.category_skeleton_left {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 200px;
}

.skeleton_title {
  width: 80px;
  height: 20px;
  background: linear-gradient(90deg, #e0e0e0 25%, #f0f0f0 50%, #e0e0e0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
  margin-top: 8px;
}

.skeleton_desc {
  width: 100px;
  height: 12px;
  background: linear-gradient(90deg, #e0e0e0 25%, #f0f0f0 50%, #e0e0e0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
}

.skeleton_image {
  width: 60%;
  height: 60px;
  background: linear-gradient(90deg, #e0e0e0 25%, #f0f0f0 50%, #e0e0e0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
  margin-top: 8px;
}

.category_skeleton_right {
  background-color: rgba(255, 255, 255, 0.8);
  display: flex;
  gap: 10px;
  width: 100%;
  padding: 10px;
  border-radius: 10px;
}

.skeleton_product {
  display: flex;
  flex-direction: column;
  gap: 3px;
  align-items: center;
}

.skeleton_product_image {
  width: 70px;
  height: 70px;
  background: linear-gradient(90deg, #e0e0e0 25%, #f0f0f0 50%, #e0e0e0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 10px;
}

.skeleton_product_price {
  width: 50px;
  height: 12px;
  background: linear-gradient(90deg, #e0e0e0 25%, #f0f0f0 50%, #e0e0e0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
</style>
