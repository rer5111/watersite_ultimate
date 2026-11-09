import { queryOptions, useQuery } from '@tanstack/react-query';
import type { SendCodeResponse, UserProfile } from '@/types';
import { parseApiError } from './error-helper';

export class ProfileApiError extends Error {
  readonly status: number;
  readonly code?: string;
  readonly details?: Record<string, string>;

  constructor(message: string, status: number, code?: string, details?: Record<string, string>) {
    super(message);
    this.name = 'ProfileApiError';
    this.status = status;
    this.code = code;
    this.details = details;
  }
}

function getApiBaseUrl(): string {
  return (import.meta.env.VITE_API_URL ?? '').replace(/\/+$/, '');
}

function getAuthHeaders(includeJson: boolean = false): HeadersInit {
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

async function handleResponseError(response: Response, defaultMessage: string): Promise<never> {
  const message = await parseApiError(response, defaultMessage);
  throw new ProfileApiError(message, response.status);
}

export async function getUserProfile(): Promise<UserProfile> {
  const base = getApiBaseUrl();
  const response = await fetch(`${base}/api/player-service/api/user/me`, {
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleResponseError(response, 'Не удалось загрузить данные профиля');
  }

  return (await response.json()) as UserProfile;
}

export const profileQueryOptions = (enabled: boolean = true) =>
  queryOptions({
    queryKey: ['user-profile'] as const,
    queryFn: getUserProfile,
    enabled,
    staleTime: 1000 * 60 * 5,
  });

export function useProfile(enabled: boolean = true) {
  return useQuery(profileQueryOptions(enabled));
}

export async function updateMinecraftNickname(minecraftNickname: string): Promise<UserProfile> {
  const base = getApiBaseUrl();
  const response = await fetch(`${base}/api/player-service/api/user/me/minecraft-nickname`, {
    method: 'PUT',
    headers: getAuthHeaders(true),
    body: JSON.stringify({ minecraftNickname }),
  });

  if (!response.ok) {
    await handleResponseError(response, 'Не удалось обновить ник Minecraft');
  }

  return (await response.json()) as UserProfile;
}

export async function sendEmailChangeCode(newEmail: string): Promise<SendCodeResponse> {
  const base = getApiBaseUrl();
  const response = await fetch(`${base}/api/auth-service/auth/change-email/send-code`, {
    method: 'POST',
    headers: getAuthHeaders(true),
    body: JSON.stringify({ newEmail }),
  });

  if (!response.ok) {
    await handleResponseError(response, 'Не удалось отправить код подтверждения');
  }

  return (await response.json()) as SendCodeResponse;
}

export async function confirmChangeEmail(newEmail: string, code: string): Promise<void> {
  const base = getApiBaseUrl();
  const response = await fetch(`${base}/api/auth-service/auth/change-email`, {
    method: 'POST',
    headers: getAuthHeaders(true),
    body: JSON.stringify({ newEmail, code }),
  });

  if (!response.ok) {
    await handleResponseError(response, 'Не удалось изменить email');
  }
}

export async function sendPasswordChangeCode(): Promise<SendCodeResponse> {
  const base = getApiBaseUrl();
  const response = await fetch(`${base}/api/auth-service/auth/change-password/send-code`, {
    method: 'POST',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleResponseError(response, 'Не удалось отправить код для смены пароля');
  }

  return (await response.json()) as SendCodeResponse;
}

export async function confirmChangePassword(code: string, newPassword: string): Promise<void> {
  const base = getApiBaseUrl();
  const response = await fetch(`${base}/api/auth-service/auth/change-password`, {
    method: 'POST',
    headers: getAuthHeaders(true),
    body: JSON.stringify({ code, newPassword }),
  });

  if (!response.ok) {
    await handleResponseError(response, 'Не удалось изменить пароль');
  }
}

export async function deleteAccount(): Promise<void> {
  const base = getApiBaseUrl();
  const response = await fetch(`${base}/api/player-service/api/user/me`, {
    method: 'DELETE',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleResponseError(response, 'Не удалось удалить аккаунт');
  }
}
