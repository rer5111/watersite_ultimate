import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { adminQueryKeys, deleteAdminEvent, updateAdminEventStatus, useAdminEvents } from '@/api';
import { Button, DataTable, Input, Select, toast, type DataTableColumn } from '@/components/ui';
import type { AdminEvent, AdminEventStatus } from '@/types';
import {
  eventStatusLabels,
  eventStatusTones,
  formatDateTime,
  formatErrorMessage,
} from './admin-utils';
import { AdminErrorState, AdminPageHeader } from './components/admin-page-shell';
import { ConfirmDialog } from './components/confirm-dialog';
import { EventDetailsDialog } from './components/event-details-dialog';
import { EventEditDialog } from './components/event-edit-dialog';
import { StatusBadge } from './components/status-badge';

type DateFilter = 'ALL' | 'UPCOMING' | 'PAST';

export function AdminEventsPage() {
  const queryClient = useQueryClient();
  const { data: events = [], isLoading, isError, error, refetch } = useAdminEvents();
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<'' | AdminEventStatus>('');
  const [dateFilter, setDateFilter] = useState<DateFilter>('ALL');
  const [viewingEvent, setViewingEvent] = useState<AdminEvent | null>(null);
  const [editingEvent, setEditingEvent] = useState<AdminEvent | null>(null);
  const [deletingEvent, setDeletingEvent] = useState<AdminEvent | null>(null);
  const [now] = useState(() => Date.now());

  const statusMutation = useMutation({
    mutationFn: ({ event, status }: { event: AdminEvent; status: AdminEventStatus }) =>
      updateAdminEventStatus(event.id, status),
    onSuccess: (updated) => {
      void queryClient.invalidateQueries({ queryKey: adminQueryKeys.events });
      toast.success(`Статус события «${updated.title}» изменён`);
    },
    onError: (mutationError) => {
      toast.error(formatErrorMessage(mutationError, 'Не удалось изменить статус события'));
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (event: AdminEvent) => deleteAdminEvent(event.id),
    onSuccess: (_result, event) => {
      void queryClient.invalidateQueries({ queryKey: adminQueryKeys.events });
      toast.success(`Событие «${event.title}» удалено`);
      setDeletingEvent(null);
      deleteMutation.reset();
    },
    onError: (mutationError) => {
      toast.error(formatErrorMessage(mutationError, 'Не удалось удалить событие'));
    },
  });

  const normalizedSearch = search.trim().toLowerCase();
  const filteredEvents = events.filter((event) => {
    const timestamp = new Date(event.startsAt).getTime();
    const matchesSearch =
      !normalizedSearch ||
      [String(event.id), event.title, event.code, event.organizer].some((value) =>
        value.toLowerCase().includes(normalizedSearch)
      );
    const matchesStatus = !statusFilter || event.status === statusFilter;
    const matchesDate =
      dateFilter === 'ALL' || (dateFilter === 'UPCOMING' ? timestamp >= now : timestamp < now);
    return matchesSearch && matchesStatus && matchesDate;
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
      header: 'Событие',
      cell: (event) => (
        <div className="flex min-w-48 flex-col">
          <span className="text-foreground font-semibold">{event.title}</span>
          <span className="text-foreground-muted text-xs">{event.code}</span>
        </div>
      ),
      sortValue: (event) => event.title.toLowerCase(),
    },
    {
      id: 'startsAt',
      header: 'Дата и время',
      cell: (event) => <span className="whitespace-nowrap">{formatDateTime(event.startsAt)}</span>,
      sortValue: (event) => new Date(event.startsAt).getTime(),
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
      cell: (event) => {
        const isPublished = event.status === 'PUBLISHED';
        const isCancelled = event.status === 'CANCELLED';
        return (
          <div className="flex min-w-80 flex-wrap gap-2">
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
              onClick={() =>
                statusMutation.mutate({
                  event,
                  status: isPublished ? 'DRAFT' : 'PUBLISHED',
                })
              }
              disabled={statusMutation.isPending || isCancelled}
            >
              {isPublished ? 'Снять' : 'Опубликовать'}
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
              className="border-border hover:bg-secondary rounded-none border px-2 py-1 text-xs"
              onClick={() => statusMutation.mutate({ event, status: 'CANCELLED' })}
              disabled={statusMutation.isPending || isCancelled}
            >
              Отменить
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
        );
      },
    },
  ];

  return (
    <div className="flex flex-col gap-6">
      <AdminPageHeader
        title="События"
        description="Публикация, редактирование и отмена событий сервера. Черновики не видны игрокам."
        actions={
          <Button
            type="button"
            variant="secondary"
            className="rounded-none"
            onClick={() => void refetch()}
            disabled={isLoading}
          >
            Обновить
          </Button>
        }
      />

      {isError ? (
        <AdminErrorState
          message={formatErrorMessage(error, 'Не удалось загрузить события')}
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
                aria-label="Поиск событий"
              />
            </div>
            <div className="w-full lg:w-52">
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
            <div className="w-full lg:w-52">
              <Select
                value={dateFilter}
                onChange={(event) => setDateFilter(event.target.value as DateFilter)}
                aria-label="Фильтр по дате"
              >
                <option value="ALL">Любая дата</option>
                <option value="UPCOMING">Предстоящие</option>
                <option value="PAST">Прошедшие</option>
              </Select>
            </div>
            <Button
              type="button"
              variant="tertiary"
              className="border-border hover:bg-secondary rounded-none border"
              onClick={() => {
                setSearch('');
                setStatusFilter('');
                setDateFilter('ALL');
              }}
              disabled={!search && !statusFilter && dateFilter === 'ALL'}
            >
              Сбросить
            </Button>
          </div>

          <DataTable
            columns={columns}
            data={filteredEvents}
            getRowId={(event) => event.id}
            isLoading={isLoading}
            emptyMessage="События не найдены"
            key={`${search}|${statusFilter}|${dateFilter}`}
            caption="Список событий"
          />
        </>
      )}

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
        title="Удалить событие?"
        description={`Событие «${deletingEvent?.title ?? ''}» будет удалено без возможности восстановления.`}
        confirmLabel="Удалить событие"
        destructive
        isPending={deleteMutation.isPending}
        error={
          deleteMutation.isError
            ? formatErrorMessage(deleteMutation.error, 'Не удалось удалить событие')
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
