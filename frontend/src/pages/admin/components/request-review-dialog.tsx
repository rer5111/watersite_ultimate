import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Button, Dialog, Textarea, toast } from '@/components/ui';
import { adminQueryKeys, approveRoleRequest, rejectRoleRequest } from '@/api';
import type { AdminRoleRequest } from '@/types';
import {
  formatDateTime,
  formatErrorMessage,
  requestStatusLabels,
  requestStatusTones,
  userRoleLabels,
} from '../admin-utils';
import { StatusBadge } from './status-badge';

export interface RequestReviewDialogProps {
  request: AdminRoleRequest;
  onClose: () => void;
}

export function RequestReviewDialog({ request, onClose }: RequestReviewDialogProps) {
  const queryClient = useQueryClient();
  const [isRejecting, setIsRejecting] = useState(false);
  const [reason, setReason] = useState('');

  const approveMutation = useMutation({
    mutationFn: () => approveRoleRequest(request.id),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: adminQueryKeys.requests });
      void queryClient.invalidateQueries({ queryKey: adminQueryKeys.users });
      toast.success(`Заявка ${request.applicantName} одобрена, роль обновлена`);
      onClose();
    },
    onError: (error) => {
      toast.error(formatErrorMessage(error, 'Не удалось одобрить заявку'));
    },
  });

  const rejectMutation = useMutation({
    mutationFn: (rejectionReason: string) => rejectRoleRequest(request.id, rejectionReason),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: adminQueryKeys.requests });
      toast.success(`Заявка ${request.applicantName} отклонена`);
      onClose();
    },
    onError: (error) => {
      toast.error(formatErrorMessage(error, 'Не удалось отклонить заявку'));
    },
  });

  const isResolved = request.status !== 'PENDING';
  const isPending = approveMutation.isPending || rejectMutation.isPending;

  const footer = (() => {
    if (isResolved) {
      return (
        <div className="flex justify-end">
          <Button type="button" variant="secondary" className="rounded-none" onClick={onClose}>
            Закрыть
          </Button>
        </div>
      );
    }

    if (isRejecting) {
      return (
        <div className="flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
          <Button
            type="button"
            variant="secondary"
            className="rounded-none"
            onClick={() => setIsRejecting(false)}
            disabled={isPending}
          >
            Назад
          </Button>
          <Button
            type="button"
            variant="primary"
            className="bg-error hover:bg-error-hover rounded-none"
            onClick={() => rejectMutation.mutate(reason)}
            disabled={isPending || !reason.trim()}
          >
            {rejectMutation.isPending ? 'Отклонение…' : 'Отклонить заявку'}
          </Button>
        </div>
      );
    }

    return (
      <div className="flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
        <Button
          type="button"
          variant="secondary"
          className="border-error/50 text-error hover:bg-error hover:text-foreground rounded-none border"
          onClick={() => setIsRejecting(true)}
          disabled={isPending}
        >
          Отклонить
        </Button>
        <Button
          type="button"
          variant="primary"
          className="rounded-none"
          onClick={() => approveMutation.mutate()}
          disabled={isPending}
        >
          {approveMutation.isPending ? 'Одобрение…' : 'Одобрить'}
        </Button>
      </div>
    );
  })();

  return (
    <Dialog
      isOpen
      title="Заявка на роль"
      description={`Заявка №${request.id} от ${formatDateTime(request.submittedAt)}`}
      onClose={onClose}
      footer={footer}
    >
      <div className="flex flex-col gap-5">
        <div className="grid gap-4 sm:grid-cols-2">
          <div className="flex flex-col gap-1">
            <span className="text-foreground-muted text-xs tracking-wider uppercase">
              Заявитель
            </span>
            <span className="text-foreground text-sm font-medium">{request.applicantName}</span>
            <span className="text-foreground-muted text-xs">{request.applicantEmail}</span>
          </div>
          <div className="flex flex-col gap-1">
            <span className="text-foreground-muted text-xs tracking-wider uppercase">
              Запрашиваемая роль
            </span>
            <span className="text-foreground text-sm font-medium">
              {userRoleLabels[request.requestedRole]}
            </span>
          </div>
          <div className="flex flex-col gap-1">
            <span className="text-foreground-muted text-xs tracking-wider uppercase">Статус</span>
            <div>
              <StatusBadge
                label={requestStatusLabels[request.status]}
                tone={requestStatusTones[request.status]}
              />
            </div>
          </div>
          {request.reviewedAt && (
            <div className="flex flex-col gap-1">
              <span className="text-foreground-muted text-xs tracking-wider uppercase">
                Рассмотрена
              </span>
              <span className="text-foreground text-sm font-medium">
                {formatDateTime(request.reviewedAt)}
              </span>
            </div>
          )}
        </div>

        <div className="flex flex-col gap-2">
          <span className="text-foreground-muted text-xs tracking-wider uppercase">
            Обоснование
          </span>
          <p className="border-border bg-surface text-foreground-muted rounded-none border p-4 text-sm leading-relaxed whitespace-pre-line">
            {request.note}
          </p>
        </div>

        {request.rejectionReason && (
          <div className="flex flex-col gap-2">
            <span className="text-foreground-muted text-xs tracking-wider uppercase">
              Причина отклонения
            </span>
            <p className="border-error/40 bg-error/10 text-foreground rounded-none border p-4 text-sm leading-relaxed">
              {request.rejectionReason}
            </p>
          </div>
        )}

        {isRejecting && (
          <div className="flex flex-col gap-2">
            <label htmlFor="rejection-reason" className="text-foreground text-sm font-medium">
              Причина отклонения <span className="text-error">*</span>
            </label>
            <Textarea
              id="rejection-reason"
              value={reason}
              onChange={(event) => setReason(event.target.value)}
              placeholder="Опишите, что нужно исправить или почему заявка отклонена"
              className="h-28"
              isError={Boolean(rejectMutation.isError)}
              disabled={isPending}
              autoFocus
            />
            <span className="text-foreground-subtle text-xs">Комментарий увидит заявитель.</span>
          </div>
        )}

        {(approveMutation.isError || rejectMutation.isError) && (
          <p className="text-error text-sm font-medium">
            {formatErrorMessage(
              approveMutation.error ?? rejectMutation.error,
              'Не удалось обработать заявку'
            )}
          </p>
        )}
      </div>
    </Dialog>
  );
}
