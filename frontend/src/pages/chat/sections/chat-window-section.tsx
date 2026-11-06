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
  const scrollRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const el = scrollRef.current;
    if (!el) return;
    el.scrollTo({ top: el.scrollHeight, behavior: 'smooth' });
  }, [messages]);

  return (
    <div
      ref={scrollRef}
      role="list"
      aria-label="Сообщения чата"
      aria-live="polite"
      aria-busy={isConnecting}
      className="bg-surface flex h-100 flex-col gap-4 overflow-y-auto p-4"
    >
      {!isConnecting && messages.length === 0 && (
        <p className="text-foreground-subtle m-auto text-sm">Сообщений пока нет. Будьте первым!</p>
      )}

      {messages.map((msg) => (
        <ChatMessageItem
          key={msg.id}
          message={msg}
          isOwn={currentUsername !== null && msg.user === currentUsername}
        />
      ))}
    </div>
  );
}
