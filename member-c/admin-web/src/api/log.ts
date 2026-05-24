import request from './request'
import type { PageResult } from './user'

export interface OperationLogItem {
  id: number
  userId: number
  username: string
  realName: string
  operationType: string
  operationContent: string
  bizType: string
  bizId: number
  result: string
  failReason: string
  ipAddress: string
  userAgent: string
  requestId: string
  operationTime: string
}

export function getLogs(params: {
  page: number
  pageSize: number
  userId?: number
  operationType?: string
  result?: string
  startTime?: string
  endTime?: string
  keyword?: string
}) {
  return request.get<any, PageResult<OperationLogItem>>('/logs', { params })
}
