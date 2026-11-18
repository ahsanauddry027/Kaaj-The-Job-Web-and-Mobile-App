import { zodResolver } from '@hookform/resolvers/zod'
import { ArrowLeft, ArrowRight, Briefcase, CheckCircle2, Info, Save, Send } from 'lucide-react'
import { useEffect, useMemo, useRef, useState } from 'react'
import { Controller, useForm, type FieldErrors } from 'react-hook-form'
import { useTranslation } from 'react-i18next'
import { Link, useNavigate, useParams } from 'react-router'
import { MapPicker } from '../../components/MapPicker'
import { TaxonomyPicker } from '../../components/TaxonomyPicker'
import { useToast } from '../../components/Toast'
import { Alert, Button, Card, Field, Input, Select, Spinner, Textarea } from '../../components/ui'
import { ApiError } from '../../lib/api'
import { cn } from '../../lib/cn'
import { localDigits } from '../../lib/format'
import { DRIVING_CODES, JOB_TYPES, SHIFTS, WAGE_PERIODS, type Job } from '../../lib/types'
import { useLang } from '../../lib/useLang'
import { useEmployerProfile } from '../employer-profile/api'
import { useCreateJob, useJob, useUpdateJob } from './api'
import { AssistantPanel } from './AssistantPanel'
import { JobCard } from './JobCard'
import {
  draftToForm,
  emptyJobForm,
  jobFormSchema,
  parseWhole,
  SERVER_FIELD,
  STEP_FIELDS,
  toJobForm,
  toJobInput,
  type JobFormMode,
  type JobFormValues,
} from './schema'

const STEP_KEYS = ['details', 'wage', 'location', 'requirements'] as const

/** "Post a job" (/employer/jobs/new) and "Edit job post" (/employer/jobs/:id/edit): a four-step form. */
export function JobFormPage() {
  const { id } = useParams()
  const { t } = useTranslation()
  const profile = useEmployerProfile()
  const existing = useJob(id)

  if (profile.isLoading || (id && existing.isLoading)) return <Spinner label={t('common.loading')} />
  if (existing.error) return <Alert tone="error">{existing.error.message}</Alert>
  if (!profile.data) {
    return (
      <Alert tone="warning" title={t('job.needProfile')} action={<Link to="/employer/profile" className="font-semibold underline">{t('nav.business')}</Link>} />
    )
  }
  if (existing.data && !existing.data.allowedActions.includes('EDIT')) {
    return (
      <Alert tone="warning" title={t('job.status.' + existing.data.status)}>
        <Link to={`/employer/jobs/${existing.data.id}`} className="font-semibold underline">
          {t('common.viewDetails')}
        </Link>
      </Alert>
    )
  }
  return <JobForm key={id ?? 'new'} job={existing.data ?? null} verified={profile.data.status === 'VERIFIED'} employerName={profile.data.name} />
}

