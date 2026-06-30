import dayjs from 'dayjs'

/** 格式化日期时间：yyyy-MM-dd HH:mm:ss */
export function formatDateTime(val) {
  if (!val) return '-'
  const d = dayjs(val)
  return d.isValid() ? d.format('YYYY-MM-DD HH:mm:ss') : '-'
}

/** 格式化日期：yyyy-MM-dd */
export function formatDate(val) {
  if (!val) return '-'
  const d = dayjs(val)
  return d.isValid() ? d.format('YYYY-MM-DD') : '-'
}

/** 金额格式化：¥1,234.56 */
export function formatMoney(val) {
  if (val === null || val === undefined || val === '') return '-'
  const num = Number(val)
  if (Number.isNaN(num)) return '-'
  return '¥' + num.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}
