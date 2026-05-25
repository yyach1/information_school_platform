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

// ── 学生端 API ──

export interface CertListItem {
  id: number
  certType: string
  certTypeLabel: string
  title: string
  status: string
  statusLabel: string
  applyTime: string
}

export interface CertDetail {
  id: number
  certType: string
  certTypeLabel: string
  title: string
  description: string
  attachmentUrl: string
  attachmentName: string
  status: string
  statusLabel: string
  approverName: string
  approveComment: string
  pdfUrl: string
  applyTime: string
  approveTime: string
  issueTime: string
  progressNodes: Array<{ type: string; time: string; message: string; label: string }>
}

export interface CertTypeItem {
  certType: string
  label: string
  description: string
  requireAttachment: boolean
}

export function getMyCertificates(params: { page?: number; pageSize?: number; status?: string; certType?: string }) {
  return request.get<any, PageResult<CertListItem>>('/student/certificates', { params })
}

export function getMyCertificateDetail(id: number) {
  return request.get<any, CertDetail>(`/student/certificates/${id}`)
}

export function getCertTypes() {
  return request.get<any, { items: CertTypeItem[] }>('/student/certificates/types')
}

export function applyCert(data: { certType: string; title: string; description?: string; attachmentUrl?: string; attachmentName?: string }) {
  return request.post<any, CertDetail>('/student/certificates', data)
}

export function resubmitCert(id: number, data: { title: string; description?: string; attachmentUrl?: string; attachmentName?: string }) {
  return request.put<any, CertDetail>(`/student/certificates/${id}`, data)
}

export function downloadCertPdf(id: number) {
  return request.get<any, { pdfUrl: string }>(`/student/certificates/${id}/download`)
}

export function getMyNotificationList(params: { page?: number; pageSize?: number; type?: string }) {
  return request.get<any, PageResult<NotificationItem>>('/notifications', { params })
}

export function getMyUnreadCount() {
  return request.get<any, UnreadCount>('/notifications/unread-count')
}

export function readNotification(id: number) {
  return request.put(`/notifications/${id}/read`)
}

export interface NotificationItem {
  id: number
  title: string
  content: string
  notificationType: string
  readStatus: string
  createdAt: string
}

export interface UnreadCount {
  noticeCount: number
  todoCount: number
  totalCount: number
}
