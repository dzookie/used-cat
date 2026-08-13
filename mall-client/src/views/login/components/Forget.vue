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

const forgetForm = ref({
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const forgetRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== forgetForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const sendCode = async () => {
  if (countdown.value > 0) return

  if (!forgetForm.value.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }

  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailPattern.test(forgetForm.value.email)) {
    ElMessage.warning('请输入正确的邮箱格式')
    return
  }

  try {
    isSendingCode.value = true
    const { data } = await httpGet(apiConfig.user.sendForgetCode, { email: forgetForm.value.email })
    if (data.code === 200) {
      ElMessage.success(data.msg || '验证码已发送')
      countdown.value = 60
      const timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    } else {
      ElMessage.error(data.msg || '发送失败')
    }
  } catch (error) {
    ElMessage.error('发送验证码失败')
  } finally {
    isSendingCode.value = false
  }
}

const handleReset = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      isLoading.value = true
      const { data } = await httpPost(apiConfig.user.forgotPassword, {
        email: forgetForm.value.email,
        code: forgetForm.value.code,
        newPassword: forgetForm.value.newPassword
      })
      if (data.code === 200) {
        ElMessage.success(data.msg || '密码重置成功')
        router.push('/login')
      } else {
        ElMessage.error(data.msg || '重置失败')
      }
    } catch (error) {
      ElMessage.error('重置密码失败')
    } finally {
      isLoading.value = false
    }
  })
}
</script>

<template>
  <div class="form_container">
    <h1 class="title">忘记密码</h1>
    <p class="subtitle">通过邮箱验证码重置你的密码</p>

    <el-form ref="formRef" :model="forgetForm" :rules="forgetRules" class="form">
      <el-form-item prop="email">
        <label>邮箱</label>
        <el-input v-model="forgetForm.email" type="text" placeholder="请输入注册邮箱" size="large" autocomplete="off" />
      </el-form-item>

      <el-form-item prop="code">
        <label class="code_label">验证码</label>
        <div class="code_input_wrapper">
          <el-input v-model="forgetForm.code" type="text" placeholder="请输入验证码" size="large" maxlength="6"
            autocomplete="off" />
          <button v-loading="isSendingCode" type="button" class="send_code_btn"
            :disabled="countdown > 0 || isSendingCode" @click="sendCode">
            {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
          </button>
        </div>
      </el-form-item>

      <el-form-item prop="newPassword">
        <label>新密码</label>
        <el-input v-model="forgetForm.newPassword" type="password" placeholder="请输入新密码" size="large" show-password
          autocomplete="new-password" />
      </el-form-item>

      <el-form-item prop="confirmPassword">
        <label>确认密码</label>
        <el-input v-model="forgetForm.confirmPassword" type="password" placeholder="请再次输入新密码" size="large"
          show-password autocomplete="new-password" />
      </el-form-item>

      <button v-loading="isLoading" type="button" class="reset_btn" @click="handleReset">重置密码</button>
      <div class="login_tip">
        想起密码了？
        <span class="login_link" @click="$router.push('/login')">返回登录</span>
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

.code_label {
  display: block;
  width: 100%;
  font-size: 0.9rem;
  color: #1a1a1a;
  font-weight: 500;
  margin-bottom: 0.5rem;
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

.reset_btn {
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

.reset_btn:hover {
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
