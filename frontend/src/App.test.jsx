import { renderToStaticMarkup } from 'react-dom/server'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'

import App from './App.jsx'
import { LanguageProvider } from './contexts/LanguageContext.jsx'
import { ThemeProvider } from './contexts/ThemeContext.jsx'

function renderAppAt(path) {
  vi.stubGlobal('localStorage', { getItem: () => null, setItem: () => {} })
  return renderToStaticMarkup(
    <MemoryRouter initialEntries={[path]}>
      <ThemeProvider>
        <LanguageProvider><App /></LanguageProvider>
      </ThemeProvider>
    </MemoryRouter>
  )
}

describe('App', () => {
  it('shows guest booking without account links', () => {
    const html = renderAppAt('/')

    expect(html).toContain('Một chốn bình yên giữa lòng biển đảo')
    expect(html).toContain('Tìm phòng trống')
    expect(html).toContain('Chọn căn phòng dành cho bạn')
    expect(html).toContain('href="/booking"')
    expect(html).not.toContain('href="/login"')
    expect(html).not.toContain('href="/register"')
  })

  it('checks room availability before collecting guest details', () => {
    const html = renderAppAt('/booking')

    expect(html).toContain('Kiểm tra phòng trống')
    expect(html).not.toContain('Thông tin khách hàng')
    expect(html).toContain('Tiếp tục và giữ phòng')
  })

  it('shows motorbike rental as the only service', () => {
    const html = renderAppAt('/services')

    expect(html).toContain('Thuê xe máy')
    expect(html).toContain('Liên hệ thuê xe')
    expect(html).toContain('ĐẶT XE TRỰC TUYẾN')
    expect(html).toContain('Kiểm tra xe trống')
    expect(html).toContain('type="date"')
    expect(html).not.toContain('Cắm lửa trại')
    expect(html).not.toContain('Dịch vụ ăn uống')
  })

  it('shows hotel contact details and the supplied map', () => {
    const html = renderAppAt('/contact')

    expect(html).toContain('tel:0914154877')
    expect(html).toContain('mailto:ngoctamhotel.binhba@gmail.com')
    expect(html).toContain('https://www.facebook.com/share/19nC7UF7Zo/')
    expect(html).toContain('11.8398432,109.2349723')
    expect(html).toContain('https://maps.app.goo.gl/hLXXHGVdn7qtM8NU9')
  })
})
