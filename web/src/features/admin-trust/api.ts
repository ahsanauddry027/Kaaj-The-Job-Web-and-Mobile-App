import { useInfiniteQuery, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '../../lib/api'
import type { AdminEmployer, CaseDecision, CaseDetail, CaseStatus, CaseSummary, CursorPage, VerificationStatus } from '../../lib/types'

export type QueueStatus = Exclude<VerificationStatus, 'UNVERIFIED'>

export function useVerificationQueue(status: QueueStatus) {
  return useInfiniteQuery({
    queryKey: ['admin-employers', status],
    initialPageParam: null as string | null,
    queryFn: ({ pageParam }) =>
      api.get<CursorPage<AdminEmployer>>(`/admin/employers?status=${status}&size=20${pageParam ? `&cursor=${pageParam}` : ''}`),
    getNextPageParam: (last) => last.nextCursor,
  })
}

export function useVerificationCounts() {
  return useQuery({ queryKey: ['admin-employers', 'counts'], queryFn: () => api.get<Record<VerificationStatus, number>>('/admin/employers/counts') })
}

export function useDecision() {
  const client = useQueryClient()
  return useMutation({
    mutationFn: ({ id, decision, reason }: { id: string; decision: 'APPROVE' | 'REJECT'; reason: string | null }) =>
      api.post<AdminEmployer>(`/admin/employers/${id}/verification`, { decision, reason }),
    onSuccess: () => client.invalidateQueries({ queryKey: ['admin-employers'] }),
  })
}

// ---- Moderation (scam screening, Module 2)

export function useCases(status: CaseStatus) {
  return useInfiniteQuery({
    queryKey: ['admin-cases', status],
    initialPageParam: null as string | null,
    queryFn: ({ pageParam }) => api.get<CursorPage<CaseSummary>>(`/admin/cases?status=${status}${pageParam ? `&cursor=${pageParam}` : ''}`),
    getNextPageParam: (last) => last.nextCursor,
  })
}

export function useCaseCounts() {
  return useQuery({ queryKey: ['admin-cases', 'counts'], queryFn: () => api.get<Record<CaseStatus, number>>('/admin/cases/counts') })
}

export function useCase(id: string | null) {
  return useQuery({ queryKey: ['admin-cases', 'one', id], queryFn: () => api.get<CaseDetail>(`/admin/cases/${id}`), enabled: !!id })
}

export function useCaseDecision() {
  const client = useQueryClient()
  return useMutation({
    mutationFn: ({ id, decision, note }: { id: string; decision: CaseDecision; note: string | null }) =>
      api.post<CaseDetail>(`/admin/cases/${id}/decision`, { decision, note }),
    onSuccess: () => client.invalidateQueries({ queryKey: ['admin-cases'] }),
  })
}
