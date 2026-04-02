import { defineStore } from 'pinia'
import axios from '../utils/axios'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}'),
    isLoggedIn: !!localStorage.getItem('token')
  }),
  
  actions: {
    // 登录
    async login(userData) {
      try {
        // 路径不包含/api前缀，axios实例会自动添加
        const response = await axios.post('/auth/login', userData)
        // 处理可能的不同响应格式
        let token, userInfo
        if (response.data.data) {
          // 处理嵌套在data中的情况
          token = response.data.data.token
          userInfo = response.data.data.user || response.data.data.userInfo
        } else {
          // 直接从响应中获取
          token = response.data.token
          userInfo = response.data.user || response.data.userInfo
        }
        
        // 确保userInfo有userId字段（兼容后端返回的id字段）
        if (userInfo && userInfo.id && !userInfo.userId) {
          userInfo.userId = userInfo.id
        }
        
        console.log('登录成功获取到的token:', token)
        console.log('登录成功获取到的userInfo:', userInfo)
        
        // 保存token和用户信息
        this.token = token
        this.userInfo = userInfo || {}
        this.isLoggedIn = !!token
        
        // 存储到localStorage
        if (token) {
          localStorage.setItem('token', token)
          localStorage.setItem('userInfo', JSON.stringify(userInfo || {}))
        }
        
        console.log('登录状态已设置:', this.isLoggedIn)
        return response
      } catch (error) {
        console.error('登录API调用失败:', error)
        const errorMsg = error.response?.data?.message || error.response?.data?.msg || error.message || '登录失败'
        ElMessage.error(errorMsg)
        throw new Error(errorMsg)
      }
    },
    
    // 发送验证码
    async sendCode(phone, role) {
      try {
        const response = await axios.post('/auth/send-code', null, {
          params: { phone, role }
        })
        ElMessage.success(response.msg || '验证码发送成功')
        return response
      } catch (error) {
        throw error
      }
    },
    
    // 退出登录
    logout() {
      this.token = ''
      this.userInfo = {}
      this.isLoggedIn = false
      
      // 清除localStorage
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    },
    
    // 检查token有效性（兼容旧方法）
    checkToken() {
      return this.checkTokenValidity(localStorage.getItem('token'))
    },
    
    // 检查token有效性（新方法，与App.vue调用兼容）
    checkTokenValidity(token) {
      return new Promise((resolve, reject) => {
        if (!token) {
          this.isLoggedIn = false
          this.userInfo = {}
          resolve(false)
          return
        }
        
        // 开发环境：对于开发环境，我们暂时信任任何非空token
        // 实际项目中应该调用真实API验证token
        if (import.meta.env.DEV) {
          console.log('开发环境：信任非空token')
          this.isLoggedIn = true
          // 为开发环境设置默认的userInfo，包含userId
          this.userInfo = {
            userId: 1,
            name: '测试用户',
            phone: '13800138000'
          }
          localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
          resolve(true)
          return
        }
        
        // 生产环境：这里应该调用实际的API验证token
        // 这里是简化实现，实际应该调用后端验证接口
        console.log('验证token有效性')
        // 如果是mock-token，认为有效
        if (token === 'mock-token') {
          this.isLoggedIn = true
          this.userInfo = {
            userId: '1',
            name: '张三',
            phone: '13800138000'
          }
          resolve(true)
        } else if (token && token.length > 10) { // 基本验证：假设有效token长度大于10
          this.isLoggedIn = true
          resolve(true)
        } else {
          this.isLoggedIn = false
          this.userInfo = {}
          localStorage.removeItem('token')
          resolve(false)
        }
      })
    },
    
    // 获取用户信息
    async getUserInfo() {
      try {
        // 路径不包含/api前缀，axios实例会自动添加
        const response = await axios.get('/user/info')
        // 处理可能的嵌套响应格式
        const userInfoData = response.data?.data || response.data
        // 确保使用address字段而不是location字段
        if (userInfoData && !userInfoData.address && userInfoData.location) {
          userInfoData.address = userInfoData.location
        }
        // 确保userInfo有userId字段（兼容后端返回的id字段）
        if (userInfoData && userInfoData.id && !userInfoData.userId) {
          userInfoData.userId = userInfoData.id
        }
        this.userInfo = userInfoData
        localStorage.setItem('userInfo', JSON.stringify(userInfoData))
        return response
      } catch (error) {
        console.error('获取用户信息失败:', error)
        // 如果获取用户信息失败，可能token失效
        this.logout()
        throw error
      }
    }
  }
})