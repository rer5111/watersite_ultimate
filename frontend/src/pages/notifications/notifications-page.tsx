import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router';
import { useAuth } from '@/context';
import {
  markAllNotificationsRead,
  markNotificationRead,
  notificationQueryKeys,
  useNotifications,
} from '@/api';
import { Button, toast } from '@/components/ui';
import { cn } from '@/lib';
import { paths } from '@/routes';
import type { BroadcastCategory } from '@/types';
import { useDelayedLoading } from '@/hooks';

const categoryStyles: Record<BroadcastCategory, { label: string; className: string }> = {
  INFO: { label: 'Информация', className: 'bg-secondary text-foreground' },
  WARNING: { label: 'Предупреждение', className: 'bg-error text-foreground' },
  SUCCESS: { label: 'Успех', className: 'bg-primary text-foreground' },
};

function formatDateTime(value: string): string {
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

export function NotificationsPage() {
  const { user, isLoading: isAuthLoading } = useAuth();
  const queryClient = useQueryClient();
  const { data: notifications = [], isPending, isError, refetch } = useNotifications(Boolean(user));
  const isSkeletonVisible = useDelayedLoading(isPending);

  const invalidate = () =>
    void queryClient.invalidateQueries({ queryKey: notificationQueryKeys.all });

  const markRead = useMutation({
    mutationFn: markNotificationRead,
    onSuccess: invalidate,
    onError: () => toast.error('Не удалось отметить уведомление прочитанным'),
  });

  const markAllRead = useMutation({
    mutationFn: markAllNotificationsRead,
    onSuccess: invalidate,
    onError: () => toast.error('Не удалось отметить уведомления прочитанными'),
  });

  if (isAuthLoading) {
    return null;
  }

  if (!user) {
    return (
      <main className="mx-auto flex min-h-[60vh] w-full max-w-4xl flex-col items-center justify-center gap-6 px-4 py-16 text-center">
        <h1 className="text-foreground text-3xl font-bold tracking-tight md:text-4xl">
          Требуется авторизация
        </h1>
        <p className="text-foreground-muted max-w-md text-base">
          Чтобы увидеть уведомления администрации сервера, войдите в аккаунт.
        </p>
        <Link to={paths.login}>
          <Button variant="primary">Войти в аккаунт</Button>
        </Link>
      </main>
    );
  }

  const unreadCount = notifications.filter((notification) => !notification.read).length;

  return (
    <main className="mx-auto flex w-full max-w-4xl flex-col gap-8 px-4 py-12 md:px-8 md:py-16">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div className="flex flex-col gap-2">
          <h1 className="text-3xl leading-tight font-bold md:text-4xl">Уведомления</h1>
          <p className="text-foreground-muted">Сообщения и рассылки от администрации сервера</p>
        </div>
        <div className="flex items-center gap-3">
          {unreadCount > 0 && (
            <span className="text-primary text-sm font-semibold">Непрочитанных: {unreadCount}</span>
          )}
          <Button
            variant="secondary"
            disabled={unreadCount === 0 || markAllRead.isPending}
            onClick={() => markAllRead.mutate()}
          >
            {markAllRead.isPending ? 'Отмечаем…' : 'Прочитать все'}
          </Button>
        </div>
      </div>

      {isError ? (
        <section className="border-border bg-card flex flex-col items-center gap-4 border p-8 text-center">
          <p className="text-foreground-muted text-sm">
            Не удалось загрузить уведомления. Проверьте соединение и повторите попытку.
          </p>
          <Button variant="secondary" onClick={() => void refetch()}>
            Повторить попытку
          </Button>
        </section>
      ) : isPending || isSkeletonVisible ? (
        isSkeletonVisible && (
          <div className="flex flex-col gap-3" aria-busy="true">
            {Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className="border-border bg-card h-32 animate-pulse border" />
            ))}
          </div>
        )
      ) : notifications.length === 0 ? (
        <section className="border-border bg-card border p-8 text-center">
          <p className="text-foreground-muted text-sm">Уведомлений пока нет.</p>
        </section>
      ) : (
        <div className="flex flex-col gap-3">
          {notifications.map((notification) => (
            <article
              key={notification.id}
              className={cn(
                'bg-card flex flex-col gap-3 border p-5',
                notification.read ? 'border-border' : 'border-primary'
              )}
            >
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div className="flex items-center gap-3">
                  <span
                    className={cn(
                      'px-2 py-1 text-xs font-semibold tracking-wide uppercase',
                      categoryStyles[notification.category].className
                    )}
                  >
                    {categoryStyles[notification.category].label}
                  </span>
                  {!notification.read && (
                    <span className="text-primary text-xs font-semibold">Новое</span>
                  )}
                </div>
                <time className="text-foreground-subtle text-xs">
                  {formatDateTime(notification.sentAt)}
                </time>
              </div>

              <div className="flex flex-col gap-2">
                <h2 className="text-foreground text-lg font-bold">{notification.subject}</h2>
                <p className="text-foreground-muted text-sm leading-relaxed whitespace-pre-line">
                  {notification.message}
                </p>
              </div>

              {!notification.read && (
                <div className="flex justify-end">
                  <Button
                    variant="secondary"
                    disabled={markRead.isPending}
                    onClick={() => markRead.mutate(notification.id)}
                  >
                    Отметить прочитанным
                  </Button>
                </div>
              )}
            </article>
          ))}
        </div>
      )}
    </main>
  );
}
