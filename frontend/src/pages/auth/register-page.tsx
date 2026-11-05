import { useRef, useState } from 'react';
import { Button, Input, MicrosoftLoginSection } from '@/components';
import { AuthApiError, sendRegistrationCode } from '@/api';
import { paths } from '@/routes';
import { Link, useNavigate } from 'react-router';

interface FormErrors {
  email?: string;
  nickname?: string;
  password?: string;
  general?: string;
}

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const NICKNAME_REGEX = /^[a-zA-Z0-9_]{3,16}$/;

export function RegisterPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [nickname, setNickname] = useState('');
  const [password, setPassword] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errors, setErrors] = useState<FormErrors>({});

  const emailInputRef = useRef<HTMLInputElement>(null);
  const nicknameInputRef = useRef<HTMLInputElement>(null);
  const passwordInputRef = useRef<HTMLInputElement>(null);

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const newErrors: FormErrors = {};
    const trimmedEmail = email.trim();
    const trimmedNickname = nickname.trim();

    if (!trimmedEmail) {
      newErrors.email = 'Введите Email';
    } else if (!EMAIL_REGEX.test(trimmedEmail)) {
      newErrors.email = 'Некорректный формат Email';
    }

    if (!trimmedNickname) {
      newErrors.nickname = 'Введите никнейм Minecraft';
    } else if (!NICKNAME_REGEX.test(trimmedNickname)) {
      newErrors.nickname = 'От 3 до 16 символов (только латиница, цифры и _)';
    }

    if (!password) {
      newErrors.password = 'Введите пароль';
    } else if (password.length < 8) {
      newErrors.password = 'Пароль должен содержать минимум 8 символов';
    }

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      if (newErrors.email) {
        emailInputRef.current?.focus();
      } else if (newErrors.nickname) {
        nicknameInputRef.current?.focus();
      } else if (newErrors.password) {
        passwordInputRef.current?.focus();
      }
      return;
    }

    setIsSubmitting(true);
    setErrors({});

    try {
      const response = await sendRegistrationCode(trimmedEmail, trimmedNickname, password);
      const cooldownUntil = Date.now() + (response.cooldownSeconds || 60) * 1000;
      sessionStorage.setItem(
        'pendingRegistration',
        JSON.stringify({
          email: trimmedEmail,
          username: trimmedNickname,
          password,
          cooldownUntil,
        })
      );
      navigate(paths.verifyEmail, {
        state: {
          email: trimmedEmail,
          username: trimmedNickname,
          password,
          cooldownUntil,
        },
      });
    } catch (err: unknown) {
      const fieldErrors: FormErrors = {};

      if (err instanceof AuthApiError) {
        if (err.details && Object.keys(err.details).length > 0) {
          if (err.details.nickname || err.details.username) {
            fieldErrors.nickname = err.details.nickname || err.details.username;
          }
          if (err.details.email) {
            fieldErrors.email = err.details.email;
          }
          if (err.details.password) {
            fieldErrors.password = err.details.password;
          }
        }

        if (Object.keys(fieldErrors).length === 0) {
          const lower = err.message.toLowerCase();
          if (lower.includes('никнейм') || lower.includes('username')) {
            fieldErrors.nickname = err.message;
          } else if (lower.includes('email') || lower.includes('почт')) {
            fieldErrors.email = err.message;
          } else if (lower.includes('парол')) {
            fieldErrors.password = err.message;
          } else {
            fieldErrors.general = err.message;
          }
        }
      } else {
        const message =
          err instanceof Error ? err.message : 'Не удалось отправить код подтверждения';
        const lower = message.toLowerCase();
        if (lower.includes('никнейм') || lower.includes('username')) {
          fieldErrors.nickname = message;
        } else if (lower.includes('email') || lower.includes('почт')) {
          fieldErrors.email = message;
        } else if (lower.includes('парол')) {
          fieldErrors.password = message;
        } else {
          fieldErrors.general = message;
        }
      }

      setErrors(fieldErrors);

      if (fieldErrors.nickname) {
        nicknameInputRef.current?.focus();
        nicknameInputRef.current?.select();
      } else if (fieldErrors.email) {
        emailInputRef.current?.focus();
        emailInputRef.current?.select();
      } else if (fieldErrors.password) {
        passwordInputRef.current?.focus();
        passwordInputRef.current?.select();
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="flex flex-col gap-6">
      <header className="flex flex-col gap-3">
        <h1 className="text-4xl font-bold">Добро пожаловать!</h1>
        <p className="text-foreground-muted text-base font-medium">
          Придумайте логин и пароль для входа на сайт
        </p>
      </header>

      <MicrosoftLoginSection />

      <form noValidate onSubmit={handleSubmit} className="flex flex-col gap-4">
        <div className="flex flex-col gap-2">
          <label htmlFor="email" className="text-foreground text-sm font-semibold">
            Email
          </label>
          <Input
            ref={emailInputRef}
            id="email"
            name="email"
            type="email"
            placeholder="Введите Email"
            value={email}
            onChange={(e) => {
              setEmail(e.target.value);
              if (errors.email || errors.general) {
                setErrors((prev) => ({ ...prev, email: undefined, general: undefined }));
              }
            }}
            isError={Boolean(errors.email)}
          />
          {errors.email && (
            <span className="text-error text-xs font-medium">{errors.email}</span>
          )}
        </div>

        <div className="flex flex-col gap-2">
          <label htmlFor="nickname" className="text-foreground text-sm font-semibold">
            Никнейм Minecraft
          </label>
          <Input
            ref={nicknameInputRef}
            id="nickname"
            name="nickname"
            placeholder="Введите никнейм Minecraft"
            value={nickname}
            onChange={(e) => {
              setNickname(e.target.value);
              if (errors.nickname || errors.general) {
                setErrors((prev) => ({ ...prev, nickname: undefined, general: undefined }));
              }
            }}
            isError={Boolean(errors.nickname)}
          />
          {errors.nickname && (
            <span className="text-error text-xs font-medium">{errors.nickname}</span>
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
            placeholder="Минимум 8 символов"
            value={password}
            onChange={(e) => {
              setPassword(e.target.value);
              if (errors.password || errors.general) {
                setErrors((prev) => ({ ...prev, password: undefined, general: undefined }));
              }
            }}
            isError={Boolean(errors.password)}
          />
          {errors.password && (
            <span className="text-error text-xs font-medium">{errors.password}</span>
          )}
        </div>

        {errors.general && (
          <p className="text-error text-xs font-medium text-center">
            {errors.general}
          </p>
        )}

        <Button type="submit" disabled={isSubmitting} className="mt-2">
          {isSubmitting ? 'Отправка кода...' : 'Зарегистрироваться'}
        </Button>
      </form>

      <p className="flex justify-center gap-2 text-sm">
        <span className="text-foreground-muted">Уже есть аккаунт?</span>
        <Link to={paths.login} className="font-medium transition-all hover:underline">
          Войти
        </Link>
      </p>
    </div>
  );
}
