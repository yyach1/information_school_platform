import axios from 'axios'
import request from './request'
import type { PageResult } from '@/types/common'

export interface FileRecord {
  id: number
  ownerId: number
  relatedType: string
  relatedId?: number
  originalName: string
  fileName: string
  fileUrl: string
  previewUrl: string
  contentType: string
  fileSize: number
  status: string
  createdAt: string
}

export interface FileQuery {
  page?: number
  pageSize?: number
  ownerId?: number | string
  relatedType?: string
  relatedId?: number | string
  keyword?: string
}

function authHeaders() {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

export function listFiles(params: FileQuery) {
  return request.get<any, PageResult<FileRecord>>('/files', { params })
}

export function deleteFile(fileId: number) {
  return request.delete<any, void>(`/files/${fileId}`)
}

export function getFileMeta(fileId: number) {
  return request.get<any, FileRecord>(`/files/${fileId}/meta`)
}

export function uploadFile(formData: FormData) {
  return request.post<any, FileRecord>('/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function downloadFileBlob(fileId: number) {
  return axios.get(`/api/files/${fileId}`, {
    responseType: 'blob',
    headers: authHeaders(),
  })
}

export function previewFileBlob(fileId: number) {
  return axios.get(`/api/files/${fileId}/preview`, {
    responseType: 'blob',
    headers: authHeaders(),
  })
}
