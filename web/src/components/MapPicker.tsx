import L from 'leaflet'
import { LocateFixed, MapPin, X } from 'lucide-react'
import { useEffect, useRef } from 'react'
import { useTranslation } from 'react-i18next'
import { localDigits } from '../lib/format'
import { useLang } from '../lib/useLang'
import { Button } from './ui'

export interface Point {
  lat: number
  lng: number
}

// Tongi, Gazipur: where the demo employers are.
const DEFAULT_CENTER: Point = { lat: 23.8916, lng: 90.4023 }

interface MapPickerProps {
  value: Point | null
  onChange?: (point: Point | null) => void
  readOnly?: boolean
  height?: number
}

/**
 * A map to pick one point, using Leaflet with OpenStreetMap tiles (free; the attribution must be shown).
 * Leaflet is called directly from a small hook instead of react-leaflet (context file §12.3).
 */
export function MapPicker({ value, onChange, readOnly = false, height = 260 }: MapPickerProps) {
  const { t } = useTranslation()
  const lang = useLang()
  const container = useRef<HTMLDivElement>(null)
  const map = useRef<L.Map | null>(null)
  const marker = useRef<L.CircleMarker | null>(null)
  const onChangeRef = useRef(onChange)
  onChangeRef.current = onChange

  // Create the map once.
  useEffect(() => {
    if (!container.current) return
    const start = value ?? DEFAULT_CENTER
    const instance = L.map(container.current, { scrollWheelZoom: false }).setView([start.lat, start.lng], value ? 15 : 12)
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    }).addTo(instance)
    if (!readOnly) {
      instance.on('click', (event: L.LeafletMouseEvent) => {
        onChangeRef.current?.({ lat: round(event.latlng.lat), lng: round(event.latlng.lng) })
      })
    }
    map.current = instance
    return () => {
      instance.remove()
      map.current = null
      marker.current = null
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [readOnly])

  // Keep the pin in step with the value.
  useEffect(() => {
    const instance = map.current
    if (!instance) return
    if (!value) {
      marker.current?.remove()
      marker.current = null
      return
    }
    const latLng: L.LatLngExpression = [value.lat, value.lng]
    if (marker.current) {
      marker.current.setLatLng(latLng)
    } else {
      marker.current = L.circleMarker(latLng, { radius: 10, color: '#1d4ed8', weight: 3, fillColor: '#3b82f6', fillOpacity: 0.6 }).addTo(instance)
    }
  }, [value])

  function useMyLocation() {
    navigator.geolocation?.getCurrentPosition((position) => {
      const point = { lat: round(position.coords.latitude), lng: round(position.coords.longitude) }
      onChange?.(point)
      map.current?.setView([point.lat, point.lng], 15)
    })
  }

  return (
    <div className="flex flex-col gap-2">
      <div ref={container} style={{ height }} className="z-0 overflow-hidden rounded-xl border border-slate-300" aria-label={t('map.hint')} />
      {!readOnly && (
        <div className="flex flex-wrap items-center gap-2 text-xs text-slate-600">
          <MapPin className="size-4 text-brand-600" aria-hidden />
          <span className="flex-1">
            {value ? t('map.pinned', { lat: localDigits(value.lat, lang), lng: localDigits(value.lng, lang) }) : t('map.hint')}
          </span>
          {'geolocation' in navigator && (
            <Button type="button" variant="ghost" className="min-h-9 px-2 text-xs" icon={<LocateFixed className="size-4" />} onClick={useMyLocation}>
              {t('map.myLocation')}
            </Button>
          )}
          {value && (
            <Button type="button" variant="ghost" className="min-h-9 px-2 text-xs" icon={<X className="size-4" />} onClick={() => onChange?.(null)}>
              {t('map.clear')}
            </Button>
          )}
        </div>
      )}
    </div>
  )
}

/** About 10 m precision is plenty for a business or a job site. */
function round(n: number): number {
  return Math.round(n * 10_000) / 10_000
}
