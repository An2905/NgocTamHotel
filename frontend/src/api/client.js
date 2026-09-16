export async function apiRequest(path, options = {}) {
  const token = sessionStorage.getItem('accessToken')
  const baseUrl = import.meta.env.VITE_API_URL || '';
  
  const response = await fetch(`\({baseUrl}/api\){path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  })

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'Yêu cầu thất bại' }))
    throw new Error(error.message || 'Yêu cầu thất bại')
  }

  return response.status === 204 ? null : response.json()
}
