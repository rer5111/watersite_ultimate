import { Link } from 'react-router';
import { useAuth } from '@/context';
import { useProfile } from '@/api';
import { Button } from '@/components/ui';
import { paths } from '@/routes';
import {
  ProfileHeroSection,
  SeasonHistorySection,
  SkinShowcaseSection,
  AccountManagementSection,
  DangerZoneSection,
} from './sections';

export function ProfilePage() {
  const { user, isLoading: isAuthLoading } = useAuth();
  const {
    data: profile,
    isLoading: isProfileLoading,
    isError,
    refetch,
  } = useProfile(Boolean(user));

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
          Для доступа к управлению профилем и аккаунтом войдите в систему или создайте новую учётную
          запись.
        </p>
        <div className="flex flex-wrap justify-center gap-4">
          <Link to={paths.login}>
            <Button variant="primary">Войти в аккаунт</Button>
          </Link>
          <Link to={paths.register}>
            <Button variant="secondary">Регистрация</Button>
          </Link>
        </div>
      </main>
    );
  }

  if (isProfileLoading) {
    return null;
  }

  if (isError || !profile) {
    return (
      <main className="mx-auto flex min-h-[60vh] w-full max-w-4xl flex-col items-center justify-center gap-6 px-4 py-16 text-center">
        <h1 className="text-foreground text-3xl font-bold tracking-tight md:text-4xl">
          Не удалось загрузить профиль
        </h1>
        <p className="text-foreground-muted max-w-md text-base">
          Произошла ошибка при загрузке ваших данных. Проверьте соединение с интернетом или
          повторите попытку.
        </p>
        <Button variant="secondary" onClick={() => void refetch()}>
          Повторить попытку
        </Button>
      </main>
    );
  }

  return (
    <main className="mx-auto flex w-full max-w-5xl flex-col gap-10 px-4 py-10 md:px-8 md:py-16">
      <ProfileHeroSection profile={profile} />
      <SkinShowcaseSection profile={profile} />
      <SeasonHistorySection playerId={profile.id} />
      <AccountManagementSection profile={profile} />
      <DangerZoneSection username={profile.username} />
    </main>
  );
}
