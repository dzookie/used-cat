<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Upload, Document, View } from '@element-plus/icons-vue'
import { httpGet, httpPost, httpPut, httpDelete, postFile } from '@/utils/request'
import apiConfig from '@/apis/api.config'
import DocPreview from '@/components/DocPreview.vue'
import SparkMD5 from 'spark-md5'

const keyword = ref('')
const typeFilter = ref('')
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const loading = ref(false)

// 文本对话框
const textDialogVisible = ref(false)
const textFormRef = ref(null)
const textFormData = reactive({
  id: null,
  title: '',
  content: ''
})
const textRules = {
  title: [{ required: true, message: '请输入文档标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入文档内容', trigger: 'blur' }]
}

// PDF上传
const CHUNK_SIZE = 5 * 1024 * 1024
const MAX_RETRY = 3
const CONCURRENT_LIMIT = 3

const uploadDialogVisible = ref(false)
const pdfTitle = ref('')
const pdfFile = ref(null)
const uploading = ref(false)
const uploadProgress = ref(0)
const uploadSpeed = ref('')
const uploadedSize = ref(0)
const totalChunks = ref(0)
const uploadedChunks = ref(0)
const uploadPaused = ref(false)
const isCalculatingMd5 = ref(false)

let abortController = null
let uploadedBytesHistory = []

// 文档预览
const previewVisible = ref(false)
const previewRow = ref(null)

const typeOptions = [
  { value: 'text', label: '纯文本' },
  { value: 'pdf', label: 'PDF文档' }
]

const formatFileSize = (bytes) => {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}

const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

const getTypeText = (type) => {
  const found = typeOptions.find(t => t.value === type)
  return found ? found.label : type || '-'
}

const getTypeTagType = (type) => {
  return type === 'pdf' ? 'primary' : 'success'
}

const getVectorStatusText = (status) => {
  return status === 1 ? '已向量化' : '未向量化'
}

const getVectorStatusType = (status) => {
  return status === 1 ? 'success' : 'warning'
}

