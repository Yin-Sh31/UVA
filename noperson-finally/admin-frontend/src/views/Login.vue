<template>
  <div class="login-container">
    <div class="login-form">
      <h2>管理员登录</h2>
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" prefix-icon="el-icon-user"></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" placeholder="密码" prefix-icon="el-icon-lock" show-password></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" style="width: 100%">登录</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import axios from '../utils/axios'

export default {
  data() {
    return {
      loginForm: {
        username: '',
        password: ''
      },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    handleLogin() {
      this.$refs.loginFormRef.validate((valid) => {
        if (valid) {
          const loginData = {
            username: this.loginForm.username,
            password: this.loginForm.password,
            loginType: 'password',
            role: 'admin'
          };
          
          axios.post('/auth/login', loginData)
            .then(res => {
              if (res.code === 200 && res.data.token) {
                // 存储token
                localStorage.setItem('adminToken', res.data.token);
                this.$message.success('登录成功');
                this.$router.push('/dashboard');
              } else {
                this.$message.error(res.message || '登录失败');
              }
            })
            .catch(error => {
              console.error('登录错误:', error);
              this.$message.error('登录失败，请检查网络或账号密码');
            });
        }
      });
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fa;
}

.login-form {
  width: 400px;
  background-color: #fff;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.login-form h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
}
</style>