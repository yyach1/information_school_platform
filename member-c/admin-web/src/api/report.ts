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
  const res = await fetch(`/api/reports/export?type=${encodeURIComponent(type)}`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  })
  if (!res.ok) {
    const text = await res.text()
    let msg = `导出失败 (${res.status})`
    try { const json = JSON.parse(text); if (json.message) msg = json.message } catch {}
    throw new Error(msg)
  }
  const blob = await res.blob()
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `report-${type}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}
