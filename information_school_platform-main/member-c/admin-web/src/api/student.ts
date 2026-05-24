import request from './request'
import type { PageResult } from './user'

export interface StudentProgressItem {
  userId: number
  username: string
  realName: string
  studentNo: string
  className: string
  grade: string
  major: string
  politicalStatus: string
  status: string
}

export function getStudentProgress(params: {
  page: number
  pageSize: number
  grade?: string
  className?: string
  politicalStatus?: string
  keyword?: string
}) {
  return request.get<any, PageResult<StudentProgressItem>>('/admin/students/progress', { params })
}
