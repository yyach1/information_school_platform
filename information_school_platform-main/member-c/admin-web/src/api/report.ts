import axios from 'axios'
import request from './request'

export interface ReportOverview {
  studentCount: number
  materialCount: number
  pendingMaterialCount: number
  approvedMaterialCount: number
  returnedMaterialCount: number
  certificateCount: number
  pendingCertificateCount: number
  todayUploadCount: number
  totalFileSize: number
}

export interface ReportCountItem {
  name: string
  count: number
}

export interface UploadTrendItem {
  date: string
  count: number
  totalSize: number
}

export function getReportOverview() {
  return request.get<any, ReportOverview>('/reports/overview')
}

export function getMaterialStatusReport() {
  return request.get<any, ReportCountItem[]>('/reports/material-status')
}

export function getProcessStatusReport() {
  return request.get<any, ReportCountItem[]>('/reports/process-status')
}

export function getCertificateStatusReport() {
  return request.get<any, ReportCountItem[]>('/reports/certificate-status')
}

export function getUploadTrendReport(days = 7) {
  return request.get<any, UploadTrendItem[]>('/reports/upload-trend', { params: { days } })
}

export async function downloadReport(type: string) {
  const token = localStorage.getItem('token')
  const response = await axios.get('/api/reports/export', {
    params: { type },
    responseType: 'blob',
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  })
  const blob = new Blob([response.data], { type: 'text/csv;charset=utf-8;' })
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `report-${type}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}
