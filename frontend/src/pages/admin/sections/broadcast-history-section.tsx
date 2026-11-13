import { useBroadcastHistory } from '@/api';
import { Button, DataTable, type DataTableColumn } from '@/components/ui';
import type { BroadcastDispatch } from '@/types';
import {
  broadcastAudienceLabels,
  broadcastCategoryLabels,
  broadcastCategoryTones,
  broadcastStatusLabels,
  broadcastStatusTones,
  formatDateTime,
  formatErrorMessage,
} from '../admin-utils';
import { AdminErrorState, StatusBadge } from '../components';

export function BroadcastHistorySection() {
  const { data: broadcasts = [], isLoading, isError, error, refetch } = useBroadcastHistory();

  const columns: DataTableColumn<BroadcastDispatch>[] = [
    {
      id: 'id',
      header: 'ID',
      cell: (dispatch) => <span className="text-foreground-muted">#{dispatch.id}</span>,
      sortValue: (dispatch) => dispatch.id,
      className: 'w-20',
    },
    {
      id: 'subject',
      header: 'Тема',
      cell: (dispatch) => (
        <span className="text-foreground min-w-48 font-semibold">{dispatch.subject}</span>
      ),
      sortValue: (dispatch) => dispatch.subject.toLowerCase(),
    },
    {
      id: 'category',
      header: 'Категория',
      cell: (dispatch) => (
        <StatusBadge
          label={broadcastCategoryLabels[dispatch.category]}
          tone={broadcastCategoryTones[dispatch.category]}
        />
      ),
      sortValue: (dispatch) => broadcastCategoryLabels[dispatch.category],
    },
    {
      id: 'audience',
      header: 'Аудитория',
      cell: (dispatch) => (
        <span className="text-foreground-muted whitespace-nowrap">
          {broadcastAudienceLabels[dispatch.audience]}
        </span>
      ),
      sortValue: (dispatch) => broadcastAudienceLabels[dispatch.audience],
    },
    {
      id: 'recipients',
      header: 'Получатели',
      cell: (dispatch) => <span className="whitespace-nowrap">{dispatch.recipients}</span>,
      sortValue: (dispatch) => dispatch.recipients,
    },
    {
      id: 'sentAt',
      header: 'Отправлено',
      cell: (dispatch) => (
        <span className="whitespace-nowrap">{formatDateTime(dispatch.sentAt)}</span>
      ),
      sortValue: (dispatch) => new Date(dispatch.sentAt).getTime(),
    },
    {
      id: 'status',
      header: 'Статус',
      cell: (dispatch) => (
        <StatusBadge
          label={broadcastStatusLabels[dispatch.status]}
          tone={broadcastStatusTones[dispatch.status]}
        />
      ),
      sortValue: (dispatch) => broadcastStatusLabels[dispatch.status],
    },
  ];

  return (
    <section className="flex flex-col gap-4">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
        <div className="flex flex-col gap-1">
          <h2 className="text-foreground text-lg font-bold">История рассылок</h2>
          <p className="text-foreground-muted text-sm">
            Последние отправленные уведомления и их статус доставки.
          </p>
        </div>
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

      {isError ? (
        <AdminErrorState
          message={formatErrorMessage(error, 'Не удалось загрузить историю рассылок')}
          onRetry={() => void refetch()}
        />
      ) : (
        <DataTable
          columns={columns}
          data={broadcasts}
          getRowId={(dispatch) => dispatch.id}
          isLoading={isLoading}
          emptyMessage="Рассылок пока не было"
          pageSize={6}
          caption="История рассылок"
        />
      )}
    </section>
  );
}
