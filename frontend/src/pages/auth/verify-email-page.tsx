import { useEffect, useRef, useState } from 'react';
import { Button } from '@/components';
import { resendVerificationCode, verifyRegistrationCode } from '@/api';
import { useAuth } from '@/context';
import { paths } from '@/routes';
import { Link, useLocation, useNavigate } from 'react-router';

interface RegistrationData {
  email: string;
  username: string;
  password?: string;
  cooldownUntil?: number;
}

function getRemainingSeconds(cooldownUntil?: number): number {
  if (!cooldownUntil) return 0;
  const remaining = Math.ceil((cooldownUntil - Date.now()) / 1000);
  return remaining > 0 ? remaining : 0;
}

export function VerifyEmailPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();

  const [regData] = useState<RegistrationData | null>(() => {
    const stateData = location.state as RegistrationData | null;
    if (stateData?.email && stateData?.username) {
      return stateData;
    }

    const saved =
      typeof window !== 'undefined' ? sessionStorage.getItem('pendingRegistration') : null;
    if (saved) {
      try {
        const parsed = JSON.parse(saved) as RegistrationData;
        if (parsed.email && parsed.username) {
          return parsed;
        }
      } catch {
        // ignore parse error
      }
    }
    return null;
  });

  const [digits, setDigits] = useState<string[]>(['', '', '', '', '', '']);
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [countdown, setCountdown] = useState<number>(() => {
    const stateData = location.state as RegistrationData | null;
    if (stateData?.cooldownUntil) {
      return getRemainingSeconds(stateData.cooldownUntil);
    }
    const saved =
      typeof window !== 'undefined' ? sessionStorage.getItem('pendingRegistration') : null;
    if (saved) {
      try {
        const parsed = JSON.parse(saved) as RegistrationData;
        if (parsed.cooldownUntil) {
          return getRemainingSeconds(parsed.cooldownUntil);
        }
      } catch {
        // ignore parse error
      }
    }
    return 0;
  });
  const [isResending, setIsResending] = useState(false);

  const inputRefs = useRef<(HTMLInputElement | null)[]>([]);

  // Focus first input on mount
  useEffect(() => {
    inputRefs.current[0]?.focus();
  }, []);

  // Countdown timer for resend
  useEffect(() => {
    if (countdown <= 0) return;
    const timer = setInterval(() => {
      setCountdown((prev) => Math.max(0, prev - 1));
    }, 1000);
    return () => clearInterval(timer);
  }, [countdown]);

  const handleVerify = async (codeToVerify: string) => {
    if (!regData || !regData.password) {
      setError('Данные регистрации отсутствуют. Пожалуйста, начните заново.');
      return;
    }

    setIsSubmitting(true);
    setError(null);

    try {
      const tokens = await verifyRegistrationCode(
        regData.email,
        regData.username,
        regData.password,
        codeToVerify
      );
      sessionStorage.removeItem('pendingRegistration');
      login(tokens);
      navigate(paths.home);
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Неверный код подтверждения';
      setError(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleInputChange = (index: number, value: string) => {
    // Only accept numeric characters
    const cleanValue = value.replace(/\D/g, '');

    setError(null);

    if (!cleanValue) {
      const nextDigits = [...digits];
      nextDigits[index] = '';
      setDigits(nextDigits);
      return;
    }

    // Single digit input
    const char = cleanValue.slice(-1);
    const nextDigits = [...digits];
    nextDigits[index] = char;
    setDigits(nextDigits);

    // Auto-advance
    if (index < 5) {
      inputRefs.current[index + 1]?.focus();
    }

    const fullCode = nextDigits.join('');
    if (fullCode.length === 6) {
      handleVerify(fullCode);
    }
  };

  const handleKeyDown = (index: number, e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Backspace') {
      if (!digits[index] && index > 0) {
        inputRefs.current[index - 1]?.focus();
      }
    } else if (e.key === 'ArrowLeft' && index > 0) {
      inputRefs.current[index - 1]?.focus();
    } else if (e.key === 'ArrowRight' && index < 5) {
      inputRefs.current[index + 1]?.focus();
    }
  };

  const handlePaste = (e: React.ClipboardEvent<HTMLInputElement>) => {
    e.preventDefault();
    const pasteData = e.clipboardData.getData('text').replace(/\D/g, '').slice(0, 6);
    if (!pasteData) return;

    const nextDigits = [...digits];
    for (let i = 0; i < 6; i++) {
      nextDigits[i] = pasteData[i] ?? '';
    }
    setDigits(nextDigits);
    setError(null);

    const focusIndex = Math.min(pasteData.length, 5);
    inputRefs.current[focusIndex]?.focus();

    if (pasteData.length === 6) {
      handleVerify(pasteData);
    }
  };

  const handleResend = async () => {
    if (!regData?.email || countdown > 0 || isResending) return;

    setIsResending(true);
    setError(null);

    try {
      const res = await resendVerificationCode(regData.email, 'REGISTRATION');
      const cooldownSec = res.cooldownSeconds || 60;
      const newCooldownUntil = Date.now() + cooldownSec * 1000;
      setCountdown(cooldownSec);
      setDigits(['', '', '', '', '', '']);
      inputRefs.current[0]?.focus();

      const updated = { ...regData, cooldownUntil: newCooldownUntil };
      sessionStorage.setItem('pendingRegistration', JSON.stringify(updated));
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Не удалось повторно отправить код';
      setError(message);
    } finally {
      setIsResending(false);
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const code = digits.join('');
    if (code.length < 6) {
      setError('Введите все 6 цифр кода');
      return;
    }
    handleVerify(code);
  };

  if (!regData) {
    return (
      <div className="flex flex-col gap-6 text-center">
        <h1 className="text-2xl font-bold">Данные регистрации не найдены</h1>
        <p className="text-foreground-muted text-sm">
          Сессия регистрации истекла или страница была открыта напрямую.
        </p>
        <Link to={paths.register}>
          <Button className="w-full">Вернуться к регистрации</Button>
        </Link>
      </div>
    );
  }

  const isCodeComplete = digits.join('').length === 6;

  return (
    <div className="flex flex-col gap-6">
      <header className="flex flex-col gap-3">
        <h1 className="text-4xl font-bold">Подтверждение почты</h1>
        <p className="text-foreground-muted text-base font-medium">
          Мы отправили 6-значный код подтверждения на почту{' '}
          <span className="text-foreground font-semibold">{regData.email}</span>
        </p>
      </header>

      <form noValidate onSubmit={handleSubmit} className="flex flex-col gap-6">
        <div className="flex flex-col items-center gap-3">
          <div className="flex w-full justify-between gap-2 sm:gap-3">
            {digits.map((digit, index) => (
              <input
                key={index}
                ref={(el) => {
                  inputRefs.current[index] = el;
                }}
                type="text"
                inputMode="numeric"
                maxLength={1}
                value={digit}
                onChange={(e) => handleInputChange(index, e.target.value)}
                onKeyDown={(e) => handleKeyDown(index, e)}
                onPaste={handlePaste}
                disabled={isSubmitting}
                className={`bg-card text-foreground aspect-square size-12 shrink-0 rounded-none border text-center text-xl font-bold transition-colors focus:ring-1 focus:outline-none sm:size-14 ${
                  error
                    ? 'border-error text-error focus:border-error focus:ring-error'
                    : 'border-border focus:border-border-hover focus:ring-ring'
                }`}
              />
            ))}
          </div>

          {error && <span className="text-error text-center text-xs font-medium">{error}</span>}
        </div>

        <Button type="submit" disabled={!isCodeComplete || isSubmitting} className="w-full">
          {isSubmitting ? 'Проверка...' : 'Подтвердить'}
        </Button>

        <div className="flex flex-col items-center gap-2 text-center text-sm">
          {countdown > 0 ? (
            <span className="text-foreground-muted">
              Повторная отправка доступна через{' '}
              <span className="text-foreground font-mono font-medium">{countdown}</span> сек.
            </span>
          ) : (
            <button
              type="button"
              onClick={handleResend}
              disabled={isResending}
              className="text-foreground cursor-pointer font-medium transition-all hover:underline disabled:opacity-50"
            >
              {isResending ? 'Отправка...' : 'Отправить код повторно'}
            </button>
          )}

          <Link
            to={paths.register}
            className="text-foreground-muted hover:text-foreground mt-1 text-xs transition-colors hover:underline"
          >
            Изменить данные
          </Link>
        </div>
      </form>
    </div>
  );
}
