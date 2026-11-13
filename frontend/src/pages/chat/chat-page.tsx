import { useChat } from '@/hooks';
import { useAuth } from '@/context';
import { ChatWindowSection, ChatInputSection } from './sections';

export function ChatPage() {
  const { messages, status, isConnecting, isSending, sendError, send } = useChat();
  const { user } = useAuth();

  return (
    <section className="mx-auto flex w-full max-w-4xl flex-col gap-8 px-4 py-16 sm:px-8">
      <div className="flex flex-col gap-2 text-center">
        <h1 className="text-4xl leading-tight font-bold">Игровой чат</h1>
        <p className="text-foreground-muted">
          Общайтесь с игроками сервера в реальном времени через сайт и Discord
        </p>
      </div>

      <div className="border-border overflow-hidden border">
        <ChatWindowSection
          messages={messages}
          currentUsername={user?.username ?? null}
          isConnecting={isConnecting}
        />
        <ChatInputSection
          status={status}
          isSending={isSending}
          sendError={sendError}
          isAuthenticated={user !== null}
          onSend={send}
        />
      </div>
    </section>
  );
}
