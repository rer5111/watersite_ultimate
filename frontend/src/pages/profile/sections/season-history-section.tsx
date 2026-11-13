import { useMySeasonParticipation, type SeasonEntry, type SeasonPhase } from '@/api';
import { Button } from '@/components/ui';

const phaseLabels: Record<SeasonPhase, string> = {
  UPCOMING: 'Скоро начнётся',
  ACTIVE: 'Идёт сейчас',
  COMPLETED: 'Завершён',
  UNKNOWN: 'Даты не указаны',
};

function formatDate(value: string | null): string | null {
  if (!value) return null;
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return null;
  return new Intl.DateTimeFormat('ru-RU', {
    month: 'long',
    year: 'numeric',
    timeZone: 'UTC',
  }).format(date);
}

function seasonDates(season: SeasonEntry): string {
  const start = formatDate(season.startDate);
  const end = formatDate(season.endDate);
  if (start && end) return `${start} — ${end}`;
  return start ?? end ?? 'Даты уточняются';
}

export function SeasonHistorySection({ playerId }: { playerId: number }) {
  const { data, isLoading, isError, refetch } = useMySeasonParticipation(true, playerId);

  return (
    <section
      aria-labelledby="season-history-title"
      className="bg-card flex flex-col gap-6 p-6 md:p-8"
    >
      <div className="flex flex-col gap-2">
        <h2 id="season-history-title" className="text-2xl font-bold">
          История сезонов
        </h2>
        <p className="text-foreground-muted text-sm">
          Сезоны, к которым привязан ваш игровой профиль.
        </p>
      </div>

      {isLoading && <p className="text-foreground-muted">Загружаем историю сезонов…</p>}

      {isError && (
        <div className="flex flex-col items-start gap-3">
          <p className="text-foreground-muted">Не удалось загрузить историю сезонов.</p>
          <Button variant="secondary" onClick={() => void refetch()}>
            Повторить
          </Button>
        </div>
      )}

      {!isLoading && !isError && data?.totalSeasons === 0 && (
        <p className="text-foreground-muted">
          Пока нет сезонов, связанных с вашим профилем. История появится после добавления участия.
        </p>
      )}

      {!isLoading && !isError && data && data.totalSeasons > 0 && (
        <>
          <p className="text-sm font-medium">Участие в сезонах: {data.totalSeasons}</p>
          <div className="grid gap-4 md:grid-cols-2">
            {data.seasons.map((season) => (
              <article key={season.id} className="border-border flex gap-4 border p-4">
                {season.image && (
                  <img
                    src={season.image}
                    alt=""
                    loading="lazy"
                    className="size-20 shrink-0 object-cover"
                    onError={(event) => {
                      event.currentTarget.hidden = true;
                    }}
                  />
                )}
                <div className="flex min-w-0 flex-col gap-1">
                  <h3 className="text-foreground truncate font-semibold">{season.title}</h3>
                  <p className="text-foreground-muted text-sm">{seasonDates(season)}</p>
                  <p className="text-primary text-sm">{phaseLabels[season.phase]}</p>
                  {season.description && (
                    <p className="text-foreground-muted mt-1 line-clamp-3 text-sm">
                      {season.description}
                    </p>
                  )}
                </div>
              </article>
            ))}
          </div>
        </>
      )}
    </section>
  );
}
