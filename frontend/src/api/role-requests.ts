import type { AdminRoleRequest, AdminRequestedRole } from '@/types';

export interface CreateRoleRequestPayload {
  requestedRole: AdminRequestedRole;
  note: string;
}

export async function createRoleRequest(payload: CreateRoleRequestPayload): Promise<AdminRoleRequest> {
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
    let message = response.status === 409
      ? 'У вас уже есть заявка на рассмотрении.'
      : `Не удалось отправить заявку (${response.status}).`;
    try {
      const problem = (await response.json()) as { message?: string; detail?: string };
      message = problem.message ?? problem.detail ?? message;
    } catch {
      // Use the status specific message when the response has no JSON body.
    }
    throw new Error(message);
  }

  return (await response.json()) as AdminRoleRequest;
}
