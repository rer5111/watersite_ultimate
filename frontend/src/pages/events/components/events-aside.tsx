import { useEffect, useRef, useState } from 'react';
import { useAdmins, useSeasons, type EventStatus } from '@/api';
import { Checkbox, ChevronDownIcon, Input } from '@/components';
import { useEventFilterParams } from '@/hooks';
import { cn } from '@/lib';

export function EventsAside({ className }: { className?: string } = {}) {
  return (
    <aside
      className={cn(
        'bg-card mx-6 my-8 flex w-64 shrink-0 flex-col px-5 lg:sticky lg:top-20 lg:self-start',
        className
      )}
    >
      <h2 className="pt-4 text-lg font-semibold">Фильтры</h2>
      <div className="divide-border flex flex-col divide-y">
        <SeasonsFilter />
        <OrganizerFilter />
        <StatusFilter />
        <DateFilter />
      </div>
    </aside>
  );
}

function toggle<T>(list: T[], value: T): T[] {
  return list.includes(value) ? list.filter((x) => x !== value) : [...list, value];
}

function SeasonsFilter() {
  const { data: seasonsList = [], isLoading, isError, refetch } = useSeasons();
  const { seasons, setFilters } = useEventFilterParams();

  return (
    <section className="flex flex-col gap-3 pt-4 pb-6">
      <h3 className="py-1 text-sm leading-5 font-semibold">Сезон</h3>
      {isLoading && null}
      {isError && (
        <div className="flex flex-col gap-2 text-sm">
          <p className="text-error">Не удалось загрузить сезоны</p>
          <button
            type="button"
            onClick={() => void refetch()}
            className="text-primary text-left hover:underline"
          >
            Повторить
          </button>
        </div>
      )}
      {!isLoading && !isError && seasonsList.length === 0 && (
        <p className="text-foreground-muted text-sm">Список пуст</p>
      )}
      {!isLoading &&
        !isError &&
        seasonsList.map((season) => (
          <label
            key={season.id}
            className="flex w-full cursor-pointer items-center gap-2.5 select-none"
          >
            <Checkbox
              checked={seasons.includes(season.id)}
              onChange={() => setFilters({ seasons: toggle(seasons, season.id) })}
            />
            <p className="text-base tabular-nums">{season.title}</p>
          </label>
        ))}
    </section>
  );
}

function OrganizerFilter() {
  const [isOpen, setIsOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);
  const { data: admins = [], isLoading, isError, refetch } = useAdmins();
  const organizers = Array.from(new Set(admins.map((admin) => admin.username)));
  const { organizers: selectedOrganizers, setFilters } = useEventFilterParams();

  useEffect(() => {
    if (!isOpen) return;
    const handleClickOutside = (e: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [isOpen]);

  const label =
    selectedOrganizers.length === 0
      ? 'Все организаторы'
      : selectedOrganizers.length === 1
        ? selectedOrganizers[0]
        : `Выбрано: ${selectedOrganizers.length}`;

  return (
    <section className="flex flex-col gap-3 pt-4 pb-6">
      <h3 className="py-1 text-sm leading-5 font-semibold">Организатор</h3>
      {isLoading && null}
      {isError && (
        <div className="flex flex-col gap-2 text-sm">
          <p className="text-error">Не удалось загрузить организаторов</p>
          <button
            type="button"
            onClick={() => void refetch()}
            className="text-primary text-left hover:underline"
          >
            Повторить
          </button>
        </div>
      )}
      {!isLoading && !isError && (
        <div ref={containerRef} className="relative w-full">
          <button
            type="button"
            onClick={() => setIsOpen((prev) => !prev)}
            aria-expanded={isOpen}
            className="text-foreground bg-input focus-visible:ring-foreground flex w-full cursor-pointer items-center justify-between rounded-none border border-transparent p-3 text-left text-sm leading-6 font-medium transition-all focus-visible:ring-2 focus-visible:outline-none"
          >
            <span className="truncate">{label}</span>
            <ChevronDownIcon
              className={cn(
                'text-foreground-muted pointer-events-none size-4 shrink-0 transition-transform',
                isOpen && 'rotate-180'
              )}
            />
          </button>
          {isOpen && (
            <div className="border-border bg-card absolute z-20 mt-1 flex max-h-56 w-full flex-col gap-1 overflow-y-auto border p-2 shadow-lg">
              {organizers.map((username) => (
                <label
                  key={username}
                  className="hover:bg-surface flex w-full cursor-pointer items-center gap-2.5 p-1.5 text-sm select-none"
                >
                  <Checkbox
                    checked={selectedOrganizers.includes(username)}
                    onChange={() =>
                      setFilters({ organizers: toggle(selectedOrganizers, username) })
                    }
                  />
                  <span className="truncate">{username}</span>
                </label>
              ))}
            </div>
          )}
        </div>
      )}
    </section>
  );
}

const STATUS_OPTIONS: { value: EventStatus; label: string }[] = [
  { value: 'COMPLETED', label: 'Завершен' },
  { value: 'PLANNED', label: 'Планируется' },
];

function StatusFilter() {
  const { statuses, setFilters } = useEventFilterParams();

  return (
    <section className="flex flex-col gap-3 pt-4 pb-6">
      <h3 className="py-1 text-sm leading-5 font-semibold">Статус</h3>
      {STATUS_OPTIONS.map((status) => (
        <label
          key={status.value}
          className="flex w-full cursor-pointer items-center gap-2.5 select-none"
        >
          <Checkbox
            checked={statuses.includes(status.value)}
            onChange={() => setFilters({ statuses: toggle(statuses, status.value) })}
          />
          <p className="text-base">{status.label}</p>
        </label>
      ))}
    </section>
  );
}

function DateFilter() {
  const { minDate, maxDate, setFilters } = useEventFilterParams();

  return (
    <section className="flex flex-col gap-3 pt-4 pb-6">
      <h3 className="py-1 text-sm leading-5 font-semibold">Дата</h3>
      <div className="flex flex-col gap-3">
        <label className="flex flex-col gap-1.5">
          <span className="text-foreground-muted text-sm font-medium">До</span>
          <Input
            type="date"
            value={maxDate ?? ''}
            onChange={(e) => setFilters({ maxDate: e.target.value || null })}
            className="w-full"
            aria-label="Дата до"
          />
        </label>
        <label className="flex flex-col gap-1.5">
          <span className="text-foreground-muted text-sm font-medium">После</span>
          <Input
            type="date"
            value={minDate ?? ''}
            onChange={(e) => setFilters({ minDate: e.target.value || null })}
            className="w-full"
            aria-label="Дата после"
          />
        </label>
      </div>
    </section>
  );
}
