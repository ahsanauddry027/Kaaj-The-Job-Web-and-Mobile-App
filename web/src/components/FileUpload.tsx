import { Camera, CheckCircle2, CloudUpload, FileText } from 'lucide-react'
import { useRef, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Button } from './ui'

/** The shared upload component (shared setup S7): checks type and size before uploading. */
export interface FileUploadProps {
  title: string
  hint: string
  accept?: string[]
  maxMb?: number
  uploadedName?: string | null
  disabled?: boolean
  busy?: boolean
  onFile: (file: File) => void
}

export const DOCUMENT_TYPES = ['application/pdf', 'image/jpeg', 'image/png']

/** Pure check, also used by tests: returns an i18n key, or null when the file is fine. */
export function checkFile(file: { type: string; size: number }, accept: string[], maxMb: number): string | null {
  if (!accept.includes(file.type)) return 'upload.wrongType'
  if (file.size > maxMb * 1024 * 1024) return 'upload.tooLarge'
  return null
}

export function FileUpload({ title, hint, accept = DOCUMENT_TYPES, maxMb = 3, uploadedName, disabled, busy, onFile }: FileUploadProps) {
  const { t } = useTranslation()
  const picker = useRef<HTMLInputElement>(null)
  const camera = useRef<HTMLInputElement>(null)
  const [error, setError] = useState<string | null>(null)

  function handle(files: FileList | null) {
    const file = files?.[0]
    if (!file) return
    const problem = checkFile(file, accept, maxMb)
    setError(problem ? t(problem, { mb: maxMb }) : null)
    if (!problem) onFile(file)
  }

  return (
    <div className="rounded-2xl border-2 border-dashed border-brand-200 bg-brand-50/40 p-5 text-center">
      <CloudUpload className="mx-auto size-8 text-brand-600" aria-hidden />
      <p className="mt-2 font-semibold text-slate-900">{title}</p>
      <p className="mt-1 text-xs text-slate-600">{hint}</p>
      <div className="mt-4 flex flex-wrap justify-center gap-2">
        <Button type="button" icon={<FileText className="size-4" />} loading={busy} disabled={disabled} onClick={() => picker.current?.click()}>
          {busy ? t('upload.uploading') : t('upload.choose')}
        </Button>
        <Button type="button" variant="secondary" icon={<Camera className="size-4" />} disabled={disabled || busy} onClick={() => camera.current?.click()}>
          {t('upload.camera')}
        </Button>
      </div>
      <input ref={picker} type="file" accept={accept.join(',')} className="sr-only" tabIndex={-1} onChange={(e) => handle(e.target.files)} />
      <input ref={camera} type="file" accept="image/*" capture="environment" className="sr-only" tabIndex={-1} onChange={(e) => handle(e.target.files)} />
      {uploadedName && !error && (
        <p className="mt-3 inline-flex items-center gap-1 text-sm text-emerald-700">
          <CheckCircle2 className="size-4" aria-hidden /> {t('upload.uploaded')}: {uploadedName}
        </p>
      )}
      {error && (
        <p role="alert" className="mt-3 text-sm font-medium text-red-600">
          {error}
        </p>
      )}
    </div>
  )
}
