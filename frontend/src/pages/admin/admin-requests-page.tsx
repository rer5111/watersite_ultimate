import { useState } from 'react';
import { useRoleRequests } from '@/api';
import { Button, DataTable, Input, Select, type DataTableColumn } from '@/components/ui';
import type { AdminRoleRequest, AdminRoleRequestStatus } from '@/types';
import {
  formatDateTime,
  formatErrorMessage,
  requestStatusLabels,
  requestStatusTones,
  userRoleLabels,
  userRoleTones,
} from './admin-utils';
import { AdminErrorState, AdminPageHeader, RequestReviewDialog, StatusBadge } from './components';

export function AdminRequestsPage() {
  const { data: requests = [], isLoading, isError, error, refetch } = useRoleRequests();
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<'' | AdminRoleRequestStatus>('');
  const [reviewingRequest, setReviewingRequest] = useState<AdminRoleRequest | null>(null);

  const normalizedSearch = search.trim().toLowerCase();
  const filteredRequests = requests.filter((request) => {
    const matchesSearch =
      !normalizedSearch ||
      [String(request.id), request.applicantName, request.applicantEmail, request.note].some(
        (value) => value.toLowerCase().includes(normalizedSearch)
      );
    const matchesStatus = !statusFilter || request.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const columns: DataTableColumn<AdminRoleRequest>[] = [
    {
      id: 'id',
      header: 'ID',
      cell: (request) => <span className="text-foreground-muted">#{request.id}</span>,
      sortValue: (request) => request.id,
      className: 'w-20',
    },
    {
      id: 'applicant',
      header: 'Заявитель',
      cell: (request) => (
        <div className="flex min-w-44 flex-col">
          <span className="text-foreground font-semibold">{request.applicantName}</span>
          <span className="text-foreground-muted text-xs">{request.applicantEmail}</span>
        </div>
      ),
      sortValue: (request) => request.applicantName.toLowerCase(),
    },
    {
      id: 'role',
      header: 'Запрашиваемая роль',
      cell: (request) => (
        <StatusBadge
          label={userRoleLabels[request.requestedRole]}
          tone={userRoleTones[request.requestedRole]}
        />
      ),
      sortValue: (request) => userRoleLabels[request.requestedRole],
    },
    {
      id: 'submittedAt',
      header: 'Подана',
      cell: (request) => (
        <span className="whitespace-nowrap">{formatDateTime(request.submittedAt)}</span>
      ),
      sortValue: (request) => new Date(request.submittedAt).getTime(),
    },
    {
      id: 'status',
      header: 'Статус',
      cell: (request) => (
        <StatusBadge
          label={requestStatusLabels[request.status]}
          tone={requestStatusTones[request.status]}
        />
      ),
      sortValue: (request) => requestStatusLabels[request.status],
    },
    {
      id: 'actions',
      header: 'Действия',
      cell: (request) => (
        <Button
          type="button"
          variant="tertiary"
          className="border-border hover:bg-secondary rounded-none border px-2 py-1 text-xs whitespace-nowrap"
          onClick={() => setReviewingRequest(request)}
        >
          {request.status === 'PENDING' ? 'Рассмотреть' : 'Открыть'}
        </Button>
      ),
    },
  ];

  return (
    <div className="flex flex-col gap-6">
      <AdminPageHeader
        title="Заявки на роли"
        description="Очередь заявок на роли модератора и администратора. Одобрение сразу повышает роль пользователя."
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
          message={formatErrorMessage(error, 'Не удалось загрузить заявки')}
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
                placeholder="Поиск по заявителю, email или тексту заявки"
                aria-label="Поиск заявок"
              />
            </div>
            <div className="w-full lg:w-56">
              <Select
                value={statusFilter}
                onChange={(event) =>
                  setStatusFilter(event.target.value as '' | AdminRoleRequestStatus)
                }
                aria-label="Фильтр по статусу заявки"
              >
                <option value="">Все статусы</option>
                {Object.entries(requestStatusLabels).map(([value, label]) => (
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
            data={filteredRequests}
            getRowId={(request) => request.id}
            isLoading={isLoading}
            emptyMessage="Заявки не найдены"
            key={`${search}|${statusFilter}`}
            caption="Очередь заявок на роли"
          />
        </>
      )}

      {reviewingRequest && (
        <RequestReviewDialog
          key={reviewingRequest.id}
          request={reviewingRequest}
          onClose={() => setReviewingRequest(null)}
        />
      )}
    </div>
  );
}
