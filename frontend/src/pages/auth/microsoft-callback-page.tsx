import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router';
import { Button } from '@/components';
import { loginWithMicrosoft } from '@/api';
import { useAuth } from '@/context';
import { paths } from '@/routes';

export function MicrosoftCallbackPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { login } = useAuth();

  const code = searchParams.get('code');
  const authError = searchParams.get('error_description') || searchParams.get('error');

  const initialError = authError
    ? `Ошибка авторизации Microsoft: ${authError}`
    : !code
      ? 'Код авторизации не найден в ответе Microsoft.'
      : null;

  const [asyncError, setAsyncError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(!initialError);

  const error = initialError || asyncError;

  useEffect(() => {
    if (!code || authError) {
      return;
    }

    const redirectUri = `${window.location.origin}${paths.microsoftCallback}`;
    loginWithMicrosoft(code, redirectUri)
      .then((tokens) => {
        login(tokens);
        navigate(paths.home, { replace: true });
      })
      .catch((err: unknown) => {
        const message = err instanceof Error ? err.message : 'Не удалось войти через Microsoft';
        setAsyncError(message);
        setIsLoading(false);
      });
  }, [code, authError, navigate, login]);

  if (isLoading) {
    return (
      <div className="flex flex-col items-center justify-center gap-4 py-12 text-center">
        <div className="border-primary size-8 shrink-0 animate-spin rounded-full border-2 border-t-transparent" />
        <p className="text-foreground-muted text-base font-medium">
          Входим через Microsoft и проверяем лицензию Minecraft...
        </p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex flex-col gap-6 text-center">
        <header className="flex flex-col gap-2">
          <h1 className="text-2xl font-bold text-red-500">Ошибка авторизации</h1>
          <p className="text-foreground-muted text-sm">{error}</p>
        </header>

        <Button onClick={() => navigate(paths.login)} className="w-full">
          Вернуться на страницу входа
        </Button>
      </div>
    );
  }

  return null;
}
