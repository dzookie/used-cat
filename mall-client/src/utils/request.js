import axios from 'axios'
import { ElMessage } from 'element-plus'

const instance = axios.create({
  baseURL: import.meta.env.VITE_BASE_URL,
  timeout: 10000
})

/**
 * 请求拦截器
 */
instance.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token && config.headers) {
      config.headers.Authorization = token.replace('Bearer ', '')
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器
 */
instance.interceptors.response.use(
  response => {
    const data = response.data
    if (data && data.code === 403) {
      localStorage.removeItem('token')
      ElMessage.error(data.message || '无权限访问，请重新登录')
      setTimeout(() => {
        window.location.href = '/login'
      }, 1000)
      return Promise.reject(new Error(data.message || '无权限访问'))
    }
    if (data && data.code === 401) {
      localStorage.removeItem('token')
      setTimeout(() => {
        window.location.href = '/login'
      }, 500)
    }
    return response
  },
  error => {
    if (error.response && (error.response.status === 401 || error.response.status === 403)) {
      localStorage.removeItem('token')
      setTimeout(() => {
        window.location.href = '/login'
      }, 500)
    }
    return Promise.reject(error)
  }
)

/**
 * GET请求
 * @param {string} url
 * @param {any} params
 * @returns
 */
export const httpGet = (url, params) => {
  return instance({
    method: 'GET',
    url,
    params
  })
}

/**
 * POST请求
 * @param {string} url
 * @param {any} data
 * @param {any} config
 * @returns
 */
export const httpPost = (url, data, config) => {
  return instance({
    method: 'POST',
    url,
    data,
    headers: {
      'Content-Type': 'application/json'
    },
    ...config
  })
}

/**
 * DELETE请求
 * @param {string} url
 * @param {any} params
 * @returns
 */
export const httpDelete = (url, params) => {
  return instance({
    method: 'DELETE',
    url,
    params
  })
}

/**
 * PUT请求
 * @param {string} url
 * @param {any} data
 * @returns
 */
export const httpPut = (url, data) => {
  return instance({
    method: 'PUT',
    url,
    data,
    headers: {
      'Content-Type': 'application/json'
    }
  })
}

/**
 * 文件数据
 * @param {*} url
 * @param {*} data
 * @returns
 */
export const postFile = (url, data) => {
  return instance({
    url,
    method: 'POST',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 流式请求
 */
export const streamRequest = (url, params) => {
  return instance({
    url,
    method: 'GET',
    params,
    responseType: 'stream'
  })
}

export default instance
