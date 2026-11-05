import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Button, Dialog, Input, Select, Textarea, toast } from '@/components/ui';
import { adminQueryKeys, updateAdminEvent } from '@/api';
import type { AdminEvent, AdminEventStatus, UpdateAdminEventPayload } from '@/types';
import { eventStatusLabels, formatErrorMessage, toDateTimeLocalValue } from '../admin-utils';

export interface EventEditDialogProps {
  event: AdminEvent;
  onClose: () => void;
}

interface EventFormErrors {
  title?: string;
  organizer?: string;
  startsAt?: string;
  description?: string;
}

export function EventEditDialog({ event, onClose }: EventEditDialogProps) {
  const queryClient = useQueryClient();
  const [title, setTitle] = useState(event.title);
  const [organizer, setOrganizer] = useState(event.organizer);
  const [startsAt, setStartsAt] = useState(toDateTimeLocalValue(event.startsAt));
  const [status, setStatus] = useState<AdminEventStatus>(event.status);
  const [description, setDescription] = useState(event.description);
  const [errors, setErrors] = useState<EventFormErrors>({});

  const mutation = useMutation({
    mutationFn: (payload: UpdateAdminEventPayload) => updateAdminEvent(event.id, payload),
    onSuccess: (updated) => {
      void queryClient.invalidateQueries({ queryKey: adminQueryKeys.events });
      toast.success(`Событие «${updated.title}» обновлено`);
      onClose();
    },
    onError: (error) => {
      toast.error(formatErrorMessage(error, 'Не удалось обновить событие'));
    },
  });

  const handleSubmit = () => {
    const nextErrors: EventFormErrors = {};
    const trimmedTitle = title.trim();
    const trimmedOrganizer = organizer.trim();
    const trimmedDescription = description.trim();
    const parsedDate = new Date(startsAt);

    if (!trimmedTitle) nextErrors.title = 'Укажите название события';
    if (!trimmedOrganizer) nextErrors.organizer = 'Укажите организатора';
    if (!startsAt || Number.isNaN(parsedDate.getTime())) {
      nextErrors.startsAt = 'Укажите корректные дату и время';
    }
    if (!trimmedDescription) nextErrors.description = 'Добавьте описание события';

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
      title="Редактировать событие"
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
          <label htmlFor="event-title" className="text-foreground text-sm font-medium">
            Название
          </label>
          <Input
            id="event-title"
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
            <label htmlFor="event-organizer" className="text-foreground text-sm font-medium">
              Организатор
            </label>
            <Input
              id="event-organizer"
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
            <label htmlFor="event-status" className="text-foreground text-sm font-medium">
              Статус
            </label>
            <Select
              id="event-status"
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

        <div className="flex flex-col gap-2">
          <label htmlFor="event-date" className="text-foreground text-sm font-medium">
            Дата и время
          </label>
          <Input
            id="event-date"
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
          <label htmlFor="event-description" className="text-foreground text-sm font-medium">
            Описание
          </label>
          <Textarea
            id="event-description"
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
            {formatErrorMessage(mutation.error, 'Не удалось обновить событие')}
          </p>
        )}
      </div>
    </Dialog>
  );
}
