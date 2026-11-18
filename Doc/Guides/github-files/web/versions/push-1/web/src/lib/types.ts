// The JSON shapes of the Kaaj API (Spring Boot records in app.kaaj.*). Keep them in step with the backend;
// later the team can generate them from /v3/api-docs with openapi-typescript.

export type Role = 'SEEKER' | 'EMPLOYER' | 'ADMIN'

export interface Me {
  id: string
  email: string
  role: Role
  locale: string
}

export interface Term {
  id: string
  nameBn: string
  nameEn: string
}

export interface CursorPage<T> {
  items: T[]
  nextCursor: string | null
}

// ---- Feature 3: employer profile and verification

export type EmployerKind = 'HOUSEHOLD' | 'SME' | 'COMPANY' | 'AGENCY' | 'NGO'
export const EMPLOYER_KINDS: EmployerKind[] = ['HOUSEHOLD', 'SME', 'COMPANY', 'AGENCY', 'NGO']

export type VerificationStatus = 'UNVERIFIED' | 'PENDING' | 'VERIFIED' | 'REJECTED'
export type DocumentType = 'TRADE_LICENCE' | 'NATIONAL_ID'

export interface EmployerProfileInput {
  name: string
  kind: EmployerKind
  address: string
  latitude: number | null
  longitude: number | null
  contactPerson: string
  contactPhone: string
  contactEmail: string | null
}

export interface EmployerProfile extends EmployerProfileInput {
  id: string
  hasDocument: boolean
  documentName: string | null
  documentType: DocumentType | null
  status: VerificationStatus
  verificationNote: string | null
  submittedAt: string | null
  verifiedAt: string | null
  createdAt: string
  canPublishJobs: boolean
  missing: string[]
}

export interface AdminEmployer extends EmployerProfileInput {
  id: string
  ownerEmail: string | null
  documentName: string | null
  documentType: DocumentType | null
  documentUrl: string | null
  status: VerificationStatus
  verificationNote: string | null
  submittedAt: string | null
  verifiedAt: string | null
  createdAt: string
}

// ---- Feature 4: post and manage jobs

export type JobStatus = 'DRAFT' | 'IN_REVIEW' | 'PUBLISHED' | 'PAUSED' | 'CLOSED' | 'BLOCKED'
export type JobType = 'FULL_TIME' | 'PART_TIME' | 'DAILY'
export type Shift = 'DAY' | 'NIGHT' | 'ROTATING' | 'FLEXIBLE'
export type WagePeriod = 'HOUR' | 'DAY' | 'WEEK' | 'MONTH' | 'PIECE'
export type JobAction = 'SUBMIT' | 'PAUSE' | 'RESUME' | 'CLOSE' | 'REPOST'
export type AllowedAction = JobAction | 'EDIT' | 'DELETE'

export const JOB_TYPES: JobType[] = ['FULL_TIME', 'PART_TIME', 'DAILY']
export const SHIFTS: Shift[] = ['DAY', 'NIGHT', 'ROTATING', 'FLEXIBLE']
export const WAGE_PERIODS: WagePeriod[] = ['MONTH', 'DAY', 'WEEK', 'HOUR', 'PIECE']
export const DRIVING_CODES = [
  'DRIVING_MOTORCYCLE',
  'DRIVING_LIGHT',
  'DRIVING_PRO_LIGHT',
  'DRIVING_PRO_MEDIUM',
  'DRIVING_PRO_HEAVY',
] as const

export interface JobInput {
  titleBn: string | null
  titleEn: string | null
  descriptionBn: string | null
  descriptionEn: string | null
  occupationId: string | null
  jobType: JobType
  shift: Shift
  hours: string | null
  openings: number | null
  wageMin: number | null
  wageMax: number | null
  wagePeriod: WagePeriod | null
  latitude: number | null
  longitude: number | null
  areaName: string | null
  remote: boolean
  minExperienceYears: number | null
  skillIds: string[]
  requiredDocuments: string[]
}

export interface EmployerBadge {
  id: string
  name: string
  verified: boolean
}

export interface Job {
  id: string
  status: JobStatus
  statusReason: string | null
  awaitingVerification: boolean
  titleBn: string
  titleEn: string
  descriptionBn: string | null
  descriptionEn: string | null
  occupation: Term | null
  jobType: JobType
  shift: Shift
  hours: string | null
  openings: number
  wageMin: number | null
  wageMax: number | null
  wagePeriod: WagePeriod | null
  wageMonthlyMax: number | null
  latitude: number | null
  longitude: number | null
  areaName: string | null
  remote: boolean
  minExperienceYears: number | null
  skills: Term[]
  requiredDocuments: string[]
  publishedAt: string | null
  expiresAt: string | null
  closedAt: string | null
  createdAt: string
  updatedAt: string
  repostedFrom: string | null
  employer: EmployerBadge | null
  allowedActions: AllowedAction[]
}

export type PublicJob = Omit<
  Job,
  'status' | 'statusReason' | 'awaitingVerification' | 'wageMonthlyMax' | 'closedAt' | 'createdAt' | 'updatedAt' | 'repostedFrom' | 'allowedActions'
>

export type JobCounts = Record<JobStatus, number>
