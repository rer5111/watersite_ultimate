import { Fragment } from 'react/jsx-runtime';
import { useEvents } from '@/api';
import { Button } from '@/components/ui';
import { SeasonCard, SeasonDivider } from '../components';

export function SeasonsSection() {
  const { data: events = [], isLoading, isError, refetch } = useEvents();

  return (
    <section className="mx-auto flex w-full max-w-7xl flex-col gap-10 px-32 py-16">
      <div className="flex flex-col items-center">
        <h2 className="py-4 text-center text-4xl leading-tight font-bold">Сезоны</h2>

        <p className="w-full max-w-160 py-4 text-center text-lg leading-relaxed font-medium text-pretty">
          На сервере действует система сезонов — каждый длится около 1–2 лет и заканчивается
          крупными событиями. Всего было 5 сезонов, каждый из которых — кусочек истории сервера.
        </p>
      </div>

      {isLoading && (
        <div className="flex flex-col gap-10 invisible" aria-hidden="true">
          <div className="h-60 w-full" />
          <div className="h-60 w-full" />
        </div>
      )}

      {isError && (
        <div className="flex flex-col items-center gap-4 py-8 text-center">
          <p className="text-foreground-muted font-medium">Не удалось загрузить события сервера.</p>
          <Button variant="secondary" onClick={() => void refetch()}>
            Повторить попытку
          </Button>
        </div>
      )}

      {!isLoading && !isError && events.length === 0 && (
        <p className="text-foreground-muted text-center py-8">Список событий пока пуст.</p>
      )}

      {!isLoading && !isError && events.length > 0 && (
        <div className="flex flex-col gap-10">
          {events.map((event, index) => (
            <Fragment key={event.id}>
              <SeasonCard
                title={event.title}
                description={event.description}
                startDate={event.startDate}
                endDate={event.endDate}
                image={event.image}
                imagePosition={index % 2 === 0 ? 'start' : 'end'}
              />

              {index !== events.length - 1 && <SeasonDivider isMirrored={index % 2 !== 0} />}
            </Fragment>
          ))}
        </div>
      )}
    </section>
  );
}
