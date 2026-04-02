<template>
  <div class="login-container">
    <!-- 装饰元素 -->
    <div class="login-decoration">
      <div class="decoration-circle circle-1"></div>
      <div class="decoration-circle circle-2"></div>
      <div class="decoration-circle circle-3"></div>
    </div>
    
    <!-- 注册表单卡片 -->
    <div class="login-card">
      <div class="login-wrapper">
        <!-- 登录头部 -->
        <div class="login-header">
          <div class="logo-container">
            <div class="logo-icon">🚁</div>
            <div class="logo-text">
              <h1>无人机管理系统</h1>
              <p>账号注册</p>
            </div>
          </div>
        </div>
        
        <!-- 注册表单 -->
        <el-form 
          :model="registerForm" 
          :rules="rules" 
          ref="registerFormRef" 
          class="login-form"
          :validate-on-rule-change="false"
        >
          <div class="form-content">
            <!-- 手机号输入 -->
            <el-form-item prop="phone" class="form-item">
              <div class="input-wrapper">
                <div class="input-icon phone-icon">📱</div>
                <el-input 
                  v-model="registerForm.phone" 
                  placeholder="请输入手机号" 
                  class="custom-input"
                  :prefix-icon="''"
                  @focus="inputFocus('phone')"
                  @blur="inputBlur('phone')"
                  :class="{ 'input-focused': focusField === 'phone', 'input-error': errorFields.phone }"
                ></el-input>
              </div>
            </el-form-item>
            

            
            <!-- 用户名输入 -->
            <el-form-item prop="username" class="form-item">
              <div class="input-wrapper">
                <div class="input-icon user-icon">👤</div>
                <el-input 
                  v-model="registerForm.username" 
                  placeholder="请输入用户名" 
                  class="custom-input"
                  :prefix-icon="''"
                  @focus="inputFocus('username')"
                  @blur="inputBlur('username')"
                  :class="{ 'input-focused': focusField === 'username', 'input-error': errorFields.username }"
                ></el-input>
              </div>
            </el-form-item>
            
            <!-- 真实姓名输入 -->
            <el-form-item prop="realName" class="form-item">
              <div class="input-wrapper">
                <div class="input-icon name-icon">🧑</div>
                <el-input 
                  v-model="registerForm.realName" 
                  placeholder="请输入真实姓名" 
                  class="custom-input"
                  :prefix-icon="''"
                  @focus="inputFocus('realName')"
                  @blur="inputBlur('realName')"
                  :class="{ 'input-focused': focusField === 'realName', 'input-error': errorFields.realName }"
                ></el-input>
              </div>
            </el-form-item>
            
            <!-- 密码输入 -->
            <el-form-item prop="password" class="form-item">
              <div class="input-wrapper">
                <div class="input-icon password-icon">🔒</div>
                <el-input 
                  v-model="registerForm.password" 
                  placeholder="请设置密码" 
                  type="password" 
                  class="custom-input"
                  :prefix-icon="''"
                  @focus="inputFocus('password')"
                  @blur="inputBlur('password')"
                  :class="{ 'input-focused': focusField === 'password', 'input-error': errorFields.password }"
                ></el-input>
              </div>
            </el-form-item>
            
            <!-- 确认密码输入 -->
            <el-form-item prop="confirmPassword" class="form-item">
              <div class="input-wrapper">
                <div class="input-icon confirm-icon">✅</div>
                <el-input 
                  v-model="registerForm.confirmPassword" 
                  placeholder="请确认密码" 
                  type="password" 
                  class="custom-input"
                  :prefix-icon="''"
                  @focus="inputFocus('confirmPassword')"
                  @blur="inputBlur('confirmPassword')"
                  :class="{ 'input-focused': focusField === 'confirmPassword', 'input-error': errorFields.confirmPassword }"
                ></el-input>
              </div>
            </el-form-item>
            
            <!-- 注册按钮 -->
            <el-form-item class="form-item login-btn-container">
              <el-button 
                type="primary" 
                @click="handleRegister"
                class="login-button"
                :loading="loading"
              >
                <span v-if="!loading">注册</span>
                <span v-else>注册中...</span>
              </el-button>
            </el-form-item>
          </div>
        </el-form>
        
        <!-- 注册底部 -->
        <div class="login-footer">
          <div class="switch-login">
            <span>已有账号？</span>
            <a href="/login" class="login-link">立即登录</a>
          </div>
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
import { ref, reactive, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

export default {
  name: 'Register',
  setup() {
    const router = useRouter()
    const registerFormRef = ref(null)
    const loading = ref(false)
    const focusField = ref('')
    const errorFields = reactive({
      phone: false, 
      username: false, 
      realName: false,
      password: false, 
      confirmPassword: false 
    })
    
    // 注册表单数据
    const registerForm = reactive({
      phone: '',
      username: '',
      realName: '',
      password: '',
      confirmPassword: '',
      role: 'owner' // 默认机主角色
    })
    
    // 表单验证规则
    const rules = {
      phone: [
        { required: true, message: '请输入手机号', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
      ],
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 2, max: 20, message: '用户名长度为2-20个字符', trigger: 'blur' }
      ],
      realName: [
        { required: true, message: '请输入真实姓名', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请设置密码', trigger: 'blur' },
        { min: 8, max: 20, message: '密码长度为8-20个字符', trigger: 'blur' },
        { pattern: /^(?=.*[0-9])(?=.*[a-zA-Z])[a-zA-Z0-9]{8,20}$/, 
          message: '密码必须包含数字和字母，长度8-20位', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请确认密码', trigger: 'blur' },
        { validator: (rule, value, callback) => {
            if (value !== registerForm.password) {
              callback(new Error('两次输入的密码不一致'))
            } else {
              callback()
            }
          }, trigger: 'blur' }
      ]
    }
    
    // 输入框聚焦处理
    const inputFocus = (field) => {
      focusField.value = field
      errorFields[field] = false
    }
    
    // 输入框失焦处理
    const inputBlur = (field) => {
      focusField.value = ''
      // 验证当前字段
      registerFormRef.value?.validateField(field, (error) => {
        if (error) {
          errorFields[field] = true
        }
      })
    }
    

    
    // 处理注册
    const handleRegister = async () => {
      try {
        // 表单验证
        await registerFormRef.value.validate()
        
        loading.value = true
        
        // 准备注册数据，移除确认密码字段
        const registerData = {
          phone: registerForm.phone,
          username: registerForm.username,
          realName: registerForm.realName,
          password: registerForm.password,
          role: registerForm.role
        }
        
        // 实际调用后端API
        console.log('注册数据:', registerData)
        
        try {
          const response = await fetch('/auth/register', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json'
            },
            body: JSON.stringify(registerData)
          })
          
          if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.message || '注册失败，请稍后重试');
          }
          
          const result = await response.json();
          console.log('注册成功:', result);
        } catch (apiError) {
          console.error('注册API调用失败:', apiError);
          throw apiError;
        }
        
        // 模拟注册成功
        ElMessage.success({
          message: '注册成功，请等待审核通过后登录...',
          duration: 1500,
          showClose: true
        })
        
        // 跳转到登录页
        setTimeout(() => {
          router.push('/login')
        }, 1000)
        
      } catch (error) {
        console.error('注册失败:', error)
        // 清除错误状态
        Object.keys(errorFields).forEach(key => {
          errorFields[key] = false
        })
      } finally {
        loading.value = false
      }
    }
    
    return {
      registerForm,
      rules,
      registerFormRef,
      loading,
      focusField,
      errorFields,
      handleRegister,
      inputFocus,
      inputBlur
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
  margin-bottom: 36px;
  position: relative;
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

/* 登录按钮样式效果 */
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

/* 切换登录链接 */
.switch-login {
  text-align: center;
  margin-bottom: 16px;
}

.login-link {
  color: #409EFF;
  font-size: 14px;
  text-decoration: none;
  padding: 8px 16px;
  border-radius: 4px;
  transition: all 0.3s ease;
  display: inline-block;
}

.login-link:hover {
  color: #66B1FF;
  background-color: rgba(64, 158, 255, 0.05);
  text-decoration: underline;
  transform: translateY(-1px);
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