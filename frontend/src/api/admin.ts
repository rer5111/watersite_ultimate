import { queryOptions, useQuery } from '@tanstack/react-query';
import type {
  AdminEvent,
  AdminRoleRequest,
  AdminSeason,
  AdminSeasonStatus,
  AdminUser,
  BroadcastDispatch,
  BroadcastPayload,
  CreateAdminEventPayload,
  CreateAdminSeasonPayload,
  UpdateAdminEventPayload,
  UpdateAdminSeasonPayload,
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

interface BackendSeason {
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
  status: AdminSeasonStatus;
  version: number;
}

function mapSeason(season: BackendSeason): AdminSeason {
  return {
    id: season.id,
    code: season.code,
    title: season.title,
    description: season.description,
    startsAt: season.startsAt ?? season.startDate,
    organizer: season.organizer ?? '',
    status: season.status ?? (season.active ? 'PUBLISHED' : 'DRAFT'),
  };
}

function seasonUpdateBody(season: BackendSeason, payload: UpdateAdminSeasonPayload): string {
  return JSON.stringify({
    code: season.code,
    title: payload.title,
    description: payload.description,
    startDate: season.startDate,
    endDate: season.endDate,
    image: season.image,
    displayOrder: season.displayOrder,
    ...(/^\d{4}-\d{2}-\d{2}T/.test(payload.startsAt) ? { startsAt: payload.startsAt } : {}),
    organizer: payload.organizer,
    status: payload.status,
    active: payload.status === 'PUBLISHED',
    version: season.version,
  });
}

async function requestBackendSeason(id: number): Promise<BackendSeason> {
  return apiRequest<BackendSeason>(`/api/season-service/api/v1/admin/seasons/${id}`);
}

export const adminQueryKeys = {
  users: ['admin', 'users'] as const,
  seasons: ['admin', 'seasons'] as const,
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

export async function getAdminSeasons(): Promise<AdminSeason[]> {
  // shortcut: the admin table loads at most 100 seasons; add pagination when the catalog grows.
  const page = await apiRequest<{ content: BackendSeason[] }>(
    '/api/season-service/api/v1/admin/seasons?size=100'
  );
  return page.content.map(mapSeason);
}

export async function createAdminSeason(payload: CreateAdminSeasonPayload): Promise<AdminSeason> {
  const parsedDate = payload.startsAt ? new Date(payload.startsAt) : new Date();
  const yearStr = Number.isNaN(parsedDate.getTime())
    ? new Date().getFullYear().toString()
    : parsedDate.getFullYear().toString();

  const body = JSON.stringify({
    code: payload.code.trim(),
    title: payload.title.trim(),
    description: payload.description.trim(),
    startDate: payload.startDate || yearStr,
    endDate: payload.endDate || 'н. в.',
    image: payload.image || '/images/seasons/season-1.webp',
    displayOrder: payload.displayOrder ?? 100,
    ...(/^\d{4}-\d{2}-\d{2}T/.test(payload.startsAt) ? { startsAt: payload.startsAt } : {}),
    organizer: payload.organizer.trim(),
    status: payload.status,
    active: payload.status === 'PUBLISHED',
  });

  const created = await apiRequest<BackendSeason>('/api/season-service/api/v1/admin/seasons', {
    method: 'POST',
    body,
  });
  return mapSeason(created);
}

export async function updateAdminSeason(
  id: number,
  payload: UpdateAdminSeasonPayload
): Promise<AdminSeason> {
  const current = await requestBackendSeason(id);
  const updated = await apiRequest<BackendSeason>(
    `/api/season-service/api/v1/admin/seasons/${id}`,
    {
      method: 'PUT',
      body: seasonUpdateBody(current, payload),
    }
  );
  return mapSeason(updated);
}

export async function updateAdminSeasonStatus(
  id: number,
  status: AdminSeasonStatus
): Promise<AdminSeason> {
  const current = await requestBackendSeason(id);
  const updated = await apiRequest<BackendSeason>(
    `/api/season-service/api/v1/admin/seasons/${id}`,
    {
      method: 'PUT',
      body: seasonUpdateBody(current, {
        title: current.title,
        description: current.description,
        startsAt: current.startDate,
        organizer: current.organizer,
        status,
      }),
    }
  );
  return mapSeason(updated);
}

export async function deleteAdminSeason(id: number): Promise<void> {
  await apiRequest<void>(`/api/season-service/api/v1/admin/seasons/${id}`, { method: 'DELETE' });
}

export async function getAdminEvents(): Promise<AdminEvent[]> {
  const page = await apiRequest<{ content: AdminEvent[] }>(
    '/api/event-service/api/v1/admin/events?size=100'
  );
  return page.content;
}

export async function createAdminEvent(payload: CreateAdminEventPayload): Promise<AdminEvent> {
  const body = JSON.stringify({
    code: payload.code.trim(),
    title: payload.title.trim(),
    description: payload.description.trim(),
    shortDescription: payload.shortDescription?.trim() || null,
    image: payload.image?.trim() || '/images/events/placeholder.webp',
    seasonNumber: payload.seasonNumber,
    organizer: payload.organizer.trim(),
    status: payload.status,
    startDate: payload.startDate || null,
    endDate: payload.endDate || null,
    displayOrder: payload.displayOrder ?? 0,
    active: payload.active ?? true,
  });

  return apiRequest<AdminEvent>('/api/event-service/api/v1/admin/events', {
    method: 'POST',
    body,
  });
}

export async function updateAdminEvent(
  id: number,
  payload: UpdateAdminEventPayload
): Promise<AdminEvent> {
  const body = JSON.stringify({
    title: payload.title?.trim(),
    description: payload.description?.trim(),
    shortDescription: payload.shortDescription?.trim() || null,
    image: payload.image?.trim() || '/images/events/placeholder.webp',
    seasonNumber: payload.seasonNumber,
    organizer: payload.organizer?.trim(),
    status: payload.status,
    startDate: payload.startDate || null,
    endDate: payload.endDate || null,
    displayOrder: payload.displayOrder ?? 0,
    active: payload.active ?? true,
  });

  return apiRequest<AdminEvent>(`/api/event-service/api/v1/admin/events/${id}`, {
    method: 'PUT',
    body,
  });
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

export const adminSeasonsQueryOptions = queryOptions({
  queryKey: adminQueryKeys.seasons,
  queryFn: getAdminSeasons,
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

export function useAdminSeasons() {
  return useQuery(adminSeasonsQueryOptions);
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
