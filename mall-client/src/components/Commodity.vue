<script setup>
const baseUrl = import.meta.env.VITE_BASE_URL

defineProps({
  commodity: {
    type: Object,
    default: {}
  }
})
</script>

<template>
  <div class="commodity_item">
    <div class="image_wrapper">
      <img :src="baseUrl + commodity.albums[0]?.path" class="commodity_image">
    </div>
    <div class="info_wrapper">
      <h3 class="title">{{ commodity.commodityDesc }}</h3>
      <div class="bottom_info">
        <span class="price"><span class="yuan">¥</span><span class="price_value">{{ commodity.price }}</span></span>
        <span class="sales">{{ commodity.browse }}人想要</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.commodity_item {
  background: #ffffff;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s ease;
  border-bottom: 2px solid var(--primary-color);
  /* border: 2px solid #e5e5e5; */
}

.commodity_item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.image_wrapper {
  width: 100%;
  height: 200px;
  aspect-ratio: 1;
  overflow: hidden;
  background: #fafafa;
}

.commodity_image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.commodity_item:hover .commodity_image {
  transform: scale(1.05);
}

.info_wrapper {
  padding: 12px;
}

.title {
  font-size: 0.9rem;
  font-weight: 500;
  color: #1a1a1a;
  margin: 0 0 8px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  word-break: break-word;
  line-height: 1.4;
  max-height: 2.8em;
}

.bottom_info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price {
  font-size: 1.1rem;
  font-weight: 600;
  color: #ff4757;
  font-family: 'CustomFont', sans-serif;
}

.price .yuan {
  font-size: 0.8rem;
  font-weight: 500;
}

.price .price_value {
  font-size: 1.1rem;
}

.sales {
  font-size: 0.8rem;
  color: #999999;
}
</style>
