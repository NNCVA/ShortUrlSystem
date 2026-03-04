import Mock from 'mockjs'

// 模拟用户数据
const mockUser = {
  id: 1,
  username: 'admin',
  role: 'ADMIN',
  createdAt: '2025-06-01T10:00:00'
}

// Mock 登录接口
Mock.mock(/\/api\/auth\/login/, 'post', (options: any) => {
  const body = JSON.parse(options.body)
  const { username, password } = body

  if (username === 'admin' && password === 'admin123456') {
    return {
      code: 200,
      message: '登录成功',
      data: {
        token: 'mock-jwt-token-' + Date.now(),
        tokenType: 'Bearer',
        expiresIn: 7200,
        userInfo: mockUser
      }
    }
  }

  return {
    code: 401,
    message: '用户名或密码错误',
    data: null
  }
})

// Mock 获取当前用户信息
Mock.mock(/\/api\/auth\/me/, 'get', () => {
  return {
    code: 200,
    message: 'success',
    data: mockUser
  }
})

// Mock 登出接口
Mock.mock(/\/api\/auth\/logout/, 'post', () => {
  return {
    code: 200,
    message: '已退出登录',
    data: null
  }
})
