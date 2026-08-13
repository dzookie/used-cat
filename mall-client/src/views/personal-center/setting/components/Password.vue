<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { httpPost } from '@/utils/request'
import apiConfig from '@/apis/api.config'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const formData = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const formRef = ref(null)
const loading = ref(false)

const validateNewPassword = (rule, value, callback) => {
  if (value === formData.value.oldPassword) {
    callback(new Error('新密码不能与旧密码相同'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== formData.value.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为6位', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为6位', trigger: 'blur' },
    { validator: validateNewPassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const submitForm = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      loading.value = true
      const { data } = await httpPost(apiConfig.user.resetPassword, {
        oldPassword: formData.value.oldPassword,
        newPassword: formData.value.newPassword,
        confirmPassword: formData.value.confirmPassword
      })
      if (data.code === 200) {
        ElMessage.success(data.message)
        userStore.loginOut()
      } else {
        ElMessage.error(data.message)
      }
    } catch (error) {
      ElMessage.error(error.message || '密码修改失败')
    } finally {
      loading.value = false
    }
  })
}
</script>

<template>
  <div class="password_container">
    <h2 class="setting_title">重置密码</h2>

    <div class="setting_content">
      <el-form ref="formRef" :model="formData" :rules="rules" class="setting_form" label-position="left" label-width="100px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="formData.oldPassword" type="password" placeholder="请输入旧密码" class="full-width" show-password />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="formData.newPassword" type="password" placeholder="请输入新密码" class="full-width" show-password />
        </el-form-item>

        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="formData.confirmPassword" type="password" placeholder="请再次输入新密码" class="full-width" show-password />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="submitForm" class="submit_btn">确认重置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.password_container {
  padding: 24px;
}

.setting_title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 24px;
}

.setting_content {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  padding: 32px;
  max-width: 400px;
  margin: 0 auto;
  width: 100%;
}

.setting_form {
  margin-top: 16px;
  width: 100%;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #333;
}

.full-width {
  width: 100%;
}

.submit_btn {
  width: 100%;
  background: #000;
  border-color: #000;
}

.submit_btn:hover {
  background: #333;
  border-color: #333;
}
</style>
