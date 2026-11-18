import { z } from 'zod'
import { toAsciiDigits } from '../../lib/format'
import type { Job, JobDraft, JobInput, JobType, Shift, Term, WagePeriod } from '../../lib/types'

/**
 * The "Post a job" form keeps what the user typed as text (Bangla digits allowed) and converts it to
 * numbers when it is sent. The rules match the server's JobRules: a draft needs only a title; publishing
 * also needs a description, a category, the wage range and period, a location (or remote) and the hours.
 */
export interface JobFormValues {
  titleBn: string
  titleEn: string
  descriptionBn: string
  descriptionEn: string
  occupation: Term | null
  openings: string
  jobType: JobType
  wageMin: string
  wageMax: string
  wagePeriod: WagePeriod | ''
  shift: Shift
  startTime: string
  endTime: string
  latitude: number | null
  longitude: number | null
  areaName: string
  remote: boolean
  skills: Term[]
  requiredDocuments: string[]
  minExperienceYears: string
}

export type JobFormMode = 'draft' | 'submit'
export type JobFormErrors = Partial<Record<keyof JobFormValues, string>>

/** The fields on each wizard step, in order. */
export const STEP_FIELDS: Array<Array<keyof JobFormValues>> = [
  ['titleBn', 'titleEn', 'occupation', 'openings', 'jobType', 'descriptionBn', 'descriptionEn'],
  ['wageMin', 'wageMax', 'wagePeriod', 'shift', 'startTime', 'endTime'],
  ['latitude', 'areaName', 'remote'],
  ['skills', 'requiredDocuments', 'minExperienceYears'],
]

export const emptyJobForm: JobFormValues = {
  titleBn: '',
  titleEn: '',
  descriptionBn: '',
  descriptionEn: '',
  occupation: null,
  openings: '1',
  jobType: 'FULL_TIME',
  wageMin: '',
  wageMax: '',
  wagePeriod: 'MONTH',
  shift: 'DAY',
  startTime: '',
  endTime: '',
  latitude: null,
  longitude: null,
  areaName: '',
  remote: false,
  skills: [],
  requiredDocuments: [],
  minExperienceYears: '',
}

/** Text → whole number; '' → null; anything else → NaN. Bangla digits and thousands commas are accepted. */
export function parseWhole(text: string): number | null {
  const clean = toAsciiDigits(text).replace(/[,\s]/g, '')
  if (clean === '') return null
  return /^\d+$/.test(clean) ? Number(clean) : Number.NaN
}

/** Checks the form; returns field → i18n message key. Used by the zod resolver and by the tests. */
export function validateJobForm(v: JobFormValues, mode: JobFormMode): JobFormErrors {
  const errors: JobFormErrors = {}
  const title = v.titleBn.trim() || v.titleEn.trim()
  if (!title) errors.titleBn = 'v.titleRequired'
  else if (title.length < 2) errors.titleBn = 'v.titleShort'

  const openings = parseWhole(v.openings)
  if (openings !== null && (Number.isNaN(openings) || openings < 1 || openings > 1000)) errors.openings = 'v.openingsRange'

  const min = parseWhole(v.wageMin)
  const max = parseWhole(v.wageMax)
  if (min !== null && (Number.isNaN(min) || min <= 0)) errors.wageMin = 'v.wagePositive'
  if (max !== null && (Number.isNaN(max) || max <= 0)) errors.wageMax = 'v.wagePositive'
  if (min !== null && max !== null && !errors.wageMin && !errors.wageMax && min > max) errors.wageMax = 'v.wageRange'

  const years = parseWhole(v.minExperienceYears)
  if (years !== null && (Number.isNaN(years) || years > 50)) errors.minExperienceYears = 'v.experienceRange'

  if (mode === 'submit') {
    if (!v.descriptionBn.trim() && !v.descriptionEn.trim()) errors.descriptionBn = 'v.descriptionRequired'
    if (!v.occupation) errors.occupation = 'v.categoryRequired'
    if (min === null) errors.wageMin ??= 'v.wageRequired'
    if (max === null) errors.wageMax ??= 'v.wageRequired'
    if (!v.wagePeriod) errors.wagePeriod = 'v.periodRequired'
    if (!v.remote && (v.latitude == null || v.longitude == null)) errors.latitude = 'v.locationRequired'
    if (!v.startTime || !v.endTime) errors.startTime = 'v.hoursRequired'
  }
  return errors
}

