import { Button } from '@/components/ui';

export type NotificationsHeaderProps = {
  unreadCount: number;
  isMarkingAllRead?: boolean;
  onMarkAllRead: () => void;
};

export function NotificationsHeader({
  unreadCount,
  isMarkingAllRead = false,
  onMarkAllRead,
}: NotificationsHeaderProps) {
  return (
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
          disabled={unreadCount === 0 || isMarkingAllRead}
          onClick={onMarkAllRead}
        >
          {isMarkingAllRead ? 'Отмечаем…' : 'Прочитать все'}
        </Button>
      </div>
    </div>
  );
}
