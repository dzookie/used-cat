<script setup>
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  url: {
    type: String,
    default: ''
  },
  title: {
    type: String,
    default: '文档预览'
  },
  textContent: {
    type: String,
    default: ''
  },
  type: {
    type: String,
    default: 'text'
  }
})

const emit = defineEmits(['update:visible'])

const BASE_URL = import.meta.env.VITE_BASE_URL

const getPdfUrl = () => {
  if (!props.url) return ''
  if (props.url.startsWith('http')) return props.url
  return BASE_URL + props.url
}

const handleClose = () => {
  emit('update:visible', false)
}

const openInNewTab = () => {
  const url = getPdfUrl()
  if (url) {
    window.open(url, '_blank')
  }
}
</script>

<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="handleClose"
    :title="title"
    width="92%"
    top="2vh"
    :close-on-click-modal="false"
    class="doc-preview-dialog"
  >
    <!-- 文本类型 -->
    <div v-if="type === 'text'" class="text-content-wrap">
      <pre class="text-content">{{ textContent || '暂无内容' }}</pre>
    </div>

    <!-- PDF类型 -->
    <div v-else class="pdf-viewer">
      <div class="iframe-toolbar">
        <span class="iframe-tip">PDF 预览</span>
        <el-button size="small" type="primary" @click="openInNewTab">
          新窗口打开
        </el-button>
      </div>
      <iframe
        :src="getPdfUrl()"
        class="pdf-iframe"
        frameborder="0"
      ></iframe>
    </div>
  </el-dialog>
</template>

<style scoped>
.text-content-wrap {
  max-height: calc(95vh - 120px);
  overflow-y: auto;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fafafa;
}

.text-content {
  padding: 20px;
  font-size: 14px;
  line-height: 1.8;
  color: #303133;
  white-space: pre-wrap;
  word-wrap: break-word;
  margin: 0;
  font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;
}

.pdf-viewer {
  height: calc(95vh - 120px);
  display: flex;
  flex-direction: column;
}

.iframe-toolbar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 8px 0 12px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 12px;
  flex-shrink: 0;
}

.iframe-tip {
  font-size: 13px;
  color: #909399;
}

.pdf-iframe {
  flex: 1;
  width: 100%;
  min-height: 400px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  background: #fff;
}
</style>

<style>
.doc-preview-dialog .el-dialog {
  height: 96vh;
  margin-top: 2vh !important;
  display: flex;
  flex-direction: column;
}

.doc-preview-dialog .el-dialog__body {
  flex: 1;
  overflow: hidden;
  padding: 0 24px 24px;
}
</style>
