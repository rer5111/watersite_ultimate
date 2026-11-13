import { queryOptions, useQuery } from '@tanstack/react-query';
import { parseApiError } from './error-helper';

export type ServerSeason = {
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

export type ServerEvent = ServerSeason;

export type SeasonPhase = 'UPCOMING' | 'ACTIVE' | 'COMPLETED' | 'UNKNOWN';

export type SeasonEntry = {
  id: number;
  title: string;
  startDate: string | null;
  endDate: string | null;
  description: string;
  image: string | null;
  phase: SeasonPhase;
};

export type SeasonParticipation = {
  playerId: number;
  totalSeasons: number;
  seasons: SeasonEntry[];
};

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
}

function isServerSeason(value: unknown): value is ServerSeason {
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

function readSeasons(value: unknown): ServerSeason[] {
  if (!isRecord(value) || !Array.isArray(value.content) || !value.content.every(isServerSeason)) {
    throw new Error('API вернул некорректный список сезонов');
  }

  return value.content;
}

export async function getSeasons(): Promise<ServerSeason[]> {
  const base = (import.meta.env.VITE_API_URL ?? '').replace(/\/+$/, '');
  const response = await fetch(`${base}/api/season-service/api/v1/seasons`);

  if (!response.ok) {
    const message = await parseApiError(response, 'Не удалось загрузить сезоны сервера');
    throw new Error(message);
  }

  const payload: unknown = await response.json();
  return readSeasons(payload);
}

export const seasonsQueryOptions = queryOptions({
  queryKey: ['seasons'] as const,
  queryFn: getSeasons,
  staleTime: 1000 * 60 * 10,
  gcTime: 1000 * 60 * 60,
});

export function useSeasons() {
  return useQuery(seasonsQueryOptions);
}

function isNullableString(value: unknown): value is string | null {
  return value === null || typeof value === 'string';
}

function isSeasonPhase(value: unknown): value is SeasonPhase {
  return value === 'UPCOMING' || value === 'ACTIVE' || value === 'COMPLETED' || value === 'UNKNOWN';
}

function isSeasonEntry(value: unknown): value is SeasonEntry {
  return (
    isRecord(value) &&
    typeof value.id === 'number' &&
    typeof value.title === 'string' &&
    isNullableString(value.startDate) &&
    isNullableString(value.endDate) &&
    typeof value.description === 'string' &&
    isNullableString(value.image) &&
    isSeasonPhase(value.phase)
  );
}

function readParticipation(value: unknown): SeasonParticipation {
  if (
    !isRecord(value) ||
    typeof value.playerId !== 'number' ||
    typeof value.totalSeasons !== 'number' ||
    !Array.isArray(value.seasons) ||
    !value.seasons.every(isSeasonEntry)
  ) {
    throw new Error('API вернул некорректную историю сезонов');
  }

  return {
    playerId: value.playerId,
    totalSeasons: value.totalSeasons,
    seasons: value.seasons,
  };
}

export async function getMySeasonParticipation(): Promise<SeasonParticipation> {
  const base = (import.meta.env.VITE_API_URL ?? '').replace(/\/+$/, '');
  const token = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null;
  const headers: HeadersInit = token ? { Authorization: `Bearer ${token}` } : {};
  const response = await fetch(`${base}/api/player-service/api/season/me`, { headers });

  if (!response.ok) {
    const message = await parseApiError(response, 'Не удалось загрузить историю сезонов');
    throw new Error(message);
  }

  return readParticipation((await response.json()) as unknown);
}

export const mySeasonParticipationQueryOptions = (enabled: boolean = true, playerId?: number) =>
  queryOptions({
    queryKey: ['my-season-participation', playerId] as const,
    queryFn: getMySeasonParticipation,
    enabled: enabled && typeof playerId === 'number',
    staleTime: 1000 * 60 * 5,
  });

export function useMySeasonParticipation(enabled: boolean = true, playerId?: number) {
  return useQuery(mySeasonParticipationQueryOptions(enabled, playerId));
}
