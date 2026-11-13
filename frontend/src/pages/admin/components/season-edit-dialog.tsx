import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Button, Dialog, Input, Select, Textarea, toast } from '@/components/ui';
import { adminQueryKeys, updateAdminSeason } from '@/api';
import type { AdminSeason, AdminSeasonStatus, UpdateAdminSeasonPayload } from '@/types';
import { seasonStatusLabels, formatErrorMessage, toDateTimeLocalValue } from '../admin-utils';

export interface SeasonEditDialogProps {
  season: AdminSeason;
  onClose: () => void;
}

interface SeasonFormErrors {
  title?: string;
  organizer?: string;
  startsAt?: string;
  description?: string;
}

export function SeasonEditDialog({ season, onClose }: SeasonEditDialogProps) {
  const queryClient = useQueryClient();
  const [title, setTitle] = useState(season.title);
  const [organizer, setOrganizer] = useState(season.organizer);
  const [startsAt, setStartsAt] = useState(toDateTimeLocalValue(season.startsAt));
  const [status, setStatus] = useState<AdminSeasonStatus>(season.status);
  const [description, setDescription] = useState(season.description);
  const [errors, setErrors] = useState<SeasonFormErrors>({});

  const mutation = useMutation({
    mutationFn: (payload: UpdateAdminSeasonPayload) => updateAdminSeason(season.id, payload),
    onSuccess: (updated) => {
      void queryClient.invalidateQueries({ queryKey: adminQueryKeys.seasons });
      toast.success(`Сезон «${updated.title}» обновлен`);
      onClose();
    },
    onError: (error) => {
      toast.error(formatErrorMessage(error, 'Не удалось обновить сезон'));
    },
  });

  const handleSubmit = () => {
    const nextErrors: SeasonFormErrors = {};
    const trimmedTitle = title.trim();
    const trimmedOrganizer = organizer.trim();
    const trimmedDescription = description.trim();
    const parsedDate = new Date(startsAt);

    if (!trimmedTitle) nextErrors.title = 'Укажите название сезона';
    if (!trimmedOrganizer) nextErrors.organizer = 'Укажите организатора';
    if (!startsAt || Number.isNaN(parsedDate.getTime())) {
      nextErrors.startsAt = 'Укажите корректные дату и время';
    }
    if (!trimmedDescription) nextErrors.description = 'Добавьте описание сезона';

    setErrors(nextErrors);
    if (Object.keys(nextErrors).length > 0) return;

    mutation.mutate({
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
      title="Редактировать сезон"
      description={`Код: ${season.code}`}
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
      <div className="flex flex-col gap-4">
        <div className="flex flex-col gap-2">
          <label htmlFor="season-title" className="text-foreground text-sm font-medium">
            Название
          </label>
          <Input
            id="season-title"
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

        <div className="grid gap-4 sm:grid-cols-2">
          <div className="flex flex-col gap-2">
            <label htmlFor="season-organizer" className="text-foreground text-sm font-medium">
              Организатор
            </label>
            <Input
              id="season-organizer"
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
            <label htmlFor="season-status" className="text-foreground text-sm font-medium">
              Статус
            </label>
            <Select
              id="season-status"
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
          <label htmlFor="season-date" className="text-foreground text-sm font-medium">
            Дата и время
          </label>
          <Input
            id="season-date"
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
          <label htmlFor="season-description" className="text-foreground text-sm font-medium">
            Описание
          </label>
          <Textarea
            id="season-description"
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
            {formatErrorMessage(mutation.error, 'Не удалось обновить сезон')}
          </p>
        )}
      </div>
    </Dialog>
  );
}
