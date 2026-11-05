import { useSyncExternalStore } from 'react';
import { toast, toastStore } from './toast-store';
import type { ToastItem } from './toast.types';

export function useToasts(): ToastItem[] {
  return useSyncExternalStore(
    (onStoreChange) => toastStore.subscribe(onStoreChange),
    () => toastStore.getToasts()
  );
}

export function useToast() {
  return toast;
}
