import { Button, Dialog } from '@/components/ui';
import type { AdminEvent } from '@/types';
import { eventStatusLabels, eventStatusTones } from '../admin-utils';
import { StatusBadge } from './status-badge';

export interface EventDetailsDialogProps {
  event: AdminEvent;
  onClose: () => void;
}

export function EventDetailsDialog({ event, onClose }: EventDetailsDialogProps) {
  const dates =
    event.dateRangeFormatted ||
    (event.startDate ? `${event.startDate} — ${event.endDate || 'н. в.'}` : 'Не указаны');

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
            <dt className="text-foreground-muted text-xs tracking-wider uppercase">Сезон</dt>
            <dd className="text-foreground text-sm font-medium">
              {event.seasonTitle || `${event.seasonNumber} сезон`}
            </dd>
          </div>
          <div className="flex flex-col gap-1">
            <dt className="text-foreground-muted text-xs tracking-wider uppercase">Организатор</dt>
            <dd className="text-foreground text-sm font-medium">{event.organizer}</dd>
          </div>
          <div className="flex flex-col gap-1">
            <dt className="text-foreground-muted text-xs tracking-wider uppercase">
              Даты проведения
            </dt>
            <dd className="text-foreground text-sm font-medium tabular-nums">{dates}</dd>
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

        {event.shortDescription && (
          <div className="flex flex-col gap-1">
            <span className="text-foreground-muted text-xs tracking-wider uppercase">
              Краткое описание
            </span>
            <p className="text-foreground text-sm leading-relaxed">{event.shortDescription}</p>
          </div>
        )}

        {event.description && (
          <div className="flex flex-col gap-2">
            <span className="text-foreground-muted text-xs tracking-wider uppercase">
              Полное описание
            </span>
            <p className="border-border bg-surface text-foreground-muted rounded-none border p-4 text-sm leading-relaxed whitespace-pre-line">
              {event.description}
            </p>
          </div>
        )}
      </div>
    </Dialog>
  );
}
