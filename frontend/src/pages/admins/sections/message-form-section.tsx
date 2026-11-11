import { useState, type FormEvent } from 'react';
import { createRoleRequest, useProfile } from '@/api';
import { Button, ChevronRightIcon, Input, Textarea } from '@/components';
import { useAuth } from '@/context';
import type { AdminRequestedRole } from '@/types';

export function MessageFormSection() {
  const { user } = useAuth();
  const { data: profile } = useProfile(Boolean(user));
  const [requestedRole, setRequestedRole] = useState<AdminRequestedRole>('ADMIN');
  const [note, setNote] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isSubmitted, setIsSubmitted] = useState(false);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!user || isSubmitting || isSubmitted) return;
    setError(null);
    setIsSubmitting(true);
    try {
      await createRoleRequest({ requestedRole, note: note.trim() });
      setIsSubmitted(true);
      setNote('');
    } catch (submissionError: unknown) {
      setError(
        submissionError instanceof Error ? submissionError.message : 'Не удалось отправить заявку.'
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  const nickname = user ? profile?.minecraftNickname || profile?.username || user.username : '';
  const isFormDisabled = !user || isSubmitting || isSubmitted;

  return (
    <section className="mx-auto flex w-full max-w-7xl flex-col gap-8 px-32 py-16">
      <div className="flex flex-col gap-3">
        <h3 className="text-center text-3xl leading-tight font-bold">
          Хотите стать администратором?
        </h3>
        <h4 className="text-foreground text-center text-xl font-medium">
          Свяжитесь с нами, заполнив форму ниже
        </h4>
      </div>

      <form className="mx-auto flex w-full flex-col gap-6 px-32" onSubmit={handleSubmit}>
        <div className="flex flex-col gap-3">
          <label htmlFor="nickname" className="text-foreground text-sm font-semibold">
            Никнейм
          </label>
          <Input
            id="nickname"
            name="nickname"
            value={nickname}
            placeholder="Никнейм"
            readOnly
            disabled={!user}
            className="disabled:cursor-not-allowed disabled:opacity-50"
          />
        </div>

        <div className="flex flex-col gap-3">
          <label htmlFor="requestedRole" className="text-foreground text-sm font-semibold">
            На какую роль подать заявку
          </label>
          <select
            id="requestedRole"
            name="requestedRole"
            value={requestedRole}
            onChange={(event) => setRequestedRole(event.target.value as AdminRequestedRole)}
            disabled={isFormDisabled}
            className="text-foreground bg-input focus-visible:ring-foreground w-full rounded-none border border-transparent p-3 text-sm font-medium focus-visible:ring-2 focus-visible:outline-none disabled:cursor-not-allowed disabled:opacity-50"
          >
            <option value="MODERATOR">Модератор</option>
            <option value="ADMIN">Администратор</option>
          </select>
        </div>

        <div className="flex flex-col gap-3">
          <label htmlFor="message" className="text-foreground text-sm font-semibold">
            Сообщение
          </label>
          <Textarea
            id="message"
            name="message"
            placeholder="Введите ваше сообщение (от 10 до 2000 символов)"
            value={note}
            onChange={(event) => setNote(event.target.value)}
            minLength={10}
            maxLength={2000}
            required
            disabled={isFormDisabled}
            className="disabled:cursor-not-allowed disabled:opacity-50"
          />
        </div>

        {!user && (
          <p className="text-error text-sm" role="alert">
            Войдите в аккаунт, чтобы отправить заявку.
          </p>
        )}
        {error && (
          <p className="text-error text-sm" role="alert">
            {error}
          </p>
        )}
        {isSubmitted && (
          <p className="text-sm" role="status">
            Заявка отправлена и ожидает рассмотрения.
          </p>
        )}
        <Button
          type="submit"
          variant="primary"
          trailingIcon={<ChevronRightIcon />}
          disabled={isFormDisabled || note.trim().length < 10}
        >
          {isSubmitting ? 'Отправка…' : isSubmitted ? 'Заявка отправлена' : 'Отправить'}
        </Button>
      </form>
    </section>
  );
}
