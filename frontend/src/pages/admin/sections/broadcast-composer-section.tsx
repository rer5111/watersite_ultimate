import { useState, type FormEvent } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { dispatchBroadcast, adminQueryKeys } from '@/api';
import { Button, Input, Select, Textarea, toast } from '@/components/ui';
import type { BroadcastAudience, BroadcastCategory, BroadcastPayload } from '@/types';
import {
  broadcastAudienceLabels,
  broadcastCategoryLabels,
  formatErrorMessage,
} from '../admin-utils';

interface BroadcastFormErrors {
  subject?: string;
  message?: string;
}

export function BroadcastComposerSection() {
  const queryClient = useQueryClient();
  const [subject, setSubject] = useState('');
  const [category, setCategory] = useState<BroadcastCategory>('INFO');
  const [audience, setAudience] = useState<BroadcastAudience>('ALL');
  const [message, setMessage] = useState('');
  const [errors, setErrors] = useState<BroadcastFormErrors>({});

  const mutation = useMutation({
    mutationFn: (payload: BroadcastPayload) => dispatchBroadcast(payload),
    onSuccess: (dispatch) => {
      void queryClient.invalidateQueries({ queryKey: adminQueryKeys.broadcasts });
      toast.success(`Рассылка «${dispatch.subject}» отправлена ${dispatch.recipients} получателям`);
      setSubject('');
      setCategory('INFO');
      setAudience('ALL');
      setMessage('');
      setErrors({});
    },
    onError: (error) => {
      toast.error(formatErrorMessage(error, 'Не удалось отправить рассылку'));
    },
  });

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const nextErrors: BroadcastFormErrors = {};

    if (!subject.trim()) {
      nextErrors.subject = 'Укажите тему рассылки';
    } else if (subject.trim().length < 3) {
      nextErrors.subject = 'Тема должна содержать минимум 3 символа';
    }

    if (!message.trim()) {
      nextErrors.message = 'Введите текст сообщения';
    } else if (message.trim().length < 10) {
      nextErrors.message = 'Сообщение должно содержать минимум 10 символов';
    }

    setErrors(nextErrors);
    if (Object.keys(nextErrors).length > 0) return;

    mutation.mutate({
      subject: subject.trim(),
      category,
      audience,
      message: message.trim(),
    });
  };

  return (
    <section className="border-border bg-card flex flex-col gap-5 rounded-none border p-5 md:p-6">
      <div className="flex flex-col gap-1">
        <h2 className="text-foreground text-lg font-bold">Новая рассылка</h2>
        <p className="text-foreground-muted text-sm">
          Сообщение будет отправлено выбранной аудитории сразу после подтверждения.
        </p>
      </div>

      <form onSubmit={handleSubmit} className="flex flex-col gap-5" noValidate>
        <div className="grid gap-4 md:grid-cols-2">
          <div className="flex flex-col gap-2">
            <label htmlFor="broadcast-subject" className="text-foreground text-sm font-medium">
              Тема <span className="text-error">*</span>
            </label>
            <Input
              id="broadcast-subject"
              value={subject}
              onChange={(event) => {
                setSubject(event.target.value);
                if (errors.subject) setErrors((prev) => ({ ...prev, subject: undefined }));
              }}
              placeholder="Например: технические работы"
              maxLength={120}
              isError={Boolean(errors.subject)}
              disabled={mutation.isPending}
            />
            {errors.subject && (
              <span className="text-error text-xs font-medium">{errors.subject}</span>
            )}
          </div>

          <div className="flex flex-col gap-2">
            <label htmlFor="broadcast-category" className="text-foreground text-sm font-medium">
              Категория
            </label>
            <Select
              id="broadcast-category"
              value={category}
              onChange={(event) => setCategory(event.target.value as BroadcastCategory)}
              disabled={mutation.isPending}
            >
              {Object.entries(broadcastCategoryLabels).map(([value, label]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
            </Select>
          </div>
        </div>

        <div className="flex flex-col gap-2">
          <label htmlFor="broadcast-audience" className="text-foreground text-sm font-medium">
            Аудитория
          </label>
          <Select
            id="broadcast-audience"
            value={audience}
            onChange={(event) => setAudience(event.target.value as BroadcastAudience)}
            disabled={mutation.isPending}
          >
            {Object.entries(broadcastAudienceLabels).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </Select>
        </div>

        <div className="flex flex-col gap-2">
          <div className="flex items-center justify-between gap-3">
            <label htmlFor="broadcast-message" className="text-foreground text-sm font-medium">
              Сообщение <span className="text-error">*</span>
            </label>
            <span className="text-foreground-subtle text-xs">{message.length}/2000</span>
          </div>
          <Textarea
            id="broadcast-message"
            value={message}
            onChange={(event) => {
              setMessage(event.target.value);
              if (errors.message) setErrors((prev) => ({ ...prev, message: undefined }));
            }}
            placeholder="Текст уведомления для игроков"
            maxLength={2000}
            isError={Boolean(errors.message)}
            disabled={mutation.isPending}
          />
          {errors.message && (
            <span className="text-error text-xs font-medium">{errors.message}</span>
          )}
        </div>

        {mutation.isError && (
          <p className="text-error text-sm font-medium">
            {formatErrorMessage(mutation.error, 'Не удалось отправить рассылку')}
          </p>
        )}

        <div className="flex justify-end">
          <Button
            type="submit"
            variant="primary"
            className="rounded-none"
            disabled={mutation.isPending}
          >
            {mutation.isPending ? 'Отправка…' : 'Отправить рассылку'}
          </Button>
        </div>
      </form>
    </section>
  );
}
