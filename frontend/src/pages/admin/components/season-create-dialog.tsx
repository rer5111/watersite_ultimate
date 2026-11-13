import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Button, Dialog, Input, Select, Textarea, toast } from '@/components/ui';
import { adminQueryKeys, createAdminSeason } from '@/api';
import type { AdminSeasonStatus, CreateAdminSeasonPayload } from '@/types';
import { seasonStatusLabels, formatErrorMessage } from '../admin-utils';

export interface SeasonCreateDialogProps {
  onClose: () => void;
}

interface SeasonFormErrors {
  code?: string;
  title?: string;
  organizer?: string;
  startsAt?: string;
  description?: string;
}

export function SeasonCreateDialog({ onClose }: SeasonCreateDialogProps) {
  const queryClient = useQueryClient();
  const [code, setCode] = useState('');
  const [title, setTitle] = useState('');
  const [organizer, setOrganizer] = useState('');
  const [startsAt, setStartsAt] = useState('');
  const [status, setStatus] = useState<AdminSeasonStatus>('DRAFT');
  const [description, setDescription] = useState('');
  const [errors, setErrors] = useState<SeasonFormErrors>({});

  const mutation = useMutation({
    mutationFn: (payload: CreateAdminSeasonPayload) => createAdminSeason(payload),
    onSuccess: (created) => {
      void queryClient.invalidateQueries({ queryKey: adminQueryKeys.seasons });
      toast.success(`Сезон «${created.title}» создан`);
      onClose();
    },
    onError: (error) => {
      toast.error(formatErrorMessage(error, 'Не удалось создать сезон'));
    },
  });

  const handleSubmit = () => {
    const nextErrors: SeasonFormErrors = {};
    const trimmedCode = code.trim();
    const trimmedTitle = title.trim();
    const trimmedOrganizer = organizer.trim();
    const trimmedDescription = description.trim();
    const parsedDate = new Date(startsAt);

    if (!trimmedCode) nextErrors.code = 'Укажите код сезона (например: season-6)';
    if (!trimmedTitle) nextErrors.title = 'Укажите название сезона';
    if (!trimmedOrganizer) nextErrors.organizer = 'Укажите организатора';
    if (!startsAt || Number.isNaN(parsedDate.getTime())) {
      nextErrors.startsAt = 'Укажите корректные дату и время';
    }
    if (!trimmedDescription) nextErrors.description = 'Добавьте описание сезона';

    setErrors(nextErrors);
    if (Object.keys(nextErrors).length > 0) return;

    mutation.mutate({
      code: trimmedCode,
      title: trimmedTitle,
      organizer: trimmedOrganizer,
      startsAt: parsedDate.toISOString(),
      status,
      description: trimmedDescription,
    });
  };

  return (
    <Dialog
      isOpen
      title="Создать сезон"
      description="Новый сезон сервера Waltercraft"
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
            {mutation.isPending ? 'Создание…' : 'Создать'}
          </Button>
        </div>
      }
    >
      <div className="flex flex-col gap-4">
        <div className="grid gap-4 sm:grid-cols-2">
          <div className="flex flex-col gap-2">
            <label htmlFor="create-season-code" className="text-foreground text-sm font-medium">
              Код сезона
            </label>
            <Input
              id="create-season-code"
              placeholder="season-6"
              value={code}
              onChange={(event) => {
                setCode(event.target.value);
                if (errors.code) setErrors((prev) => ({ ...prev, code: undefined }));
              }}
              isError={Boolean(errors.code)}
              disabled={mutation.isPending}
            />
            {errors.code && <span className="text-error text-xs font-medium">{errors.code}</span>}
          </div>

          <div className="flex flex-col gap-2">
            <label htmlFor="create-season-title" className="text-foreground text-sm font-medium">
              Название
            </label>
            <Input
              id="create-season-title"
              placeholder="6 сезон"
              value={title}
              onChange={(event) => {
                setTitle(event.target.value);
                if (errors.title) setErrors((prev) => ({ ...prev, title: undefined }));
              }}
              isError={Boolean(errors.title)}
              disabled={mutation.isPending}
            />
            {errors.title && <span className="text-error text-xs font-medium">{errors.title}</span>}
          </div>
        </div>

        <div className="grid gap-4 sm:grid-cols-2">
          <div className="flex flex-col gap-2">
            <label
              htmlFor="create-season-organizer"
              className="text-foreground text-sm font-medium"
            >
              Организатор
            </label>
            <Input
              id="create-season-organizer"
              placeholder="Никнейм организатора"
              value={organizer}
              onChange={(event) => {
                setOrganizer(event.target.value);
                if (errors.organizer) setErrors((prev) => ({ ...prev, organizer: undefined }));
              }}
              isError={Boolean(errors.organizer)}
              disabled={mutation.isPending}
            />
            {errors.organizer && (
              <span className="text-error text-xs font-medium">{errors.organizer}</span>
            )}
          </div>

          <div className="flex flex-col gap-2">
            <label htmlFor="create-season-status" className="text-foreground text-sm font-medium">
              Статус
            </label>
            <Select
              id="create-season-status"
              value={status}
              onChange={(event) => setStatus(event.target.value as AdminSeasonStatus)}
              disabled={mutation.isPending}
            >
              {Object.entries(seasonStatusLabels).map(([value, label]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
            </Select>
          </div>
        </div>

        <div className="flex flex-col gap-2">
          <label htmlFor="create-season-date" className="text-foreground text-sm font-medium">
            Дата и время старта
          </label>
          <Input
            id="create-season-date"
            type="datetime-local"
            value={startsAt}
            onChange={(event) => {
              setStartsAt(event.target.value);
              if (errors.startsAt) setErrors((prev) => ({ ...prev, startsAt: undefined }));
            }}
            isError={Boolean(errors.startsAt)}
            disabled={mutation.isPending}
          />
          {errors.startsAt && (
            <span className="text-error text-xs font-medium">{errors.startsAt}</span>
          )}
        </div>

        <div className="flex flex-col gap-2">
          <label
            htmlFor="create-season-description"
            className="text-foreground text-sm font-medium"
          >
            Описание
          </label>
          <Textarea
            id="create-season-description"
            placeholder="Описание сезона..."
            value={description}
            onChange={(event) => {
              setDescription(event.target.value);
              if (errors.description) {
                setErrors((prev) => ({ ...prev, description: undefined }));
              }
            }}
            className="h-32"
            isError={Boolean(errors.description)}
            disabled={mutation.isPending}
          />
          {errors.description && (
            <span className="text-error text-xs font-medium">{errors.description}</span>
          )}
        </div>

        {mutation.isError && (
          <p className="text-error text-sm font-medium">
            {formatErrorMessage(mutation.error, 'Не удалось создать сезон')}
          </p>
        )}
      </div>
    </Dialog>
  );
}
