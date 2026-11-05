import { SkinAvatar } from '@/components/ui';
import type { UserProfile } from '@/types';

export interface PlayerCardProps {
  profile: UserProfile;
}

function formatDate(dateStr: string | null): string {
  if (!dateStr) return 'Неизвестно';
  try {
    const date = new Date(dateStr);
    if (isNaN(date.getTime())) return dateStr;
    return new Intl.DateTimeFormat('ru-RU', {
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    }).format(date);
  } catch {
    return dateStr;
  }
}

export function PlayerCard({ profile }: PlayerCardProps) {
  const isAdmin = profile.roles.includes('ADMIN');

  return (
    <div className="bg-card flex flex-col gap-6 p-6 md:flex-row md:items-center md:justify-between md:p-8">
      <div className="flex flex-col items-center gap-6 text-center sm:flex-row sm:text-left">
        <div className="relative shrink-0">
          <SkinAvatar
            uuid={profile.minecraftUuid}
            name={profile.minecraftNickname || profile.username}
            mode="face"
            className="aspect-square size-24 shrink-0 rounded-none border-2 border-primary/40 object-cover [image-rendering:pixelated] sm:size-28"
          />
        </div>

        <div className="flex flex-col gap-2">
          <div className="flex flex-wrap items-center justify-center gap-2.5 sm:justify-start">
            <h1 className="text-3xl font-bold tracking-tight text-foreground">
              {profile.minecraftNickname || profile.username}
            </h1>
            <span
              className={`rounded-none px-2.5 py-0.5 text-xs font-semibold uppercase tracking-wider ${
                isAdmin
                  ? 'bg-primary text-primary-foreground'
                  : 'bg-secondary text-secondary-foreground'
              }`}
            >
              {isAdmin ? 'Администратор' : 'Игрок'}
            </span>
          </div>

          <p className="text-sm text-foreground-muted">
            Логин на сайте: <span className="font-medium text-foreground">{profile.username}</span>
          </p>

          <p className="text-sm text-foreground-muted">
            Почта:{' '}
            <span className="font-medium text-foreground">
              {profile.email ?? 'Не привязана'}
            </span>
          </p>
        </div>
      </div>

      <div className="flex flex-col justify-center border-t border-border pt-4 text-center sm:text-left md:border-t-0 md:border-l md:pl-8 md:pt-0">
        <span className="text-xs uppercase tracking-wider text-foreground-muted">
          Дата регистрации
        </span>
        <span className="mt-1 text-base font-semibold text-foreground">
          {formatDate(profile.joinDate)}
        </span>
      </div>
    </div>
  );
}
