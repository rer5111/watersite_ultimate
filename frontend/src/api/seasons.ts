import { queryOptions, useQuery } from '@tanstack/react-query';

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
    throw new Error(`Не удалось загрузить историю сезонов (${response.status})`);
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
