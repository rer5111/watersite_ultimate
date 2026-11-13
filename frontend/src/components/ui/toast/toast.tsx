import type { ReactNode } from 'react';
import { CheckIcon, CloseIcon, InfoCircleIcon } from '@/components/ui';
import { useToasts } from './use-toast';
import { toast } from './toast-store';

export function ToastProvider({ children }: { children: ReactNode }) {
  const toasts = useToasts();

  return (
    <>
      {children}
      <div
        role="region"
        aria-label="Уведомления"
        className="pointer-events-none fixed right-4 bottom-4 z-50 flex w-full max-w-sm flex-col gap-2 sm:right-6 sm:bottom-6"
      >
        {toasts.map((item) => (
          <div
            key={item.id}
            role={item.type === 'error' ? 'alert' : 'status'}
            className="bg-surface border-border pointer-events-auto flex items-center justify-between gap-3 border p-4 shadow-2xl transition-all duration-200"
          >
            <div className="flex items-center gap-3">
              {item.type === 'success' && <CheckIcon className="text-primary size-5 shrink-0" />}
              {item.type === 'error' && <InfoCircleIcon className="text-error size-5 shrink-0" />}
              {item.type === 'info' && (
                <InfoCircleIcon className="text-foreground-muted size-5 shrink-0" />
              )}
              <span className="text-foreground text-sm font-medium">{item.message}</span>
            </div>
            <button
              type="button"
              onClick={() => toast.dismiss(item.id)}
              className="text-foreground-muted hover:text-foreground cursor-pointer transition-colors"
              aria-label="Закрыть уведомление"
            >
              <CloseIcon className="size-4 shrink-0" />
            </button>
          </div>
        ))}
      </div>
    </>
  );
}
