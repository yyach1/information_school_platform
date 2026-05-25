import request from './request'
import type { PageResult } from './user'

export interface MaterialListItem {
  id: number
  studentNo: string
  studentName: string
  grade: string
  className: string
  processType: string
  processName: string
  nodeName: string
  materialType: string
  status: string
  submitTime: string
}

export interface MaterialApprovalRecord {
  id: number
  approverId: number
  approverName?: string
  result: string
  comment?: string
  approveTime: string
}

export interface MaterialDetail {
  id: number
  studentProcessId: number
  studentId: number
  studentNo: string
  studentName: string
  grade: string
  className: string
  processType: string
  processName: string
  nodeName: string
  materialType: string
  fileUrl: string
  fileName: string
  description?: string
  status: string
  submitTime: string
  approvals: MaterialApprovalRecord[]
}

export function listMaterials(params: {
  page?: number
  pageSize?: number
  status?: string
  grade?: string
  className?: string
  processType?: string
}) {
  return request.get<any, PageResult<MaterialListItem>>('/admin/materials', { params })
}

export function getMaterialDetail(id: number) {
  return request.get<any, MaterialDetail>(`/admin/materials/${id}`)
}

export function auditMaterial(id: number, data: { result: 'APPROVED' | 'RETURNED'; comment?: string }) {
  return request.post(`/admin/materials/${id}/audit`, data)
}
