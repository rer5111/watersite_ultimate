import { Link } from 'react-router';
import { BellIcon, buttonVariants } from '@/components/ui';
import { useUnreadNotificationCount } from '@/api';
import { useAuth } from '@/context';
import { cn } from '@/lib';
import { paths } from '@/routes';

export function NotificationBell() {
  const { user } = useAuth();
  const { data: unreadCount = 0 } = useUnreadNotificationCount(Boolean(user));

  if (!user) return null;

  const label = unreadCount > 0 ? `Уведомления, непрочитанных: ${unreadCount}` : 'Уведомления';

  return (
    <Link
      to={paths.notifications}
      aria-label={label}
      className={cn(
        buttonVariants({ variant: 'tertiary' }),
        'hover:bg-tertiary-hover/50 active:bg-tertiary-active/50 relative h-10 px-3'
      )}
    >
      <BellIcon className="size-5" />
      {unreadCount > 0 && (
        <span className="bg-primary text-foreground absolute -top-1 -right-1 min-w-5 rounded-none px-1.5 text-center text-[10px] leading-5 font-bold">
          {unreadCount > 99 ? '99+' : unreadCount}
        </span>
      )}
    </Link>
  );
}
