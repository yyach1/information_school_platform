export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  pageSize: number
  pages: number
}

export interface SelectOption {
  label: string
  value: string | number
}
