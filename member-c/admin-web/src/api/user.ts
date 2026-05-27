import request from './request'
import type { UserInfo } from './auth'

export type { UserInfo } from './auth'

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  pageSize: number
  pages: number
}

export interface UserCreateParams {
  username: string
  password: string
  realName: string
  role: string
  phone?: string
  email?: string
  studentNo?: string
  className?: string
  grade?: string
  major?: string
  politicalStatus?: string
  teacherNo?: string
  department?: string
  title?: string
}

export function getUserList(params: {
  page: number
  pageSize: number
  keyword?: string
  role?: string
  status?: string
}) {
  return request.get<any, PageResult<UserInfo>>('/users', { params })
}

export function getUserById(id: number) {
  return request.get<any, UserInfo>(`/users/${id}`)
}

export function createUser(data: UserCreateParams) {
  return request.post<any, UserInfo>('/users', data)
}

export function updateUser(id: number, data: any) {
  return request.put<any, UserInfo>(`/users/${id}`, data)
}

export function resetPassword(id: number, newPassword: string) {
  return request.put(`/users/${id}/password`, { newPassword })
}

export function updateUserStatus(id: number, status: string) {
  return request.put(`/users/${id}/status`, { status })
}

export function deleteUser(id: number, adminPassword: string) {
  return request.delete(`/users/${id}`, { data: { adminPassword } })
}
