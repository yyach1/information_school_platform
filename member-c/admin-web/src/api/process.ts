import request from './request'
import type { PageResult } from './user'

export interface ProcessItem {
  id: number
  name: string
  type: string
  description?: string
  status: string
  version?: number
  createdAt?: string
}

export interface ProcessNodeItem {
  id?: number
  processId?: number
  nodeName: string
  nodeOrder: number
  approverRole: string
  requiredMaterial?: string
}

export function listProcesses(params: {
  page?: number
  pageSize?: number
  keyword?: string
  status?: string
}) {
  return request.get<any, PageResult<ProcessItem>>('/admin/processes', { params })
}

export function createProcess(data: { name: string; type: string; description?: string }) {
  return request.post('/admin/processes', data)
}

export function updateProcess(id: number, data: { name: string; description?: string; status: string }) {
  return request.put(`/admin/processes/${id}`, data)
}

export function updateProcessNodes(
  id: number,
  data: { nodes: ProcessNodeItem[] }
) {
  return request.put(`/admin/processes/${id}/nodes`, data)
}

export function getProcessNodes(id: number) {
  return request.get<any, ProcessNodeItem[]>(`/admin/processes/${id}/nodes`)
}
