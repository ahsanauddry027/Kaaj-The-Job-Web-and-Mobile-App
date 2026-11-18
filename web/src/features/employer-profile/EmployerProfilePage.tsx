import { zodResolver } from '@hookform/resolvers/zod'
import { Briefcase, Mail, MapPin, Phone, Save, User } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { useTranslation } from 'react-i18next'
import { FileUpload } from '../../components/FileUpload'
import { MapPicker } from '../../components/MapPicker'
import { useToast } from '../../components/Toast'
import { Alert, Button, Card, Field, Input, Select, Spinner } from '../../components/ui'
import { ApiError } from '../../lib/api'
import { EMPLOYER_KINDS, type DocumentType } from '../../lib/types'
import { useEmployerProfile, useResubmit, useSaveProfile, useUploadDocument } from './api'
import { profileSchema, toFormValues, toInput, type ProfileFormValues } from './schema'
import { VerificationPanel } from './VerificationPanel'

/** Feature 3: the employer's business profile, the document upload and the verification status. */
export function EmployerProfilePage() {
  const { t } = useTranslation()
  const toast = useToast()
  const profileQuery = useEmployerProfile()
  const save = useSaveProfile()
  const upload = useUploadDocument()
  const resubmit = useResubmit()
  const [documentType, setDocumentType] = useState<DocumentType>('TRADE_LICENCE')
  const profile = profileQuery.data ?? null

  const form = useForm<ProfileFormValues>({ resolver: zodResolver(profileSchema), defaultValues: toFormValues(null) })
  const errors = form.formState.errors
  const message = (key?: string) => (key ? t(key) : undefined)
  const latitude = form.watch('latitude')
  const longitude = form.watch('longitude')

  // Fill the form once the saved profile arrives.
  useEffect(() => {
    if (profileQuery.isSuccess) form.reset(toFormValues(profileQuery.data))
  }, [profileQuery.isSuccess, profileQuery.data, form])

  const onSubmit = form.handleSubmit((values) =>
    save.mutate(toInput(values), {
      onSuccess: () => toast(t('profile.saved')),
      onError: (error) => {
        if (error instanceof ApiError) {
          for (const [field, text] of Object.entries(error.fieldErrors)) {
            form.setError(field as keyof ProfileFormValues, { message: text })
          }
        }
      },
    }),
  )

  if (profileQuery.isLoading) return <Spinner label={t('common.loading')} />
  if (profileQuery.error) return <Alert tone="error">{profileQuery.error.message}</Alert>

  return (
    <div className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_380px]">
      <Card>
        <div className="mb-6 flex items-start gap-3">
          <div className="grid size-12 shrink-0 place-items-center rounded-2xl bg-brand-600 text-white">
            <Briefcase className="size-6" aria-hidden />
          </div>
          <div>
            <h1 className="text-xl font-bold md:text-2xl">{t('profile.title')}</h1>
            <p className="mt-1 text-sm text-slate-600">{t('profile.subtitle')}</p>
          </div>
        </div>

        <form onSubmit={onSubmit} noValidate className="flex flex-col gap-5">
          <div className="grid gap-4 md:grid-cols-2">
            <Field label={t('profile.name')} required error={message(errors.name?.message)}>
              {(id, d) => <Input id={id} aria-describedby={d} aria-invalid={!!errors.name} placeholder={t('profile.namePlaceholder')} {...form.register('name')} />}
            </Field>
            <Field label={t('profile.kind')} required error={message(errors.kind?.message)}>
              {(id, d) => (
                <Select id={id} aria-describedby={d} {...form.register('kind')}>
                  {EMPLOYER_KINDS.map((kind) => (
                    <option key={kind} value={kind}>
                      {t(`kind.${kind}`)}
                    </option>
                  ))}
                </Select>
              )}
            </Field>
            <Field label={t('profile.address')} required error={message(errors.address?.message)}>
              {(id, d) => (
                <div className="relative">
                  <MapPin className="pointer-events-none absolute top-3 left-3 size-4 text-slate-400" aria-hidden />
                  <Input id={id} className="pl-9" aria-describedby={d} aria-invalid={!!errors.address} placeholder={t('profile.addressPlaceholder')} {...form.register('address')} />
                </div>
              )}
            </Field>
            <Field label={t('profile.contactPerson')} required error={message(errors.contactPerson?.message)}>
              {(id, d) => (
                <div className="relative">
                  <User className="pointer-events-none absolute top-3 left-3 size-4 text-slate-400" aria-hidden />
                  <Input id={id} className="pl-9" aria-describedby={d} aria-invalid={!!errors.contactPerson} {...form.register('contactPerson')} />
                </div>
              )}
            </Field>
            <Field label={t('profile.contactPhone')} required error={message(errors.contactPhone?.message)} hint={t('profile.contactPhoneHint')}>
              {(id, d) => (
                <div className="relative">
                  <Phone className="pointer-events-none absolute top-3 left-3 size-4 text-slate-400" aria-hidden />
                  <Input id={id} className="pl-9" type="tel" inputMode="tel" autoComplete="tel" aria-describedby={d} aria-invalid={!!errors.contactPhone} placeholder="01712-345678" {...form.register('contactPhone')} />
                </div>
              )}
            </Field>
            <Field label={t('profile.contactEmail')} optionalLabel={t('common.optional')} error={message(errors.contactEmail?.message)}>
              {(id, d) => (
                <div className="relative">
                  <Mail className="pointer-events-none absolute top-3 left-3 size-4 text-slate-400" aria-hidden />
                  <Input id={id} className="pl-9" type="email" aria-describedby={d} aria-invalid={!!errors.contactEmail} {...form.register('contactEmail')} />
                </div>
              )}
            </Field>
          </div>

          <Field label={t('profile.pin')} optionalLabel={t('common.optional')}>
            {() => (
              <MapPicker
                value={latitude != null && longitude != null ? { lat: latitude, lng: longitude } : null}
                onChange={(point) => {
                  form.setValue('latitude', point?.lat ?? null, { shouldDirty: true })
                  form.setValue('longitude', point?.lng ?? null, { shouldDirty: true })
                }}
              />
            )}
          </Field>

          <div className="flex justify-end">
            <Button type="submit" loading={save.isPending} icon={<Save className="size-4" />}>
              {t('profile.save')}
            </Button>
          </div>
          {save.error && !(save.error instanceof ApiError && Object.keys(save.error.fieldErrors).length) && <Alert tone="error">{save.error.message}</Alert>}
        </form>

        <div className="mt-6 flex flex-col gap-4 border-t border-slate-200 pt-6">
          <Field label={t('profile.documentType')}>
            {(id) => (
              <Select id={id} value={documentType} onChange={(e) => setDocumentType(e.target.value as DocumentType)} className="md:max-w-xs">
                <option value="TRADE_LICENCE">{t('docType.TRADE_LICENCE')}</option>
                <option value="NATIONAL_ID">{t('docType.NATIONAL_ID')}</option>
              </Select>
            )}
          </Field>
          <FileUpload
            title={t('profile.uploadTitle')}
            hint={t('profile.uploadHint')}
            uploadedName={profile?.documentName}
            disabled={!profile}
            busy={upload.isPending}
            onFile={(file) => upload.mutate({ file, documentType }, { onSuccess: () => toast(t('verification.banner.PENDING')) })}
          />
          {!profile && <Alert tone="warning">{t('profile.createFirst')}</Alert>}
          {upload.error && <Alert tone="error">{upload.error.message}</Alert>}
          <Alert tone="info">{t('profile.infoPublish')}</Alert>
        </div>
      </Card>

      <div className="lg:sticky lg:top-6 lg:self-start">
        <VerificationPanel profile={profile} onResubmit={() => resubmit.mutate()} resubmitting={resubmit.isPending} />
        {resubmit.error && (
          <div className="mt-3">
            <Alert tone="error">{resubmit.error.message}</Alert>
          </div>
        )}
      </div>
    </div>
  )
}
