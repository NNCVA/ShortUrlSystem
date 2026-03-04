import Mock from 'mockjs'
import './auth'
import './shortlink'

// 配置 Mock 请求延迟
Mock.setup({
  timeout: '100-300'
})
