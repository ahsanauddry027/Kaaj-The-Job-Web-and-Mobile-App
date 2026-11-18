import i18n from '../i18n'

/** An error answer from the API (RFC 9457 ProblemDetail), with any form errors by field. */
export class ApiError extends Error {
  readonly status: number
  readonly code: string | undefined
  readonly fieldErrors: Record<string, string>

  constructor(status: number, body: Record<string, unknown> | undefined, fallback: string) {
    super(typeof body?.detail === 'string' ? body.detail : fallback)
    this.status = status
    this.code = typeof body?.code === 'string' ? body.code : undefined
    this.fieldErrors = (body?.errors as Record<string, string> | undefined) ?? {}
  }
}

function readCookie(name: string): string | null {
  const match = document.cookie.split('; ').find((part) => part.startsWith(name + '='))
  return match ? decodeURIComponent(match.slice(name.length + 1)) : null
}

/** Spring Security puts the CSRF token in the XSRF-TOKEN cookie; we send it back in a header. */
async function csrfToken(refresh = false): Promise<string | null> {
  if (refresh || !readCookie('XSRF-TOKEN')) {
    await fetch('/api/v1/auth/csrf', { credentials: 'same-origin' })
  }
  return readCookie('XSRF-TOKEN')
}

interface RequestOptions {
  body?: unknown
  form?: FormData
  headers?: Record<string, string>
}

async function request<T>(method: string, path: string, options: RequestOptions = {}, retried = false): Promise<T> {
  const headers: Record<string, string> = {
    Accept: 'application/json',
    'Accept-Language': i18n.language,
    ...options.headers,
  }
  if (method !== 'GET') {
    const token = await csrfToken()
    if (token) headers['X-XSRF-TOKEN'] = token
  }
  let body: BodyInit | undefined
  if (options.form) {
    body = options.form // the browser sets the multipart boundary itself
  } else if (options.body !== undefined) {
    headers['Content-Type'] = 'application/json'
    body = JSON.stringify(options.body)
  }

  let response: Response
  try {
    response = await fetch('/api/v1' + path, { method, headers, body, credentials: 'same-origin' })
  } catch {
    throw new ApiError(0, undefined, i18n.t('common.networkError'))
  }

  // A 403 on a change can be an expired CSRF token (for example after signing out): refresh it once.
  if (response.status === 403 && method !== 'GET' && !retried) {
    await csrfToken(true)
    return request<T>(method, path, options, true)
  }
  if (response.status === 204) return undefined as T

  const isJson = (response.headers.get('content-type') ?? '').includes('json')
  const data = isJson ? await response.json() : undefined
  if (!response.ok) {
    throw new ApiError(response.status, data, i18n.t('common.somethingWrong'))
  }
  return data as T
}

export const api = {
  get: <T>(path: string) => request<T>('GET', path),
  post: <T>(path: string, body?: unknown, headers?: Record<string, string>) => request<T>('POST', path, { body, headers }),
  put: <T>(path: string, body: unknown) => request<T>('PUT', path, { body }),
  patch: <T>(path: string, body: unknown) => request<T>('PATCH', path, { body }),
  delete: <T>(path: string) => request<T>('DELETE', path),
  upload: <T>(path: string, form: FormData) => request<T>('POST', path, { form }),
}

/** For GET requests where "not found" just means "nothing yet" (for example, no business profile). */
export async function getOrNull<T>(path: string): Promise<T | null> {
  try {
    return await api.get<T>(path)
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) return null
    throw error
  }
}
