import { useEffect, useRef, useState, useCallback } from 'react';
import { getChatMessages, sendChatMessage, getChatStatus } from '@/api/chat';
import type { ChatMessage, ChatStatus } from '@/api/chat';

const POLL_INTERVAL_MS = 3000;

export type UseChatReturn = {
  messages: ChatMessage[];
  status: ChatStatus | null;
  isConnecting: boolean;
  isSending: boolean;
  sendError: string | null;
  send: (message: string) => Promise<void>;
};

export function useChat(): UseChatReturn {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [status, setStatus] = useState<ChatStatus | null>(null);
  const [isConnecting, setIsConnecting] = useState(true);
  const [isSending, setIsSending] = useState(false);
  const [sendError, setSendError] = useState<string | null>(null);

  const lastIdRef = useRef(0);

  const fetchMessages = useCallback(async () => {
    try {
      const [incoming, chatStatus] = await Promise.all([
        getChatMessages(lastIdRef.current),
        getChatStatus(),
      ]);

      if (incoming.length > 0) {
        const maxId = incoming[incoming.length - 1].id;
        lastIdRef.current = maxId;
        setMessages((prev) => [...prev, ...incoming]);
      }

      setStatus(chatStatus);
    } catch {
      // silent — network hiccup, retry on next tick
    } finally {
      setIsConnecting(false);
    }
  }, []);

  useEffect(() => {
    void fetchMessages();
    const timer = setInterval(() => void fetchMessages(), POLL_INTERVAL_MS);
    return () => clearInterval(timer);
  }, [fetchMessages]);

  const send = useCallback(
    async (message: string) => {
      setSendError(null);
      setIsSending(true);
      try {
        await sendChatMessage(message);
      } catch (err) {
        setSendError(err instanceof Error ? err.message : 'Не удалось отправить сообщение');
        throw err;
      } finally {
        setIsSending(false);
        // Poll immediately to pick up the sent message
        void fetchMessages();
      }
    },
    [fetchMessages]
  );

  return { messages, status, isConnecting, isSending, sendError, send };
}
