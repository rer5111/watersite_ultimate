import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { adminQueryKeys, deleteAdminSeason, updateAdminSeasonStatus, useAdminSeasons } from '@/api';
import { Button, DataTable, Input, Select, toast, type DataTableColumn } from '@/components/ui';
import type { AdminSeason, AdminSeasonStatus } from '@/types';
import {
  seasonStatusLabels,
  seasonStatusTones,
  formatDateTime,
  formatErrorMessage,
} from './admin-utils';
import {
  AdminErrorState,
  AdminPageHeader,
  ConfirmDialog,
  SeasonCreateDialog,
  SeasonDetailsDialog,
  SeasonEditDialog,
  StatusBadge,
} from './components';

type DateFilter = 'ALL' | 'UPCOMING' | 'PAST';

export function AdminSeasonsPage() {
  const queryClient = useQueryClient();
  const { data: seasons = [], isLoading, isError, error, refetch } = useAdminSeasons();
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<'' | AdminSeasonStatus>('');
  const [dateFilter, setDateFilter] = useState<DateFilter>('ALL');
  const [isCreating, setIsCreating] = useState(false);
  const [viewingSeason, setViewingSeason] = useState<AdminSeason | null>(null);
  const [editingSeason, setEditingSeason] = useState<AdminSeason | null>(null);
  const [deletingSeason, setDeletingSeason] = useState<AdminSeason | null>(null);
  const [now] = useState(() => Date.now());

  const statusMutation = useMutation({
    mutationFn: ({ season, status }: { season: AdminSeason; status: AdminSeasonStatus }) =>
      updateAdminSeasonStatus(season.id, status),
    onSuccess: (updated) => {
      void queryClient.invalidateQueries({ queryKey: adminQueryKeys.seasons });
      toast.success(`Статус сезона «${updated.title}» изменён`);
    },
    onError: (mutationError) => {
      toast.error(formatErrorMessage(mutationError, 'Не удалось изменить статус сезона'));
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (season: AdminSeason) => deleteAdminSeason(season.id),
    onSuccess: (_result, season) => {
      void queryClient.invalidateQueries({ queryKey: adminQueryKeys.seasons });
      toast.success(`Сезон «${season.title}» удалён`);
      setDeletingSeason(null);
      deleteMutation.reset();
    },
    onError: (mutationError) => {
      toast.error(formatErrorMessage(mutationError, 'Не удалось удалить сезон'));
    },
  });

  const normalizedSearch = search.trim().toLowerCase();
  const filteredSeasons = seasons.filter((season) => {
    const timestamp = new Date(season.startsAt).getTime();
    const matchesSearch =
      !normalizedSearch ||
      [String(season.id), season.title, season.code, season.organizer].some((value) =>
        value.toLowerCase().includes(normalizedSearch)
      );
    const matchesStatus = !statusFilter || season.status === statusFilter;
    const matchesDate =
      dateFilter === 'ALL' || (dateFilter === 'UPCOMING' ? timestamp >= now : timestamp < now);
    return matchesSearch && matchesStatus && matchesDate;
  });

  const columns: DataTableColumn<AdminSeason>[] = [
    {
      id: 'id',
      header: 'ID',
      cell: (season) => <span className="text-foreground-muted">#{season.id}</span>,
      sortValue: (season) => season.id,
      className: 'w-20',
    },
    {
      id: 'title',
      header: 'Сезон',
      cell: (season) => (
        <div className="flex min-w-48 flex-col">
          <span className="text-foreground font-semibold">{season.title}</span>
          <span className="text-foreground-muted text-xs">{season.code}</span>
        </div>
      ),
      sortValue: (season) => season.title.toLowerCase(),
    },
    {
      id: 'startsAt',
      header: 'Дата и время',
      cell: (season) => (
        <span className="whitespace-nowrap">{formatDateTime(season.startsAt)}</span>
      ),
      sortValue: (season) => new Date(season.startsAt).getTime(),
    },
    {
      id: 'organizer',
      header: 'Организатор',
      cell: (season) => <span className="text-foreground-muted">{season.organizer}</span>,
      sortValue: (season) => season.organizer.toLowerCase(),
    },
    {
      id: 'status',
      header: 'Статус',
      cell: (season) => (
        <StatusBadge
          label={seasonStatusLabels[season.status]}
          tone={seasonStatusTones[season.status]}
        />
      ),
      sortValue: (season) => seasonStatusLabels[season.status],
    },
    {
      id: 'actions',
      header: 'Действия',
      cell: (season) => {
        const isPublished = season.status === 'PUBLISHED';
        const isCancelled = season.status === 'CANCELLED';
        return (
          <div className="flex min-w-80 flex-wrap gap-2">
            <Button
              type="button"
              variant="tertiary"
              className="border-border hover:bg-secondary rounded-none border px-2 py-1 text-xs"
              onClick={() => setViewingSeason(season)}
            >
              Просмотр
            </Button>
            <Button
              type="button"
              variant="tertiary"
              className="border-border hover:bg-secondary rounded-none border px-2 py-1 text-xs"
              onClick={() =>
                statusMutation.mutate({
                  season,
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
              onClick={() => setEditingSeason(season)}
            >
              Изменить
            </Button>
            <Button
              type="button"
              variant="tertiary"
              className="border-border hover:bg-secondary rounded-none border px-2 py-1 text-xs"
              onClick={() => statusMutation.mutate({ season, status: 'CANCELLED' })}
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
                setDeletingSeason(season);
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
        title="Сезоны"
        description="Публикация, редактирование и отмена сезонов сервера. Черновики не видны игрокам."
        actions={
          <div className="flex gap-2">
            <Button
              type="button"
              variant="primary"
              className="rounded-none"
              onClick={() => setIsCreating(true)}
            >
              Создать сезон
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
          message={formatErrorMessage(error, 'Не удалось загрузить сезоны')}
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
                aria-label="Поиск сезонов"
              />
            </div>
            <div className="w-full lg:w-52">
              <Select
                value={statusFilter}
                onChange={(event) => setStatusFilter(event.target.value as '' | AdminSeasonStatus)}
                aria-label="Фильтр по статусу"
              >
                <option value="">Все статусы</option>
                {Object.entries(seasonStatusLabels).map(([value, label]) => (
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
            data={filteredSeasons}
            getRowId={(season) => season.id}
            isLoading={isLoading}
            emptyMessage="Сезоны не найдены"
            key={`${search}|${statusFilter}|${dateFilter}`}
            caption="Список сезонов"
          />
        </>
      )}

      {isCreating && <SeasonCreateDialog onClose={() => setIsCreating(false)} />}

      {viewingSeason && (
        <SeasonDetailsDialog season={viewingSeason} onClose={() => setViewingSeason(null)} />
      )}

      {editingSeason && (
        <SeasonEditDialog
          key={editingSeason.id}
          season={editingSeason}
          onClose={() => setEditingSeason(null)}
        />
      )}

      <ConfirmDialog
        isOpen={Boolean(deletingSeason)}
        title="Удалить сезон?"
        description={`Сезон «${deletingSeason?.title ?? ''}» будет удален без возможности восстановления.`}
        confirmLabel="Удалить сезон"
        destructive
        isPending={deleteMutation.isPending}
        error={
          deleteMutation.error
            ? formatErrorMessage(deleteMutation.error, 'Не удалось удалить сезон')
            : null
        }
        onConfirm={() => {
          if (deletingSeason) deleteMutation.mutate(deletingSeason);
        }}
        onClose={() => {
          if (deleteMutation.isPending) return;
          setDeletingSeason(null);
          deleteMutation.reset();
        }}
      />
    </div>
  );
}
