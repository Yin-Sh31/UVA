import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import axios from './utils/axios'

const app = createApp(App)
const pinia = createPinia()

// 挂载全局属性
app.config.globalProperties.$axios = axios

app.use(pinia)
app.use(ElementPlus)
app.use(router)

app.mount('#app')