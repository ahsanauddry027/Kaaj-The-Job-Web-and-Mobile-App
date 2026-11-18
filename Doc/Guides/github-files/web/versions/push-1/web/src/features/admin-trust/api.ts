import { useInfiniteQuery, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '../../lib/api'
import type { AdminEmployer, CursorPage, VerificationStatus } from '../../lib/types'

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
