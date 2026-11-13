import { useEvents } from '@/api';
import { Button } from '@/components/ui';
import { useEventFilterParams } from '@/hooks';
import { EventCard, EventsAside } from './components';

export function EventsPage() {
  const { seasons, organizers, statuses, minDate, maxDate } = useEventFilterParams();
  const { data, isLoading, isError, refetch } = useEvents({
    seasons: seasons.length ? seasons : undefined,
    organizers: organizers.length ? organizers : undefined,
    statuses: statuses.length ? statuses : undefined,
    minDate,
    maxDate,
  });

  const events = data?.content ?? [];

  return (
    <div className="mx-auto flex w-full max-w-[100rem] flex-col lg:flex-row">
      <EventsAside />
      <main className="min-w-0 flex-1 p-6 md:p-8">
        <h1 className="py-4 text-3xl font-bold">Ивенты</h1>

        {isLoading && null}

        {isError && (
          <div className="flex flex-col items-start gap-4 py-8">
            <p className="text-error font-medium">Не удалось загрузить список ивентов.</p>
            <Button variant="secondary" onClick={() => void refetch()}>
              Повторить попытку
            </Button>
          </div>
        )}

        {!isLoading && !isError && events.length === 0 && (
          <p className="text-foreground-muted py-8 text-left">Ивенты не найдены.</p>
        )}

        {!isLoading && !isError && events.length > 0 && (
          <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
            {events.map((event) => (
              <EventCard key={event.id} {...event} />
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
