import type { AdminRoleRequest, AdminRequestedRole } from '@/types';
import { parseApiError } from './error-helper';

export interface CreateRoleRequestPayload {
  requestedRole: AdminRequestedRole;
  note: string;
}

export async function createRoleRequest(
  payload: CreateRoleRequestPayload
): Promise<AdminRoleRequest> {
  const baseUrl = (import.meta.env.VITE_API_URL ?? '').replace(/\/+$/, '');
  const token = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null;
  const response = await fetch(`${baseUrl}/api/player-service/api/user/role-requests`, {
    method: 'POST',
    headers: {
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    const fallback =
      response.status === 409
        ? 'У вас уже есть заявка на рассмотрении.'
        : 'Не удалось отправить заявку';
    const message = await parseApiError(response, fallback);
    throw new Error(message);
  }

  return (await response.json()) as AdminRoleRequest;
}
