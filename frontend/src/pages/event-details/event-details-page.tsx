import { useEvent } from '@/api';
import { Button } from '@/components';
import { useParams } from 'react-router';

export function EventDetailsPage() {
  const { idOrCode } = useParams<{ idOrCode: string }>();
  const { data: event, isLoading, isError, refetch } = useEvent(idOrCode);

  if (isLoading) return null;

  if (isError || !event) {
    return (
      <div className="flex flex-col items-start gap-4 p-8">
        <p className="font-medium">Не удалось загрузить ивент.</p>
        <Button variant="secondary" onClick={() => void refetch()}>
          Повторить
        </Button>
      </div>
    );
  }
  return (
    <article className="mx-auto max-w-4xl px-12 py-16">
      <div className="mx-auto flex flex-col items-center gap-2">
        <div className="text-foreground-muted flex flex-col items-center gap-1">
          <p className="font-medium">{event.dateRangeFormatted}</p>
          <div className="flex gap-2">
            <p className="font-medium">Организатор: {event.organizer}</p>
            <span className="font-medium">•</span>
            <p className="font-medium">{event.seasonTitle}</p>
          </div>
        </div>

        <h1 className="py-2 text-3xl font-bold">{event.title}</h1>
      </div>

      <img src={event.image} alt={event.title} className="aspect-video w-full text-transparent" />

      <h2 className="py-3 text-2xl font-bold">Описание</h2>
      <p>{event.description}</p>
    </article>
  );
}
