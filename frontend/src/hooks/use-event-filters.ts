import type { EventFilters, EventStatus } from '@/api';
import { useCallback } from 'react';
import { useSearchParams } from 'react-router';

const EVENT_STATUSES: readonly EventStatus[] = ['PLANNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'];

function isEventStatus(value: string): value is EventStatus {
  return (EVENT_STATUSES as readonly string[]).includes(value);
}

export function useEventFilterParams() {
  const [searchParams, setSearchParams] = useSearchParams();

  const seasons = searchParams
    .getAll('season')
    .map(Number)
    .filter((n) => !Number.isNaN(n) && n > 0);
  const organizers = searchParams.getAll('organizer').filter(Boolean);
  const statuses = searchParams
    .getAll('status')
    .map((status) => status.toUpperCase())
    .filter(isEventStatus);
  const minDate = searchParams.get('minDate') ?? undefined;
  const maxDate = searchParams.get('maxDate') ?? undefined;

  const setFilters = useCallback(
    (filters: Partial<EventFilters>) => {
      setSearchParams((params) => {
        if (filters.seasons !== undefined) {
          params.delete('season');
          filters.seasons.forEach((param) => params.append('season', String(param)));
        }

        if (filters.organizers !== undefined) {
          params.delete('organizer');
          filters.organizers.forEach((param) => params.append('organizer', param));
        }

        if (filters.statuses !== undefined) {
          params.delete('status');
          filters.statuses.forEach((param) => params.append('status', param.toLowerCase()));
        }

        if ('minDate' in filters) {
          params.delete('minDate');
          if (filters.minDate) params.set('minDate', filters.minDate);
        }

        if ('maxDate' in filters) {
          params.delete('maxDate');
          if (filters.maxDate) params.set('maxDate', filters.maxDate);
        }

        return params;
      });
    },
    [setSearchParams]
  );

  return { seasons, organizers, statuses, minDate, maxDate, setFilters };
}
