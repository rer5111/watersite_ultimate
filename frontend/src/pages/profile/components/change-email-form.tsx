import { useState, type FormEvent } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { Button, Input, toast } from '@/components/ui';
import { confirmChangeEmail, sendEmailChangeCode, ProfileApiError } from '@/api';
import { useCooldown } from '@/hooks';

const EMAIL_COOLDOWN_KEY = 'waltercraft_change_email_cooldown_until';
const EMAIL_SENT_KEY = 'waltercraft_change_email_code_sent_until';
const EMAIL_PENDING_KEY = 'waltercraft_change_email_pending_address';

function getStoredEmailSent(): boolean {
  if (typeof window === 'undefined') return false;
  try {
    const stored = localStorage.getItem(EMAIL_SENT_KEY);
    if (!stored) return false;
    const until = parseInt(stored, 10);
    if (Number.isNaN(until) || until <= Date.now()) {
      localStorage.removeItem(EMAIL_SENT_KEY);
      localStorage.removeItem(EMAIL_PENDING_KEY);
      return false;
    }
    return true;
  } catch {
    return false;
  }
}

function getStoredPendingEmail(): string {
  if (typeof window === 'undefined') return '';
  try {
    if (!getStoredEmailSent()) return '';
    return localStorage.getItem(EMAIL_PENDING_KEY) || '';
  } catch {
    return '';
  }
}

export interface ChangeEmailFormProps {
  currentEmail: string | null;
}

export function ChangeEmailForm({ currentEmail }: ChangeEmailFormProps) {
  const queryClient = useQueryClient();
  const [newEmail, setNewEmail] = useState<string>(getStoredPendingEmail);
  const [code, setCode] = useState('');
  const [codeSent, setCodeSent] = useState<boolean>(getStoredEmailSent);
  const [isSendingCode, setIsSendingCode] = useState(false);
  const [isConfirming, setIsConfirming] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const { countdown, startCooldown, resetCooldown, isCooldownActive } = useCooldown(EMAIL_COOLDOWN_KEY);

  const handleSendCode = async () => {
    const trimmed = newEmail.trim().toLowerCase();
    if (!trimmed) {
      setError('Введите новый адрес электронной почты');
      return;
    }

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(trimmed)) {
      setError('Введите корректный адрес электронной почты');
      return;
    }

    if (currentEmail && trimmed === currentEmail.toLowerCase()) {
      setError('Новый email совпадает с текущим');
      return;
    }

    setIsSendingCode(true);
    setError(null);

    try {
      const resp = await sendEmailChangeCode(trimmed);
      const cooldownSeconds = resp.cooldownSeconds || 60;
      const expiresInSeconds = resp.expiresInSeconds || 600;

      startCooldown(cooldownSeconds);
      try {
        localStorage.setItem(EMAIL_SENT_KEY, String(Date.now() + expiresInSeconds * 1000));
        localStorage.setItem(EMAIL_PENDING_KEY, trimmed);
      } catch {
        // ignore
      }
      setCodeSent(true);
      toast.info(`Код подтверждения отправлен на почту ${trimmed}`);
    } catch (err) {
      if (err instanceof ProfileApiError) {
        setError(err.message);
      } else {
        setError('Не удалось отправить код подтверждения');
      }
    } finally {
      setIsSendingCode(false);
    }
  };

  const handleConfirm = async (e: FormEvent) => {
    e.preventDefault();
    const trimmedCode = code.trim();

    if (!trimmedCode || trimmedCode.length !== 6) {
      setError('Введите 6-значный код подтверждения');
      return;
    }

    setIsConfirming(true);
    setError(null);

    try {
      await confirmChangeEmail(newEmail.trim().toLowerCase(), trimmedCode);
      await queryClient.invalidateQueries({ queryKey: ['user-profile'] });
      toast.success('Адрес электронной почты успешно обновлен!');
      setNewEmail('');
      setCode('');
      setCodeSent(false);
      resetCooldown();
      try {
        localStorage.removeItem(EMAIL_SENT_KEY);
        localStorage.removeItem(EMAIL_PENDING_KEY);
      } catch {
        // ignore
      }
    } catch (err) {
      if (err instanceof ProfileApiError) {
        setError(err.message);
      } else {
        setError('Неверный код подтверждения или ошибка сервера');
      }
    } finally {
      setIsConfirming(false);
    }
  };

  return (
    <form onSubmit={handleConfirm} className="bg-card flex flex-col gap-4 p-6 md:p-8">
      <div className="flex flex-col gap-1">
        <h3 className="text-xl font-bold text-foreground">Электронная почта</h3>
        <p className="text-sm text-foreground-muted">
          Текущая почта:{' '}
          <span className="font-medium text-foreground">
            {currentEmail ?? 'Не указана'}
          </span>
          . Для подтверждения нового адреса потребуется одноразовый код.
        </p>
      </div>

      <div className="flex flex-col gap-3">
        <div className="flex flex-col gap-2">
          <label htmlFor="change-email-input" className="text-sm font-medium text-foreground">
            Новый адрес электронной почты
          </label>
          <div className="flex flex-col gap-2 sm:flex-row">
            <Input
              id="change-email-input"
              type="email"
              value={newEmail}
              onChange={(e) => {
                setNewEmail(e.target.value);
                if (error) setError(null);
              }}
              placeholder="example@mail.ru"
              disabled={isSendingCode || isConfirming}
              isError={Boolean(error && !codeSent)}
            />
            <Button
              type="button"
              variant="secondary"
              onClick={() => void handleSendCode()}
              disabled={isSendingCode || isCooldownActive || !newEmail.trim()}
              className="shrink-0"
            >
              {isCooldownActive
                ? `Повтор (${countdown} с)`
                : isSendingCode
                  ? 'Отправка...'
                  : codeSent
                    ? 'Отправить повторно'
                    : 'Получить код'}
            </Button>
          </div>
          {error && !codeSent && (
            <span className="text-error text-xs font-medium">
              {error}
            </span>
          )}
        </div>

        {codeSent && (
          <div className="flex flex-col gap-2 pt-2">
            <label htmlFor="change-email-code-input" className="text-sm font-medium text-foreground">
              Одноразовый 6-значный код
            </label>
            <div className="flex flex-col gap-2 sm:flex-row">
              <Input
                id="change-email-code-input"
                type="text"
                inputMode="numeric"
                maxLength={6}
                value={code}
                onChange={(e) => {
                  setCode(e.target.value.replace(/\D/g, ''));
                  if (error) setError(null);
                }}
                placeholder="123456"
                disabled={isConfirming}
                isError={Boolean(error && codeSent)}
                autoComplete="one-time-code"
              />
              <Button
                type="submit"
                variant="primary"
                disabled={isConfirming || code.trim().length !== 6}
                className="shrink-0"
              >
                {isConfirming ? 'Проверка...' : 'Подтвердить смену'}
              </Button>
            </div>
            {error && codeSent && (
              <span className="text-error text-xs font-medium">
                {error}
              </span>
            )}
          </div>
        )}
      </div>
    </form>
  );
}
