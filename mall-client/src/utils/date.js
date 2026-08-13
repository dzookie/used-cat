export function formatTimeDifference(dateString) {
  const targetDate = new Date(dateString)
  const now = new Date()

  const diffMs = now - targetDate
  const diffSeconds = Math.floor(diffMs / 1000)
  const diffMinutes = Math.floor(diffSeconds / 60)
  const diffHours = Math.floor(diffMinutes / 60)
  const diffDays = Math.floor(diffHours / 24)

  if (diffDays < 30) {
    return `${diffDays}天`
  }

  const diffMonths = Math.floor(diffDays / 30)
  const years = Math.floor(diffMonths / 12)
  const months = diffMonths % 12

  if (years === 0) {
    return `${months}个月`
  }

  if (months === 0) {
    return `${years}年`
  }

  return `${years}年${months}月`
}

export function formatDateTime(dateStr) {
  if (!dateStr) return ''

  let str = dateStr
  if (Array.isArray(dateStr)) {
    const [y, m, d, h = 0, min = 0, s = 0] = dateStr
    str = `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')} ${String(h).padStart(2, '0')}:${String(min).padStart(2, '0')}:${String(s).padStart(2, '0')}`
    return str
  }
  if (!isNaN(dateStr) && dateStr !== '') {
    const d = new Date(dateStr)
    if (!isNaN(d.getTime())) {
      return d.getFullYear() + '-' +
        String(d.getMonth() + 1).padStart(2, '0') + '-' +
        String(d.getDate()).padStart(2, '0') + ' ' +
        String(d.getHours()).padStart(2, '0') + ':' +
        String(d.getMinutes()).padStart(2, '0') + ':' +
        String(d.getSeconds()).padStart(2, '0')
    }
  }
  if (typeof str !== 'string') {
    str = String(str)
  }
  return str.replace('T', ' ').substring(0, 19)
}
