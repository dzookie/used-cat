<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { httpPost, httpGet } from '@/utils/request'
import apiConfig from '@/apis/api.config'

const router = useRouter()
const formRef = ref(null)
const isLoading = ref(false)
const isSendingCode = ref(false)
const countdown = ref(0)

const registerForm = ref({
  email: '',
  code: '',
  password: '',
  confirmPassword: ''
})

const loginRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.value.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

/**
 * 发送邮箱验证码
 */
const sendCode = async () => {
  if (countdown.value > 0) return

  if (!registerForm.value.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }

  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailPattern.test(registerForm.value.email)) {
    ElMessage.warning('请输入正确的邮箱格式')
    return
  }

  try {
    isSendingCode.value = true
    const { data } = await httpGet(apiConfig.user.sendCode, { email: registerForm.value.email })
    if (data.code === 200) {
      ElMessage.success(data.message)
      countdown.value = 60
      const timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    } else {
      ElMessage.error(data.message)
    }
  } catch (error) {
    ElMessage.error(error.message || '发送验证码失败')
  } finally {
    isSendingCode.value = false
  }
}

/**
 * 注册
 */
const handleRegister = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) {
      isLoading.value = false
      return
    }
    try {
      isLoading.value = true
      const { data } = await httpPost(apiConfig.user.register, {
        email: registerForm.value.email,
        code: registerForm.value.code,
        password: registerForm.value.password
      })
      if (data.code === 200) {
        ElMessage.success(data.message)
        router.push('/login')
      } else {
        ElMessage.error(data.message)
      }
    } catch (error) {
      ElMessage.error(error.message || '注册失败')
    } finally {
      isLoading.value = false
    }
  })
}
</script>

<template>
  <div class="form_container">
    <h1 class="title">欢迎注册</h1>
    <p class="subtitle">加入 <i class="brand_name">USED CAT</i> 发现更多好物</p>

    <el-form ref="formRef" :model="registerForm" :rules="loginRules" class="form">
      <el-form-item prop="email">
        <label>邮箱</label>
        <el-input v-model="registerForm.email" type="text" placeholder="请输入邮箱" size="large" autocomplete="off" />
      </el-form-item>

      <el-form-item prop="code">
        <label class="code_label">验证码</label>
        <div class="code_input_wrapper">
          <el-input v-model="registerForm.code" type="text" placeholder="请输入验证码" size="large" maxlength="6"
            autocomplete="off" />
          <button v-loading="isSendingCode" type="button" class="send_code_btn" :disabled="countdown > 0 || isSendingCode" @click="sendCode">
            {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
          </button>
        </div>
      </el-form-item>

      <el-form-item prop="password">
        <label>密码</label>
        <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" size="large" show-password
          autocomplete="new-password" />
      </el-form-item>

      <el-form-item prop="confirmPassword">
        <label>确认密码</label>
        <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码" size="large"
          show-password autocomplete="new-password" />
      </el-form-item>

      <button v-loading="isLoading" type="button" class="register_btn" @click="handleRegister">注册</button>
      <div class="login_tip">
        已有账户？
        <span class="login_link" @click="$router.push('/login')">立即登录</span>
      </div>
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

.code_label {
  display: block;
  width: 100%;
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

.code_input_wrapper {
  display: flex;
  width: 100%;
  gap: 10px;
}

.code_input_wrapper :deep(.el-input) {
  flex: 1;
}

.send_code_btn {
  width: 110px;
  height: 44px;
  background: #1a1a1a;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  font-size: 0.85rem;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s ease;
  white-space: nowrap;
}

.send_code_btn:hover:not(:disabled) {
  background: #333333;
}

.send_code_btn:disabled {
  background: #cccccc;
  cursor: not-allowed;
}

.register_btn {
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
  margin-top: 0.5rem;
}

.register_btn:hover {
  background: #333333;
}

.login_tip {
  text-align: center;
  margin-top: 1.5rem;
  font-size: 0.9rem;
  color: #666666;
}

.login_link {
  color: #1a1a1a;
  text-decoration: none;
  font-weight: 500;
  cursor: pointer;
}

.login_link:hover {
  text-decoration: underline;
}
</style>
