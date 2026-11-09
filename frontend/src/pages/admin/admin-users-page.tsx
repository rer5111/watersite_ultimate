import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { adminQueryKeys, deleteAdminUser, updateAdminUser, useAdminUsers } from '@/api';
import { Button, DataTable, Input, Select, toast, type DataTableColumn } from '@/components/ui';
import { useAuth } from '@/context';
import type { AdminUser, AdminUserRole, AdminUserStatus } from '@/types';
import {
  formatDate,
  formatErrorMessage,
  userRoleLabels,
  userRoleTones,
  userStatusLabels,
  userStatusTones,
} from './admin-utils';
import { AdminErrorState, AdminPageHeader } from './components/admin-page-shell';
import { ConfirmDialog } from './components/confirm-dialog';
import { StatusBadge } from './components/status-badge';
import { UserEditDialog } from './components/user-edit-dialog';

export function AdminUsersPage() {
  const queryClient = useQueryClient();
  const { user: currentUser } = useAuth();
  const { data: users = [], isLoading, isError, error, refetch } = useAdminUsers();
  const [search, setSearch] = useState('');
  const [roleFilter, setRoleFilter] = useState<'' | AdminUserRole>('');
  const [statusFilter, setStatusFilter] = useState<'' | AdminUserStatus>('');
  const [editingUser, setEditingUser] = useState<AdminUser | null>(null);
  const [deletingUser, setDeletingUser] = useState<AdminUser | null>(null);

  const isUserAdmin = (user: AdminUser) => user.role === 'ADMIN';
  const isCurrentUser = (user: AdminUser) =>
    currentUser?.id !== undefined
      ? user.id === currentUser.id
      : user.username === currentUser?.username;

  const banMutation = useMutation({
    mutationFn: ({ user, status }: { user: AdminUser; status: AdminUserStatus }) =>
      updateAdminUser(user.id, { role: user.role, status }),
    onSuccess: (updated) => {
      void queryClient.invalidateQueries({ queryKey: adminQueryKeys.users });
      toast.success(
        updated.status === 'BANNED'
          ? `Пользователь ${updated.username} забанен`
          : `Пользователь ${updated.username} разбанен`
      );
    },
    onError: (mutationError) => {
      toast.error(formatErrorMessage(mutationError, 'Не удалось изменить статус пользователя'));
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (user: AdminUser) => deleteAdminUser(user.id),
    onSuccess: (_result, user) => {
      void queryClient.invalidateQueries({ queryKey: adminQueryKeys.users });
      toast.success(`Пользователь ${user.username} удалён`);
      setDeletingUser(null);
      deleteMutation.reset();
    },
    onError: (mutationError) => {
      toast.error(formatErrorMessage(mutationError, 'Не удалось удалить пользователя'));
    },
  });

  const normalizedSearch = search.trim().toLowerCase();
  const filteredUsers = users.filter((user) => {
    const matchesSearch =
      !normalizedSearch ||
      [String(user.id), user.username, user.displayName, user.email].some((value) =>
        value.toLowerCase().includes(normalizedSearch)
      );
    const matchesRole = !roleFilter || user.role === roleFilter;
    const matchesStatus = !statusFilter || user.status === statusFilter;
    return matchesSearch && matchesRole && matchesStatus;
  });

  const columns: DataTableColumn<AdminUser>[] = [
    {
      id: 'id',
      header: 'ID',
      cell: (user) => <span className="text-foreground-muted">#{user.id}</span>,
      sortValue: (user) => user.id,
      className: 'w-20',
    },
    {
      id: 'username',
      header: 'Пользователь',
      cell: (user) => (
        <div className="flex min-w-40 flex-col">
          <span className="text-foreground font-semibold">{user.displayName}</span>
          <span className="text-foreground-muted text-xs">@{user.username}</span>
        </div>
      ),
      sortValue: (user) => user.displayName.toLowerCase(),
    },
    {
      id: 'email',
      header: 'Email',
      cell: (user) => <span className="text-foreground-muted">{user.email}</span>,
      sortValue: (user) => user.email.toLowerCase(),
    },
    {
      id: 'role',
      header: 'Роль',
      cell: (user) => (
        <StatusBadge label={userRoleLabels[user.role]} tone={userRoleTones[user.role]} />
      ),
      sortValue: (user) => userRoleLabels[user.role],
    },
    {
      id: 'status',
      header: 'Статус',
      cell: (user) => (
        <StatusBadge label={userStatusLabels[user.status]} tone={userStatusTones[user.status]} />
      ),
      sortValue: (user) => userStatusLabels[user.status],
    },
    {
      id: 'registeredAt',
      header: 'Регистрация',
      cell: (user) => <span className="whitespace-nowrap">{formatDate(user.registeredAt)}</span>,
      sortValue: (user) => new Date(user.registeredAt).getTime(),
    },
    {
      id: 'actions',
      header: 'Действия',
      cell: (user) => {
        const banDisabledReason = isCurrentUser(user)
          ? 'Нельзя заблокировать собственный аккаунт'
          : isUserAdmin(user) && user.status !== 'BANNED'
            ? 'Нельзя заблокировать администратора'
            : undefined;

        const deleteDisabledReason = isCurrentUser(user)
          ? 'Нельзя удалить собственный аккаунт'
          : isUserAdmin(user)
            ? 'Нельзя удалить администратора'
            : undefined;

        return (
          <div className="flex min-w-52 flex-wrap gap-2">
            <Button
              type="button"
              variant="tertiary"
              className="border-border hover:bg-secondary rounded-none border px-2 py-1 text-xs"
              onClick={() => setEditingUser(user)}
            >
              Изменить
            </Button>
            <Button
              type="button"
              variant="tertiary"
              className="border-border hover:bg-secondary rounded-none border px-2 py-1 text-xs disabled:opacity-40 disabled:cursor-not-allowed"
              onClick={() =>
                banMutation.mutate({
                  user,
                  status: user.status === 'BANNED' ? 'ACTIVE' : 'BANNED',
                })
              }
              disabled={banMutation.isPending || !!banDisabledReason}
              title={banDisabledReason}
            >
              {user.status === 'BANNED' ? 'Разбанить' : 'Забанить'}
            </Button>
            <Button
              type="button"
              variant="tertiary"
              className="border-error/40 text-error hover:bg-error hover:text-foreground rounded-none border px-2 py-1 text-xs disabled:opacity-40 disabled:cursor-not-allowed"
              onClick={() => {
                deleteMutation.reset();
                setDeletingUser(user);
              }}
              disabled={deleteMutation.isPending || !!deleteDisabledReason}
              title={deleteDisabledReason}
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
        title="Пользователи"
        description="Управление аккаунтами, ролями и статусами игроков. Действия применяются сразу."
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
          message={formatErrorMessage(error, 'Не удалось загрузить пользователей')}
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
                placeholder="Поиск по ID, имени или email"
                aria-label="Поиск пользователей"
              />
            </div>
            <div className="w-full lg:w-52">
              <Select
                value={roleFilter}
                onChange={(event) => setRoleFilter(event.target.value as '' | AdminUserRole)}
                aria-label="Фильтр по роли"
              >
                <option value="">Все роли</option>
                {Object.entries(userRoleLabels).map(([value, label]) => (
                  <option key={value} value={value}>
                    {label}
                  </option>
                ))}
              </Select>
            </div>
            <div className="w-full lg:w-52">
              <Select
                value={statusFilter}
                onChange={(event) => setStatusFilter(event.target.value as '' | AdminUserStatus)}
                aria-label="Фильтр по статусу"
              >
                <option value="">Все статусы</option>
                {Object.entries(userStatusLabels).map(([value, label]) => (
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
                setRoleFilter('');
                setStatusFilter('');
              }}
              disabled={!search && !roleFilter && !statusFilter}
            >
              Сбросить
            </Button>
          </div>

          <DataTable
            columns={columns}
            data={filteredUsers}
            getRowId={(user) => user.id}
            isLoading={isLoading}
            emptyMessage="Пользователи не найдены"
            key={`${search}|${roleFilter}|${statusFilter}`}
            caption="Список пользователей"
          />
        </>
      )}

      {editingUser && (
        <UserEditDialog
          key={editingUser.id}
          user={editingUser}
          onClose={() => setEditingUser(null)}
        />
      )}

      <ConfirmDialog
        isOpen={Boolean(deletingUser)}
        title="Удалить пользователя?"
        description={`Аккаунт ${deletingUser?.username ?? ''} будет удалён без возможности восстановления.`}
        confirmLabel="Удалить пользователя"
        destructive
        isPending={deleteMutation.isPending}
        error={
          deleteMutation.isError
            ? formatErrorMessage(deleteMutation.error, 'Не удалось удалить пользователя')
            : null
        }
        onConfirm={() => {
          if (deletingUser) deleteMutation.mutate(deletingUser);
        }}
        onClose={() => {
          if (deleteMutation.isPending) return;
          setDeletingUser(null);
          deleteMutation.reset();
        }}
      />
    </div>
  );
}
