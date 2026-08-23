import { apiRequest } from './client.js'

export function login(credentials) {
  return apiRequest('/auth/login', {
    method: 'POST',
    body: JSON.stringify(credentials),
  })
}

export function register(account) {
  return apiRequest('/auth/register', {
    method: 'POST',
    body: JSON.stringify(account),
  })
}

export function saveSession(auth) {
  sessionStorage.setItem('accessToken', auth.token)
  sessionStorage.setItem('currentUser', JSON.stringify({
    id: auth.userId,
    username: auth.username,
    email: auth.email,
  }))
}

export function isAuthenticated() {
  return Boolean(sessionStorage.getItem('accessToken'))
}
