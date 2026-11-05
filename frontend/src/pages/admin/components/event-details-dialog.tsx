import { Button, Dialog } from '@/components/ui';
import type { AdminEvent } from '@/types';
import { eventStatusLabels, eventStatusTones, formatDateTime } from '../admin-utils';
import { StatusBadge } from './status-badge';

export interface EventDetailsDialogProps {
  event: AdminEvent;
  onClose: () => void;
}

export function EventDetailsDialog({ event, onClose }: EventDetailsDialogProps) {
  return (
    <Dialog
      isOpen
      title={event.title}
      description={`Код: ${event.code}`}
      onClose={onClose}
      footer={
        <div className="flex justify-end">
          <Button type="button" variant="secondary" className="rounded-none" onClick={onClose}>
            Закрыть
          </Button>
        </div>
      }
    >
      <div className="flex flex-col gap-5">
        <dl className="grid gap-4 sm:grid-cols-2">
          <div className="flex flex-col gap-1">
            <dt className="text-foreground-muted text-xs tracking-wider uppercase">Дата и время</dt>
            <dd className="text-foreground text-sm font-medium">
              {formatDateTime(event.startsAt)}
            </dd>
          </div>
          <div className="flex flex-col gap-1">
            <dt className="text-foreground-muted text-xs tracking-wider uppercase">Организатор</dt>
            <dd className="text-foreground text-sm font-medium">{event.organizer}</dd>
          </div>
          <div className="flex flex-col gap-1">
            <dt className="text-foreground-muted text-xs tracking-wider uppercase">Статус</dt>
            <dd>
              <StatusBadge
                label={eventStatusLabels[event.status]}
                tone={eventStatusTones[event.status]}
              />
            </dd>
          </div>
        </dl>

        <div className="flex flex-col gap-2">
          <span className="text-foreground-muted text-xs tracking-wider uppercase">Описание</span>
          <p className="border-border bg-surface text-foreground-muted rounded-none border p-4 text-sm leading-relaxed whitespace-pre-line">
            {event.description}
          </p>
        </div>
      </div>
    </Dialog>
  );
}
