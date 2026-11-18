import { describe, expect, it } from 'vitest'
import { emptyJobForm, parseWhole, toJobInput, validateJobForm, type JobFormValues } from './schema'

const complete: JobFormValues = {
  ...emptyJobForm,
  titleBn: 'পিকআপ ড্রাইভার',
  descriptionBn: 'টঙ্গীতে পিকআপ চালাতে হবে',
  occupation: { id: '0193c8b0-0000-7000-8000-000000000001', nameBn: 'ড্রাইভার', nameEn: 'Driver' },
  openings: '২',
  wageMin: '১৬,০০০',
  wageMax: '20000',
  wagePeriod: 'MONTH',
  startTime: '08:00',
  endTime: '18:00',
  latitude: 23.8916,
  longitude: 90.4023,
}

describe('job form rules (same as the server)', () => {
  it('accepts a complete post for publishing', () => {
    expect(validateJobForm(complete, 'submit')).toEqual({})
  })

  it('always requires the wage before publishing', () => {
    const errors = validateJobForm({ ...complete, wageMin: '', wageMax: '' }, 'submit')
    expect(errors.wageMin).toBe('v.wageRequired')
    expect(errors.wageMax).toBe('v.wageRequired')
  })

  it('lets a draft leave the wage out, but never upside down', () => {
    expect(validateJobForm({ ...complete, wageMin: '', wageMax: '' }, 'draft')).toEqual({})
    expect(validateJobForm({ ...complete, wageMin: '20000', wageMax: '16000' }, 'draft').wageMax).toBe('v.wageRange')
  })

  it('needs a map pin unless the job is remote', () => {
    const noPin = { ...complete, latitude: null, longitude: null }
    expect(validateJobForm(noPin, 'submit').latitude).toBe('v.locationRequired')
    expect(validateJobForm({ ...noPin, remote: true }, 'submit')).toEqual({})
  })

  it('needs only a title for a draft', () => {
    expect(validateJobForm({ ...emptyJobForm, titleBn: 'লোডার' }, 'draft')).toEqual({})
    expect(validateJobForm(emptyJobForm, 'draft').titleBn).toBe('v.titleRequired')
  })

  it('reads Bangla digits and commas, and refuses words', () => {
    expect(parseWhole('১৬,০০০')).toBe(16000)
    expect(parseWhole('')).toBeNull()
    expect(parseWhole('ten')).toBeNaN()
  })

  it('builds the API body', () => {
    const input = toJobInput(complete)
    expect(input).toMatchObject({
      titleBn: 'পিকআপ ড্রাইভার',
      titleEn: null,
      openings: 2,
      wageMin: 16000,
      wageMax: 20000,
      wagePeriod: 'MONTH',
      hours: '08:00-18:00',
      occupationId: complete.occupation!.id,
      remote: false,
    })
    expect(toJobInput({ ...complete, remote: true }).latitude).toBeNull()
  })
})
