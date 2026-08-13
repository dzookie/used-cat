<template>
  <div class="notfound">
    <img src="@/assets/icons/404.png" alt="404 Not Found" class="img"/>
    <h1>404 Not Found</h1>
    <button @click="$router.push('/')" class="back_btn">返回首页</button>
  </div>
</template>

<style scoped>
.notfound{
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  gap: 20px;
}
.back_btn{
  margin-top: 20px;
  padding: 12px 24px;
  border-radius: 8px;
  border: none;
  background: #1a1a1a;
  color: #ffffff;
  cursor: pointer;
  transition: background 0.2s ease;
}
.img{
  width: 300px;
}
</style>