import { queryOptions, useQuery } from '@tanstack/react-query';
import type {
  AdminEvent,
  AdminEventStatus,
  AdminRoleRequest,
  AdminUser,
  BroadcastDispatch,
  BroadcastPayload,
  UpdateAdminEventPayload,
  UpdateAdminUserPayload,
} from '@/types';
import { parseApiError } from './error-helper';

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

async function apiRequest<T>(path: string, init: RequestInit = {}): Promise<T> {
  const response = await fetch(`${getApiBaseUrl()}${path}`, {
    ...init,
    headers: getAuthHeaders(init.body !== undefined && init.body !== null),
  });

  if (!response.ok) {
    const message = await parseApiError(response);
    throw new Error(message);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}

interface BackendEvent {
  id: number;
  code: string;
  title: string;
  description: string;
  startDate: string;
  endDate: string;
  image: string;
  displayOrder: number;
  startsAt: string | null;
  active: boolean;
  organizer: string;
  status: AdminEventStatus;
  version: number;
}

function mapEvent(event: BackendEvent): AdminEvent {
  return {
    id: event.id,
    code: event.code,
    title: event.title,
    description: event.description,
    startsAt: event.startsAt ?? event.startDate,
    organizer: event.organizer ?? '',
    status: event.status ?? (event.active ? 'PUBLISHED' : 'DRAFT'),
  };
}

function eventUpdateBody(event: BackendEvent, payload: UpdateAdminEventPayload): string {
  return JSON.stringify({
    code: event.code,
    title: payload.title,
    description: payload.description,
    startDate: event.startDate,
    endDate: event.endDate,
    image: event.image,
    displayOrder: event.displayOrder,
    ...(/^\d{4}-\d{2}-\d{2}T/.test(payload.startsAt) ? { startsAt: payload.startsAt } : {}),
    organizer: payload.organizer,
    status: payload.status,
    active: payload.status === 'PUBLISHED',
    version: event.version,
  });
}

async function requestBackendEvent(id: number): Promise<BackendEvent> {
  return apiRequest<BackendEvent>(`/api/event-service/api/v1/admin/events/${id}`);
}

export const adminQueryKeys = {
  users: ['admin', 'users'] as const,
  events: ['admin', 'events'] as const,
  requests: ['admin', 'requests'] as const,
  broadcasts: ['admin', 'broadcasts'] as const,
};

export async function getAdminUsers(): Promise<AdminUser[]> {
  return apiRequest<AdminUser[]>('/api/player-service/api/user/admin/users');
}

export async function updateAdminUser(
  id: number,
  payload: UpdateAdminUserPayload
): Promise<AdminUser> {
  return apiRequest<AdminUser>(`/api/player-service/api/user/admin/users/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  });
}

export async function deleteAdminUser(id: number): Promise<void> {
  await apiRequest<void>(`/api/player-service/api/user/${id}`, { method: 'DELETE' });
}

export async function getAdminEvents(): Promise<AdminEvent[]> {
  // shortcut: the admin table loads at most 100 events; add pagination when the catalog grows.
  const page = await apiRequest<{ content: BackendEvent[] }>(
    '/api/event-service/api/v1/admin/events?size=100'
  );
  return page.content.map(mapEvent);
}

export async function updateAdminEvent(
  id: number,
  payload: UpdateAdminEventPayload
): Promise<AdminEvent> {
  const current = await requestBackendEvent(id);
  const updated = await apiRequest<BackendEvent>(`/api/event-service/api/v1/admin/events/${id}`, {
    method: 'PUT',
    body: eventUpdateBody(current, payload),
  });
  return mapEvent(updated);
}

export async function updateAdminEventStatus(
  id: number,
  status: AdminEventStatus
): Promise<AdminEvent> {
  const current = await requestBackendEvent(id);
  const updated = await apiRequest<BackendEvent>(`/api/event-service/api/v1/admin/events/${id}`, {
    method: 'PUT',
    body: eventUpdateBody(current, {
      title: current.title,
      description: current.description,
      startsAt: current.startDate,
      organizer: current.organizer,
      status,
    }),
  });
  return mapEvent(updated);
}

export async function deleteAdminEvent(id: number): Promise<void> {
  await apiRequest<void>(`/api/event-service/api/v1/admin/events/${id}`, { method: 'DELETE' });
}

export async function getRoleRequests(): Promise<AdminRoleRequest[]> {
  return apiRequest<AdminRoleRequest[]>('/api/player-service/api/user/admin/role-requests');
}

export async function approveRoleRequest(id: number): Promise<AdminRoleRequest> {
  return apiRequest<AdminRoleRequest>(
    `/api/player-service/api/user/admin/role-requests/${id}/approve`,
    { method: 'POST' }
  );
}

export async function rejectRoleRequest(id: number, reason: string): Promise<AdminRoleRequest> {
  return apiRequest<AdminRoleRequest>(
    `/api/player-service/api/user/admin/role-requests/${id}/reject`,
    { method: 'POST', body: JSON.stringify({ reason }) }
  );
}

export async function getBroadcastHistory(): Promise<BroadcastDispatch[]> {
  return apiRequest<BroadcastDispatch[]>('/api/notification-service/api/broadcasts');
}

export async function dispatchBroadcast(payload: BroadcastPayload): Promise<BroadcastDispatch> {
  const users = await getAdminUsers();
  // shortcut: organizers are not a stored role yet, count staff organizers; add an organizer role to make this exact.
  const recipients = users.filter((user) => {
    if (payload.audience === 'ALL') return true;
    if (payload.audience === 'ADMINS') return user.role === 'ADMIN';
    if (payload.audience === 'MODERATORS') return user.role === 'MODERATOR';
    return user.role === 'MODERATOR' || user.role === 'ADMIN';
  }).length;

  return apiRequest<BroadcastDispatch>('/api/notification-service/api/broadcasts', {
    method: 'POST',
    body: JSON.stringify({ ...payload, recipients }),
  });
}

export const adminUsersQueryOptions = queryOptions({
  queryKey: adminQueryKeys.users,
  queryFn: getAdminUsers,
});

export const adminEventsQueryOptions = queryOptions({
  queryKey: adminQueryKeys.events,
  queryFn: getAdminEvents,
});

export const roleRequestsQueryOptions = queryOptions({
  queryKey: adminQueryKeys.requests,
  queryFn: getRoleRequests,
});

export const broadcastHistoryQueryOptions = queryOptions({
  queryKey: adminQueryKeys.broadcasts,
  queryFn: getBroadcastHistory,
});

export function useAdminUsers() {
  return useQuery(adminUsersQueryOptions);
}

export function useAdminEvents() {
  return useQuery(adminEventsQueryOptions);
}

export function useRoleRequests() {
  return useQuery(roleRequestsQueryOptions);
}

export function useBroadcastHistory() {
  return useQuery(broadcastHistoryQueryOptions);
}
