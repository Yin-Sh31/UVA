<template>
  <div class="login-container">
    <!-- 装饰元素 -->
    <div class="login-decoration">
      <div class="decoration-circle circle-1"></div>
      <div class="decoration-circle circle-2"></div>
      <div class="decoration-circle circle-3"></div>
    </div>
    
    <!-- 登录表单卡片 -->
    <div class="login-card">
      <div class="login-wrapper">
        <!-- 登录头部 -->
        <div class="login-header">
          <div class="logo-container">
            <div class="logo-icon">🚁</div>
            <div class="logo-text">
              <h1>无人机管理系统</h1>
              <p>机主登录平台</p>
            </div>
          </div>
        </div>
        
        <!-- 登录方式切换 -->
        <div class="login-tabs">
          <div 
            class="login-tab" 
            :class="{ active: loginType === 'sms' }"
            @click="loginType = 'sms'"
          >
            验证码登录
          </div>
          <div 
            class="login-tab" 
            :class="{ active: loginType === 'password' }"
            @click="loginType = 'password'"
          >
            密码登录
          </div>
        </div>
        
        <!-- 登录表单 -->
        <el-form 
          :model="loginForm" 
          :rules="rules" 
          ref="loginFormRef" 
          class="login-form"
          :validate-on-rule-change="false"
        >
          <div class="form-content">
            <!-- 手机号输入 -->
            <el-form-item prop="phone" class="form-item">
              <div class="input-wrapper">
                <div class="input-icon phone-icon">📱</div>
                <el-input 
                  v-model="loginForm.phone" 
                  placeholder="请输入手机号"
                  class="custom-input"
                  :prefix-icon="''"
                  @focus="inputFocus('phone')"
                  @blur="inputBlur('phone')"
                  :class="{ 'input-focused': focusField === 'phone', 'input-error': errorFields.phone }"
                ></el-input>
              </div>
            </el-form-item>
            
            <!-- 验证码输入 - 仅在验证码登录时显示 -->
            <el-form-item v-if="loginType === 'sms'" prop="code" class="form-item">
              <div class="input-wrapper">
                <div class="input-icon code-icon">🔑</div>
                <el-input 
                  v-model="loginForm.code" 
                  placeholder="请输入验证码"
                  class="custom-input code-input"
                  :prefix-icon="''"
                  @focus="inputFocus('code')"
                  @blur="inputBlur('code')"
                  :class="{ 'input-focused': focusField === 'code', 'input-error': errorFields.code }"
                >
                  <template #append>
                    <el-button 
                      :disabled="countdown > 0"
                      @click="handleSendCode"
                      class="code-button"
                      :class="{ 'code-button-active': countdown === 0 }"
                    >
                      {{ countdown > 0 ? `${countdown}秒后重试` : '发送验证码' }}
                    </el-button>
                  </template>
                </el-input>
              </div>
            </el-form-item>
            
            <!-- 密码输入 - 仅在密码登录时显示 -->
            <el-form-item v-if="loginType === 'password'" prop="password" class="form-item">
              <div class="input-wrapper">
                <div class="input-icon password-icon">🔒</div>
                <el-input 
                  v-model="loginForm.password" 
                  type="password"
                  placeholder="请输入密码"
                  class="custom-input"
                  :prefix-icon="''"
                  @focus="inputFocus('password')"
                  @blur="inputBlur('password')"
                  :class="{ 'input-focused': focusField === 'password', 'input-error': errorFields.password }"
                  show-password
                ></el-input>
              </div>
            </el-form-item>
            
            <!-- 登录按钮 -->
            <el-form-item class="form-item login-btn-container">
              <el-button 
                type="primary" 
                @click="handleLogin"
                class="login-button"
                :loading="loading"
              >
                <span v-if="!loading">登录</span>
                <span v-else>登录中...</span>
              </el-button>
            </el-form-item>
            
          </div>
        </el-form>
        
        <!-- 注册链接 - 使用组件方法处理跳转 -->
        <div style="margin-top: 16px; text-align: center;">
          <button @click="handleRegisterClick" class="register-button" style="text-decoration: none; color: #409EFF; font-size: 14px; padding: 8px 16px; border-radius: 4px; position: relative; z-index: 10; cursor: pointer; display: inline-block; background: none; border: none;">
            没有账号？立即注册
          </button>
        </div>
        
        <!-- 登录底部 -->
        <div class="login-footer">
          <div class="customer-service">
            <div class="service-icon">📞</div>
            <span>如有问题请联系客服：<a href="tel:400-888-8888" class="service-phone">400-888-8888</a></span>
          </div>
          <div class="system-info">
            <span>© 2025 无人机管理系统 v1.0.0</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onBeforeUnmount, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { ElMessage } from 'element-plus'