function JobForm({ job, verified, employerName }: { job: Job | null; verified: boolean; employerName: string }) {
  const { t } = useTranslation()
  const lang = useLang()
  const toast = useToast()
  const navigate = useNavigate()
  const create = useCreateJob()
  const update = useUpdateJob(job?.id ?? '')
  const [step, setStep] = useState(0)
  const [serverError, setServerError] = useState<string | null>(null)
  const [idempotencyKey] = useState(() => crypto.randomUUID())
  const mode = useRef<JobFormMode>('submit')
  const isDraft = !job || job.status === 'DRAFT'

  const resolver = useMemo(() => zodResolver(jobFormSchema(() => mode.current)), [])
  const form = useForm<JobFormValues>({ resolver, defaultValues: job ? toJobForm(job) : emptyJobForm })
  const { errors } = form.formState
  const values = form.watch()
  const err = (field: keyof JobFormValues) => {
    const message = errors[field]?.message
    return typeof message === 'string' ? t(message) : undefined
  }

  useEffect(() => {
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }, [step])

  // Once a field shows an error, check it again as the user types, so the message goes away as soon as
  // it's fixed. (Checking on blur instead would move the buttons while they are being clicked.)
  useEffect(() => {
    const related: Partial<Record<keyof JobFormValues, Array<keyof JobFormValues>>> = {
      endTime: ['startTime'],
      wageMin: ['wageMax'],
      longitude: ['latitude'],
    }
    const subscription = form.watch((_, { name }) => {
      const changed = name?.split('.')[0] as keyof JobFormValues | undefined
      if (!changed) return
      const fields = [changed, ...(related[changed] ?? [])].filter((field) => form.getFieldState(field).error)
      if (fields.length > 0) void form.trigger(fields)
    })
    return () => subscription.unsubscribe()
  }, [form])

  async function next() {
    mode.current = 'submit'
    if (await form.trigger(STEP_FIELDS[step])) setStep((s) => Math.min(s + 1, 3))
  }

  function goToFirstError(fieldErrors: FieldErrors<JobFormValues>) {
    const index = STEP_FIELDS.findIndex((fields) => fields.some((field) => fieldErrors[field]))
    if (index >= 0) setStep(index)
  }

  function save(submit: boolean) {
    mode.current = submit ? 'submit' : 'draft'
    setServerError(null)
    void form.handleSubmit(async (formValues) => {
      const input = toJobInput(formValues)
      try {
        const saved = job
          ? await update.mutateAsync({ input, submit: submit && isDraft })
          : await create.mutateAsync({ input, submit, key: idempotencyKey })
        if (saved.status === 'PUBLISHED') toast(t('job.published'))
        else if (saved.awaitingVerification) toast(t('job.waiting'))
        else toast(job ? t('job.updated') : t('job.created'))
        navigate(`/employer/jobs/${saved.id}`)
      } catch (error) {
        if (error instanceof ApiError && Object.keys(error.fieldErrors).length > 0) {
          const mapped: FieldErrors<JobFormValues> = {}
          for (const [field, text] of Object.entries(error.fieldErrors)) {
            const name = SERVER_FIELD[field] ?? (field as keyof JobFormValues)
            form.setError(name, { message: text })
            mapped[name] = { type: 'server', message: text }
          }
          goToFirstError(mapped)
        }
        setServerError(error instanceof Error ? error.message : t('common.somethingWrong'))
      }
    }, goToFirstError)()
  }

  const busy = create.isPending || update.isPending
  const openings = parseWhole(values.openings)

  return (
    <div className="flex flex-col gap-6">
      <Card>
        <div className="flex flex-col gap-6 lg:flex-row lg:items-start lg:justify-between">
          <div className="flex items-start gap-3">
            <div className="grid size-12 shrink-0 place-items-center rounded-2xl bg-brand-600 text-white">
              <Briefcase className="size-6" aria-hidden />
            </div>
            <div>
              <h1 className="text-xl font-bold md:text-2xl">{job ? t('job.editTitle') : t('job.postTitle')}</h1>
              <p className="mt-1 text-sm text-slate-600">{t('job.postSubtitle')}</p>
            </div>
          </div>
          <ol className="flex items-center gap-1 sm:gap-2" aria-label="steps">
            {STEP_KEYS.map((key, index) => (
              <li key={key} className="flex items-center gap-1 sm:gap-2">
                <button
                  type="button"
                  onClick={() => index < step && setStep(index)}
                  className="flex flex-col items-center gap-1"
                  aria-current={index === step ? 'step' : undefined}
                >
                  <span
                    className={cn(
                      'grid size-8 place-items-center rounded-full border-2 text-sm font-bold',
                      index < step && 'border-brand-600 bg-brand-600 text-white',
                      index === step && 'border-brand-600 bg-brand-600 text-white ring-4 ring-brand-100',
                      index > step && 'border-slate-300 bg-white text-slate-500',
                    )}
                  >
                    {index < step ? <CheckCircle2 className="size-4" aria-hidden /> : localDigits(index + 1, lang)}
                  </span>
                  <span className={cn('text-xs font-semibold', index === step ? 'text-slate-900' : 'text-slate-500')}>{t(`job.steps.${key}`)}</span>
                </button>
                {index < 3 && <span className={cn('mb-5 h-0.5 w-6 sm:w-12', index < step ? 'bg-brand-600' : 'bg-slate-200')} />}
              </li>
            ))}
          </ol>
        </div>

        {!job && (
          <div className="mt-5">
            <AssistantPanel
              onUse={(draft) => {
                form.reset(draftToForm(draft, form.getValues()))
                setStep(0)
                setServerError(null)
                toast(t('assistant.filled'))
              }}
            />
          </div>
        )}

        {!verified && (
          <div className="mt-5">
            <Alert tone="warning" title={t('job.notVerifiedTitle')} action={<Link to="/employer/profile" className="font-semibold underline">{t('job.goVerify')}</Link>}>
              {t('job.notVerified')}
            </Alert>
          </div>
        )}

        <div className="mt-6 grid gap-6 lg:grid-cols-[minmax(0,1fr)_320px]">
          <form noValidate onSubmit={(e) => e.preventDefault()} className="flex flex-col gap-5">
            {step === 0 && (
              <>
                <div className="grid gap-4 md:grid-cols-2">
                  <Field label={t('job.titleBn')} required error={err('titleBn')}>
                    {(id, d) => <Input id={id} aria-describedby={d} aria-invalid={!!errors.titleBn} placeholder={t('job.titlePlaceholder')} {...form.register('titleBn')} />}
                  </Field>
                  <Field label={t('job.titleEn')} error={err('titleEn')}>
                    {(id, d) => <Input id={id} aria-describedby={d} lang="en" placeholder="Driver" {...form.register('titleEn')} />}
                  </Field>
                  <Field label={t('job.category')} required error={err('occupation')}>
                    {(id, d) => (
                      <Controller
                        control={form.control}
                        name="occupation"
                        render={({ field }) => (
                          <TaxonomyPicker kind="occupations" inputId={id} describedBy={d} invalid={!!errors.occupation} placeholder={t('job.categoryPlaceholder')} value={field.value} onChange={field.onChange} />
                        )}
                      />
                    )}
                  </Field>
                  <Field label={t('job.openings')} required error={err('openings')}>
                    {(id, d) => <Input id={id} aria-describedby={d} aria-invalid={!!errors.openings} inputMode="numeric" {...form.register('openings')} />}
                  </Field>
                </div>
                <fieldset>
                  <legend className="mb-2 text-sm font-semibold">{t('job.jobType')}</legend>
                  <div className="flex flex-wrap gap-4">
                    {JOB_TYPES.map((type) => (
                      <label key={type} className="inline-flex items-center gap-2 text-sm">
                        <input type="radio" value={type} className="size-4 accent-brand-600" {...form.register('jobType')} />
                        {t(`job.type.${type}`)}
                      </label>
                    ))}
                  </div>
                </fieldset>
                <Field label={t('job.descriptionBn')} required error={err('descriptionBn')}>
                  {(id, d) => <Textarea id={id} aria-describedby={d} aria-invalid={!!errors.descriptionBn} placeholder={t('job.descriptionPlaceholder')} {...form.register('descriptionBn')} />}
                </Field>
                <Field label={t('job.descriptionEn')}>
                  {(id, d) => <Textarea id={id} aria-describedby={d} lang="en" {...form.register('descriptionEn')} />}
                </Field>
              </>
            )}

            {step === 1 && (
              <>
                <Alert tone="info">{t('job.wageHint')}</Alert>
                <div className="grid gap-4 md:grid-cols-3">
                  <Field label={t('job.wageMin')} required error={err('wageMin')}>
                    {(id, d) => <Input id={id} aria-describedby={d} aria-invalid={!!errors.wageMin} inputMode="numeric" placeholder="16000" {...form.register('wageMin')} />}
                  </Field>
                  <Field label={t('job.wageMax')} required error={err('wageMax')}>
                    {(id, d) => <Input id={id} aria-describedby={d} aria-invalid={!!errors.wageMax} inputMode="numeric" placeholder="20000" {...form.register('wageMax')} />}
                  </Field>
                  <Field label={t('job.wagePeriod')} required error={err('wagePeriod')}>
                    {(id, d) => (
                      <Select id={id} aria-describedby={d} {...form.register('wagePeriod')}>
                        {WAGE_PERIODS.map((period) => (
                          <option key={period} value={period}>
                            {t(`job.periodLabel.${period}`)}
                          </option>
                        ))}
                      </Select>
                    )}
                  </Field>
                </div>
                <div className="grid gap-4 md:grid-cols-3">
                  <Field label={t('job.shiftLabel')}>
                    {(id) => (
                      <Select id={id} {...form.register('shift')}>
                        {SHIFTS.map((shift) => (
                          <option key={shift} value={shift}>
                            {t(`job.shift.${shift}`)}
                          </option>
                        ))}
                      </Select>
                    )}
                  </Field>
                  <Field label={`${t('job.hours')}: ${t('job.startTime')}`} required error={err('startTime')}>
                    {(id, d) => <Input id={id} type="time" aria-describedby={d} aria-invalid={!!errors.startTime} {...form.register('startTime')} />}
                  </Field>
                  <Field label={t('job.endTime')} required>
                    {(id) => <Input id={id} type="time" {...form.register('endTime')} />}
                  </Field>
                </div>
              </>
            )}

            {step === 2 && (
              <>
                <label className="inline-flex items-center gap-2 text-sm font-medium">
                  <input type="checkbox" className="size-4 accent-brand-600" {...form.register('remote')} />
                  {t('job.remote')}
                </label>
                {!values.remote && (
                  <Field label={t('job.steps.location')} required error={err('latitude')}>
                    {() => (
                      <MapPicker
                        value={values.latitude != null && values.longitude != null ? { lat: values.latitude, lng: values.longitude } : null}
                        onChange={(point) => {
                          form.setValue('latitude', point?.lat ?? null, { shouldDirty: true })
                          form.setValue('longitude', point?.lng ?? null, { shouldDirty: true })
                          if (point) form.clearErrors('latitude')
                        }}
                      />
                    )}
                  </Field>
                )}
                <Field label={t('job.areaName')} optionalLabel={t('common.optional')}>
                  {(id) => <Input id={id} placeholder={t('job.areaPlaceholder')} {...form.register('areaName')} />}
                </Field>
              </>
            )}

            {step === 3 && (
              <>
                <Field label={t('job.skills')} optionalLabel={t('common.optional')} error={err('skills')}>
                  {(id, d) => (
                    <Controller
                      control={form.control}
                      name="skills"
                      render={({ field }) => (
                        <TaxonomyPicker multiple kind="skills" inputId={id} describedBy={d} placeholder={t('job.skillsPlaceholder')} value={field.value} onChange={field.onChange} />
                      )}
                    />
                  )}
                </Field>
                <fieldset>
                  <legend className="mb-2 text-sm font-semibold">
                    {t('job.documents')} <span className="font-normal text-slate-500">({t('common.optional')})</span>
                  </legend>
                  <div className="grid gap-2 sm:grid-cols-2">
                    {DRIVING_CODES.map((code) => (
                      <label key={code} className="inline-flex items-center gap-2 text-sm">
                        <input type="checkbox" value={code} className="size-4 accent-brand-600" {...form.register('requiredDocuments')} />
                        {t(`documentCode.${code}`)}
                      </label>
                    ))}
                  </div>
                </fieldset>
                <Field label={t('job.minExperience')} optionalLabel={t('common.optional')} error={err('minExperienceYears')}>
                  {(id, d) => <Input id={id} aria-describedby={d} inputMode="numeric" className="max-w-40" {...form.register('minExperienceYears')} />}
                </Field>
              </>
            )}

            {serverError && <Alert tone="error">{serverError}</Alert>}

            <div className="flex flex-wrap items-center gap-3 border-t border-slate-200 pt-5">
              {step > 0 && (
                <Button type="button" variant="ghost" icon={<ArrowLeft className="size-4" />} onClick={() => setStep((s) => s - 1)}>
                  {t('common.back')}
                </Button>
              )}
              {isDraft && (
                <Button type="button" variant="secondary" icon={<Save className="size-4" />} loading={busy && mode.current === 'draft'} disabled={busy} onClick={() => save(false)}>
                  {t('job.saveDraft')}
                </Button>
              )}
              <span className="flex-1" />
              {step < 3 ? (
                <Button type="button" onClick={next} icon={<ArrowRight className="size-4" />}>
                  {t('common.next')}
                </Button>
              ) : (
                <Button type="button" icon={<Send className="size-4" />} loading={busy && mode.current === 'submit'} disabled={busy} onClick={() => save(true)}>
                  {isDraft ? t('job.publish') : t('job.publishEdit')}
                </Button>
              )}
            </div>
          </form>

          <aside className="flex flex-col gap-4">
            <div className="rounded-2xl border border-brand-100 bg-brand-50 p-4">
              <p className="flex items-center gap-2 font-bold text-brand-700">
                <Info className="size-5" aria-hidden />
                {t('job.tipsTitle')}
              </p>
              <ul className="mt-3 flex flex-col gap-2 text-sm text-slate-700">
                {(t('job.tips', { returnObjects: true }) as string[]).map((tip) => (
                  <li key={tip} className="flex gap-2">
                    <CheckCircle2 className="mt-0.5 size-4 shrink-0 text-emerald-600" aria-hidden />
                    {tip}
                  </li>
                ))}
              </ul>
            </div>
            <div>
              <p className="mb-2 text-sm font-semibold text-slate-700">{t('job.preview')}</p>
              <JobCard
                job={{
                  titleBn: values.titleBn || values.titleEn,
                  titleEn: values.titleEn || values.titleBn,
                  occupation: values.occupation,
                  jobType: values.jobType,
                  shift: values.shift,
                  hours: values.startTime && values.endTime ? `${values.startTime}-${values.endTime}` : null,
                  openings: openings && !Number.isNaN(openings) ? openings : 1,
                  wageMin: parseWhole(values.wageMin) || null,
                  wageMax: parseWhole(values.wageMax) || null,
                  wagePeriod: values.wagePeriod || null,
                  areaName: values.areaName || null,
                  remote: values.remote,
                  employerName,
                  employerVerified: verified,
                }}
              />
            </div>
          </aside>
        </div>
      </Card>
    </div>
  )
}
