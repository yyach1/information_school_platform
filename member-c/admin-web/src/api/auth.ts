import request from './request'

export interface LoginParams {
  username: string
  password: string
}

export interface UserInfo {
  userId: number
  username: string
  realName: string
  role: string
  phone: string
  email: string
  status: string
  avatarUrl?: string
  studentNo?: string
  className?: string
  grade?: string
  major?: string
  politicalStatus?: string
  teacherNo?: string
  department?: string
  title?: string
}

export interface LoginResult {
  token: string
  tokenType: string
  expiresIn: number
  userInfo: UserInfo
}

export function login(params: LoginParams) {
  return request.post<any, LoginResult>('/auth/login', params)
}

export function getCurrentUser() {
  return request.get<any, UserInfo>('/auth/me')
}

export function logout() {
  return request.post('/auth/logout')
}
