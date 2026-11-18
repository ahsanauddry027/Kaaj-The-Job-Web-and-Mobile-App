import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api, ApiError } from '../../lib/api'
import type { Me, Role } from '../../lib/types'

/** The signed-in user, or null for a guest (GET /api/v1/me answers 401). */
export function useCurrentUser() {
  return useQuery({
    queryKey: ['me'],
    queryFn: async () => {
      try {
        return await api.get<Me>('/me')
      } catch (error) {
        if (error instanceof ApiError && error.status === 401) return null
        throw error
      }
    },
    staleTime: 60_000,
  })
}

export function homeFor(role: Role | undefined): string {
  if (role === 'EMPLOYER') return '/employer'
  if (role === 'ADMIN') return '/admin/verification'
  if (role === 'SEEKER') return '/seeker'
  return '/'
}

export function useLogin() {
  const client = useQueryClient()
  return useMutation({
    mutationFn: (body: { email: string; password: string }) => api.post<Me>('/auth/login', body),
    onSuccess: (me) => {
      client.clear()
      client.setQueryData(['me'], me)
    },
  })
}

export function useRegister() {
  const client = useQueryClient()
  return useMutation({
    mutationFn: (body: { email: string; password: string; role: Role }) => api.post<Me>('/auth/register', body),
    onSuccess: (me) => {
      client.clear()
      client.setQueryData(['me'], me)
    },
  })
}

export function useLogout() {
  const client = useQueryClient()
  return useMutation({
    mutationFn: () => api.post<void>('/auth/logout'),
    onSettled: () => {
      client.clear()
      client.setQueryData(['me'], null)
    },
  })
}
