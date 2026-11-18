import { describe, expect, it } from 'vitest'
import { checkFile, DOCUMENT_TYPES } from './FileUpload'

describe('upload checks before sending', () => {
  it('allows PDF, JPG and PNG up to 3 MB', () => {
    expect(checkFile({ type: 'application/pdf', size: 1024 }, DOCUMENT_TYPES, 3)).toBeNull()
    expect(checkFile({ type: 'image/png', size: 3 * 1024 * 1024 }, DOCUMENT_TYPES, 3)).toBeNull()
  })

  it('refuses other types and big files', () => {
    expect(checkFile({ type: 'application/zip', size: 10 }, DOCUMENT_TYPES, 3)).toBe('upload.wrongType')
    expect(checkFile({ type: 'image/jpeg', size: 3 * 1024 * 1024 + 1 }, DOCUMENT_TYPES, 3)).toBe('upload.tooLarge')
  })
})
