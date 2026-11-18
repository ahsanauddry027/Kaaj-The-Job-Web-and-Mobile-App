import { describe, expect, it } from 'vitest'
import { daysLeft, formatTaka, formatWage, localDigits, toAsciiDigits } from './format'

describe('number and money formatting', () => {
  it('shows taka with Bangla digits in Bangla and Latin digits in English', () => {
    expect(formatTaka(18000, 'bn')).toBe('৳১৮,০০০')
    expect(formatTaka(18000, 'en')).toBe('৳18,000')
  })

  it('formats a wage range with its period', () => {
    expect(formatWage(16000, 20000, 'মাস', 'bn')).toBe('৳১৬,০০০ – ২০,০০০ / মাস')
    expect(formatWage(700, 700, 'day', 'en')).toBe('৳700 / day')
    expect(formatWage(null, null, 'month', 'en')).toBeNull()
  })

  it('converts digits both ways', () => {
    expect(toAsciiDigits('০১৭১২-৩৪৫৬৭৮')).toBe('01712-345678')
    expect(localDigits('08:00-18:00', 'bn')).toBe('০৮:০০-১৮:০০')
    expect(localDigits(30, 'en')).toBe('30')
  })

  it('counts whole days left, never below zero', () => {
    const now = new Date('2026-11-05T09:00:00Z')
    expect(daysLeft('2026-12-05T09:00:00Z', now)).toBe(30)
    expect(daysLeft('2026-11-01T09:00:00Z', now)).toBe(0)
  })
})
