import { z } from 'zod'
import { toAsciiDigits } from '../../lib/format'
import { EMPLOYER_KINDS, type EmployerKind, type EmployerProfile, type EmployerProfileInput } from '../../lib/types'

/** Bangladeshi mobile numbers: 01XXXXXXXXX or +8801XXXXXXXXX (spaces and dashes allowed). Same rule as the server. */
export const BD_MOBILE = /^(\+?880[-\s]?|0)1[3-9](?:[-\s]?\d){8}$/

// Error messages are i18n keys; the form translates them.
export const profileSchema = z.object({
  name: z.string().trim().min(2, 'v.nameLength').max(120, 'v.nameLength'),
  kind: z.enum(EMPLOYER_KINDS as [EmployerKind, ...EmployerKind[]], 'v.required'),
  address: z.string().trim().min(1, 'v.required').max(300),
  contactPerson: z.string().trim().min(1, 'v.required').max(120),
  contactPhone: z
    .string()
    .trim()
    .min(1, 'v.required')
    .refine((value) => BD_MOBILE.test(toAsciiDigits(value)), 'v.phone'),
  contactEmail: z.union([z.literal(''), z.email('v.email')]),
  latitude: z.number().nullable(),
  longitude: z.number().nullable(),
})

export type ProfileFormValues = z.infer<typeof profileSchema>

export function toInput(values: ProfileFormValues): EmployerProfileInput {
  return {
    ...values,
    contactPhone: toAsciiDigits(values.contactPhone),
    contactEmail: values.contactEmail === '' ? null : values.contactEmail,
  }
}

export function toFormValues(profile: EmployerProfile | null | undefined): ProfileFormValues {
  return {
    name: profile?.name ?? '',
    kind: profile?.kind ?? 'SME',
    address: profile?.address ?? '',
    contactPerson: profile?.contactPerson ?? '',
    contactPhone: profile?.contactPhone ?? '',
    contactEmail: profile?.contactEmail ?? '',
    latitude: profile?.latitude ?? null,
    longitude: profile?.longitude ?? null,
  }
}