const fetchList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    if (keyword.value.trim()) {
      params.keyword = keyword.value.trim()
    }
    if (typeFilter.value) {
      params.type = typeFilter.value
    }
    const res = await httpGet(apiConfig.admin.knowledgeList, params)
    const data = res.data
    if (data.code === 200) {
      tableData.value = data.data.items || []
      total.value = data.data.total || 0
    } else {
      ElMessage.error(data.message || '查询失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchList()
}

const handleReset = () => {
  keyword.value = ''
  typeFilter.value = ''
  currentPage.value = 1
  fetchList()
}

const handleRefresh = () => {
  fetchList()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  fetchList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchList()
}

// 新增文本
const handleAddText = () => {
  textFormData.id = null
  textFormData.title = ''
  textFormData.content = ''
  textDialogVisible.value = true
}

// 编辑文本
const handleEdit = (row) => {
  if (row.type !== 'text') {
    ElMessage.warning('PDF类型文档不支持直接编辑')
    return
  }
  textFormData.id = row.id
  textFormData.title = row.title
  textFormData.content = row.content || ''
  textDialogVisible.value = true
}

const handleTextSubmit = async () => {
  if (!textFormRef.value) return
  try {
    await textFormRef.value.validate()
    const isAdd = !textFormData.id
    const payload = {
      title: textFormData.title,
      content: textFormData.content
    }
    let res
    if (isAdd) {
      res = await httpPost(apiConfig.admin.addKnowledgeText, payload)
    } else {
      payload.id = textFormData.id
      res = await httpPut(apiConfig.admin.updateKnowledge, payload)
    }
    const data = res.data
    if (data.code === 200) {
      ElMessage.success(isAdd ? '新增成功' : '更新成功')
      textDialogVisible.value = false
      fetchList()
    } else {
      ElMessage.error(data.message || '操作失败')
    }
  } catch (e) {
    if (e !== false) {
      console.error(e)
      ElMessage.error('操作失败')
    }
  }
}

// 上传PDF
const handleUploadPdf = () => {
  pdfTitle.value = ''
  pdfFile.value = null
  uploadProgress.value = 0
  uploadSpeed.value = ''
  uploadedSize.value = 0
  totalChunks.value = 0
  uploadedChunks.value = 0
  uploadPaused.value = false
  isCalculatingMd5.value = false
  uploadedBytesHistory = []
  uploadDialogVisible.value = true
}

const handleFileChange = (file) => {
  if (!file.name.toLowerCase().endsWith('.pdf')) {
    ElMessage.error('仅支持PDF格式文件')
    return false
  }
  pdfFile.value = file.raw
  if (!pdfTitle.value) {
    pdfTitle.value = file.name.replace('.pdf', '').replace('.PDF', '')
  }
  return false
}

const calculateFileMd5 = (file) => {
  return new Promise((resolve, reject) => {
    const blobSlice = File.prototype.slice || File.prototype.mozSlice || File.prototype.webkitSlice
    const chunkSize = 2097152
    const chunks = Math.ceil(file.size / chunkSize)
    let currentChunk = 0
    const spark = new SparkMD5.ArrayBuffer()
    const fileReader = new FileReader()

    fileReader.onload = (e) => {
      spark.append(e.target.result)
      currentChunk++
      if (currentChunk < chunks) {
        loadNext()
      } else {
        resolve(spark.end())
      }
    }

    fileReader.onerror = () => {
      reject(new Error('MD5计算失败'))
    }

    function loadNext() {
      const start = currentChunk * chunkSize
      const end = ((start + chunkSize) >= file.size) ? file.size : start + chunkSize
      fileReader.readAsArrayBuffer(blobSlice.call(file, start, end))
    }

    loadNext()
  })
}

const updateProgress = (uploaded, total) => {
  uploadedSize.value = uploaded
  uploadProgress.value = total > 0 ? Math.min(Math.round((uploaded / total) * 100), 100) : 0
  const now = Date.now()
  uploadedBytesHistory.push({ bytes: uploaded, time: now })
  uploadedBytesHistory = uploadedBytesHistory.filter(item => now - item.time < 3000)
  if (uploadedBytesHistory.length >= 2) {
    const first = uploadedBytesHistory[0]
    const last = uploadedBytesHistory[uploadedBytesHistory.length - 1]
    const timeDiff = (last.time - first.time) / 1000
    const byteDiff = last.bytes - first.bytes
    if (timeDiff > 0 && byteDiff > 0) {
      const speed = byteDiff / timeDiff
      if (speed < 1024) {
        uploadSpeed.value = speed.toFixed(1) + ' B/s'
      } else if (speed < 1024 * 1024) {
        uploadSpeed.value = (speed / 1024).toFixed(1) + ' KB/s'
      } else {
        uploadSpeed.value = (speed / (1024 * 1024)).toFixed(2) + ' MB/s'
      }
    }
  }
}

const uploadChunkWithRetry = async (file, fileMd5, chunkIndex, chunkTotal, retryCount = 0) => {
  if (uploadPaused.value) {
    await new Promise(resolve => {
      const checkPause = setInterval(() => {
        if (!uploadPaused.value) {
          clearInterval(checkPause)
          resolve()
        }
      }, 100)
    })
  }

  const start = chunkIndex * CHUNK_SIZE
  const end = Math.min(start + CHUNK_SIZE, file.size)
  const chunk = file.slice(start, end)

  const formData = new FormData()
  formData.append('file', chunk, chunkIndex + '.chunk')
  formData.append('fileMd5', fileMd5)
  formData.append('chunkIndex', chunkIndex)
  formData.append('chunkTotal', chunkTotal)

  try {
    const res = await postFile(apiConfig.admin.knowledgeChunkUpload, formData)
    const data = res.data
    if (data.code === 200) {
      uploadedChunks.value++
      updateProgress(uploadedChunks.value * CHUNK_SIZE, file.size)
      return true
    } else {
      throw new Error(data.message || '分片上传失败')
    }
  } catch (e) {
    if (retryCount < MAX_RETRY) {
      return uploadChunkWithRetry(file, fileMd5, chunkIndex, chunkTotal, retryCount + 1)
    }
    throw e
  }
}

const uploadChunksConcurrently = async (file, fileMd5, chunkTotal, skipChunks = []) => {
  const skipSet = new Set(skipChunks)
  const chunksToUpload = []
  for (let i = 0; i < chunkTotal; i++) {
    if (!skipSet.has(i)) {
      chunksToUpload.push(i)
    }
  }

  uploadedChunks.value = skipChunks.length
  updateProgress(uploadedChunks.value * CHUNK_SIZE, file.size)

  let currentIndex = 0
  const results = new Array(chunkTotal).fill(false)

  const worker = async () => {
    while (currentIndex < chunksToUpload.length) {
      const idx = currentIndex++
      const chunkIndex = chunksToUpload[idx]
      try {
        const success = await uploadChunkWithRetry(file, fileMd5, chunkIndex, chunkTotal)
        results[chunkIndex] = success
      } catch (e) {
        results[chunkIndex] = false
        throw e
      }
    }
  }

  const workers = []
  const limit = Math.min(CONCURRENT_LIMIT, chunksToUpload.length)
  for (let i = 0; i < limit; i++) {
    workers.push(worker())
  }

  await Promise.all(workers)
  return results.every(r => r)
}

const handleUploadSubmit = async () => {
  if (!pdfFile.value) {
    ElMessage.warning('请选择PDF文件')
    return
  }
  if (!pdfTitle.value.trim()) {
    ElMessage.warning('请输入文档标题')
    return
  }

  const file = pdfFile.value
  uploading.value = true
  uploadProgress.value = 0
  uploadSpeed.value = ''
  uploadedChunks.value = 0
  uploadPaused.value = false
  uploadedBytesHistory = []

  try {
    isCalculatingMd5.value = true
    const fileMd5 = await calculateFileMd5(file)
    isCalculatingMd5.value = false

    const chunks = Math.ceil(file.size / CHUNK_SIZE)
    totalChunks.value = chunks

    const checkRes = await httpGet(apiConfig.admin.knowledgeChunkCheck, {
      fileMd5,
      fileName: file.name
    })
    const checkData = checkRes.data
    let skipChunks = []
    if (checkData.code === 200 && checkData.data && checkData.data.uploadedChunks) {
      skipChunks = checkData.data.uploadedChunks
      if (skipChunks.length > 0) {
        ElMessage.info(`检测到 ${skipChunks.length} 个已上传分片，将断点续传`)
      }
    }

    abortController = new AbortController()

    const allUploaded = await uploadChunksConcurrently(file, fileMd5, chunks, skipChunks)

    if (allUploaded) {
      const mergeFormData = new FormData()
      mergeFormData.append('fileMd5', fileMd5)
      mergeFormData.append('fileName', file.name)
      mergeFormData.append('title', pdfTitle.value.trim())
      mergeFormData.append('fileSize', file.size)
      const mergeRes = await postFile(apiConfig.admin.knowledgeChunkMerge, mergeFormData)
      const mergeData = mergeRes.data
      if (mergeData.code === 200) {
        ElMessage.success('上传成功')
        uploadDialogVisible.value = false
        fetchList()
      } else {
        ElMessage.error(mergeData.message || '合并失败')
      }
    } else {
      ElMessage.error('上传失败，部分分片未上传成功')
    }
  } catch (e) {
    console.error(e)
    if (e.message && e.message.includes('取消')) {
      ElMessage.info('上传已取消')
    } else {
      ElMessage.error('上传失败：' + (e.message || '未知错误'))
    }
  } finally {
    uploading.value = false
    isCalculatingMd5.value = false
    abortController = null
  }
}

const handlePauseResume = () => {
  uploadPaused.value = !uploadPaused.value
}

const handleCancelUpload = () => {
  ElMessageBox.confirm('确定要取消上传吗？已上传的分片将保留，下次可续传', '取消上传', {
    confirmButtonText: '确定取消',
    cancelButtonText: '继续上传',
    type: 'warning'
  }).then(() => {
    if (abortController) {
      abortController.abort()
    }
    uploading.value = false
    uploadPaused.value = false
  }).catch(() => {})
}

const vectorizingIds = ref(new Set())

const handleVectorize = (row) => {
  if (vectorizingIds.value.has(row.id)) {
    ElMessage.warning('正在向量化中，请稍候...')
    return
  }
  if (row.vectorStatus === 1) {
    ElMessageBox.confirm(
      `文档「${row.title}」已向量化，是否重新向量化？`,
      '重新向量化',
      {
        confirmButtonText: '确定重新向量化',
        cancelButtonText: '取消',
        type: 'warning',
        customClass: 'knowledge-msgbox'
      }
    ).then(() => {
      doVectorize(row)
    }).catch(() => {})
    return
  }
  ElMessageBox.confirm(
    `确定要将文档「${row.title}」向量化并存入向量数据库吗？`,
    '向量化确认',
    {
      confirmButtonText: '开始向量化',
      cancelButtonText: '取消',
      type: 'primary',
      customClass: 'knowledge-msgbox'
    }
  ).then(() => {
    doVectorize(row)
  }).catch(() => {})
}

const doVectorize = async (row) => {
  vectorizingIds.value.add(row.id)
  try {
    const res = await httpPost(
      apiConfig.admin.knowledgeVectorize + '/' + row.id,
      {},
      { timeout: 5 * 60 * 1000 }
    )
    const data = res.data
    if (data.code === 200) {
      ElMessage.success(`向量化成功，共 ${data.data.chunkCount} 个切片`)
      fetchList()
    } else {
      ElMessage.error(data.message || '向量化失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('向量化失败：' + (e.message || '未知错误'))
  } finally {
    vectorizingIds.value.delete(row.id)
  }
}

// 查看文档
const handlePreview = (row) => {
  previewRow.value = row
  console.log(row);
  
  previewVisible.value = true
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除文档「${row.title}」吗？此操作不可恢复。`,
    '删除文档',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'error'
    }
  ).then(async () => {
    try {
      const res = await httpDelete(`${apiConfig.admin.deleteKnowledge}/${row.id}`)
      const data = res.data
      if (data.code === 200) {
        ElMessage.success('删除成功')
        fetchList()
      } else {
        ElMessage.error(data.message || '删除失败')
      }
    } catch (e) {
      console.error(e)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchList()
})
</script>

<template>
  <div class="knowledge-page">
    <!-- 标题 + 筛选区 -->
    <div class="filter-card">
      <div class="page-title-wrap">
        <h1 class="page-title">知识库管理</h1>
        <p class="page-desc">管理平台AI知识库文档，为智能客服提供知识支撑</p>
      </div>
      <div class="filter-form">
        <div class="filter-item">
          <label class="filter-label">关键字</label>
          <el-input
            v-model="keyword"
            placeholder="搜索文档标题"
            clearable
            class="filter-keyword-input"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
        <div class="filter-item">
          <label class="filter-label">文档类型</label>
          <el-select v-model="typeFilter" placeholder="全部类型" class="filter-input" clearable>
            <el-option
              v-for="item in typeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </div>
        <div class="filter-actions">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button class="btn-reset" @click="handleReset">重置</el-button>
        </div>
      </div>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" @click="handleAddText">
          <el-icon><Plus /></el-icon>
          新增文本知识
        </el-button>
        <el-button type="primary" plain @click="handleUploadPdf">
          <el-icon><Upload /></el-icon>
          上传PDF文档
        </el-button>
      </div>
      <div class="toolbar-right">
        <el-button class="btn-refresh" circle @click="handleRefresh" title="刷新">
          <el-icon><Refresh /></el-icon>
        </el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="table-card">
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="70">
          <template #default="{ row }">
            <span class="doc-id">{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="文档标题" min-width="200">
          <template #default="{ row }">
            <div class="title-cell">
              <el-icon class="title-icon" v-if="row.type === 'pdf'"><Document /></el-icon>
              <span class="title-text">{{ row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)" effect="plain" size="small">
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="文件大小" width="110">
          <template #default="{ row }">
            <span class="size-text">{{ formatFileSize(row.fileSize) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="chunkCount" label="分块数" width="90">
          <template #default="{ row }">
            {{ row.chunkCount || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="vectorStatus" label="向量化" width="100">
          <template #default="{ row }">
            <el-tag :type="getVectorStatusType(row.vectorStatus)" effect="plain" size="small">
              {{ getVectorStatusText(row.vectorStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button type="primary" link @click="handlePreview(row)">查看</el-button>
              <el-button
                v-if="row.type === 'text'"
                type="primary"
                link
                @click="handleEdit(row)"
              >
                编辑
              </el-button>
              <el-button
                :type="row.vectorStatus === 1 ? 'success' : 'warning'"
                link
                :loading="vectorizingIds.has(row.id)"
                @click="handleVectorize(row)"
              >
                {{ row.vectorStatus === 1 ? '已向量化' : '向量化' }}
              </el-button>
              <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-area">
      <div class="pagination-info">共 {{ total }} 条数据</div>
      <el-pagination
        background
        layout="prev, pager, next, sizes, jumper"
        :total="total"
        :current-page="currentPage"
        :page-size="pageSize"
        :page-sizes="[20, 50, 100]"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 新增/编辑文本 对话框 -->
    <el-dialog
      v-model="textDialogVisible"
      :title="textFormData.id ? '编辑知识文档' : '新增文本知识'"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="textFormRef"
        :model="textFormData"
        :rules="textRules"
        label-width="100px"
      >
        <el-form-item label="文档标题" prop="title">
          <el-input v-model="textFormData.title" placeholder="请输入文档标题" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="文档内容" prop="content">
          <el-input
            v-model="textFormData.content"
            type="textarea"
            :rows="12"
            placeholder="请输入知识内容，内容越详细，AI回答越准确"
            maxlength="10000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="textDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleTextSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 上传PDF 对话框 -->
    <el-dialog
      v-model="uploadDialogVisible"
      title="上传PDF文档"
      width="560px"
      :close-on-click-modal="false"
      :close-on-press-escape="!uploading"
    >
      <el-form label-width="100px">
        <el-form-item label="文档标题">
          <el-input v-model="pdfTitle" placeholder="请输入文档标题（默认使用文件名）" maxlength="200" show-word-limit :disabled="uploading" />
        </el-form-item>
        <el-form-item label="选择文件">
          <el-upload
            :auto-upload="false"
            :show-file-list="true"
            :limit="1"
            accept=".pdf"
            :on-change="handleFileChange"
            :on-remove="() => { pdfFile = null }"
            :disabled="uploading"
          >
            <el-button type="primary" plain :disabled="uploading">
              <el-icon><Upload /></el-icon>
              选择PDF文件
            </el-button>
            <template #tip>
              <div class="upload-tip">仅支持 PDF 格式，支持大文件分片上传和断点续传</div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item v-if="uploading" label="上传进度">
          <div class="progress-wrapper">
            <el-progress
              :percentage="uploadProgress"
              :status="uploadPaused ? 'warning' : ''"
              :stroke-width="12"
            />
            <div class="progress-info">
              <span class="progress-text">{{ uploadProgress }}%</span>
              <span v-if="uploadSpeed" class="speed-text">{{ uploadSpeed }}</span>
            </div>
            <div class="progress-detail">
              已上传 {{ formatFileSize(uploadedSize) }} / {{ formatFileSize(pdfFile ? pdfFile.size : 0) }}
              <span v-if="totalChunks > 0">（{{ uploadedChunks }}/{{ totalChunks }} 分片）</span>
            </div>
            <div v-if="isCalculatingMd5" class="calculating-text">
              正在计算文件校验值...
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleCancelUpload" v-if="uploading">
          取消上传
        </el-button>
        <el-button @click="uploadDialogVisible = false" v-else>
          取消
        </el-button>
        <el-button v-if="uploading" @click="handlePauseResume" :type="uploadPaused ? 'primary' : 'default'">
          {{ uploadPaused ? '继续上传' : '暂停上传' }}
        </el-button>
        <el-button type="primary" @click="handleUploadSubmit" :loading="uploading" :disabled="uploading">
          {{ uploading ? '上传中...' : '确定上传' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 文档预览 -->
    <DocPreview
      v-if="previewRow"
      v-model:visible="previewVisible"
      :url="previewRow.filePath || ''"
      :title="previewRow.title"
      :text-content="previewRow.content || ''"
      :type="previewRow.type || 'text'"
    />
  </div>
</template>

<style scoped>
.knowledge-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 筛选卡片 */
.filter-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.page-title-wrap {
  margin-bottom: 20px;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 6px 0;
}

.page-desc {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

.filter-form {
  display: flex;
  gap: 16px;
  align-items: flex-end;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.filter-label {
  font-size: 13px;
  color: #909399;
  font-weight: 500;
}

.filter-input {
  width: 160px;
}

.filter-keyword-input {
  width: 280px;
}

.filter-actions {
  margin-left: auto;
  display: flex;
  gap: 12px;
}

:deep(.filter-form .el-button--primary) {
  background: #ffe60f;
  border-color: #ffe60f;
  color: #1a1a2e;
}

:deep(.filter-form .el-button--primary:hover) {
  background: #ffd700;
  border-color: #ffd700;
  color: #1a1a2e;
}

:deep(.btn-reset) {
  background: #fffbeb !important;
  border-color: #ffe60f !important;
  color: #d4b800 !important;
}

:deep(.btn-reset:hover) {
  background: #fff3b0 !important;
  border-color: #ffd700 !important;
  color: #b89a00 !important;
}

:deep(.btn-refresh) {
  background: #fffbeb !important;
  border-color: #ffe60f !important;
  color: #d4b800 !important;
}

:deep(.btn-refresh:hover) {
  background: #fff3b0 !important;
  border-color: #ffd700 !important;
  color: #b89a00 !important;
}

/* 工具栏 */
.toolbar {
  background: #ffffff;
  border-radius: 12px;
  padding: 16px 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.toolbar-left {
  display: flex;
  gap: 12px;
}

.toolbar-right {
  display: flex;
  gap: 8px;
  align-items: center;
}

:deep(.toolbar .el-button--primary) {
  background: #ffe60f;
  border-color: #ffe60f;
  color: #1a1a2e;
}

:deep(.toolbar .el-button--primary:hover) {
  background: #ffd700;
  border-color: #ffd700;
  color: #1a1a2e;
}

:deep(.toolbar .el-button--primary.is-plain) {
  background: #fffbeb !important;
  border-color: #ffe60f !important;
  color: #d4b800 !important;
}

:deep(.toolbar .el-button--primary.is-plain:hover) {
  background: #ffe60f !important;
  color: #1a1a2e !important;
}

/* 表格卡片 */
.table-card {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

:deep(.el-table) {
  --el-table-tr-bg-color: #ffffff;
  --el-table-header-bg-color: #fafafa;
  --el-table-border-color: #f2f6fc;
  --el-table-tr-hover-bg-color: #fffbeb;
  --el-table-tr-stripe-bg-color: #fafafa;
  --el-table-text-color: #606266;
  --el-table-header-text-color: #303133;
}

:deep(.el-table th.el-table__cell) {
  background: #fafafa;
}

:deep(.el-table td.el-table__cell) {
  border-bottom: 1px solid #f2f6fc;
}

:deep(.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell) {
  background: #fafafa;
}

:deep(.el-table__body tr:hover > td.el-table__cell) {
  background: #fffbeb !important;
}

:deep(.el-table .cell) {
  padding-left: 16px;
  padding-right: 16px;
}

.doc-id {
  font-family: 'Menlo', 'Monaco', 'Consolas', monospace;
  font-size: 13px;
  color: #909399;
}

.title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon {
  color: #d4b800;
  font-size: 18px;
  flex-shrink: 0;
}

.title-text {
  font-weight: 500;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.size-text {
  font-size: 13px;
  color: #606266;
}

.action-btns {
  display: flex;
  gap: 4px;
}

:deep(.el-button--primary.is-link) {
  color: #d4b800;
}

:deep(.el-button--primary.is-link:hover) {
  color: #e6c800;
}

:deep(.el-button--danger.is-link) {
  color: #f56c6c;
}

/* 分页 */
.pagination-area {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
}

.pagination-info {
  font-size: 14px;
  color: #909399;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background-color: #ffe60f !important;
  color: #1a1a2e !important;
  border-color: #ffe60f !important;
}

:deep(.el-pagination.is-background .btn-prev),
:deep(.el-pagination.is-background .btn-next),
:deep(.el-pagination.is-background .el-pager li) {
  border-radius: 8px;
  margin: 0 4px;
  color: #606266;
}

:deep(.el-pagination.is-background .el-pager li:hover) {
  color: #d4b800;
}

:deep(.el-pagination__sizes .el-input .el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-pagination__jump .el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-pagination__jump .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(255, 230, 15, 0.1);
  border-color: #ffe60f;
}

/* 对话框 */
:deep(.el-dialog) {
  border-radius: 12px;
}

:deep(.el-dialog__header) {
  padding: 20px 24px 16px;
}

:deep(.el-dialog__title) {
  font-weight: 600;
  color: #303133;
}

:deep(.el-dialog__body) {
  padding: 0 24px 24px;
}

:deep(.el-dialog__footer) {
  padding: 16px 24px 20px;
  border-top: 1px solid #f2f6fc;
}

:deep(.el-dialog .el-button--primary) {
  background: #ffe60f;
  border-color: #ffe60f;
  color: #1a1a2e;
}

:deep(.el-dialog .el-button--primary:hover) {
  background: #ffd700;
  border-color: #ffd700;
  color: #1a1a2e;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.progress-wrapper {
  width: 100%;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.progress-text {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.speed-text {
  font-size: 12px;
  color: #d4b800;
  font-weight: 500;
}

.progress-detail {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.calculating-text {
  font-size: 12px;
  color: #d4b800;
  margin-top: 8px;
}

:deep(.el-progress-bar__inner) {
  background: #ffe60f;
}
</style>