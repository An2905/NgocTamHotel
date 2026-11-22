async function request(path, options = {}) {
  // Trong dev, gọi cùng origin để Vite proxy chuyển request tới BE local.
  // URL Railway trong .env chỉ dùng cho bản build production.
  const baseUrl = import.meta.env.DEV ? '' : (import.meta.env.VITE_API_URL || '');
  const response = await fetch(`${baseUrl}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  })

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'Yêu cầu thất bại' }))
    throw new Error(error.message || 'Yêu cầu thất bại')
  }

  return response.status === 204 ? null : response.json()
}

export function apiRequest(path, options = {}) {
  return request(`/api${path}`, options);
}

export function roomRequest(path, options = {}) {
  return apiRequest(path, options);
}
