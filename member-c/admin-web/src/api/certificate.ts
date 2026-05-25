import request from './request'
import type { PageResult } from './user'

export interface CertificateDetail {
  id: number
  certType: string
  certTypeLabel: string
  title: string
  description: string
  attachmentUrl: string
  attachmentName: string
  status: string
  statusLabel: string
  studentName: string
  studentNo: string
  className: string
  approverName: string
  approveComment: string
  pdfUrl: string
  applyTime: string
  approveTime: string
  issueTime: string
  progressNodes: Array<{ type: string; time: string; message: string; label: string }>
}

export function getCertificateList(params: {
  page?: number
  pageSize?: number
  status?: string
  certType?: string
  grade?: string
  className?: string
}) {
  return request.get<any, PageResult<CertificateDetail>>('/admin/certificates', { params })
}

export function getCertificateDetail(id: number) {
  return request.get<any, CertificateDetail>(`/admin/certificates/${id}`)
}

export function auditCertificate(id: number, data: { result: string; comment?: string }) {
  return request.post(`/admin/certificates/${id}/audit`, data)
}

export function issueCertificate(id: number, data: { pdfUrl: string }) {
  return request.post(`/admin/certificates/${id}/issue`, data)
}

export function getCertificateStats() {
  return request.get<any, Record<string, number>>('/admin/certificates/stats')
}
