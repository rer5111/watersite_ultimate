import type { EventStatus, EventSummary } from '@/api';
import { cn } from '@/lib';
import { Link } from 'react-router';
import { paths } from '@/routes';

const statusConfig: Record<EventStatus, { label: string; textClass: string }> = {
  IN_PROGRESS: {
    label: 'В процессе',
    textClass: 'text-primary',
  },
  PLANNED: {
    label: 'Планируется',
    textClass: 'text-foreground-muted',
  },
  COMPLETED: {
    label: 'Завершен',
    textClass: 'text-foreground-subtle',
  },
  CANCELLED: {
    label: 'Отменен',
    textClass: 'text-error',
  },
};

export type EventCardProps = EventSummary;

export function EventCard({
  id,
  code,
  image,
  title,
  seasonTitle,
  seasonNumber,
  organizer,
  dateRangeFormatted,
  status,
  shortDescription,
}: EventCardProps) {
  const currentStatus = statusConfig[status] ?? {
    label: status,
    textClass: 'text-foreground-muted',
  };

  const seasonLabel = seasonTitle || `${seasonNumber} сезон`;
  const eventHref = paths.eventDetail.replace(':idOrCode', code || String(id));

  return (
    <Link
      to={eventHref}
      className="focus-visible:ring-foreground block h-full focus-visible:ring-2 focus-visible:outline-none"
    >
      <article className="bg-card hover:bg-card/80 flex h-full flex-col text-left transition-colors">
        <div className="bg-surface aspect-16/9 w-full shrink-0 overflow-hidden">
          <img
            src={image || '/images/events/placeholder.webp'}
            alt={title}
            loading="eager"
            decoding="async"
            className="h-full w-full object-cover text-[0px] text-transparent"
          />
        </div>

        <div className="flex flex-1 flex-col p-6">
          <div className="flex flex-col gap-2">
            <div className="flex items-center justify-between gap-3 text-xs">
              <span className={cn('font-medium', currentStatus.textClass)}>
                {currentStatus.label}
              </span>
              <span className="text-foreground-subtle font-medium tabular-nums">{seasonLabel}</span>
            </div>

            <h3 className="text-foreground text-xl leading-snug font-bold">{title}</h3>

            {shortDescription && (
              <p className="text-foreground-muted line-clamp-2 text-sm leading-relaxed">
                {shortDescription}
              </p>
            )}
          </div>

          <div className="border-border/40 mt-4 flex flex-col gap-1.5 border-t pt-4 text-sm">
            <p className="text-foreground-muted">
              <span className="text-foreground-subtle">Организатор:</span>{' '}
              <span className="text-foreground font-medium">{organizer}</span>
            </p>
            {dateRangeFormatted && (
              <p className="text-foreground-muted">
                <span className="text-foreground-subtle">Даты:</span>{' '}
                <span className="text-foreground font-medium">{dateRangeFormatted}</span>
              </p>
            )}
          </div>
        </div>
      </article>
    </Link>
  );
}
