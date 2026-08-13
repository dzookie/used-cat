import apiConfig from '@/apis/api.config'
import { httpGet } from '@/utils/request'
import { ElMessage } from 'element-plus'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const loginUser = ref(null)
  const userLoaded = ref(false)
  let fetchPromise = null

  /**
   * 获取当前登录用户信息
   * @param {boolean} force - 是否强制刷新
   */
  const getCurrLoginUser = async (force = false) => {
    if (!force && userLoaded.value && loginUser.value) {
      return
    }
    if (fetchPromise && !force) {
      return fetchPromise
    }
    let token = localStorage.getItem('token')
    if (token) {
      fetchPromise = (async () => {
        try {
          const { data } = await httpGet(apiConfig.user.getCurrUser, null)
          if (data.code === 200) {
            loginUser.value = data.data
            userLoaded.value = true
          } else {
            ElMessage.error(data.msg)
            localStorage.removeItem('token')
            loginUser.value = null
            userLoaded.value = true
          }
        } catch (error) {
          console.error(error)
          localStorage.removeItem('token')
          loginUser.value = null
          userLoaded.value = true
        } finally {
          fetchPromise = null
        }
      })()
      return fetchPromise
    } else {
      loginUser.value = null
      userLoaded.value = true
      fetchPromise = null
    }
  }

  /**
   * 退出登录
   */
  const loginOut = () => {
    loginUser.value = null
    userLoaded.value = false
    localStorage.removeItem('token')
    window.location.href = '/login'
  }

  const isLoggedIn = () => {
    return !!localStorage.getItem('token') && !!loginUser.value
  }

  const isAdmin = () => {
    if (!loginUser.value || loginUser.value.role == null) return false
    return Number(loginUser.value.role) === 1
  }

  const getUserRole = () => {
    return loginUser.value ? Number(loginUser.value.role) : null
  }

  return {
    loginUser,
    getCurrLoginUser,
    loginOut,
    isLoggedIn,
    isAdmin,
    getUserRole
  }
})