export default {
  name: 'Login',
  setup() {
    const router = useRouter()
    const userStore = useUserStore()
    const loginFormRef = ref(null)
    const loading = ref(false)
    const countdown = ref(0)
    const focusField = ref('')
    const errorFields = reactive({ phone: false, code: false })
    let timer = null
    
    // 登录方式状态
    const loginType = ref('sms') // 'sms' 或 'password'
    
    // 登录表单数据
    const loginForm = reactive({
      phone: '',
      code: '',
      password: ''
    })
    
    // 表单验证规则
    const rules = computed(() => {
      const baseRules = {
        phone: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
        ]
      }
      
      if (loginType.value === 'sms') {
        baseRules.code = [
          { required: true, message: '请输入验证码', trigger: 'blur' },
          { len: 6, message: '验证码为6位数字', trigger: 'blur' },
          { pattern: /^\d{6}$/, message: '验证码为6位数字', trigger: 'blur' }
        ]
      } else {
        baseRules.password = [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { pattern: /^(?=.*[0-9])(?=.*[a-zA-Z])[a-zA-Z0-9]{8,20}$/, message: '密码必须包含数字和字母，长度8-20位', trigger: 'blur' }
        ]
      }
      
      return baseRules
    })
    
    // 输入框聚焦处理
    const inputFocus = (field) => {
      focusField.value = field
      errorFields[field] = false
    }
    
    // 输入框失焦处理
    const inputBlur = (field) => {
      focusField.value = ''
      // 验证当前字段
      loginFormRef.value?.validateField(field, (error) => {
        if (error) {
          errorFields[field] = true
        }
      })
    }
    
    // 发送验证码
    const handleSendCode = async () => {
      try {
        // 验证手机号
        await loginFormRef.value.validateField('phone')
        
        if (!loginForm.phone) {
          errorFields.phone = true
          return
        }
        
        // 实际API调用发送验证码
        const response = await fetch(`/auth/send-code?phone=${encodeURIComponent(loginForm.phone)}&role=owner`, {
          method: 'POST'
        })
        
        const data = await response.json()
        
        if (data.success) {
          // 开始倒计时
          startCountdown()
          
          // 显示成功消息
          ElMessage.success({
            message: '验证码已发送，请注意查收',
            duration: 2000,
            showClose: true
          })
          
          // 自动聚焦到验证码输入框
          setTimeout(() => {
            const codeInput = document.querySelector('.code-input input')
            if (codeInput) codeInput.focus()
          }, 300)
        } else {
          throw new Error(data.message || '发送验证码失败')
        }
        
      } catch (error) {
        console.error('发送验证码失败:', error)
        ElMessage.error({
          message: error.message || '发送验证码失败，请稍后重试',
          duration: 2000,
          showClose: true
        })
      }
    }
    
    // 开始倒计时
    const startCountdown = () => {
      countdown.value = 60
      
      if (timer) {
        clearInterval(timer)
      }
      
      timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer)
          timer = null
        }
      }, 1000)
    }
    
    // 处理登录
    const handleLogin = async () => {
      try {
        await loginFormRef.value.validate();
        loading.value = true;
        
        if (!['sms', 'password'].includes(loginType.value)) {
          throw new Error('不支持的登录方式');
        }
        
        const loginData = loginType.value === 'sms' 
          ? { phone: loginForm.phone, code: loginForm.code, role: 'owner', loginType: 'code' }
          : { phone: loginForm.phone, password: loginForm.password, loginType: 'password' };
        
        await userStore.login(loginData);
        
        ElMessage.success('登录成功，正在跳转...');
        setTimeout(() => {
          router.push('/dashboard');
        }, 1000);
      } catch (error) {
        console.error('登录失败:', error);
        ElMessage.error(error.message || '登录失败，请稍后重试');
        Object.keys(errorFields).forEach(key => {
          errorFields[key] = false;
        });
      } finally {
        loading.value = false;
      }
    }
    
    // 组件销毁前清理定时器
    onBeforeUnmount(() => {
      if (timer) {
        clearInterval(timer)
      }
    })
    
    // 跳转到注册页面
    const handleRegisterClick = () => {
      // 完全清除用户状态，包括store和localStorage
      userStore.logout()
      console.log('清除用户状态并准备跳转到注册页面')
      // 强制使用编程式导航，确保正确跳转
      router.push('/register')
    }
    
    // 直接使用原生JavaScript跳转，完全绕过路由机制
    const handleRegisterRedirect = () => {
      console.log('使用原生JavaScript直接跳转到注册页面')
      // 清除可能的token
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      // 直接设置window.location
      window.location.href = '/register'
    }
    
    return {
      loginForm,
      rules,
      loginType,
      loginFormRef,
      loading,
      countdown,
      focusField,
      errorFields,
      handleSendCode,
      handleLogin,
      inputFocus,
      inputBlur,
      handleRegisterClick,
      handleRegisterRedirect
    }
  }
}
</script>

