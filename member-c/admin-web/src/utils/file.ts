export function formatFileSize(size?: number | null): string {
  if (!size || size <= 0) return '-'
  const units = ['B', 'KB', 'MB', 'GB']
  let value = size
  let index = 0
  while (value >= 1024 && index < units.length - 1) {
    value = value / 1024
    index += 1
  }
  return `${value.toFixed(index === 0 ? 0 : 1)} ${units[index]}`
}

export function downloadBlob(blob: Blob, filename: string) {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

export function buildApiUrl(path: string) {
  const base = import.meta.env.VITE_API_BASE_URL || '/api'
  if (path.startsWith('/api/')) return path
  if (path.startsWith('/')) return `${base}${path}`
  return `${base}/${path}`
}
