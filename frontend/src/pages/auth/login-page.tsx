import { useEffect, useRef, useState, type KeyboardEvent } from 'react';
import { Button, Checkbox, Input } from '@/components/ui';
import { MicrosoftDeviceModal } from './components';
import { loginWithCredentials } from '@/api';
import { useAuth } from '@/context';
import { paths } from '@/routes';
import { Link, useNavigate } from 'react-router';

const REMEMBERED_LOGIN_KEY = 'remembered_login';

interface FormErrors {
  identifier?: string;
  password?: string;
  general?: string;
}

export function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [isDeviceModalOpen, setIsDeviceModalOpen] = useState(false);
  const [identifier, setIdentifier] = useState(() => {
    return (
      (typeof window !== 'undefined' ? localStorage.getItem(REMEMBERED_LOGIN_KEY) : null) || ''
    );
  });
  const [password, setPassword] = useState('');
  const [rememberMe, setRememberMe] = useState(() => {
    return Boolean(
      typeof window !== 'undefined' ? localStorage.getItem(REMEMBERED_LOGIN_KEY) : false
    );
  });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errors, setErrors] = useState<FormErrors>({});

  const identifierInputRef = useRef<HTMLInputElement>(null);
  const passwordInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (localStorage.getItem(REMEMBERED_LOGIN_KEY)) {
      passwordInputRef.current?.focus();
    }
  }, []);

  const handleDeviceSuccess = () => {
    setIsDeviceModalOpen(false);
    navigate(paths.home);
  };

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const newErrors: FormErrors = {};
    const trimmedIdentifier = identifier.trim();

    if (!trimmedIdentifier) {
      newErrors.identifier = 'Введите email или никнейм Minecraft';
    }
    if (!password) {
      newErrors.password = 'Введите пароль';
    }

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      if (newErrors.identifier) {
        identifierInputRef.current?.focus();
      } else if (newErrors.password) {
        passwordInputRef.current?.focus();
      }
      return;
    }

    setIsSubmitting(true);
    setErrors({});

    try {
      const tokens = await loginWithCredentials(trimmedIdentifier, password);
      if (rememberMe) {
        localStorage.setItem(REMEMBERED_LOGIN_KEY, trimmedIdentifier);
      } else {
        localStorage.removeItem(REMEMBERED_LOGIN_KEY);
      }
      login(tokens);
      navigate(paths.home);
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Неверный email/никнейм или пароль';
      setErrors({
        identifier: message,
        password: ' ',
      });
      identifierInputRef.current?.focus();
      identifierInputRef.current?.select();
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleIdentifierKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      passwordInputRef.current?.focus();
    }
  };

  return (
    <div className="flex flex-col gap-6">
      <header className="flex flex-col gap-3">
        <h1 className="text-4xl font-bold">С возвращением!</h1>
        <p className="text-foreground-muted text-base font-medium">
          Введите свои данные для входа на сайт
        </p>
      </header>

      <Button
        type="button"
        variant="secondary"
        onClick={() => setIsDeviceModalOpen(true)}
        className="flex w-full items-center justify-center gap-2.5 py-2.5 font-semibold"
      >
        <svg className="size-4 shrink-0" viewBox="0 0 21 21" fill="none">
          <path d="M1 1h9v9H1V1z" fill="#f25022" />
          <path d="M11 1h9v9h-9V1z" fill="#7fba00" />
          <path d="M1 11h9v9H1v-9z" fill="#00a4ef" />
          <path d="M11 11h9v9h-9v-9z" fill="#ffb900" />
        </svg>
        Войти через Microsoft (Лицензия)
      </Button>

      <MicrosoftDeviceModal
        isOpen={isDeviceModalOpen}
        onClose={() => setIsDeviceModalOpen(false)}
        onSuccess={handleDeviceSuccess}
      />

      <div className="relative flex items-center justify-center">
        <div className="border-border w-full border-t" />
        <span className="bg-background text-foreground-muted absolute px-3 text-xs uppercase">
          или
        </span>
      </div>

      <form noValidate onSubmit={handleSubmit} className="flex flex-col gap-4">
        <div className="flex flex-col gap-2">
          <label htmlFor="login" className="text-foreground text-sm font-semibold">
            Email или никнейм Minecraft
          </label>
          <Input
            ref={identifierInputRef}
            id="login"
            name="login"
            autoComplete="login"
            placeholder="Введите email или никнейм"
            value={identifier}
            onChange={(e) => {
              setIdentifier(e.target.value);
              if (errors.identifier || errors.general) {
                setErrors((prev) => ({
                  ...prev,
                  identifier: undefined,
                  general: undefined,
                }));
              }
            }}
            onKeyDown={handleIdentifierKeyDown}
            isError={Boolean(errors.identifier)}
          />
          {errors.identifier && errors.identifier.trim() && (
            <span className="text-error text-xs font-medium">{errors.identifier}</span>
          )}
        </div>

        <div className="flex flex-col gap-2">
          <label htmlFor="password" className="text-foreground text-sm font-semibold">
            Пароль
          </label>
          <Input
            ref={passwordInputRef}
            id="password"
            name="password"
            type="password"
            autoComplete="password"
            placeholder="Введите пароль"
            value={password}
            onChange={(e) => {
              setPassword(e.target.value);
              if (errors.password || errors.general) {
                setErrors((prev) => ({
                  ...prev,
                  password: undefined,
                  general: undefined,
                }));
              }
            }}
            isError={Boolean(errors.password)}
          />
          {errors.password && errors.password.trim() && (
            <span className="text-error text-xs font-medium">{errors.password}</span>
          )}
        </div>

        <label className="flex w-fit cursor-pointer items-center gap-3 select-none">
          <Checkbox
            name="rememberMe"
            checked={rememberMe}
            onChange={(e) => setRememberMe(e.target.checked)}
          />
          <span className="text-foreground-muted text-sm">Запомнить меня</span>
        </label>

        <Button type="submit" disabled={isSubmitting} className="mt-4">
          {isSubmitting ? 'Вход...' : 'Войти'}
        </Button>
      </form>

      <p className="flex justify-center gap-2 text-sm">
        <span className="text-foreground-muted">Нет аккаунта?</span>
        <Link to={paths.register} className="font-medium transition-all hover:underline">
          Зарегистрироваться
        </Link>
      </p>
    </div>
  );
}