<style scoped>
/* 全局样式重置 */
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

/* 登录容器 */
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #409EFF 0%, #66B1FF 100%);
  position: relative;
  overflow: hidden;
  padding: 20px;
}

/* 装饰元素 */
.login-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 8s ease-in-out infinite;
}

.circle-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  left: -100px;
  animation-delay: 0s;
}

.circle-2 {
  width: 200px;
  height: 200px;
  bottom: -50px;
  right: -50px;
  animation-delay: 2s;
}

.circle-3 {
  width: 150px;
  height: 150px;
  top: 50%;
  right: 10%;
  transform: translateY(-50%);
  animation-delay: 4s;
}

/* 登录卡片 */
.login-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 440px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.login-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.15);
}

/* 登录包装器 */
.login-wrapper {
  padding: 40px;
  position: relative;
}

/* 登录头部 */
.login-header {
  text-align: center;
  margin-bottom: 24px;
  position: relative;
}

/* 登录方式切换标签 */
.login-tabs {
  display: flex;
  margin-bottom: 24px;
  background: #f5f7fa;
  border-radius: 8px;
  padding: 4px;
}

.login-tab {
  flex: 1;
  text-align: center;
  padding: 10px 0;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
  font-weight: 500;
  color: #606266;
}

.login-tab.active {
  background: #ffffff;
  color: #409EFF;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
}

.login-tab:hover:not(.active) {
  color: #409EFF;
}

.logo-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.logo-icon {
  font-size: 48px;
  background: linear-gradient(135deg, #409EFF, #66B1FF);
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.3);
  animation: bounce 2s ease-in-out infinite;
}

.logo-text h1 {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 4px 0;
  background: linear-gradient(135deg, #303133, #606266);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.logo-text p {
  font-size: 14px;
  color: #606266;
  margin: 0;
}

/* 表单样式 */
.login-form {
  width: 100%;
}

.form-content {
  position: relative;
}

.form-item {
  margin-bottom: 24px !important;
  position: relative;
}

/* 输入框包装器 */
.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  background: #fafafa;
  border-radius: 12px;
  transition: all 0.3s ease;
  height: 50px;
  overflow: hidden;
}

