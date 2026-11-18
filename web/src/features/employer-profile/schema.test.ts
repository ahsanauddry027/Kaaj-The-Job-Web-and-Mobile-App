import { describe, expect, it } from 'vitest'
import { BD_MOBILE, profileSchema, toInput } from './schema'

const valid = {
  name: 'Karim Transport',
  kind: 'SME' as const,
  address: 'Station Road, Tongi',
  contactPerson: 'Karim Uddin',
  contactPhone: '০১৭১২-৩৪৫৬৭৮',
  contactEmail: '',
  latitude: null,
  longitude: null,
}

describe('business profile form', () => {
  it('accepts Bangladeshi mobile numbers in either digit set', () => {
    expect(BD_MOBILE.test('01712345678')).toBe(true)
    expect(BD_MOBILE.test('+8801712345678')).toBe(true)
    expect(BD_MOBILE.test('01212345678')).toBe(false)
    expect(profileSchema.safeParse(valid).success).toBe(true)
  })

  it('refuses a short name and a wrong phone number', () => {
    const result = profileSchema.safeParse({ ...valid, name: 'K', contactPhone: '12345' })
    expect(result.success).toBe(false)
    const messages = result.error!.issues.map((issue) => `${String(issue.path[0])}:${issue.message}`)
    expect(messages).toContain('name:v.nameLength')
    expect(messages).toContain('contactPhone:v.phone')
  })

  it('sends Latin digits and no empty email', () => {
    expect(toInput(valid)).toMatchObject({ contactPhone: '01712-345678', contactEmail: null })
  })
})
