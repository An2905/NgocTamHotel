import { renderToStaticMarkup } from 'react-dom/server'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it } from 'vitest'

import App from './App.jsx'

describe('App', () => {
  it('renders the hotel dashboard', () => {
    const html = renderToStaticMarkup(<MemoryRouter><App /></MemoryRouter>)

    expect(html).toContain('Một chốn bình yên giữa lòng thành phố')
    expect(html).toContain('Kiểm tra phòng trống')
    expect(html).toContain('Hạng phòng')
  })
})
