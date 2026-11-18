import { describe, expect, it } from 'vitest'

// Every page and component (the views), read as text.
const views = import.meta.glob(['./**/*.tsx', '!./**/*.test.tsx'], { query: '?raw', import: 'default', eager: true }) as Record<string, string>

describe('MVC layers in the web app', () => {
  it('finds the views', () => {
    expect(Object.keys(views).length).toBeGreaterThan(20)
  })

  it('views never call the server themselves: they use the hooks in features/*/api.ts', () => {
    const direct = /\bapi\.(get|post|put|patch|delete|upload)\b|\bgetOrNull\(|\bfetch\(/
    const offenders = Object.entries(views)
      .filter(([, source]) => direct.test(source))
      .map(([file]) => file)
    expect(offenders).toEqual([])
  })
})
