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

async function fetchBlob(url: string): Promise<Blob> {
  const token = localStorage.getItem('token')
  const res = await fetch(url, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  })
  if (!res.ok) {
    const text = await res.text()
    let msg = `请求失败 (${res.status})`
    try {
      const json = JSON.parse(text)
      if (json.message) msg = json.message
    } catch {}
    throw new Error(msg)
  }
  return res.blob()
}

export function downloadFileBlob(fileId: number): Promise<Blob> {
  return fetchBlob(`/api/files/${fileId}`)
}

export function previewFileBlob(fileId: number): Promise<Blob> {
  return fetchBlob(`/api/files/${fileId}/preview`)
}
