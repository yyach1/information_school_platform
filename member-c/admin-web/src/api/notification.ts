import request from './request'
import type { PageResult } from './user'

export interface NotificationItem {
  id: number
  title: string
  content: string
  notificationType: string
  bizType: string
  bizId: number
  readStatus: string
  createdAt: string
  readAt: string
}

export interface UnreadCount {
  noticeCount: number
  todoCount: number
  totalCount: number
}

export function getNotifications(params: {
  page: number
  pageSize: number
  type?: string
  readStatus?: string
}) {
  return request.get<any, PageResult<NotificationItem>>('/notifications', { params })
}

export function getUnreadCount() {
  return request.get<any, UnreadCount>('/notifications/unread-count')
}

export function markAsRead(id: number) {
  return request.put(`/notifications/${id}/read`)
}

export function createNotification(data: {
  receiverId: number
  title: string
  content: string
  notificationType?: string
  bizType?: string
  bizId?: number
}) {
  return request.post('/notifications', data)
}
