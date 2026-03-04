import http from './http'

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  tokenType: string
  expiresIn: number
  userInfo: {
    id: number
    username: string
    role: string
  }
}

export interface UserInfo {
  id: number
  username: string
  role: string
  createdAt?: string
}

export const AuthApi = {
  login: (data: LoginRequest) => http.post<any, any>('/api/auth/login', data),

  me: () => http.get<any, any>('/api/auth/me'),

  logout: () => http.post<any, any>('/api/auth/logout', {})
}
