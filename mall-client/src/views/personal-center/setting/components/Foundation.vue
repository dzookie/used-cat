<script setup>
import { computed, ref, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { httpPost, postFile } from '@/utils/request'
import apiConfig from '@/apis/api.config'
import { User, Camera } from '@element-plus/icons-vue'

const userStore = useUserStore()

const BASE_URL = import.meta.env.VITE_BASE_URL

const userInfo = computed(() => {
  return userStore.loginUser || {}
})

const avatarUrl = computed(() => {
  if (!userInfo.value.avatar) return ''
  const timestamp = Date.now()
  return `${BASE_URL}${userInfo.value.avatar}?t=${timestamp}`
})

const formData = ref({
  email: '',
  nickname: ''
})

watch(() => userInfo.value, (newVal) => {
  if (newVal) {
    formData.value.email = newVal.email || ''
    formData.value.nickname = newVal.nickname || ''
  }
}, { immediate: true })

const formRef = ref(null)

const submitForm = async () => {
  try {
    const { data } = await httpPost(apiConfig.user.updateUser, {
      userId: userInfo.value.userId,
      nickname: formData.value.nickname
    })
    if (data.code === 200) {
      ElMessage.success('修改成功')
      userStore.getCurrLoginUser(true)
    } else {
      ElMessage.error(data.message || '修改失败')
    }
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const fileInputRef = ref(null)

const handleAvatarClick = () => {
  fileInputRef.value?.click()
}

/**
 * 头像上传
 * @param event 事件对象
 */
const handleFileChange = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  const formData = new FormData()
  formData.append('userId', userInfo.value.userId)
  formData.append('avatar', file)

  try {
    const { data } = await postFile(apiConfig.user.updateAvatar, formData)
    if (data.code === 200) {
      ElMessage.success(data.message)
      userStore.getCurrLoginUser(true)
    } else {
      ElMessage.error(data.message)
    }
  } catch (error) {
    ElMessage.error(error.message)
  }

  event.target.value = ''
}
</script>

<template>
  <div class="foundation_container">
    <h2 class="setting_title">账户设置</h2>

    <div class="setting_content">
      <div class="avatar_section" @click="handleAvatarClick">
        <img v-if="userInfo.avatar" :src="avatarUrl" alt="头像" class="avatar" />
        <div v-else class="avatar_placeholder">
          <el-icon :size="40">
            <User />
          </el-icon>
        </div>
        <div class="avatar_mask">
          <el-icon :size="24">
            <Camera />
          </el-icon>
        </div>
        <input ref="fileInputRef" type="file" accept="image/*" style="display: none" @change="handleFileChange" />
      </div>

      <el-form ref="formRef" :model="formData" class="setting_form" label-position="left" label-width="60px">
        <el-form-item label="账号">
          <el-input v-model="formData.email" disabled class="full-width" />
        </el-form-item>

        <el-form-item label="昵称">
          <el-input v-model="formData.nickname" placeholder="请输入昵称" class="full-width" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm" class="submit_btn">保存</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.foundation_container {
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

.avatar_section {
  display: flex;
  justify-content: center;
  margin-bottom: 32px;
  position: relative;
  width: 80px;
  height: 80px;
  margin-left: auto;
  margin-right: auto;
  cursor: pointer;
}

.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #f0f0f0;
}

.avatar_placeholder {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.avatar_mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar_section:hover .avatar_mask {
  opacity: 1;
}

.setting_form {
  margin-top: 16px;
  width: 100%;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #333;
}

:deep(.el-input.is-disabled .el-input__wrapper) {
  background-color: #f7f7f7;
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
