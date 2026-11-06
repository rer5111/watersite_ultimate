import type { ChatMessage } from '@/api/chat';
import { SkinAvatar } from '@/components/ui';
import { OriginBadge } from './origin-badge';
import { cn } from '@/lib';

export type ChatMessageItemProps = {
  message: ChatMessage;
  isOwn: boolean;
};

function formatTime(sentAt: string): string {
  try {
    return new Date(sentAt).toLocaleTimeString('ru-RU', {
      hour: '2-digit',
      minute: '2-digit',
    });
  } catch {
    return '';
  }
}

export function ChatMessageItem({ message, isOwn }: ChatMessageItemProps) {
  return (
    <div className={cn('flex items-start gap-3', isOwn && 'flex-row-reverse')} role="listitem">
      <SkinAvatar name={message.user} mode="face" className="mt-0.5 size-8 shrink-0 rounded" />

      <div className={cn('flex max-w-[75%] flex-col gap-1', isOwn && 'items-end')}>
        <div className={cn('flex items-center gap-2', isOwn && 'flex-row-reverse')}>
          <span className="text-foreground text-sm font-semibold">{message.user}</span>
          <OriginBadge origin={message.origin} />
          <span className="text-foreground-subtle text-xs">{formatTime(message.sentAt)}</span>
        </div>

        <p
          className={cn(
            'bg-card text-foreground w-fit px-3 py-2 text-sm leading-relaxed wrap-break-word',
            isOwn && 'bg-primary/40'
          )}
        >
          {message.message}
        </p>
      </div>
    </div>
  );
}
