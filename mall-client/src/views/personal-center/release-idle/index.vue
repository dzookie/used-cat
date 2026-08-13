<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { httpGet, postFile, httpPost } from '@/utils/request'
import apiConfig from '@/apis/api.config'
import { Plus, Delete, View, MagicStick } from '@element-plus/icons-vue'

const userStore = useUserStore()
const BASE_URL = import.meta.env.VITE_BASE_URL

const formRef = ref(null)
const categoryList = ref([])

const formData = ref({
  images: [],
  commodityName: 'xxxxx',
  commodityDesc: '',
  commodityType: '',
  price: 0,
  brand: '',
  useStatus: ''
})

const rules = {
  images: [
    {
      required: true,
      message: '请上传商品图片',
      trigger: 'change',
      validator: (rule, value, callback) => {
        if (formData.value.images.length === 0) {
          callback(new Error('请上传商品图片'))
        } else {
          callback()
        }
      }
    }
  ],
  commodityName: [
    { required: true, message: '请输入商品名称', trigger: 'blur' }
  ],
  commodityDesc: [
    { required: true, message: '请输入商品描述', trigger: 'blur' },
    { min: 5, message: '描述至少5个字符', trigger: 'blur' }
  ],
  commodityType: [
    { required: true, message: '请选择商品类型', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入商品价格', trigger: 'blur' },
    { pattern: /^\d+(\.\d{1,2})?$/, message: '请输入有效的价格', trigger: 'blur' }
  ],
  brand: [
    { required: true, message: '请输入品牌', trigger: 'blur' }
  ],
  useStatus: [
    { required: true, message: '请选择使用程度', trigger: 'change' }
  ]
}

const loading = ref(false)
const beautifying = ref(false)
const showViewer = ref(false)
const previewUrlList = ref([])
const currentImageIndex = ref(0)

const handleImagePreview = (index) => {
  currentImageIndex.value = index
  previewUrlList.value = formData.value.images.map(img => BASE_URL + img)
  showViewer.value = true
}

/**
 * 预览商品图片
 * @param {Object} file - 上传的文件对象
 */
const handlePreview = (file) => {
  previewUrl.value = file.url
  showViewer.value = true
}

/**
 * 获取商品分类列表
 */
const getCategoryList = async () => {
  try {
    const { data } = await httpGet(apiConfig.category.getCategoryList, null)
    if (data.code === 200) {
      categoryList.value = data.data.map(item => ({
        value: item.typeId,
        label: item.typeName
      }))
    }
  } catch (error) {
    ElMessage.error('获取分类失败')
  }
}



const handleImageChange = async (file, files) => {
  const isImage = file.raw.type.startsWith('image/')
  const isLt5M = file.raw.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB')
    return false
  }

  try {
    const formDataObj = new FormData()
    formDataObj.append('avatar', file.raw)

    const { data } = await postFile(apiConfig.commodity.uploadCommunityImg, formDataObj)

    if (data.code === 200) {
      file.url = BASE_URL + data.data
      formData.value.images.push(data.data)
      return true
    } else {
      ElMessage.error(data.message || '图片上传失败')
      return false
    }
  } catch (error) {
    ElMessage.error('图片上传失败')
    return false
  }
}

const submitForm = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    console.log(userStore.loginUser)

    try {
      const { data } = await httpPost(apiConfig.commodity.addCommodity, {
        ...formData.value,
        userId: userStore.loginUser.userId
      })

      if (data.code === 200) {
        ElMessage.success('发布成功')
        resetForm()
      } else {
        ElMessage.error(data.message || '发布失败')
      }
    } catch (error) {
      ElMessage.error('错误：' + error.message || '发布失败')
    } finally {
      loading.value = false
    }
  })
}

/**
 * AI美化商品描述
 */
const beautifyDescription = async () => {
  if (!formData.value.commodityDesc.trim()) {
    ElMessage.warning('请先输入商品描述')
    return
  }
  beautifying.value = true
  try {
    const { data } = await httpPost(apiConfig.ai.beautifyDescription, null, {
      params: { text: formData.value.commodityDesc }
    })
    if (data.code === 200) {
      formData.value.commodityDesc = data.data
      ElMessage.success('美化完成')
    } else {
      ElMessage.error(data.message || '美化失败')
    }
  } catch (error) {
    ElMessage.error('AI美化请求失败')
  } finally {
    beautifying.value = false
  }
}

const resetForm = () => {
  formRef.value?.resetFields()
  formData.value.images = []
}

const removeImage = (index) => {
  formData.value.images.splice(index, 1)
}

