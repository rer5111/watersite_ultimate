import { queryOptions, useQuery } from '@tanstack/react-query';
import { parseApiError } from './error-helper';

export type ServerEvent = {
  id: number;
  code: string;
  title: string;
  description: string;
  startDate: string;
  endDate: string;
  image: string;
  displayOrder: number;
  active: boolean;
};

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
}

function isServerEvent(value: unknown): value is ServerEvent {
  return (
    isRecord(value) &&
    typeof value.id === 'number' &&
    typeof value.code === 'string' &&
    typeof value.title === 'string' &&
    typeof value.description === 'string' &&
    typeof value.startDate === 'string' &&
    typeof value.endDate === 'string' &&
    typeof value.image === 'string' &&
    typeof value.displayOrder === 'number' &&
    typeof value.active === 'boolean'
  );
}

function readEvents(value: unknown): ServerEvent[] {
  if (!isRecord(value) || !Array.isArray(value.content) || !value.content.every(isServerEvent)) {
    throw new Error('API вернул некорректный список событий');
  }

  return value.content;
}

export async function getEvents(): Promise<ServerEvent[]> {
  const base = (import.meta.env.VITE_API_URL ?? '').replace(/\/+$/, '');
  const response = await fetch(`${base}/api/event-service/api/v1/events`);

  if (!response.ok) {
    const message = await parseApiError(response, 'Не удалось загрузить события сервера');
    throw new Error(message);
  }

  const payload: unknown = await response.json();
  return readEvents(payload);
}

export const eventsQueryOptions = queryOptions({
  queryKey: ['events'] as const,
  queryFn: getEvents,
  staleTime: 1000 * 60 * 10,
  gcTime: 1000 * 60 * 60,
});

export function useEvents() {
  return useQuery(eventsQueryOptions);
}
