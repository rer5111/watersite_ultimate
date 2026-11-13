import { queryOptions, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { parseApiError } from './error-helper';

export type EventStatus = 'PLANNED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

export type EventSummary = {
  id: number;
  code: string;
  title: string;
  shortDescription: string;
  image: string;
  seasonNumber: number;
  seasonTitle: string;
  organizer: string;
  status: EventStatus;
  startDate: string | null;
  endDate: string | null;
  dateRangeFormatted: string;
  participantsCount: number;
  displayOrder: number;
  active: boolean;
};

export type EventParticipant = {
  id: number;
  userId: number | null;
  username: string;
  role: string;
  notes: string | null;
  createdAt: string;
};

export type EventDetail = EventSummary & {
  description: string;
  participants: EventParticipant[];
  version: number;
  createdAt: string;
  updatedAt: string;
};

export type EventFilters = {
  seasons: number[];
  organizers: string[];
  statuses: EventStatus[];
  minDate: string | null;
  maxDate: string | null;
};

export type EventQueryParams = {
  seasons?: number[];
  organizers?: string[];
  statuses?: EventStatus[];
  minDate?: string;
  maxDate?: string;
  search?: string;
  page?: number;
  size?: number;
};

type PageResponse<T> = {
  content: T[];
  page: {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
  };
};

function getApiBaseUrl(): string {
  return (import.meta.env.VITE_API_URL ?? '').replace(/\/+$/, '');
}

export async function getEvents(
  params: EventQueryParams = {}
): Promise<PageResponse<EventSummary>> {
  const base = getApiBaseUrl();
  const searchParams = new URLSearchParams();

  if (params.seasons && params.seasons.length > 0) {
    searchParams.set('seasons', params.seasons.join(','));
  }
  if (params.search && params.search.trim() !== '') {
    searchParams.set('search', params.search.trim());
  }
  if (params.organizers && params.organizers.length > 0) {
    searchParams.set('organizer', params.organizers.join(','));
  }
  if (params.statuses && params.statuses.length > 0) {
    searchParams.set('status', params.statuses.join(','));
  }
  if (params.minDate) {
    searchParams.set('dateFrom', params.minDate);
  }
  if (params.maxDate) {
    searchParams.set('dateTo', params.maxDate);
  }
  if (params.page !== undefined) {
    searchParams.set('page', String(params.page));
  }
  if (params.size !== undefined) {
    searchParams.set('size', String(params.size));
  }

  const queryStr = searchParams.toString();
  const url = `${base}/api/event-service/api/v1/events${queryStr ? `?${queryStr}` : ''}`;
  const response = await fetch(url);

  if (!response.ok) {
    const message = await parseApiError(response, 'Не удалось загрузить список ивентов');
    throw new Error(message);
  }

  return response.json();
}

export async function getEventDetail(idOrCode: string | number): Promise<EventDetail> {
  const base = getApiBaseUrl();
  const response = await fetch(`${base}/api/event-service/api/v1/events/${idOrCode}`);

  if (!response.ok) {
    const message = await parseApiError(response, 'Не удалось загрузить информацию об ивенте');
    throw new Error(message);
  }

  return response.json();
}

export async function getEventFilters(): Promise<EventFilters> {
  const base = getApiBaseUrl();
  const response = await fetch(`${base}/api/event-service/api/v1/events/filters`);

  if (!response.ok) {
    const message = await parseApiError(response, 'Не удалось загрузить фильтры ивентов');
    throw new Error(message);
  }

  return response.json();
}

export async function participateInEvent(eventId: number, notes?: string): Promise<EventDetail> {
  const base = getApiBaseUrl();
  const token = localStorage.getItem('access_token');
  const url = new URL(`${base}/api/event-service/api/v1/events/${eventId}/participate`);
  if (notes) {
    url.searchParams.set('notes', notes);
  }

  const response = await fetch(url.toString(), {
    method: 'POST',
    headers: {
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
  });

  if (!response.ok) {
    const message = await parseApiError(response, 'Не удалось записаться на ивент');
    throw new Error(message);
  }

  return response.json();
}

export function eventsQueryOptions(params: EventQueryParams = {}) {
  return queryOptions({
    queryKey: ['events', params] as const,
    queryFn: () => getEvents(params),
    staleTime: 1000 * 60 * 5,
  });
}

export function eventDetailQueryOptions(idOrCode: string | number | undefined) {
  return queryOptions({
    queryKey: ['event', idOrCode] as const,
    queryFn: () => getEventDetail(idOrCode!),
    enabled: Boolean(idOrCode),
    staleTime: 1000 * 60 * 5,
  });
}

export function eventFiltersQueryOptions() {
  return queryOptions({
    queryKey: ['event-filters'] as const,
    queryFn: getEventFilters,
    staleTime: 1000 * 60 * 10,
  });
}

export function useEvents(params: EventQueryParams = {}) {
  return useQuery(eventsQueryOptions(params));
}

export function useEvent(idOrCode: string | number | undefined) {
  return useQuery(eventDetailQueryOptions(idOrCode));
}

export function useEventFilters() {
  return useQuery(eventFiltersQueryOptions());
}

export function useParticipateInEvent() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ eventId, notes }: { eventId: number; notes?: string }) =>
      participateInEvent(eventId, notes),
    onSuccess: (updatedEvent) => {
      queryClient.setQueryData(['event', String(updatedEvent.id)], updatedEvent);
      queryClient.setQueryData(['event', updatedEvent.code], updatedEvent);
      queryClient.invalidateQueries({ queryKey: ['events'] });
    },
  });
}
