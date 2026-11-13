import { queryOptions, useMutation, useQuery } from '@tanstack/react-query';
import { parseApiError } from './error-helper';

export type DonationStatus = 'PENDING' | 'SUCCEEDED' | 'CANCELED';

export type DonationCreateResponse = {
  donationId: string;
  actionUrl: string;
  receiver: string;
  quickpayForm: string;
  targets: string;
  paymentType: string;
  sum: number;
  label: string;
  successUrl: string;
};

export type DonationStatusResponse = {
  id: string;
  userId: number;
  payerUsername: string;
  amountRub: number;
  currency: string;
  status: DonationStatus;
  message: string | null;
  createdAt: string;
};

function getApiBaseUrl(): string {
  return (import.meta.env.VITE_API_URL ?? '').replace(/\/+$/, '');
}

function getAuthHeaders(includeJson: boolean = false): Record<string, string> {
  const token = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null;
  const headers: Record<string, string> = {};
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  if (includeJson) {
    headers['Content-Type'] = 'application/json';
  }
  return headers;
}

export async function createDonation(amount: number, message?: string): Promise<DonationCreateResponse> {
  const response = await fetch(`${getApiBaseUrl()}/api/payment-service/api/v1/payments/donations`, {
    method: 'POST',
    headers: getAuthHeaders(true),
    body: JSON.stringify({ amount, message }),
  });

  if (!response.ok) {
    const errorMsg = await parseApiError(response, 'Не удалось создать платеж');
    throw new Error(errorMsg);
  }

  return (await response.json()) as DonationCreateResponse;
}

export function submitYoomoneyForm(data: DonationCreateResponse): void {
  const form = document.createElement('form');
  form.method = 'POST';
  form.action = data.actionUrl;

  const fields: Record<string, string> = {
    receiver: data.receiver,
    'quickpay-form': data.quickpayForm,
    targets: data.targets,
    paymentType: data.paymentType,
    sum: data.sum.toFixed(2),
    label: data.label,
    successURL: data.successUrl,
  };

  for (const [key, value] of Object.entries(fields)) {
    const input = document.createElement('input');
    input.type = 'hidden';
    input.name = key;
    input.value = value;
    form.appendChild(input);
  }

  document.body.appendChild(form);
  form.submit();
}

export async function getDonationStatus(donationId: string): Promise<DonationStatusResponse> {
  const response = await fetch(
    `${getApiBaseUrl()}/api/payment-service/api/v1/payments/donations/${donationId}`,
    {
      headers: getAuthHeaders(false),
    }
  );

  if (!response.ok) {
    const errorMsg = await parseApiError(response, 'Не удалось получить статус пожертвования');
    throw new Error(errorMsg);
  }

  return (await response.json()) as DonationStatusResponse;
}

export const donationStatusQueryOptions = (donationId: string | null) =>
  queryOptions({
    queryKey: ['donations', donationId] as const,
    queryFn: () => getDonationStatus(donationId!),
    enabled: Boolean(donationId),
    refetchInterval: (query) => {
      const status = query.state.data?.status;
      if (status === 'SUCCEEDED' || status === 'CANCELED') {
        return false;
      }
      return 2000;
    },
  });

export function useDonationStatus(donationId: string | null) {
  return useQuery(donationStatusQueryOptions(donationId));
}

export function useCreateDonation() {
  return useMutation({
    mutationFn: ({ amount, message }: { amount: number; message?: string }) =>
      createDonation(amount, message),
  });
}
