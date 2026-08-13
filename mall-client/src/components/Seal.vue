<script setup>

</script>

<template>
  <div class="seal_container">
    <div class="line"></div>
  </div>
</template>

<style scoped>
.seal_container {
  width: 100vw;
  height: 100vh;
  background-color: transparent;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 9999;

  .line {
    width: 100%;
    height: 20px;
    background-color: var(--primary-color);
  }
}
</style>