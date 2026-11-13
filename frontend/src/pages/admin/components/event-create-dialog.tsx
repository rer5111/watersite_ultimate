import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Button, Dialog, Input, Select, Textarea, toast } from '@/components/ui';
import { adminQueryKeys, createAdminEvent } from '@/api';
import type { AdminEventStatus, CreateAdminEventPayload } from '@/types';
import { eventStatusLabels, formatErrorMessage } from '../admin-utils';

export interface EventCreateDialogProps {
  onClose: () => void;
}

interface EventFormErrors {
  code?: string;
  title?: string;
  seasonNumber?: string;
  organizer?: string;
  description?: string;
}

export function EventCreateDialog({ onClose }: EventCreateDialogProps) {
  const queryClient = useQueryClient();
  const [code, setCode] = useState('');
  const [title, setTitle] = useState('');
  const [seasonNumber, setSeasonNumber] = useState('1');
  const [organizer, setOrganizer] = useState('');
  const [status, setStatus] = useState<AdminEventStatus>('PLANNED');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [shortDescription, setShortDescription] = useState('');
  const [description, setDescription] = useState('');
  const [image, setImage] = useState('');
  const [errors, setErrors] = useState<EventFormErrors>({});

  const mutation = useMutation({
    mutationFn: (payload: CreateAdminEventPayload) => createAdminEvent(payload),
    onSuccess: (created) => {
      void queryClient.invalidateQueries({ queryKey: adminQueryKeys.events });
      toast.success(`Ивент «${created.title}» создан`);
      onClose();
    },
    onError: (error) => {
      toast.error(formatErrorMessage(error, 'Не удалось создать ивент'));
    },
  });

  const handleSubmit = () => {
    const nextErrors: EventFormErrors = {};
    const trimmedCode = code.trim();
    const trimmedTitle = title.trim();
    const trimmedOrganizer = organizer.trim();
    const trimmedDescription = description.trim();
    const parsedSeason = parseInt(seasonNumber, 10);

    if (!trimmedCode) nextErrors.code = 'Укажите код ивента (например: event-1)';
    if (!trimmedTitle) nextErrors.title = 'Укажите название ивента';
    if (!parsedSeason || parsedSeason < 1)
      nextErrors.seasonNumber = 'Укажите корректный номер сезона';
    if (!trimmedOrganizer) nextErrors.organizer = 'Укажите организатора';
    if (!trimmedDescription) nextErrors.description = 'Добавьте описание ивента';

    setErrors(nextErrors);
    if (Object.keys(nextErrors).length > 0) return;

    mutation.mutate({
      code: trimmedCode,
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
      title="Создать ивент"
      description="Новое событие на сервере Waltercraft"
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
            <label htmlFor="create-event-code" className="text-foreground text-sm font-medium">
              Код ивента
            </label>
            <Input
              id="create-event-code"
              placeholder="event-1"
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
            <label htmlFor="create-event-title" className="text-foreground text-sm font-medium">
              Название
            </label>
            <Input
              id="create-event-title"
              placeholder="Битва за Ватерград"
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

        <div className="grid gap-4 sm:grid-cols-3">
          <div className="flex flex-col gap-2">
            <label htmlFor="create-event-season" className="text-foreground text-sm font-medium">
              Номер сезона
            </label>
            <Input
              id="create-event-season"
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
            <label htmlFor="create-event-organizer" className="text-foreground text-sm font-medium">
              Организатор
            </label>
            <Input
              id="create-event-organizer"
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
            <label htmlFor="create-event-status" className="text-foreground text-sm font-medium">
              Статус
            </label>
            <Select
              id="create-event-status"
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
            <label
              htmlFor="create-event-start-date"
              className="text-foreground text-sm font-medium"
            >
              Дата начала
            </label>
            <Input
              id="create-event-start-date"
              type="date"
              value={startDate}
              onChange={(event) => setStartDate(event.target.value)}
              disabled={mutation.isPending}
            />
          </div>

          <div className="flex flex-col gap-2">
            <label htmlFor="create-event-end-date" className="text-foreground text-sm font-medium">
              Дата окончания
            </label>
            <Input
              id="create-event-end-date"
              type="date"
              value={endDate}
              onChange={(event) => setEndDate(event.target.value)}
              disabled={mutation.isPending}
            />
          </div>
        </div>

        <div className="flex flex-col gap-2">
          <label htmlFor="create-event-image" className="text-foreground text-sm font-medium">
            URL обложки
          </label>
          <Input
            id="create-event-image"
            placeholder="/images/events/event-1.webp"
            value={image}
            onChange={(event) => setImage(event.target.value)}
            disabled={mutation.isPending}
          />
        </div>

        <div className="flex flex-col gap-2">
          <label htmlFor="create-event-short-desc" className="text-foreground text-sm font-medium">
            Краткое описание
          </label>
          <Input
            id="create-event-short-desc"
            placeholder="Краткое описание для карточки..."
            value={shortDescription}
            onChange={(event) => setShortDescription(event.target.value)}
            disabled={mutation.isPending}
          />
        </div>

        <div className="flex flex-col gap-2">
          <label htmlFor="create-event-description" className="text-foreground text-sm font-medium">
            Полное описание
          </label>
          <Textarea
            id="create-event-description"
            placeholder="Подробное описание ивента, правила, призы..."
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
            {formatErrorMessage(mutation.error, 'Не удалось создать ивент')}
          </p>
        )}
      </div>
    </Dialog>
  );
}
