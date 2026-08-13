import 'animate.css'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import 'element-plus/dist/index.css'
import './assets/main.css'

import App from './App.vue'
import router from './router'
import { setupRouterGuards } from './router'

const app = createApp(App)

const pinia = createPinia()
app.use(pinia)
app.use(router)
setupRouterGuards(router)
app.use(ElementPlus, { locale: zhCn })

app.mount('#app')
