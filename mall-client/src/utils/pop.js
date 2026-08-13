import { ElMessage } from 'element-plus'

/**
 * 弹窗提示
 * @param {*} code 状态码
 * @param {*} message 提示信息
 */
export const popMessage = (code, message) => {
  ElMessage({
    message: message,
    type: code === 200 ? 'success' : 'error'
  })
}
