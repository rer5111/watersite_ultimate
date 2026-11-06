import { Button, Input } from '@/components/ui';
import { ChevronRightIcon } from '@/components/ui';
import type { ChatStatus } from '@/api/chat';
import { useRef, useState } from 'react';

export type ChatInputSectionProps = {
  status: ChatStatus | null;
  isSending: boolean;
  sendError: string | null;
  isAuthenticated: boolean;
  onSend: (message: string) => Promise<void>;
};

export function ChatInputSection({
  status,
  isSending,
  sendError,
  isAuthenticated,
  onSend,
}: ChatInputSectionProps) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [localError, setLocalError] = useState<string | null>(null);

  const isDisconnected = status !== null && (!status.discordConfigured || !status.discordConnected);

  async function handleSubmit(event: React.SubmitEvent<HTMLFormElement>) {
    event.preventDefault();
    setLocalError(null);

    const input = inputRef.current;
    if (!input) return;

    const message = input.value.trim();
    if (!message) return;

    if (message.length > 500) {
      setLocalError('Сообщение не может быть длиннее 500 символов');
      return;
    }

    try {
      await onSend(message);
      input.value = '';
    } catch {
      // sendError is set in the hook; no extra handling needed here
    }
  }

  const displayError = localError ?? sendError;

  if (!isAuthenticated) {
    return (
      <div className="bg-surface border-border border-t px-4 py-3">
        <p className="text-foreground-subtle text-center text-sm">
          Войдите в аккаунт, чтобы писать в чат
        </p>
      </div>
    );
  }

  return (
    <div className="bg-surface border-border border-t">
      {isDisconnected && (
        <p className="text-foreground-subtle px-4 py-2 text-center text-xs">
          Discord-бот недоступен — сообщения временно не принимаются
        </p>
      )}

      {displayError && (
        <p role="alert" className="text-error px-4 pt-2 text-xs">
          {displayError}
        </p>
      )}

      <form
        onSubmit={(e) => void handleSubmit(e)}
        className="flex items-stretch"
        aria-label="Форма отправки сообщения"
        autoComplete="off"
      >
        <Input
          ref={inputRef}
          name="message"
          placeholder="Написать в чат…"
          maxLength={500}
          disabled={isSending || isDisconnected}
          isError={Boolean(displayError)}
          aria-label="Текст сообщения"
          className="flex-1 rounded-none border-0 focus-visible:ring-inset"
          autoComplete="off"
        />
        <Button
          type="submit"
          variant="primary"
          disabled={isSending || isDisconnected}
          trailingIcon={<ChevronRightIcon />}
          className="rounded-none"
        >
          {isSending ? 'Отправка…' : 'Отправить'}
        </Button>
      </form>
    </div>
  );
}
