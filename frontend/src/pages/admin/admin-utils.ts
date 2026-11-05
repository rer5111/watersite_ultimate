import type {
  AdminEventStatus,
  AdminRoleRequestStatus,
  AdminUserRole,
  AdminUserStatus,
  BroadcastAudience,
  BroadcastCategory,
  BroadcastStatus,
} from '@/types';

export const userRoleLabels: Record<AdminUserRole, string> = {
  USER: 'Игрок',
  MODERATOR: 'Модератор',
  ADMIN: 'Администратор',
};

export const userStatusLabels: Record<AdminUserStatus, string> = {
  ACTIVE: 'Активен',
  BANNED: 'Забанен',
  DEACTIVATED: 'Деактивирован',
};

export const eventStatusLabels: Record<AdminEventStatus, string> = {
  DRAFT: 'Черновик',
  PUBLISHED: 'Опубликовано',
  CANCELLED: 'Отменено',
};

export const requestStatusLabels: Record<AdminRoleRequestStatus, string> = {
  PENDING: 'На рассмотрении',
  APPROVED: 'Одобрена',
  REJECTED: 'Отклонена',
};

export const broadcastAudienceLabels: Record<BroadcastAudience, string> = {
  ALL: 'Все пользователи',
  ORGANIZERS: 'Организаторы',
  MODERATORS: 'Модераторы',
  ADMINS: 'Администраторы',
};

export const broadcastCategoryLabels: Record<BroadcastCategory, string> = {
  INFO: 'Информация',
  WARNING: 'Предупреждение',
  SUCCESS: 'Успех',
};

export const broadcastStatusLabels: Record<BroadcastStatus, string> = {
  SENT: 'Отправлено',
  SCHEDULED: 'Запланировано',
  FAILED: 'Ошибка',
};

export type BadgeTone = 'success' | 'danger' | 'muted' | 'info';

export const userRoleTones: Record<AdminUserRole, BadgeTone> = {
  ADMIN: 'success',
  MODERATOR: 'info',
  USER: 'muted',
};

export const userStatusTones: Record<AdminUserStatus, BadgeTone> = {
  ACTIVE: 'success',
  BANNED: 'danger',
  DEACTIVATED: 'muted',
};

export const eventStatusTones: Record<AdminEventStatus, BadgeTone> = {
  PUBLISHED: 'success',
  DRAFT: 'muted',
  CANCELLED: 'danger',
};

export const requestStatusTones: Record<AdminRoleRequestStatus, BadgeTone> = {
  PENDING: 'info',
  APPROVED: 'success',
  REJECTED: 'danger',
};

export const broadcastStatusTones: Record<BroadcastStatus, BadgeTone> = {
  SENT: 'success',
  SCHEDULED: 'info',
  FAILED: 'danger',
};

export const broadcastCategoryTones: Record<BroadcastCategory, BadgeTone> = {
  INFO: 'info',
  WARNING: 'danger',
  SUCCESS: 'success',
};

export function formatDate(value: string): string {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat('ru-RU', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  }).format(date);
}

export function formatDateTime(value: string): string {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat('ru-RU', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date);
}

export function toDateTimeLocalValue(value: string): string {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '';
  const pad = (part: number) => String(part).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

export function formatErrorMessage(error: unknown, fallback: string): string {
  return error instanceof Error && error.message ? error.message : fallback;
}
