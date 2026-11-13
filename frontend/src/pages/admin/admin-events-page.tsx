import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { adminQueryKeys, deleteAdminEvent, useAdminEvents } from '@/api';
import { Button, DataTable, Input, Select, toast, type DataTableColumn } from '@/components/ui';
import type { AdminEvent, AdminEventStatus } from '@/types';
import { eventStatusLabels, eventStatusTones, formatErrorMessage } from './admin-utils';
import {
  AdminErrorState,
  AdminPageHeader,
  ConfirmDialog,
  EventCreateDialog,
  EventDetailsDialog,
  EventEditDialog,
  StatusBadge,
} from './components';

export function AdminEventsPage() {
  const queryClient = useQueryClient();
  const { data: events = [], isLoading, isError, error, refetch } = useAdminEvents();
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<'' | AdminEventStatus>('');
  const [isCreating, setIsCreating] = useState(false);
  const [viewingEvent, setViewingEvent] = useState<AdminEvent | null>(null);
  const [editingEvent, setEditingEvent] = useState<AdminEvent | null>(null);
  const [deletingEvent, setDeletingEvent] = useState<AdminEvent | null>(null);

  const deleteMutation = useMutation({
    mutationFn: (event: AdminEvent) => deleteAdminEvent(event.id),
    onSuccess: (_result, event) => {
      void queryClient.invalidateQueries({ queryKey: adminQueryKeys.events });
      toast.success(`Ивент «${event.title}» удалён`);
      setDeletingEvent(null);
      deleteMutation.reset();
    },
    onError: (mutationError) => {
      toast.error(formatErrorMessage(mutationError, 'Не удалось удалить ивент'));
    },
  });

  const normalizedSearch = search.trim().toLowerCase();
  const filteredEvents = events.filter((event) => {
    const matchesSearch =
      !normalizedSearch ||
      [String(event.id), event.title, event.code, event.organizer].some((value) =>
        value.toLowerCase().includes(normalizedSearch)
      );
    const matchesStatus = !statusFilter || event.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const columns: DataTableColumn<AdminEvent>[] = [
    {
      id: 'id',
      header: 'ID',
      cell: (event) => <span className="text-foreground-muted">#{event.id}</span>,
      sortValue: (event) => event.id,
      className: 'w-20',
    },
    {
      id: 'title',
      header: 'Ивент',
      cell: (event) => (
        <div className="flex min-w-48 flex-col">
          <span className="text-foreground font-semibold">{event.title}</span>
          <span className="text-foreground-muted text-xs">{event.code}</span>
        </div>
      ),
      sortValue: (event) => event.title.toLowerCase(),
    },
    {
      id: 'season',
      header: 'Сезон',
      cell: (event) => (
        <span className="text-foreground-muted whitespace-nowrap">
          {event.seasonTitle || `${event.seasonNumber} сезон`}
        </span>
      ),
      sortValue: (event) => event.seasonNumber,
    },
    {
      id: 'dates',
      header: 'Даты',
      cell: (event) => {
        const dates =
          event.dateRangeFormatted ||
          (event.startDate ? `${event.startDate} — ${event.endDate || 'н. в.'}` : '—');
        return <span className="whitespace-nowrap tabular-nums">{dates}</span>;
      },
      sortValue: (event) => event.startDate || '',
    },
    {
      id: 'organizer',
      header: 'Организатор',
      cell: (event) => <span className="text-foreground-muted">{event.organizer}</span>,
      sortValue: (event) => event.organizer.toLowerCase(),
    },
    {
      id: 'status',
      header: 'Статус',
      cell: (event) => (
        <StatusBadge
          label={eventStatusLabels[event.status]}
          tone={eventStatusTones[event.status]}
        />
      ),
      sortValue: (event) => eventStatusLabels[event.status],
    },
    {
      id: 'actions',
      header: 'Действия',
      cell: (event) => (
        <div className="flex min-w-56 flex-wrap gap-2">
          <Button
            type="button"
            variant="tertiary"
            className="border-border hover:bg-secondary rounded-none border px-2 py-1 text-xs"
            onClick={() => setViewingEvent(event)}
          >
            Просмотр
          </Button>
          <Button
            type="button"
            variant="tertiary"
            className="border-border hover:bg-secondary rounded-none border px-2 py-1 text-xs"
            onClick={() => setEditingEvent(event)}
          >
            Изменить
          </Button>
          <Button
            type="button"
            variant="tertiary"
            className="border-error/40 text-error hover:bg-error hover:text-foreground rounded-none border px-2 py-1 text-xs"
            onClick={() => {
              deleteMutation.reset();
              setDeletingEvent(event);
            }}
          >
            Удалить
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div className="flex flex-col gap-6">
      <AdminPageHeader
        title="Ивенты"
        description="Создание, редактирование и управление игровыми событиями сервера."
        actions={
          <div className="flex gap-2">
            <Button
              type="button"
              variant="primary"
              className="rounded-none"
              onClick={() => setIsCreating(true)}
            >
              Создать ивент
            </Button>
            <Button
              type="button"
              variant="secondary"
              className="rounded-none"
              onClick={() => void refetch()}
              disabled={isLoading}
            >
              Обновить
            </Button>
          </div>
        }
      />

      {isError ? (
        <AdminErrorState
          message={formatErrorMessage(error, 'Не удалось загрузить ивенты')}
          onRetry={() => void refetch()}
        />
      ) : (
        <>
          <div className="border-border bg-card flex flex-col gap-3 rounded-none border p-4 lg:flex-row lg:items-center">
            <div className="flex-1">
              <Input
                type="search"
                value={search}
                onChange={(event) => setSearch(event.target.value)}
                placeholder="Поиск по названию, коду или организатору"
                aria-label="Поиск ивентов"
              />
            </div>
            <div className="w-full lg:w-56">
              <Select
                value={statusFilter}
                onChange={(event) => setStatusFilter(event.target.value as '' | AdminEventStatus)}
                aria-label="Фильтр по статусу"
              >
                <option value="">Все статусы</option>
                {Object.entries(eventStatusLabels).map(([value, label]) => (
                  <option key={value} value={value}>
                    {label}
                  </option>
                ))}
              </Select>
            </div>
            <Button
              type="button"
              variant="tertiary"
              className="border-border hover:bg-secondary rounded-none border"
              onClick={() => {
                setSearch('');
                setStatusFilter('');
              }}
              disabled={!search && !statusFilter}
            >
              Сбросить
            </Button>
          </div>

          <DataTable
            columns={columns}
            data={filteredEvents}
            getRowId={(event) => event.id}
            isLoading={isLoading}
            emptyMessage="Ивенты не найдены"
            key={`${search}|${statusFilter}`}
            caption="Список ивентов"
          />
        </>
      )}

      {isCreating && <EventCreateDialog onClose={() => setIsCreating(false)} />}

      {viewingEvent && (
        <EventDetailsDialog event={viewingEvent} onClose={() => setViewingEvent(null)} />
      )}

      {editingEvent && (
        <EventEditDialog
          key={editingEvent.id}
          event={editingEvent}
          onClose={() => setEditingEvent(null)}
        />
      )}

      <ConfirmDialog
        isOpen={Boolean(deletingEvent)}
        title="Удалить ивент?"
        description={`Ивент «${deletingEvent?.title ?? ''}» будет удален без возможности восстановления.`}
        confirmLabel="Удалить ивент"
        destructive
        isPending={deleteMutation.isPending}
        error={
          deleteMutation.error
            ? formatErrorMessage(deleteMutation.error, 'Не удалось удалить ивент')
            : null
        }
        onConfirm={() => {
          if (deletingEvent) deleteMutation.mutate(deletingEvent);
        }}
        onClose={() => {
          if (deleteMutation.isPending) return;
          setDeletingEvent(null);
          deleteMutation.reset();
        }}
      />
    </div>
  );
}