.input-wrapper:focus-within {
  background: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

/* 输入框图标 */
.input-icon {
  position: absolute;
  left: 16px;
  z-index: 1;
  font-size: 20px;
  color: #909399;
  transition: all 0.3s ease;
}

/* 自定义输入框 */
.custom-input {
  height: 100%;
  border: none;
  border-radius: 12px;
  padding-left: 50px !important;
  padding-right: 16px;
  font-size: 15px;
  color: #303133;
  background: transparent;
  transition: all 0.3s ease;
}

.custom-input:focus {
  box-shadow: none;
  border-color: transparent;
}

.custom-input::placeholder {
  color: #c0c4cc;
  font-size: 14px;
}

.custom-input.input-focused {
  color: #409EFF;
}

.custom-input.input-error {
  color: #f56c6c;
}

.input-wrapper:focus-within .input-icon {
  color: #409EFF;
}

/* 验证码输入框特殊样式 */
.code-input {
  padding-right: 130px !important;
}

/* 验证码按钮 */
.code-button {
  position: absolute;
  right: 4px;
  top: 50%;
  transform: translateY(-50%);
  height: 36px;
  padding: 0 16px;
  border-radius: 8px;
  font-size: 13px;
  color: #909399;
  background: #f0f0f0;
  border: none;
  transition: all 0.3s ease;
  min-width: 120px;
}

.code-button:hover:not(:disabled) {
  background: #e6f0ff;
  color: #409EFF;
}

.code-button-active {
  background: #ecf5ff;
  color: #409EFF;
}

.code-button-active:hover {
  background: #d9ecff;
  color: #3a8ee6;
  transform: translateY(-50%) scale(1.05);
}

/* 登录按钮容器 */
.login-btn-container {
  margin-bottom: 32px !important;
}

/* 登录按钮 */
.login-button {
  width: 100%;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #409EFF, #66B1FF);
  border: none;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.3);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

/* 注册按钮样式 */
.register-button {
  color: #409EFF;
  font-size: 14px;
  padding: 8px 16px;
  border-radius: 4px;
  transition: all 0.3s ease;
  position: relative;
  z-index: 10;
}

.register-button:hover {
  color: #66B1FF !important;
  background-color: rgba(64, 158, 255, 0.05);
}

/* 注册链接样式（备用） */
.register-link {
  color: #409EFF;
  font-size: 14px;
  text-decoration: none;
  padding: 8px 16px;
  border-radius: 4px;
  transition: all 0.3s ease;
  display: inline-block;
}

.register-link:hover {
  color: #66B1FF;
  background-color: rgba(64, 158, 255, 0.05);
  text-decoration: underline;
  transform: translateY(-1px);
}

.login-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s ease;
}

.login-button:hover::before {
  left: 100%;
}

.login-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(64, 158, 255, 0.4);
  background: linear-gradient(135deg, #66B1FF, #409EFF);
}

.login-button:active:not(:disabled) {
  transform: translateY(0);
}

/* 登录底部 */
.login-footer {
  text-align: center;
  border-top: 1px solid #f0f0f0;
  padding-top: 24px;
}

.customer-service {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 14px;
  color: #606266;
}

.service-icon {
  font-size: 16px;
}

.service-phone {
  color: #409EFF;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s ease;
}

.service-phone:hover {
  color: #66B1FF;
  text-decoration: underline;
}

.system-info {
  font-size: 12px;
  color: #909399;
}

/* 动画效果 */
@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(5deg);
  }
}

@keyframes bounce {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-container {
    padding: 16px;
  }
  
  .login-wrapper {
    padding: 32px 24px;
  }
  
  .logo-container {
    flex-direction: column;
    gap: 12px;
  }
  
  .logo-icon {
    font-size: 40px;
    padding: 12px;
  }
  
  .logo-text h1 {
    font-size: 20px;
  }
  
  .form-item {
    margin-bottom: 20px !important;
  }
  
  .input-wrapper {
    height: 46px;
  }
  
  .custom-input {
    font-size: 14px;
  }
  
  .login-button {
    height: 46px;
    font-size: 15px;
  }
  
  .code-button {
    min-width: 110px;
    font-size: 12px;
  }
  
  .login-footer {
    padding-top: 20px;
  }
}

@media (max-width: 480px) {
  .login-container {
    padding: 12px;
  }
  
  .login-wrapper {
    padding: 24px 20px;
  }
  
  .logo-icon {
    font-size: 36px;
    padding: 10px;
  }
  
  .logo-text h1 {
    font-size: 18px;
  }
  
  .login-header {
    margin-bottom: 28px;
  }
  
  .form-item {
    margin-bottom: 16px !important;
  }
  
  .input-wrapper {
    height: 44px;
  }
  
  .login-button {
    height: 44px;
    font-size: 14px;
  }
  
  .login-btn-container {
    margin-bottom: 24px !important;
  }
  
  .customer-service {
    font-size: 13px;
  }
  
  .system-info {
    font-size: 11px;
  }
}
</style>