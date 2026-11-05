import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Button, Dialog, Select, toast } from '@/components/ui';
import { adminQueryKeys, updateAdminUser } from '@/api';
import type { AdminUser, AdminUserRole, AdminUserStatus, UpdateAdminUserPayload } from '@/types';
import { formatErrorMessage, userRoleLabels, userStatusLabels } from '../admin-utils';

export interface UserEditDialogProps {
  user: AdminUser;
  onClose: () => void;
}

export function UserEditDialog({ user, onClose }: UserEditDialogProps) {
  const queryClient = useQueryClient();
  const [role, setRole] = useState<AdminUserRole>(user.role);
  const [status, setStatus] = useState<AdminUserStatus>(user.status);

  const mutation = useMutation({
    mutationFn: (payload: UpdateAdminUserPayload) => updateAdminUser(user.id, payload),
    onSuccess: (updated) => {
      void queryClient.invalidateQueries({ queryKey: adminQueryKeys.users });
      toast.success(`Пользователь ${updated.username} обновлён`);
      onClose();
    },
    onError: (error) => {
      toast.error(formatErrorMessage(error, 'Не удалось обновить пользователя'));
    },
  });

  const handleSubmit = () => {
    if (role === user.role && status === user.status) {
      toast.info('Изменения не выбраны');
      return;
    }
    mutation.mutate({ role, status });
  };

  return (
    <Dialog
      isOpen
      title="Изменить пользователя"
      description={`${user.displayName} · ${user.email}`}
      onClose={onClose}
      footer={
        <div className="flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
          <Button
            type="button"
            variant="secondary"
            className="rounded-none"
            onClick={onClose}
            disabled={mutation.isPending}
          >
            Отмена
          </Button>
          <Button
            type="button"
            variant="primary"
            className="rounded-none"
            onClick={handleSubmit}
            disabled={mutation.isPending}
          >
            {mutation.isPending ? 'Сохранение…' : 'Сохранить'}
          </Button>
        </div>
      }
    >
      <div className="flex flex-col gap-5">
        <div className="grid gap-4 sm:grid-cols-2">
          <div className="flex flex-col gap-2">
            <label htmlFor={`user-role-${user.id}`} className="text-foreground text-sm font-medium">
              Роль
            </label>
            <Select
              id={`user-role-${user.id}`}
              value={role}
              onChange={(event) => setRole(event.target.value as AdminUserRole)}
              disabled={mutation.isPending}
            >
              {Object.entries(userRoleLabels).map(([value, label]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
            </Select>
          </div>

          <div className="flex flex-col gap-2">
            <label
              htmlFor={`user-status-${user.id}`}
              className="text-foreground text-sm font-medium"
            >
              Статус
            </label>
            <Select
              id={`user-status-${user.id}`}
              value={status}
              onChange={(event) => setStatus(event.target.value as AdminUserStatus)}
              disabled={mutation.isPending}
            >
              {Object.entries(userStatusLabels).map(([value, label]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
            </Select>
          </div>
        </div>

        {mutation.isError && (
          <p className="text-error text-sm font-medium">
            {formatErrorMessage(mutation.error, 'Не удалось обновить пользователя')}
          </p>
        )}
      </div>
    </Dialog>
  );
}
