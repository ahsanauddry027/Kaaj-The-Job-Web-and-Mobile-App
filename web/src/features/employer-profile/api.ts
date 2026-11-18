import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api, getOrNull } from '../../lib/api'
import type { DocumentType, EmployerProfile, EmployerProfileInput } from '../../lib/types'

/** The employer's own profile, or null before it exists (the API answers 404). */
export function useEmployerProfile() {
  return useQuery({ queryKey: ['employer-profile'], queryFn: () => getOrNull<EmployerProfile>('/employer/profile') })
}

function useRefreshAfter() {
  const client = useQueryClient()
  return (profile: EmployerProfile) => {
    client.setQueryData(['employer-profile'], profile)
    void client.invalidateQueries({ queryKey: ['jobs'] }) // the badge and drafts depend on verification
  }
}

export function useSaveProfile() {
  const refresh = useRefreshAfter()
  return useMutation({
    mutationFn: (input: EmployerProfileInput) => api.put<EmployerProfile>('/employer/profile', input),
    onSuccess: refresh,
  })
}

export function useUploadDocument() {
  const refresh = useRefreshAfter()
  return useMutation({
    mutationFn: ({ file, documentType }: { file: File; documentType: DocumentType }) => {
      const form = new FormData()
      form.append('file', file)
      form.append('documentType', documentType)
      return api.upload<EmployerProfile>('/employer/profile/document', form)
    },
    onSuccess: refresh,
  })
}

export function useResubmit() {
  const refresh = useRefreshAfter()
  return useMutation({ mutationFn: () => api.post<EmployerProfile>('/employer/profile/submit'), onSuccess: refresh })
}
