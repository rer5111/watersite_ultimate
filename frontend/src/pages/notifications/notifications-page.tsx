import { useMutation, useQueryClient } from '@tanstack/react-query';
import {
  markAllNotificationsRead,
  markNotificationRead,
  notificationQueryKeys,
  useNotifications,
} from '@/api';
import { Button, toast } from '@/components/ui';
import { useAuth } from '@/context';
import { useDelayedLoading } from '@/hooks';
import {
  NotificationCard,
  NotificationsAuthPrompt,
  NotificationsHeader,
  NotificationsSkeleton,
} from './components';

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
    return <NotificationsAuthPrompt />;
  }

  const unreadCount = notifications.filter((notification) => !notification.read).length;

  return (
    <main className="mx-auto flex w-full max-w-4xl flex-col gap-8 px-4 py-12 md:px-8 md:py-16">
      <NotificationsHeader
        unreadCount={unreadCount}
        isMarkingAllRead={markAllRead.isPending}
        onMarkAllRead={() => markAllRead.mutate()}
      />

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
        isSkeletonVisible && <NotificationsSkeleton />
      ) : notifications.length === 0 ? (
        <section className="border-border bg-card border p-8 text-center">
          <p className="text-foreground-muted text-sm">Уведомлений пока нет.</p>
        </section>
      ) : (
        <div className="flex flex-col gap-3">
          {notifications.map((notification) => (
            <NotificationCard
              key={notification.id}
              notification={notification}
              isMarkingRead={markRead.isPending && markRead.variables === notification.id}
              onMarkRead={(id) => markRead.mutate(id)}
            />
          ))}
        </div>
      )}
    </main>
  );
}
