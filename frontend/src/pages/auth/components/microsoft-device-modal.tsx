import { useEffect, useRef, useState } from 'react';
import { Button } from '@/components';
import { getMicrosoftDeviceCode, pollMicrosoftDeviceLogin } from '@/api';
import { useAuth } from '@/context';
import type { DeviceCodeResponse } from '@/types';

interface MicrosoftDeviceModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

export function MicrosoftDeviceModal({ isOpen, onClose, onSuccess }: MicrosoftDeviceModalProps) {
  const { login } = useAuth();
  const [data, setData] = useState<DeviceCodeResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [copied, setCopied] = useState(false);
  const [retryCount, setRetryCount] = useState(0);
  const pollIntervalRef = useRef<number | null>(null);

  const stopPolling = () => {
    if (pollIntervalRef.current !== null) {
      window.clearInterval(pollIntervalRef.current);
      pollIntervalRef.current = null;
    }
  };

  useEffect(() => {
    if (!isOpen) {
      stopPolling();
      return;
    }

    let isMounted = true;
    stopPolling();

    getMicrosoftDeviceCode()
      .then((codeData) => {
        if (!isMounted) return;
        setData(codeData);

        const intervalMs = Math.max((codeData.interval || 5) * 1000, 3000);
        pollIntervalRef.current = window.setInterval(async () => {
          try {
            const res = await pollMicrosoftDeviceLogin(codeData.deviceCode);
            if (res.status === 'SUCCESS' && isMounted) {
              stopPolling();
              if (res.tokens) {
                login(res.tokens);
              }
              onSuccess();
            }
          } catch (err: unknown) {
            if (!isMounted) return;
            stopPolling();
            const msg = err instanceof Error ? err.message : 'Ошибка при проверке статуса';
            setError(msg);
          }
        }, intervalMs);
      })
      .catch((err: unknown) => {
        if (!isMounted) return;
        const msg = err instanceof Error ? err.message : 'Не удалось получить код от Microsoft';
        setError(msg);
      });

    return () => {
      isMounted = false;
      stopPolling();
    };
  }, [isOpen, onSuccess, retryCount, login]);

  if (!isOpen) {
    return null;
  }

  const isLoading = data === null && !error;

  const handleCopy = async () => {
    if (!data?.userCode) return;
    try {
      await navigator.clipboard.writeText(data.userCode);
      setCopied(true);
      window.setTimeout(() => setCopied(false), 2000);
    } catch {
      // fallback
    }
  };

  const handleOpenLink = () => {
    if (!data?.verificationUri) return;
    window.open(data.verificationUri, '_blank', 'noopener,noreferrer');
  };

  const handleRetry = () => {
    setData(null);
    setError(null);
    setCopied(false);
    setRetryCount((prev) => prev + 1);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 p-4 backdrop-blur-sm">
      <div className="bg-background w-full max-w-lg p-6 shadow-2xl">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-xl font-bold">Лицензия Minecraft</h2>
          <button
            type="button"
            onClick={onClose}
            className="text-foreground-muted hover:text-foreground cursor-pointer transition-colors"
            aria-label="Закрыть"
          >
            <svg
              viewBox="0 0 24 24"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
              className="size-6"
            >
              <path d="M17 7L7 17M7 7L17 17" stroke="currentColor" stroke-width="2" />
            </svg>
          </button>
        </div>

        {isLoading && (
          <div className="flex flex-col items-center justify-center py-8 text-center">
            <div className="border-primary size-8 animate-spin rounded-full border-2 border-t-transparent" />
            <p className="text-foreground-muted mt-3 text-sm">Получаем код от Microsoft...</p>
          </div>
        )}

        {error && (
          <div className="flex flex-col gap-3 py-2">
            <div className="p-3 text-sm text-red-500">{error}</div>
            <Button type="button" variant="secondary" onClick={handleRetry}>
              Попробовать снова
            </Button>
          </div>
        )}

        {!isLoading && !error && data && (
          <div className="flex flex-col gap-4">
            <p className="text-foreground-muted text-sm">
              Для входа скопируйте код авторизации ниже и вставьте на странице подтверждения
            </p>

            <div className="bg-foreground/5 flex flex-col items-center justify-center py-4">
              <span className="text-foreground-muted text-xs tracking-wider uppercase">
                Код авторизации
              </span>
              <span className="text-primary mt-1 font-mono text-3xl font-extrabold tracking-widest select-all">
                {data.userCode}
              </span>
            </div>

            <div className="grid grid-cols-2 gap-2">
              <Button
                type="button"
                variant="secondary"
                onClick={handleCopy}
                className="font-medium"
              >
                {copied ? 'Скопировано!' : 'Скопировать код'}
              </Button>
              <Button
                type="button"
                onClick={handleOpenLink}
                className="font-medium"
                trailingIcon={
                  <svg
                    viewBox="0 0 24 24"
                    fill="none"
                    xmlns="http://www.w3.org/2000/svg"
                    className="size-4"
                  >
                    <path d="M7 17L17 7M17 7H7M17 7V17" stroke="currentColor" stroke-width="2" />
                  </svg>
                }
              >
                Открыть страницу
              </Button>
            </div>

            <div className="bg-card flex items-center gap-3 p-3">
              <div className="border-primary size-4 shrink-0 animate-spin rounded-full border-2 border-t-transparent" />
              <div className="flex flex-col gap-1">
                <span className="text-foreground text-xs font-semibold">Ожидаем подтверждения</span>
                <span className="text-foreground-muted text-xs">
                  После ввода кода на сайте Microsoft вход произойдёт автоматически
                </span>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
