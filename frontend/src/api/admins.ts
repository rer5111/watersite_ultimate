import { queryOptions, useQuery } from '@tanstack/react-query';
import type { AdminProfile } from '@/types';

function isAdminProfile(value: unknown): value is AdminProfile {
  if (typeof value !== 'object' || value === null || Array.isArray(value)) {
    return false;
  }
  const candidate = value as Record<string, unknown>;
  return (
    typeof candidate.username === 'string' &&
    (typeof candidate.uuid === 'string' || candidate.uuid === null)
  );
}

export async function getAdmins(): Promise<AdminProfile[]> {
  const base = (import.meta.env.VITE_API_URL ?? '').replace(/\/+$/, '');
  const response = await fetch(`${base}/api/player-service/api/user/admins`);

  if (!response.ok) {
    throw new Error(`Не удалось загрузить список администрации (${response.status})`);
  }

  const payload: unknown = await response.json();
  if (!Array.isArray(payload) || !payload.every(isAdminProfile)) {
    throw new Error('API вернул некорректный список администрации');
  }

  return payload;
}

export const DEFAULT_ADMINS: readonly AdminProfile[] = [
  { username: 'LeadCone_TNF', uuid: 'ccb40ba9-e0af-4939-9c80-3ce15fb417ed' },
  { username: 'rer_5111', uuid: 'ce03a881-7be7-4cbe-940c-25b254a7a2a9' },
  { username: 'Kotyarendj', uuid: '8a9861cd-de95-4de9-ba5d-15e5c533370b' },
  { username: 'Tabller', uuid: 'bcab9602-1050-4aad-af23-9b4f811d8e80' },
  { username: 'Cheshirius', uuid: null },
] as const;

export const adminsQueryOptions = queryOptions({
  queryKey: ['admins'] as const,
  queryFn: getAdmins,
  placeholderData: () => [...DEFAULT_ADMINS],
  staleTime: 1000 * 60 * 5,
});

export function useAdmins() {
  return useQuery(adminsQueryOptions);
}
