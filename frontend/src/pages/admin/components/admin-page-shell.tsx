import type { ReactNode } from 'react';
import { Button } from '@/components/ui';

export interface AdminPageHeaderProps {
  title: string;
  description: string;
  actions?: ReactNode;
}

export function AdminPageHeader({ title, description, actions }: AdminPageHeaderProps) {
  return (
    <header className="border-border flex flex-col gap-4 border-b pb-6 sm:flex-row sm:items-end sm:justify-between">
      <div className="flex flex-col gap-2">
        <span className="text-primary text-xs font-semibold tracking-widest uppercase">
          Админ-панель
        </span>
        <h1 className="text-foreground text-3xl font-bold tracking-tight">{title}</h1>
        <p className="text-foreground-muted max-w-2xl text-sm">{description}</p>
      </div>
      {actions && <div className="flex flex-wrap items-center gap-3">{actions}</div>}
    </header>
  );
}

export interface AdminErrorStateProps {
  message: string;
  onRetry: () => void;
}

export function AdminErrorState({ message, onRetry }: AdminErrorStateProps) {
  return (
    <div
      role="alert"
      className="border-error/50 bg-card flex flex-col items-start gap-4 rounded-none border p-6"
    >
      <div className="flex flex-col gap-1">
        <h2 className="text-error text-base font-semibold">Не удалось загрузить данные</h2>
        <p className="text-foreground-muted text-sm">{message}</p>
      </div>
      <Button variant="secondary" className="rounded-none" onClick={onRetry}>
        Повторить попытку
      </Button>
    </div>
  );
}
