import { describe, expect, it } from 'vitest'
import bn from './bn.json'
import en from './en.json'

function keys(value: unknown, prefix = ''): string[] {
  if (value && typeof value === 'object' && !Array.isArray(value)) {
    return Object.entries(value).flatMap(([key, child]) => keys(child, prefix ? `${prefix}.${key}` : key))
  }
  return [prefix]
}

describe('translations', () => {
  it('Bangla and English have exactly the same keys', () => {
    expect(keys(bn).sort()).toEqual(keys(en).sort())
  })
})
