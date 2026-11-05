import { Button, Dialog } from '@/components/ui';

export interface ConfirmDialogProps {
  isOpen: boolean;
  title: string;
  description: string;
  confirmLabel: string;
  isPending?: boolean;
  error?: string | null;
  destructive?: boolean;
  onConfirm: () => void;
  onClose: () => void;
}

export function ConfirmDialog({
  isOpen,
  title,
  description,
  confirmLabel,
  isPending = false,
  error,
  destructive = false,
  onConfirm,
  onClose,
}: ConfirmDialogProps) {
  return (
    <Dialog
      isOpen={isOpen}
      title={title}
      onClose={onClose}
      footer={
        <div className="flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
          <Button
            type="button"
            variant="secondary"
            className="rounded-none"
            onClick={onClose}
            disabled={isPending}
          >
            Отмена
          </Button>
          <Button
            type="button"
            variant="primary"
            className={destructive ? 'bg-error hover:bg-error-hover rounded-none' : 'rounded-none'}
            onClick={onConfirm}
            disabled={isPending}
          >
            {isPending ? 'Выполнение…' : confirmLabel}
          </Button>
        </div>
      }
    >
      <p className="text-foreground-muted text-sm leading-relaxed">{description}</p>
      {error && <p className="text-error mt-4 text-sm font-medium">{error}</p>}
    </Dialog>
  );
}
