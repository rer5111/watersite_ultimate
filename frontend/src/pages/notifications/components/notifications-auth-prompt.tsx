import { Link } from 'react-router';
import { Button } from '@/components/ui';
import { paths } from '@/routes';

export function NotificationsAuthPrompt() {
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