/** A zod schema for React Hook Form whose rules depend on the button pressed (save draft or publish). */
export function jobFormSchema(mode: () => JobFormMode) {
  return z.custom<JobFormValues>().superRefine((values, ctx) => {
    for (const [path, message] of Object.entries(validateJobForm(values, mode()))) {
      ctx.addIssue({ code: 'custom', path: [path], message })
    }
  })
}

const orNull = (text: string) => (text.trim() === '' ? null : text.trim())

export function toJobInput(v: JobFormValues): JobInput {
  return {
    titleBn: orNull(v.titleBn),
    titleEn: orNull(v.titleEn),
    descriptionBn: orNull(v.descriptionBn),
    descriptionEn: orNull(v.descriptionEn),
    occupationId: v.occupation?.id ?? null,
    jobType: v.jobType,
    shift: v.shift,
    hours: v.startTime && v.endTime ? `${v.startTime}-${v.endTime}` : null,
    openings: parseWhole(v.openings),
    wageMin: parseWhole(v.wageMin),
    wageMax: parseWhole(v.wageMax),
    wagePeriod: v.wagePeriod || null,
    latitude: v.remote ? null : v.latitude,
    longitude: v.remote ? null : v.longitude,
    areaName: orNull(v.areaName),
    remote: v.remote,
    minExperienceYears: parseWhole(v.minExperienceYears),
    skillIds: v.skills.map((s) => s.id),
    requiredDocuments: v.requiredDocuments,
  }
}

export function toJobForm(job: Job): JobFormValues {
  const [startTime = '', endTime = ''] = job.hours?.split('-') ?? []
  return {
    titleBn: job.titleBn ?? '',
    titleEn: job.titleEn === job.titleBn ? '' : (job.titleEn ?? ''),
    descriptionBn: job.descriptionBn ?? '',
    descriptionEn: job.descriptionEn === job.descriptionBn ? '' : (job.descriptionEn ?? ''),
    occupation: job.occupation,
    openings: String(job.openings),
    jobType: job.jobType,
    wageMin: job.wageMin == null ? '' : String(job.wageMin),
    wageMax: job.wageMax == null ? '' : String(job.wageMax),
    wagePeriod: job.wagePeriod ?? '',
    shift: job.shift,
    startTime,
    endTime,
    latitude: job.latitude,
    longitude: job.longitude,
    areaName: job.areaName ?? '',
    remote: job.remote,
    skills: job.skills,
    requiredDocuments: job.requiredDocuments,
    minExperienceYears: job.minExperienceYears == null ? '' : String(job.minExperienceYears),
  }
}

/**
 * A draft from the job-post assistant, as form values. Everything stays editable; nothing is sent until
 * the employer saves or publishes. The map pin is never guessed: the employer pins the place in step 3.
 */
export function draftToForm(draft: JobDraft, base: JobFormValues = emptyJobForm): JobFormValues {
  const [startTime = '', endTime = ''] = draft.hours?.split('-') ?? []
  return {
    ...base,
    titleBn: draft.titleBn ?? base.titleBn,
    titleEn: draft.titleEn ?? base.titleEn,
    descriptionBn: draft.descriptionBn ?? base.descriptionBn,
    descriptionEn: draft.descriptionEn ?? base.descriptionEn,
    occupation: draft.occupation ?? base.occupation,
    openings: draft.openings == null ? base.openings : String(draft.openings),
    jobType: draft.jobType ?? base.jobType,
    wageMin: draft.wageMin == null ? base.wageMin : String(draft.wageMin),
    wageMax: draft.wageMax == null ? base.wageMax : String(draft.wageMax),
    wagePeriod: draft.wagePeriod ?? base.wagePeriod,
    shift: draft.shift ?? base.shift,
    startTime: startTime || base.startTime,
    endTime: endTime || base.endTime,
    areaName: draft.areaName ?? base.areaName,
    skills: draft.skills.length > 0 ? draft.skills : base.skills,
    requiredDocuments: draft.requiredDocuments.length > 0 ? draft.requiredDocuments : base.requiredDocuments,
  }
}

/** Server field names → form field names, for errors that come back from the API. */
export const SERVER_FIELD: Record<string, keyof JobFormValues> = {
  occupationId: 'occupation',
  hours: 'startTime',
  skillIds: 'skills',
  longitude: 'latitude',
}
