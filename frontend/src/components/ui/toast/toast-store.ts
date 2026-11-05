import type { ToastItem, ToastType } from './toast.types';

type ToastListener = (toasts: ToastItem[]) => void;

class ToastStore {
  private toasts: ToastItem[] = [];
  private listeners = new Set<ToastListener>();

  getToasts(): ToastItem[] {
    return this.toasts;
  }

  subscribe(listener: ToastListener): () => void {
    this.listeners.add(listener);
    listener(this.toasts);
    return () => {
      this.listeners.delete(listener);
    };
  }

  private notify(): void {
    const copy = [...this.toasts];
    for (const listener of this.listeners) {
      listener(copy);
    }
  }

  dismiss(id: string): void {
    this.toasts = this.toasts.filter((t) => t.id !== id);
    this.notify();
  }

  show(message: string, type: ToastType = 'info'): void {
    const id = `${Date.now()}-${Math.random().toString(36).substring(2, 9)}`;
    this.toasts = [...this.toasts.slice(-4), { id, type, message }];
    this.notify();

    window.setTimeout(() => {
      this.dismiss(id);
    }, 4000);
  }
}

export const toastStore = new ToastStore();

export const toast = {
  success: (message: string) => toastStore.show(message, 'success'),
  error: (message: string) => toastStore.show(message, 'error'),
  info: (message: string) => toastStore.show(message, 'info'),
  dismiss: (id: string) => toastStore.dismiss(id),
};
