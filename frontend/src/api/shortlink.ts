import http from './http'

export interface ShortLink {
  id: number
  name: string
  originalUrl: string
  shortCode: string
  status: 'ENABLED' | 'DISABLED'
  clickCount: number
  createdAt: string
  updatedAt: string
}

export interface ShortLinkListQuery {
  keyword?: string
  status?: string
  page?: number
  pageSize?: number
}

export interface ShortLinkListResponse {
  total: number
  page: number
  pageSize: number
  list: ShortLink[]
}

export interface CreateShortLinkRequest {
  name: string
  originalUrl: string
}

export interface UpdateShortLinkRequest {
  name: string
  originalUrl: string
}

export interface GenerateRequest {
  name: string
  originalUrl: string
}

export interface GenerateResponse {
  shortCode: string
  shortUrl: string
  originalUrl: string
}

export const ShortLinkApi = {
  // 获取短链接列表
  list: (params?: ShortLinkListQuery) =>
    http.get<any, any>('/api/links', { params }),

  // 创建短链接
  create: (data: CreateShortLinkRequest) =>
    http.post<any, any>('/api/links', data),

  // 更新短链接
  update: (id: number, data: UpdateShortLinkRequest) =>
    http.put<any, any>(`/api/links/${id}`, data),

  // 删除短链接
  remove: (id: number) =>
    http.delete<any, any>(`/api/links/${id}`),

  // 启用短链接
  enable: (id: number) =>
    http.patch<any, any>(`/api/links/${id}/toggle`),

  // 禁用短链接
  disable: (id: number) =>
    http.patch<any, any>(`/api/links/${id}/toggle`),

  // 演示页生成短码（公开）
  generate: (data: GenerateRequest) =>
    http.post<any, any>('/api/links', data),

  // 跳转（获取原始URL）
  redirect: (shortCode: string) =>
    http.get<any, any>(`/api/s/${shortCode}`, { timeout: 5000 })
}
