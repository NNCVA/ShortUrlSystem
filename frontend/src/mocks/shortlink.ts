import Mock from 'mockjs'

// 模拟短链接数据
const mockShortLinks: any[] = [
  {
    id: 1,
    name: '浙江理工大学官网',
    originalUrl: 'https://www.zstu.edu.cn',
    shortCode: 'zstu01',
    status: 'ENABLED',
    clickCount: 128,
    createdAt: '2025-06-01T10:00:00',
    updatedAt: '2025-06-01T10:00:00'
  },
  {
    id: 2,
    name: '百度',
    originalUrl: 'https://www.baidu.com',
    shortCode: 'baidu02',
    status: 'ENABLED',
    clickCount: 256,
    createdAt: '2025-06-02T10:00:00',
    updatedAt: '2025-06-02T10:00:00'
  },
  {
    id: 3,
    name: 'GitHub',
    originalUrl: 'https://github.com',
    shortCode: 'github3',
    status: 'DISABLED',
    clickCount: 64,
    createdAt: '2025-06-03T10:00:00',
    updatedAt: '2025-06-03T10:00:00'
  }
]

// 生成随机短码
function generateShortCode(): string {
  const chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'
  let code = ''
  for (let i = 0; i < 6; i++) {
    code += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  return code
}

// Mock 获取短链接列表
Mock.mock(/\/api\/shortlinks/, 'get', (options: any) => {
  const url = new URL(options.url, 'http://localhost')
  const name = url.searchParams.get('name')
  const page = parseInt(url.searchParams.get('page') || '1')
  const pageSize = parseInt(url.searchParams.get('pageSize') || '10')

  let filteredList = mockShortLinks
  if (name) {
    filteredList = filteredList.filter(item =>
      item.name.toLowerCase().includes(name.toLowerCase())
    )
  }

  const start = (page - 1) * pageSize
  const end = start + pageSize
  const list = filteredList.slice(start, end)

  return {
    code: 200,
    message: 'success',
    data: {
      total: filteredList.length,
      page,
      pageSize,
      list
    }
  }
})

// Mock 创建短链接
Mock.mock(/\/api\/shortlinks/, 'post', (options: any) => {
  const body = JSON.parse(options.body)
  const newLink = {
    id: mockShortLinks.length + 1,
    name: body.name,
    originalUrl: body.originalUrl,
    shortCode: generateShortCode(),
    status: 'ENABLED',
    clickCount: 0,
    createdAt: new Date().toISOString().replace('T', ' ').substring(0, 19),
    updatedAt: new Date().toISOString().replace('T', ' ').substring(0, 19)
  }
  mockShortLinks.push(newLink)

  return {
    code: 200,
    message: '创建成功',
    data: newLink
  }
})

// Mock 更新短链接
Mock.mock(/\/api\/shortlinks\/\d+/, 'put', (options: any) => {
  const body = JSON.parse(options.body)
  const id = parseInt(options.url.match(/\/(\d+)$/)?.[1] || '0')
  const link = mockShortLinks.find(item => item.id === id)

  if (link) {
    link.name = body.name
    link.originalUrl = body.originalUrl
    link.updatedAt = new Date().toISOString().replace('T', ' ').substring(0, 19)
    return {
      code: 200,
      message: '更新成功',
      data: link
    }
  }

  return {
    code: 404,
    message: '短链接不存在',
    data: null
  }
})

// Mock 删除短链接
Mock.mock(/\/api\/shortlinks\/\d+/, 'delete', (options: any) => {
  const id = parseInt(options.url.match(/\/(\d+)$/)?.[1] || '0')
  const index = mockShortLinks.findIndex(item => item.id === id)

  if (index > -1) {
    mockShortLinks.splice(index, 1)
    return {
      code: 200,
      message: '删除成功',
      data: null
    }
  }

  return {
    code: 404,
    message: '短链接不存在或已被删除',
    data: null
  }
})

// Mock 启用短链接
Mock.mock(/\/api\/shortlinks\/\d+\/enable/, 'patch', (options: any) => {
  const id = parseInt(options.url.match(/\/(\d+)\/enable/)?.[1] || '0')
  const link = mockShortLinks.find(item => item.id === id)

  if (link) {
    link.status = 'ENABLED'
    return {
      code: 200,
      message: '已启用',
      data: { id, status: 'ENABLED' }
    }
  }

  return {
    code: 404,
    message: '短链接不存在',
    data: null
  }
})

// Mock 禁用短链接
Mock.mock(/\/api\/shortlinks\/\d+\/disable/, 'patch', (options: any) => {
  const id = parseInt(options.url.match(/\/(\d+)\/disable/)?.[1] || '0')
  const link = mockShortLinks.find(item => item.id === id)

  if (link) {
    link.status = 'DISABLED'
    return {
      code: 200,
      message: '已禁用',
      data: { id, status: 'DISABLED' }
    }
  }

  return {
    code: 404,
    message: '短链接不存在',
    data: null
  }
})

// Mock 演示页生成短码
Mock.mock(/\/api\/shortlinks\/generate/, 'post', (options: any) => {
  const body = JSON.parse(options.body)
  const shortCode = generateShortCode()

  return {
    code: 200,
    message: '生成成功',
    data: {
      shortCode,
      shortUrl: `http://localhost:5173/${shortCode}`,
      originalUrl: body.originalUrl
    }
  }
})
