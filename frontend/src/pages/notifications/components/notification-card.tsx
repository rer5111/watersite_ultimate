import { Button } from '@/components/ui';
import { cn } from '@/lib';
import type { BroadcastCategory, UserNotification } from '@/types';

export type NotificationCardProps = {
  notification: UserNotification;
  onMarkRead?: (id: number) => void;
  isMarkingRead?: boolean;
};

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

export function NotificationCard({
  notification,
  onMarkRead,
  isMarkingRead = false,
}: NotificationCardProps) {
  const categoryConfig = categoryStyles[notification.category] ?? categoryStyles.INFO;

  return (
    <article
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
              categoryConfig.className
            )}
          >
            {categoryConfig.label}
          </span>
          {!notification.read && <span className="text-primary text-xs font-semibold">Новое</span>}
        </div>
        <time className="text-foreground-subtle text-xs">{formatDateTime(notification.sentAt)}</time>
      </div>

      <div className="flex flex-col gap-2">
        <h2 className="text-foreground text-lg font-bold">{notification.subject}</h2>
        <p className="text-foreground-muted text-sm leading-relaxed whitespace-pre-line">
          {notification.message}
        </p>
      </div>

      {!notification.read && onMarkRead && (
        <div className="flex justify-end">
          <Button
            variant="secondary"
            disabled={isMarkingRead}
            onClick={() => onMarkRead(notification.id)}
          >
            {isMarkingRead ? 'Отмечаем…' : 'Отметить прочитанным'}
          </Button>
        </div>
      )}
    </article>
  );
}
