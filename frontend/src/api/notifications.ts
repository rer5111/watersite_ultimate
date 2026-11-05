import { queryOptions, useQuery } from '@tanstack/react-query';
import type { UserNotification } from '@/types';

function getApiBaseUrl(): string {
  return (import.meta.env.VITE_API_URL ?? '').replace(/\/+$/, '');
}

function getAuthHeaders(): Record<string, string> {
  const token = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null;
  return token ? { Authorization: `Bearer ${token}` } : {};
}

async function apiRequest<T>(path: string, init: RequestInit = {}): Promise<T> {
  const response = await fetch(`${getApiBaseUrl()}${path}`, {
    ...init,
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    let message = `Ошибка запроса (${response.status})`;
    try {
      const problem = (await response.json()) as { message?: string; detail?: string };
      if (problem.message) message = problem.message;
      else if (problem.detail) message = problem.detail;
    } catch {
      // keep status-based message
    }
    throw new Error(message);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}

export const notificationQueryKeys = {
  all: ['notifications'] as const,
  unreadCount: ['notifications', 'unread-count'] as const,
};

export async function getNotifications(): Promise<UserNotification[]> {
  return apiRequest<UserNotification[]>('/api/notification-service/api/notifications');
}

export async function getUnreadNotificationCount(): Promise<number> {
  const response = await apiRequest<{ count: number }>(
    '/api/notification-service/api/notifications/unread-count'
  );
  return response.count;
}

export async function markNotificationRead(id: number): Promise<void> {
  await apiRequest<void>(`/api/notification-service/api/notifications/${id}/read`, {
    method: 'POST',
  });
}

export async function markAllNotificationsRead(): Promise<void> {
  await apiRequest<void>('/api/notification-service/api/notifications/read-all', {
    method: 'POST',
  });
}

export const notificationsQueryOptions = (enabled: boolean = true) =>
  queryOptions({
    queryKey: notificationQueryKeys.all,
    queryFn: getNotifications,
    enabled,
  });

export const unreadNotificationsQueryOptions = (enabled: boolean = true) =>
  queryOptions({
    queryKey: notificationQueryKeys.unreadCount,
    queryFn: getUnreadNotificationCount,
    enabled,
    refetchInterval: 60_000,
  });

export function useNotifications(enabled: boolean = true) {
  return useQuery(notificationsQueryOptions(enabled));
}

export function useUnreadNotificationCount(enabled: boolean = true) {
  return useQuery(unreadNotificationsQueryOptions(enabled));
}