onMounted(() => {
  getCategoryList()
})
</script>

<template>
  <div class="release_container">
    <h2 class="release_title">发布闲置</h2>

    <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px" class="release_form">
      <el-form-item label="商品图片" prop="images">
        <div class="image_upload">
          <div class="image-list">
            <div v-for="(img, index) in formData.images" :key="index" class="image-item">
              <el-image :src="BASE_URL + img" fit="cover" class="preview-image" @click="handleImagePreview(index)" />
              <div class="image-actions">
                <el-icon @click.stop="handleImagePreview(index)">
                  <View />
                </el-icon>
                <el-icon @click.stop="removeImage(index)">
                  <Delete />
                </el-icon>
              </div>
            </div>
            <el-upload v-if="formData.images.length < 9" class="upload-btn" action="#" :auto-upload="false"
              :show-file-list="false" :on-change="handleImageChange" accept="image/*">
              <el-icon class="upload_icon">
                <Plus />
              </el-icon>
            </el-upload>
          </div>
          <el-image-viewer v-if="showViewer" :url-list="previewUrlList" :initial-index="currentImageIndex" @close="showViewer = false" />
          <div class="upload_tip">最多上传9张图片，支持jpg、png格式，大小不超过5MB</div>
        </div>
      </el-form-item>

      <!-- <el-form-item label="商品名称" prop="commodityName">
        <el-input v-model="formData.commodityName" placeholder="请输入商品名称" />
      </el-form-item> -->

      <el-form-item label="商品描述" prop="commodityDesc">
        <div class="desc_wrapper">
          <el-input v-model="formData.commodityDesc" type="textarea" :rows="5" placeholder="请描述商品的详细信息、新旧程度、使用感受等"
            maxlength="500" show-word-limit class="desc_input" />
          <el-tooltip content="AI美化" placement="right" effect="light">
            <el-button :loading="beautifying" :icon="MagicStick" @click="beautifyDescription" class="ai_btn" size="small" circle></el-button>
          </el-tooltip>
        </div>
      </el-form-item>

      <el-form-item label="商品类型" prop="commodityType">
        <el-select v-model="formData.commodityType" placeholder="请选择商品类型" style="width: 100%">
          <el-option v-for="item in categoryList" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>

      <el-form-item label="价格" prop="price">
        <el-input-number v-model="formData.price" :precision="2" :step="0.1" :min="0" placeholder="请输入价格"
          style="width: 100%" />
      </el-form-item>

      <el-form-item label="品牌" prop="brand">
        <el-input v-model="formData.brand" placeholder="请输入品牌名称/无" />
      </el-form-item>

      <el-form-item label="使用程度" prop="useStatus">
        <el-select v-model="formData.useStatus" placeholder="请选择或输入使用程度" style="width: 100%" filterable allow-create
          default-first-option>
          <el-option label="全新" value="全新" />
          <el-option label="几乎全新" value="几乎全新" />
          <el-option label="轻微使用痕迹" value="轻微使用痕迹" />
          <el-option label="全新未拆封" value="全新未拆封" />
          <el-option label="明显磕碰划痕" value="明显磕碰划痕" />
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="submitForm" :loading="loading" class="submit_btn">
          发布闲置
        </el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<style scoped>
.release_container {
  padding: 24px;
  background: #fff;
  border-radius: 8px;
  min-height: calc(100vh - 100px);
}

.release_title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 24px;
}

.release_form {
  max-width: 600px;
}

.image_upload :deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
  line-height: 100px;
}

.image_upload :deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 100px;
  height: 100px;
}

.upload_icon {
  font-size: 24px;
  color: #8c939d;
}

.upload_tip {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.image-item {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
}

.preview-image {
  width: 100%;
  height: 100%;
}

.image-actions {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
  opacity: 0;
  transition: opacity 0.3s;
}

.image-item:hover .image-actions {
  opacity: 1;
}

.image-actions .el-icon {
  font-size: 20px;
  color: #fff;
  cursor: pointer;
}

.upload-btn {
  width: 100px;
  height: 100px;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: border-color 0.3s;
}

.upload-btn:hover {
  border-color: #000;
}

.submit_btn {
  width: 120px;
  background-color: var(--primary-color);
  color: #000;
  border: 1px solid #000;
}

.ai_btn {
  background-color: var(--primary-color);
  color: #000;
  border: 1px solid #000;
  flex-shrink: 0;
}

.desc_wrapper {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  width: 100%;
}

.desc_input {
  flex: 1;
}
</style>
