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
  return (
    <div className="bg-card flex flex-col gap-6 p-6 md:flex-row md:items-center md:justify-between md:p-8">
      <div className="flex flex-col items-center gap-6 text-center sm:flex-row sm:text-left">
        <div className="relative shrink-0">
          <SkinAvatar
            uuid={profile.minecraftUuid}
            name={profile.minecraftNickname || profile.username}
            mode="face"
            className="aspect-square size-24 shrink-0 rounded-none object-cover"
          />
        </div>

        <div className="flex flex-col gap-2">
          <div className="flex flex-wrap items-center justify-center gap-2.5 sm:justify-start">
            <h1 className="text-foreground text-2xl font-bold tracking-tight">
              {profile.minecraftNickname || profile.username}
            </h1>
          </div>

          <p className="text-foreground-muted text-sm">
            Почта:{' '}
            <span className="text-foreground font-medium">{profile.email ?? 'Не привязана'}</span>
          </p>
        </div>
      </div>

      <div className="border-border flex flex-col justify-center border-t pt-4 text-center sm:text-left md:border-t-0 md:border-l md:pt-0 md:pl-8">
        <span className="text-foreground-muted text-xs tracking-wider uppercase">
          Дата регистрации
        </span>
        <span className="text-foreground mt-1 text-base font-semibold">
          {formatDate(profile.joinDate)}
        </span>
      </div>
    </div>
  );
}
