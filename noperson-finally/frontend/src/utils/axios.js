import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const instance = axios.create({
  baseURL: 'http://localhost:8082/api', // 基础URL设置为后端API地址
  timeout: 10000
})

// 请求拦截器
instance.interceptors.request.use(
  config => {
    console.log('===== 请求开始 =====')
    console.log('请求URL:', config.url)
    console.log('请求方法:', config.method)
    console.log('请求参数:', config.params || config.data)
    
    // 从localStorage中获取token
    const token = localStorage.getItem('token')
    console.log('本地存储Token:', token ? '存在' : '不存在')
    
    if (token) {
      // 设置Authorization请求头，添加Bearer前缀
      config.headers.Authorization = `Bearer ${token}`
      console.log('添加Authorization头:', config.headers.Authorization.substring(0, 20) + '...')
    }
    
    // 打印完整请求头
    console.log('完整请求头:', config.headers)
    console.log('===== 请求结束 =====')
    
    return config
  },
  error => {
    console.error('===== 请求拦截器错误 =====')
    console.error('请求错误详情:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
instance.interceptors.response.use(
  response => {
    console.log('===== 响应开始 =====')
    console.log('响应URL:', response.config.url)
    console.log('响应状态:', response.status, response.statusText)
    console.log('原始响应头:', response.headers)
    console.log('原始响应数据:', response.data)
    
    const res = response.data
    
    // 根据后端接口规范处理响应
    if (res.code !== 200) {
      console.error('业务错误:', res)
      console.error('错误代码:', res.code)
      // 同时检查msg和message字段，优先使用msg
      const errorMessage = res.msg || res.message || '未知错误'
      console.error('错误消息:', errorMessage)
      console.error('完整错误数据:', JSON.stringify(res))
      // 显示错误信息
      ElMessage.error({ message: errorMessage || '请求失败', duration: 2000, showClose: true })
      
      // 如果是401未授权，清除token并重定向到登录页
      if (res.code === 401) {
        console.error('未授权，清除token并跳转登录页')
        localStorage.removeItem('token')
        // 使用原生JavaScript跳转，避免在拦截器中使用Vue的路由
        window.location.href = '/login'
      }
      
      console.log('===== 响应结束（业务错误）=====')
      return Promise.reject(new Error(errorMessage || '请求失败'))
    }
    
    console.log('响应成功，返回数据')
    console.log('===== 响应结束 =====')
    return res
  },
  error => {
    console.error('===== 响应拦截器错误 =====')
    console.error('错误类型:', error.name)
    console.error('错误消息:', error.message)
    
    // 处理网络错误
    if (!error.response) {
      console.error('无响应:', error.request)
      console.error('可能的原因:', '1. 服务器未启动 2. 网络连接问题 3. CORS问题')
      ElMessage.error({ message: '网络错误，请检查网络连接', duration: 2000, showClose: true })
    } else {
      // 处理不同的错误状态码
      console.error('响应状态:', error.response.status, error.response.statusText)
      console.error('响应头:', error.response.headers)
      console.error('响应数据:', error.response.data)
      
      const status = error.response.status
      switch (status) {
        case 401:
          console.error('401未授权，清除token并跳转登录页')
          ElMessage.error({ message: '未授权，请重新登录', duration: 2000, showClose: true })
          localStorage.removeItem('token')
          // 使用原生JavaScript跳转，避免在拦截器中使用Vue的路由
          window.location.href = '/login'
          break
        case 403:
          ElMessage.error({ message: '拒绝访问', duration: 2000, showClose: true })
          break
        case 404:
          ElMessage.error({ message: '请求的资源不存在', duration: 2000, showClose: true })
          break
        case 500:
          ElMessage.error({ message: '服务器内部错误', duration: 2000, showClose: true })
          break
        default:
          ElMessage.error({ message: `请求失败: ${status} ${error.response.statusText}`, duration: 2000, showClose: true })
      }
    }
    
    console.log('===== 错误处理结束 =====')
    return Promise.reject(error)
  }
)

export default instance