import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Button, Dialog, Input, Select, Textarea, toast } from '@/components/ui';
import { adminQueryKeys, updateAdminEvent } from '@/api';
import type { AdminEvent, AdminEventStatus, UpdateAdminEventPayload } from '@/types';
import { eventStatusLabels, formatErrorMessage } from '../admin-utils';

export interface EventEditDialogProps {
  event: AdminEvent;
  onClose: () => void;
}

interface EventFormErrors {
  title?: string;
  seasonNumber?: string;
  organizer?: string;
  description?: string;
}

export function EventEditDialog({ event, onClose }: EventEditDialogProps) {
  const queryClient = useQueryClient();
  const [title, setTitle] = useState(event.title);
  const [seasonNumber, setSeasonNumber] = useState(String(event.seasonNumber));
  const [organizer, setOrganizer] = useState(event.organizer);
  const [status, setStatus] = useState<AdminEventStatus>(event.status);
  const [startDate, setStartDate] = useState(event.startDate ?? '');
  const [endDate, setEndDate] = useState(event.endDate ?? '');
  const [shortDescription, setShortDescription] = useState(event.shortDescription ?? '');
  const [description, setDescription] = useState(event.description ?? '');
  const [image, setImage] = useState(event.image ?? '');
  const [errors, setErrors] = useState<EventFormErrors>({});

  const mutation = useMutation({
    mutationFn: (payload: UpdateAdminEventPayload) => updateAdminEvent(event.id, payload),
    onSuccess: (updated) => {
      void queryClient.invalidateQueries({ queryKey: adminQueryKeys.events });
      toast.success(`Ивент «${updated.title}» обновлен`);
      onClose();
    },
    onError: (error) => {
      toast.error(formatErrorMessage(error, 'Не удалось обновить ивент'));
    },
  });

  const handleSubmit = () => {
    const nextErrors: EventFormErrors = {};
    const trimmedTitle = title.trim();
    const trimmedOrganizer = organizer.trim();
    const trimmedDescription = description.trim();
    const parsedSeason = parseInt(seasonNumber, 10);

    if (!trimmedTitle) nextErrors.title = 'Укажите название ивента';
    if (!parsedSeason || parsedSeason < 1)
      nextErrors.seasonNumber = 'Укажите корректный номер сезона';
    if (!trimmedOrganizer) nextErrors.organizer = 'Укажите организатора';
    if (!trimmedDescription) nextErrors.description = 'Добавьте описание ивента';

    setErrors(nextErrors);
    if (Object.keys(nextErrors).length > 0) return;

    mutation.mutate({
      title: trimmedTitle,
      seasonNumber: parsedSeason,
      organizer: trimmedOrganizer,
      status,
      startDate: startDate || null,
      endDate: endDate || null,
      shortDescription: shortDescription.trim() || undefined,
      description: trimmedDescription,
      image: image.trim() || undefined,
    });
  };

  return (
    <Dialog
      isOpen
      title="Редактировать ивент"
      description={`Код: ${event.code}`}
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
          <label htmlFor="edit-event-title" className="text-foreground text-sm font-medium">
            Название
          </label>
          <Input
            id="edit-event-title"
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

        <div className="grid gap-4 sm:grid-cols-3">
          <div className="flex flex-col gap-2">
            <label htmlFor="edit-event-season" className="text-foreground text-sm font-medium">
              Номер сезона
            </label>
            <Input
              id="edit-event-season"
              type="number"
              min="1"
              value={seasonNumber}
              onChange={(event) => {
                setSeasonNumber(event.target.value);
                if (errors.seasonNumber)
                  setErrors((prev) => ({ ...prev, seasonNumber: undefined }));
              }}
              isError={Boolean(errors.seasonNumber)}
              disabled={mutation.isPending}
            />
            {errors.seasonNumber && (
              <span className="text-error text-xs font-medium">{errors.seasonNumber}</span>
            )}
          </div>

          <div className="flex flex-col gap-2">
            <label htmlFor="edit-event-organizer" className="text-foreground text-sm font-medium">
              Организатор
            </label>
            <Input
              id="edit-event-organizer"
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
            <label htmlFor="edit-event-status" className="text-foreground text-sm font-medium">
              Статус
            </label>
            <Select
              id="edit-event-status"
              value={status}
              onChange={(event) => setStatus(event.target.value as AdminEventStatus)}
              disabled={mutation.isPending}
            >
              {Object.entries(eventStatusLabels).map(([value, label]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
            </Select>
          </div>
        </div>

        <div className="grid gap-4 sm:grid-cols-2">
          <div className="flex flex-col gap-2">
            <label htmlFor="edit-event-start-date" className="text-foreground text-sm font-medium">
              Дата начала
            </label>
            <Input
              id="edit-event-start-date"
              type="date"
              value={startDate}
              onChange={(event) => setStartDate(event.target.value)}
              disabled={mutation.isPending}
            />
          </div>

          <div className="flex flex-col gap-2">
            <label htmlFor="edit-event-end-date" className="text-foreground text-sm font-medium">
              Дата окончания
            </label>
            <Input
              id="edit-event-end-date"
              type="date"
              value={endDate}
              onChange={(event) => setEndDate(event.target.value)}
              disabled={mutation.isPending}
            />
          </div>
        </div>

        <div className="flex flex-col gap-2">
          <label htmlFor="edit-event-image" className="text-foreground text-sm font-medium">
            URL обложки
          </label>
          <Input
            id="edit-event-image"
            value={image}
            onChange={(event) => setImage(event.target.value)}
            disabled={mutation.isPending}
          />
        </div>

        <div className="flex flex-col gap-2">
          <label htmlFor="edit-event-short-desc" className="text-foreground text-sm font-medium">
            Краткое описание
          </label>
          <Input
            id="edit-event-short-desc"
            value={shortDescription}
            onChange={(event) => setShortDescription(event.target.value)}
            disabled={mutation.isPending}
          />
        </div>

        <div className="flex flex-col gap-2">
          <label htmlFor="edit-event-description" className="text-foreground text-sm font-medium">
            Полное описание
          </label>
          <Textarea
            id="edit-event-description"
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
            {formatErrorMessage(mutation.error, 'Не удалось обновить ивент')}
          </p>
        )}
      </div>
    </Dialog>
  );
}
