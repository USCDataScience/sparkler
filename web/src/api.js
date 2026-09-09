export async function get(path) {
  const r = await fetch(path)
  if (!r.ok) throw new Error(`${r.status} ${path}`)
  return r.json()
}

export async function send(path, method, body) {
  const r = await fetch(path, {
    method,
    headers: { 'Content-Type': 'application/json' },
    body: body ? JSON.stringify(body) : undefined
  })
  if (!r.ok) {
    const t = await r.text()
    throw new Error(t || `${r.status} ${path}`)
  }
  return r.json()
}

export function exportUrl(job) {
  return `/api/export?job=${encodeURIComponent(job)}&fmt=json`
}
