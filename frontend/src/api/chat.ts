import { parseApiError } from './error-helper';

export type ChatMessageOrigin = 'WEB' | 'DISCORD' | 'MINECRAFT';

export type ChatMessage = {
  id: number;
  user: string;
  message: string;
  origin: ChatMessageOrigin;
  sentAt: string;
};

export type ChatStatus = {
  discordConfigured: boolean;
  discordConnected: boolean;
};

function isChatMessage(value: unknown): value is ChatMessage {
  if (typeof value !== 'object' || value === null) return false;
  const v = value as Record<string, unknown>;
  return (
    typeof v.id === 'number' &&
    typeof v.user === 'string' &&
    typeof v.message === 'string' &&
    typeof v.origin === 'string' &&
    typeof v.sentAt === 'string'
  );
}

function getBase(): string {
  return (import.meta.env.VITE_API_URL ?? '').replace(/\/+$/, '');
}

export async function getChatMessages(afterId: number): Promise<ChatMessage[]> {
  const response = await fetch(
    `${getBase()}/api/chat-service/api/v1/chat/messages?afterId=${afterId}`
  );

  if (!response.ok) {
    const message = await parseApiError(response, 'Не удалось загрузить сообщения');
    throw new Error(message);
  }

  const payload: unknown = await response.json();
  if (!Array.isArray(payload) || !payload.every(isChatMessage)) {
    throw new Error('API вернул некорректный список сообщений');
  }

  return payload;
}

export async function getChatStatus(): Promise<ChatStatus> {
  const response = await fetch(`${getBase()}/api/chat-service/api/v1/chat/status`);

  if (!response.ok) {
    const message = await parseApiError(response, 'Не удалось получить статус чата');
    throw new Error(message);
  }

  const payload: unknown = await response.json();
  if (
    typeof payload !== 'object' ||
    payload === null ||
    typeof (payload as Record<string, unknown>).discordConfigured !== 'boolean' ||
    typeof (payload as Record<string, unknown>).discordConnected !== 'boolean'
  ) {
    throw new Error('API вернул некорректный статус чата');
  }

  return payload as ChatStatus;
}

export async function sendChatMessage(message: string): Promise<void> {
  const accessToken = localStorage.getItem('accessToken');

  const response = await fetch(`${getBase()}/api/chat-service/api/v1/chat/messages`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
    },
    body: JSON.stringify({ message }),
  });

  if (!response.ok) {
    const message = await parseApiError(response, 'Не удалось отправить сообщение');
    throw new Error(message);
  }
}
