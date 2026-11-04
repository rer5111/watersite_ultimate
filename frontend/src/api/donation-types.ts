import { queryOptions, useQuery } from '@tanstack/react-query';

export type DonationType = {
  id: number;
  code: string;
  name: string;
  description: string | null;
  priceRub: number;
};

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
}

function isDonationType(value: unknown): value is DonationType {
  return (
    isRecord(value) &&
    typeof value.id === 'number' &&
    typeof value.code === 'string' &&
    typeof value.name === 'string' &&
    (typeof value.description === 'string' || value.description === null) &&
    typeof value.priceRub === 'number' &&
    Number.isFinite(value.priceRub)
  );
}

function readDonationTypes(value: unknown): DonationType[] {
  if (!isRecord(value) || !Array.isArray(value.content) || !value.content.every(isDonationType)) {
    throw new Error('API вернул некорректный каталог пожертвований');
  }

  return value.content;
}

export async function getDonationTypes(): Promise<DonationType[]> {
  const base = (import.meta.env.VITE_API_URL ?? '').replace(/\/+$/, '');
  const response = await fetch(`${base}/api/donation-service/api/v1/donation-types`);

  if (!response.ok) {
    throw new Error(`Не удалось загрузить варианты пожертвования (${response.status})`);
  }

  const payload: unknown = await response.json();
  return readDonationTypes(payload);
}

export const donationTypesQueryOptions = queryOptions({
  queryKey: ['donation-types'] as const,
  queryFn: getDonationTypes,
  staleTime: 1000 * 60 * 5,
});

export function useDonationTypes() {
  return useQuery(donationTypesQueryOptions);
}
