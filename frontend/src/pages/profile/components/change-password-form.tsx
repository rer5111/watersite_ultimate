import { useState, type FormEvent } from 'react';
import { Button, Input, toast } from '@/components/ui';
import { confirmChangePassword, sendPasswordChangeCode, ProfileApiError } from '@/api';
import { useCooldown } from '@/hooks';

const COOLDOWN_KEY = 'waltercraft_change_password_cooldown_until';
const CODE_SENT_KEY = 'waltercraft_change_password_code_sent_until';

function getStoredCodeSent(): boolean {
  if (typeof window === 'undefined') return false;
  try {
    const stored = localStorage.getItem(CODE_SENT_KEY);
    if (!stored) return false;
    const until = parseInt(stored, 10);
    if (Number.isNaN(until) || until <= Date.now()) {
      localStorage.removeItem(CODE_SENT_KEY);
      return false;
    }
    return true;
  } catch {
    return false;
  }
}

export interface ChangePasswordFormProps {
  hasEmail: boolean;
  emailHint?: string | null;
}

export function ChangePasswordForm({ hasEmail, emailHint }: ChangePasswordFormProps) {
  const [code, setCode] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [codeSent, setCodeSent] = useState<boolean>(getStoredCodeSent);
  const [isSendingCode, setIsSendingCode] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const { countdown, startCooldown, resetCooldown, isCooldownActive } = useCooldown(COOLDOWN_KEY);

  const handleSendCode = async () => {
    if (!hasEmail) {
      setError('Для смены пароля привяжите адрес электронной почты в блоке выше');
      return;
    }

    setIsSendingCode(true);
    setError(null);

    try {
      const resp = await sendPasswordChangeCode();
      const cooldownSeconds = resp.cooldownSeconds || 60;
      const expiresInSeconds = resp.expiresInSeconds || 600;

      startCooldown(cooldownSeconds);
      try {
        localStorage.setItem(CODE_SENT_KEY, String(Date.now() + expiresInSeconds * 1000));
      } catch {
        // ignore
      }
      setCodeSent(true);
      toast.info(`Код подтверждения отправлен на почту (${emailHint ?? 'привязанную к аккаунту'})`);
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

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();

    const trimmedCode = code.trim();
    if (!trimmedCode || trimmedCode.length !== 6) {
      setError('Введите 6-значный код подтверждения');
      return;
    }

    if (!newPassword || newPassword.length < 8) {
      setError('Новый пароль должен содержать не менее 8 символов');
      return;
    }

    if (newPassword !== confirmPassword) {
      setError('Пароли не совпадают');
      return;
    }

    setIsSubmitting(true);
    setError(null);

    try {
      await confirmChangePassword(trimmedCode, newPassword);
      toast.success('Пароль успешно изменен!');
      setCode('');
      setNewPassword('');
      setConfirmPassword('');
      setCodeSent(false);
      resetCooldown();
      try {
        localStorage.removeItem(CODE_SENT_KEY);
      } catch {
        // ignore
      }
    } catch (err) {
      if (err instanceof ProfileApiError) {
        setError(err.message);
      } else {
        setError('Неверный код подтверждения или ошибка смены пароля');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="bg-card flex flex-col gap-4 p-6 md:p-8">
      <div className="flex flex-col gap-1">
        <h3 className="text-xl font-bold text-foreground">Безопасность и пароль</h3>
        <p className="text-sm text-foreground-muted">
          Смена пароля подтверждается одноразовым кодом, отправленным на вашу привязанную почту.
        </p>
      </div>

      {!hasEmail && (
        <div className="bg-surface border border-border p-3 text-sm text-foreground-muted">
          К вашему аккаунту не привязана электронная почта. Сначала укажите email выше, чтобы менять пароль.
        </div>
      )}

      <div className="flex flex-col gap-4">
        <div className="flex flex-col gap-2">
          <label htmlFor="change-pass-new-input" className="text-sm font-medium text-foreground">
            Новый пароль
          </label>
          <Input
            id="change-pass-new-input"
            type="password"
            value={newPassword}
            onChange={(e) => {
              setNewPassword(e.target.value);
              if (error) setError(null);
            }}
            placeholder="Не менее 8 символов"
            disabled={!hasEmail || isSubmitting}
            isError={Boolean(error && newPassword.length > 0 && newPassword.length < 8)}
            autoComplete="new-password"
          />
        </div>

        <div className="flex flex-col gap-2">
          <label htmlFor="change-pass-confirm-input" className="text-sm font-medium text-foreground">
            Повторите новый пароль
          </label>
          <Input
            id="change-pass-confirm-input"
            type="password"
            value={confirmPassword}
            onChange={(e) => {
              setConfirmPassword(e.target.value);
              if (error) setError(null);
            }}
            placeholder="Повторите пароль"
            disabled={!hasEmail || isSubmitting}
            isError={Boolean(error && confirmPassword.length > 0 && newPassword !== confirmPassword)}
            autoComplete="new-password"
          />
        </div>

        <div className="flex flex-col gap-2">
          <label htmlFor="change-pass-code-input" className="text-sm font-medium text-foreground">
            Код подтверждения
          </label>
          <div className="flex flex-col gap-2 sm:flex-row">
            <Input
              id="change-pass-code-input"
              type="text"
              inputMode="numeric"
              maxLength={6}
              value={code}
              onChange={(e) => {
                setCode(e.target.value.replace(/\D/g, ''));
                if (error) setError(null);
              }}
              placeholder="6-значный код из письма"
              disabled={!hasEmail || isSubmitting}
              isError={Boolean(error && code.length > 0 && code.length < 6)}
              autoComplete="one-time-code"
            />
            <Button
              type="button"
              variant="secondary"
              onClick={() => void handleSendCode()}
              disabled={!hasEmail || isSendingCode || isCooldownActive}
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
          {codeSent && (
            <p className="text-xs text-foreground-muted">
              Код отправлен на почту{emailHint ? <> (<span className="text-foreground font-medium">{emailHint}</span>)</> : ''}. Срок действия: 10 минут.
            </p>
          )}
        </div>

        {error && (
          <span className="text-error text-xs font-medium">
            {error}
          </span>
        )}

        <div className="flex justify-end pt-2">
          <Button
            type="submit"
            variant="primary"
            disabled={!hasEmail || isSubmitting || code.length !== 6 || newPassword.length < 8}
            className="w-full sm:w-auto"
          >
            {isSubmitting ? 'Сохранение...' : 'Сменить пароль'}
          </Button>
        </div>
      </div>
    </form>
  );
}
