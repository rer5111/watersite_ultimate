import { useEffect, useRef } from 'react';
import type { ChatMessage } from '@/api/chat';
import { ChatMessageItem } from '../components';

export type ChatWindowSectionProps = {
  messages: ChatMessage[];
  currentUsername: string | null;
  isConnecting: boolean;
};

export function ChatWindowSection({
  messages,
  currentUsername,
  isConnecting,
}: ChatWindowSectionProps) {
  const bottomRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  return (
    <div
      role="list"
      aria-label="Сообщения чата"
      aria-live="polite"
      aria-busy={isConnecting}
      className="bg-surface flex h-[480px] flex-col gap-4 overflow-y-auto p-4"
    >
      {isConnecting && (
        <p className="text-foreground-subtle m-auto text-sm">Подключение к чату…</p>
      )}

      {!isConnecting && messages.length === 0 && (
        <p className="text-foreground-subtle m-auto text-sm">
          Сообщений пока нет. Будьте первым!
        </p>
      )}

      {messages.map((msg) => (
        <ChatMessageItem
          key={msg.id}
          message={msg}
          isOwn={currentUsername !== null && msg.user === currentUsername}
        />
      ))}

      <div ref={bottomRef} />
    </div>
  );
}
