import { Button, Dialog } from '@/components/ui';
import type { AdminSeason } from '@/types';
import { seasonStatusLabels, seasonStatusTones, formatDateTime } from '../admin-utils';
import { StatusBadge } from './status-badge';

export interface SeasonDetailsDialogProps {
  season: AdminSeason;
  onClose: () => void;
}

export function SeasonDetailsDialog({ season, onClose }: SeasonDetailsDialogProps) {
  return (
    <Dialog
      isOpen
      title={season.title}
      description={`Код: ${season.code}`}
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
              {formatDateTime(season.startsAt)}
            </dd>
          </div>
          <div className="flex flex-col gap-1">
            <dt className="text-foreground-muted text-xs tracking-wider uppercase">Организатор</dt>
            <dd className="text-foreground text-sm font-medium">{season.organizer}</dd>
          </div>
          <div className="flex flex-col gap-1">
            <dt className="text-foreground-muted text-xs tracking-wider uppercase">Статус</dt>
            <dd>
              <StatusBadge
                label={seasonStatusLabels[season.status]}
                tone={seasonStatusTones[season.status]}
              />
            </dd>
          </div>
        </dl>

        <div className="flex flex-col gap-2">
          <span className="text-foreground-muted text-xs tracking-wider uppercase">Описание</span>
          <p className="border-border bg-surface text-foreground-muted rounded-none border p-4 text-sm leading-relaxed whitespace-pre-line">
            {season.description}
          </p>
        </div>
      </div>
    </Dialog>
  );
}
