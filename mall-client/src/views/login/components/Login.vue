<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { httpPost, httpGet } from '@/utils/request'
import apiConfig from '@/apis/api.config'
import { useUserStore } from '@/stores/user'
import qqIcon from '@/assets/icons/qq.svg'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref(null)
const isLoading = ref(false)
const rememberMe = ref(false)
const apiBaseUrl = import.meta.env.VITE_BASE_URL || 'http://localhost:7777'

const loginForm = ref({
  email: '',
  password: ''
})

const loginRules = {
  email: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为6位', trigger: 'blur' }
  ]
}

const handleQqLogin = () => {
  window.location.href = apiBaseUrl + '/qq/authorize'
}

const handleLogin = async () => {
  if (!formRef.value) return
  isLoading.value = true
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const { data } = await httpPost(apiConfig.user.login, loginForm.value)
        if (data.code === 200) {
          localStorage.setItem('token', data.data)
          if (rememberMe.value) {
            localStorage.setItem('saved_email', loginForm.value.email)
            localStorage.setItem('saved_password', loginForm.value.password)
          } else {
            localStorage.removeItem('saved_email')
            localStorage.removeItem('saved_password')
          }
          await userStore.getCurrLoginUser(true)
          const redirect = route.query.redirect
          if (redirect) {
            router.push(redirect)
          } else {
            if (userStore.isAdmin()) {
              router.push('/admin')
            } else {
              router.push('/')
            }
          }
        }
        ElMessage({
          message: data.message,
          type: data.code === 200 ? 'success' : 'error'
        })

      } catch (error) {
        ElMessage.error(error.message)
      } finally {
        isLoading.value = false
      }
    }
  })
}

defineExpose({
  handleLogin
})

onMounted(() => {
  const savedEmail = localStorage.getItem('saved_email')
  const savedPassword = localStorage.getItem('saved_password')
  if (savedEmail && savedPassword) {
    loginForm.value.email = savedEmail
    loginForm.value.password = savedPassword
    rememberMe.value = true
  }

  const token = route.query.token
  if (token) {
    localStorage.setItem('token', token)
    const redirect = '/'
    router.push(redirect)
  }
})
</script>

<template>
  <div class="form_container">
    <h1 class="title">欢迎回来</h1>
    <p class="subtitle">登录您的 <i class="brand_name">USED CAT</i> 账户</p>

    <el-form ref="formRef" :model="loginForm" :rules="loginRules" class="form">
      <el-form-item prop="email">
        <label>用户名</label>
        <el-input v-model="loginForm.email" type="text" placeholder="请输入用户名" size="large" />
      </el-form-item>

      <el-form-item prop="password">
        <label>密码</label>
        <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" size="large" show-password />
      </el-form-item>

      <div class="form_options">
        <label class="remember_label">
          <input type="checkbox" v-model="rememberMe" />
          <span>记住我</span>
        </label>
        <span class="forgot_link" @click="$router.push('/forget')">忘记密码？</span>
      </div>

      <button v-loading="isLoading" type="button" class="login_btn" @click="handleLogin">登录</button>
      <div class="register_tip">
        还没有账户？
        <span class="register_link" @click="$router.push('/register')">立即注册</span>
      </div>

      <!-- <div class="third_party_login">
        <div class="divider">
          <span class="divider_text">其他登录方式</span>
        </div>
        <div class="third_party_icons">
          <img :src="qqIcon" alt="QQ登录" class="qq_icon" @click="handleQqLogin" title="QQ登录" />
        </div>
      </div> -->
    </el-form>
  </div>
</template>

<style scoped>
.form_container {
  width: 100%;
  max-width: 380px;
}

.title {
  font-size: 1.75rem;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 0.5rem;
}

.subtitle {
  font-size: 0.95rem;
  color: #666666;
  margin-bottom: 2.5rem;
}

.brand_name {
  color: var(--primary-color);
  text-shadow: 0px 2px 0px #000;
}

.form_item {
  margin-bottom: 1.5rem;
}

.form_item label {
  display: block;
  font-size: 0.9rem;
  color: #1a1a1a;
  font-weight: 500;
  margin-bottom: 0.5rem;
}

.form_item input[type="text"],
.form_item input[type="password"] {
  width: 100%;
  height: 44px;
  padding: 0 16px;
  border: 1px solid #e5e5e5;
  border-radius: 8px;
  font-size: 0.9rem;
  color: #1a1a1a;
  outline: none;
  transition: border-color 0.2s ease;
}

.form_item input::placeholder {
  color: #999999;
}

.form_item input:focus {
  border-color: #1a1a1a;
}

:deep(.el-input__inner:-webkit-autofill) {
  -webkit-box-shadow: 0 0 0 1000px #fff inset !important;
  -webkit-text-fill-color: #333 !important;
  transition: background-color 5000s ease-in-out 0s;
}

.form_options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.remember_label {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  color: #666666;
}

.remember_label input[type="checkbox"] {
  width: 16px;
  height: 16px;
  accent-color: #1a1a1a;
}

.forgot_link {
  font-size: 0.9rem;
  color: #1a1a1a;
  text-decoration: none;
  font-weight: 500;
}

.forgot_link:hover {
  text-decoration: underline;
}

.login_btn {
  width: 100%;
  height: 44px;
  background: #1a1a1a;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s ease;
}

.login_btn:hover {
  background: #333333;
}

.register_tip {
  text-align: center;
  margin-top: 1.5rem;
  font-size: 0.9rem;
  color: #666666;
}

.register_link {
  color: #1a1a1a;
  text-decoration: none;
  font-weight: 500;
  cursor: pointer;
}

.register_link:hover {
  text-decoration: underline;
}

.third_party_login {
  margin-top: 1.5rem;
}

.divider {
  display: flex;
  align-items: center;
  margin-bottom: 1.25rem;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: #e5e5e5;
}

.divider_text {
  padding: 0 1rem;
  font-size: 0.8rem;
  color: #999999;
}

.third_party_icons {
  display: flex;
  justify-content: center;
  gap: 1rem;
}

.qq_icon {
  width: 40px;
  height: 40px;
  cursor: pointer;
}
</style>
