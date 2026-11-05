import { Navigate, Outlet } from 'react-router';
import { ButtonLink } from '@/components/ui';
import { useAuth } from '@/context';
import { paths } from '@/routes/paths';

export function AdminRoute() {
  const { user, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="bg-background text-foreground-muted flex min-h-dvh items-center justify-center p-6 text-sm">
        Проверка доступа…
      </div>
    );
  }

  if (!user) {
    return <Navigate to={paths.login} replace />;
  }

  const isAdmin = user.roles.some((role) => role.toUpperCase() === 'ADMIN');

  if (!isAdmin) {
    return (
      <main className="bg-background flex min-h-dvh flex-col items-center justify-center gap-6 p-6 text-center">
        <div className="border-border bg-card flex w-full max-w-lg flex-col items-center gap-4 rounded-none border p-8">
          <span className="text-error text-xs font-semibold tracking-widest uppercase">
            Ошибка 403
          </span>
          <h1 className="text-foreground text-2xl font-bold">Доступ запрещён</h1>
          <p className="text-foreground-muted max-w-sm text-sm leading-relaxed">
            Для входа в админ-панель требуется роль администратора. Если вы считаете, что это
            ошибка, обратитесь к владельцу сервера.
          </p>
          <ButtonLink to={paths.home} variant="secondary" className="rounded-none">
            Вернуться на главную
          </ButtonLink>
        </div>
      </main>
    );
  }

  return <Outlet />;
}
