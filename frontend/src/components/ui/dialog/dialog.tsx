import { useEffect, useId, type ReactNode } from 'react';
import { createPortal } from 'react-dom';
import { CloseIcon } from '@/components/ui/icons';
import { cn } from '@/lib';

export interface DialogProps {
  isOpen: boolean;
  title: string;
  description?: string;
  onClose: () => void;
  children: ReactNode;
  footer?: ReactNode;
  className?: string;
}

export function Dialog({
  isOpen,
  title,
  description,
  onClose,
  children,
  footer,
  className,
}: DialogProps) {
  const titleId = useId();
  const descriptionId = useId();

  useEffect(() => {
    if (!isOpen) return;

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        onClose();
      }
    };

    const previousOverflow = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    document.addEventListener('keydown', handleKeyDown);

    return () => {
      document.body.style.overflow = previousOverflow;
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  return createPortal(
    <div
      className="bg-background/80 fixed inset-0 z-50 flex items-center justify-center overflow-y-auto p-4 backdrop-blur-sm"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) {
          onClose();
        }
      }}
    >
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        aria-describedby={description ? descriptionId : undefined}
        className={cn(
          'bg-card border-border my-auto flex w-full max-w-xl flex-col rounded-none border shadow-2xl',
          className
        )}
      >
        <div className="border-border flex items-start justify-between gap-4 border-b p-6">
          <div className="flex flex-col gap-1">
            <h2 id={titleId} className="text-foreground text-xl font-bold">
              {title}
            </h2>
            {description && (
              <p id={descriptionId} className="text-foreground-muted text-sm">
                {description}
              </p>
            )}
          </div>
          <button
            type="button"
            onClick={onClose}
            className="text-foreground-muted hover:text-foreground -mt-1 cursor-pointer rounded-none p-1 transition-colors"
            aria-label="Закрыть окно"
          >
            <CloseIcon className="size-5" />
          </button>
        </div>

        <div className="p-6">{children}</div>

        {footer && <div className="border-border border-t p-6">{footer}</div>}
      </div>
    </div>,
    document.body
  );
}
