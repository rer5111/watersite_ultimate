import type { BroadcastCategory } from './admin';

export interface UserNotification {
  id: number;
  subject: string;
  category: BroadcastCategory;
  message: string;
  sentAt: string;
  read: boolean;
}
