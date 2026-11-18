import { Check, Search, X } from 'lucide-react'
import { useEffect, useId, useState } from 'react'
import { useTaxonomySearch } from '../features/taxonomy/api'
import { cn } from '../lib/cn'
import { termName } from '../lib/format'
import type { Term } from '../lib/types'
import { useLang } from '../lib/useLang'

interface Props {
  kind: 'occupations' | 'skills'
  placeholder: string
  inputId?: string
  describedBy?: string
  invalid?: boolean
}

interface SingleProps extends Props {
  multiple?: false
  value: Term | null
  onChange: (term: Term | null) => void
}

interface MultiProps extends Props {
  multiple: true
  value: Term[]
  onChange: (terms: Term[]) => void
}

function useDebounced(text: string, ms = 250) {
  const [value, setValue] = useState(text)
  useEffect(() => {
    const timer = setTimeout(() => setValue(text), ms)
    return () => clearTimeout(timer)
  }, [text, ms])
  return value
}

/**
 * Picks occupations or skills from the shared taxonomy, searching Bangla, Banglish and English names with
 * spelling tolerance on the server ({@code GET /api/v1/taxonomy/...?q=}).
 */
export function TaxonomyPicker(props: SingleProps | MultiProps) {
  const lang = useLang()
  const listId = useId()
  const [text, setText] = useState('')
  const [open, setOpen] = useState(false)
  const query = useDebounced(text)
  const { data = [] } = useTaxonomySearch(props.kind, query, open)

  const selected: Term[] = props.multiple ? props.value : props.value ? [props.value] : []
  const isSelected = (term: Term) => selected.some((s) => s.id === term.id)

  function choose(term: Term) {
    if (props.multiple) {
      props.onChange(isSelected(term) ? props.value.filter((s) => s.id !== term.id) : [...props.value, term])
      setText('')
    } else {
      props.onChange(term)
      setText('')
      setOpen(false)
    }
  }

  function remove(term: Term) {
    if (props.multiple) props.onChange(props.value.filter((s) => s.id !== term.id))
    else props.onChange(null)
  }

  return (
    <div className="relative" onBlur={(e) => !e.currentTarget.contains(e.relatedTarget) && setOpen(false)}>
      {selected.length > 0 && (
        <div className="mb-2 flex flex-wrap gap-2">
          {selected.map((term) => (
            <span key={term.id} className="inline-flex items-center gap-1 rounded-full bg-brand-50 px-3 py-1 text-sm text-brand-700 ring-1 ring-brand-200">
              {termName(term, lang)}
              <button type="button" onClick={() => remove(term)} className="rounded-full p-0.5 hover:bg-brand-100" aria-label={`× ${termName(term, lang)}`}>
                <X className="size-3.5" aria-hidden />
              </button>
            </span>
          ))}
        </div>
      )}
      <div className="relative">
        <Search className="pointer-events-none absolute top-1/2 left-3 size-4 -translate-y-1/2 text-slate-400" aria-hidden />
        <input
          id={props.inputId}
          role="combobox"
          aria-expanded={open}
          aria-controls={listId}
          aria-describedby={props.describedBy}
          aria-invalid={props.invalid || undefined}
          className={cn(
            'w-full rounded-xl border border-slate-300 bg-white py-2.5 pr-3.5 pl-9 text-sm placeholder:text-slate-400',
            'focus:border-brand-600 focus:ring-2 focus:ring-brand-100 focus:outline-none aria-[invalid=true]:border-red-500',
          )}
          placeholder={props.placeholder}
          value={text}
          onFocus={() => setOpen(true)}
          onChange={(e) => {
            setText(e.target.value)
            setOpen(true)
          }}
          onKeyDown={(e) => {
            if (e.key === 'Enter' && data[0]) {
              e.preventDefault()
              choose(data[0])
            }
            if (e.key === 'Escape') setOpen(false)
          }}
        />
      </div>
      {open && data.length > 0 && (
        <ul id={listId} role="listbox" className="absolute z-30 mt-1 max-h-64 w-full overflow-auto rounded-xl border border-slate-200 bg-white py-1 shadow-lg">
          {data.map((term) => (
            <li key={term.id} role="option" aria-selected={isSelected(term)}>
              <button
                type="button"
                className="flex w-full items-center justify-between gap-2 px-3.5 py-2.5 text-left text-sm hover:bg-brand-50"
                onMouseDown={(e) => e.preventDefault()}
                onClick={() => choose(term)}
              >
                <span>
                  {termName(term, lang)}
                  <span className="ml-2 text-xs text-slate-500">{lang === 'bn' ? term.nameEn : term.nameBn}</span>
                </span>
                {isSelected(term) && <Check className="size-4 text-brand-600" aria-hidden />}
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
