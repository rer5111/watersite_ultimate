import { useState, type ReactNode } from 'react';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib';

export interface DataTableColumn<T> {
  id: string;
  header: string;
  cell: (row: T) => ReactNode;
  sortValue?: (row: T) => string | number;
  className?: string;
}

export interface DataTableProps<T> {
  columns: DataTableColumn<T>[];
  data: T[];
  getRowId: (row: T) => string | number;
  isLoading?: boolean;
  emptyMessage?: string;
  pageSize?: number;
  caption?: string;
}

type SortDirection = 'asc' | 'desc';

interface SortState {
  columnId: string;
  direction: SortDirection;
}

function compareValues(left: string | number, right: string | number): number {
  if (typeof left === 'number' && typeof right === 'number') {
    return left - right;
  }
  return String(left).localeCompare(String(right), 'ru', { numeric: true });
}

export function DataTable<T>({
  columns,
  data,
  getRowId,
  isLoading = false,
  emptyMessage = 'Данные не найдены',
  pageSize = 8,
  caption,
}: DataTableProps<T>) {
  const [sort, setSort] = useState<SortState | null>(null);
  const [page, setPage] = useState(0);

  const sortedData = (() => {
    if (!sort) return data;
    const column = columns.find((item) => item.id === sort.columnId);
    if (!column?.sortValue) return data;
    const sortValue = column.sortValue;
    return [...data].sort((left, right) => {
      const result = compareValues(sortValue(left), sortValue(right));
      return sort.direction === 'asc' ? result : -result;
    });
  })();

  const pageCount = Math.max(1, Math.ceil(sortedData.length / pageSize));
  const currentPage = Math.min(page, pageCount - 1);
  const rows = sortedData.slice(currentPage * pageSize, (currentPage + 1) * pageSize);

  const handleSort = (column: DataTableColumn<T>) => {
    if (!column.sortValue) return;
    setSort((previous) => {
      if (previous?.columnId !== column.id) {
        return { columnId: column.id, direction: 'asc' };
      }
      return {
        columnId: column.id,
        direction: previous.direction === 'asc' ? 'desc' : 'asc',
      };
    });
  };

  return (
    <div className="border-border bg-card w-full rounded-none border">
      <div className="overflow-x-auto">
        <table className="w-full border-collapse text-left text-sm">
          {caption && <caption className="sr-only">{caption}</caption>}
          <thead className="bg-surface">
            <tr>
              {columns.map((column) => {
                const isSorted = sort?.columnId === column.id;
                const ariaSort = isSorted
                  ? sort.direction === 'asc'
                    ? 'ascending'
                    : 'descending'
                  : 'none';

                return (
                  <th
                    key={column.id}
                    scope="col"
                    aria-sort={column.sortValue ? ariaSort : undefined}
                    className={cn(
                      'border-border text-foreground-muted border-b px-4 py-3 text-xs font-semibold tracking-wider uppercase',
                      column.className
                    )}
                  >
                    {column.sortValue ? (
                      <button
                        type="button"
                        onClick={() => handleSort(column)}
                        className="hover:text-foreground flex cursor-pointer items-center gap-2 rounded-none text-left text-xs font-semibold tracking-wider uppercase transition-colors"
                      >
                        {column.header}
                        <span aria-hidden="true" className="text-foreground-subtle">
                          {isSorted ? (sort.direction === 'asc' ? '↑' : '↓') : '↕'}
                        </span>
                      </button>
                    ) : (
                      column.header
                    )}
                  </th>
                );
              })}
            </tr>
          </thead>

          <tbody>
            {isLoading && (
              <tr>
                <td
                  colSpan={columns.length}
                  className="text-foreground-muted px-4 py-10 text-center"
                >
                  Загрузка данных…
                </td>
              </tr>
            )}

            {!isLoading && rows.length === 0 && (
              <tr>
                <td
                  colSpan={columns.length}
                  className="text-foreground-muted px-4 py-10 text-center"
                >
                  {emptyMessage}
                </td>
              </tr>
            )}

            {!isLoading &&
              rows.map((row) => (
                <tr
                  key={getRowId(row)}
                  className="border-border hover:bg-surface/50 border-b transition-colors last:border-b-0"
                >
                  {columns.map((column) => (
                    <td
                      key={column.id}
                      className={cn('text-foreground px-4 py-3 align-middle', column.className)}
                    >
                      {column.cell(row)}
                    </td>
                  ))}
                </tr>
              ))}
          </tbody>
        </table>
      </div>

      {!isLoading && sortedData.length > 0 && (
        <div className="border-border flex flex-col gap-3 border-t px-4 py-3 sm:flex-row sm:items-center sm:justify-between">
          <span className="text-foreground-muted text-xs">
            Всего записей: <span className="text-foreground font-medium">{sortedData.length}</span>
          </span>
          <div className="flex items-center gap-3">
            <Button
              type="button"
              variant="secondary"
              className="rounded-none px-3 py-1.5 text-xs"
              onClick={() => setPage(currentPage - 1)}
              disabled={currentPage === 0}
            >
              Назад
            </Button>
            <span className="text-foreground-muted text-xs whitespace-nowrap">
              Страница {currentPage + 1} из {pageCount}
            </span>
            <Button
              type="button"
              variant="secondary"
              className="rounded-none px-3 py-1.5 text-xs"
              onClick={() => setPage(currentPage + 1)}
              disabled={currentPage >= pageCount - 1}
            >
              Вперёд
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}
