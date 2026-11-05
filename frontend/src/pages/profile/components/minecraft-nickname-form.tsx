import { useState, type FormEvent } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { Button, Input, toast } from '@/components/ui';
import { updateMinecraftNickname, ProfileApiError } from '@/api';

export interface MinecraftNicknameFormProps {
  currentNickname: string;
}

export function MinecraftNicknameForm({ currentNickname }: MinecraftNicknameFormProps) {
  const queryClient = useQueryClient();
  const [nickname, setNickname] = useState(currentNickname);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    const trimmed = nickname.trim();

    if (!trimmed) {
      setError('Никнейм не может быть пустым');
      return;
    }

    if (trimmed.length < 3 || trimmed.length > 16) {
      setError('Никнейм должен содержать от 3 до 16 символов');
      return;
    }

    if (!/^[a-zA-Z0-9_]+$/.test(trimmed)) {
      setError('Разрешены только латинские буквы, цифры и символ подчеркивания');
      return;
    }

    if (trimmed === currentNickname) {
      setError('Укажите никнейм, отличный от текущего');
      return;
    }

    setIsSubmitting(true);
    setError(null);

    try {
      const updatedProfile = await updateMinecraftNickname(trimmed);
      queryClient.setQueryData(['user-profile'], updatedProfile);
      await queryClient.invalidateQueries({ queryKey: ['user-profile'] });
      toast.success('Никнейм Minecraft успешно обновлен!');
    } catch (err) {
      if (err instanceof ProfileApiError) {
        setError(err.message);
      } else {
        setError('Не удалось обновить никнейм. Попробуйте позже.');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="bg-card flex flex-col gap-4 p-6 md:p-8">
      <div className="flex flex-col gap-1">
        <h3 className="text-xl font-bold text-foreground">Никнейм Minecraft</h3>
        <p className="text-sm text-foreground-muted">
          Этот ник используется для синхронизации скина и отображения вашего персонажа на сервере.
        </p>
      </div>

      <div className="flex flex-col gap-2">
        <label htmlFor="minecraft-nickname-input" className="text-sm font-medium text-foreground">
          Игровой ник
        </label>
        <Input
          id="minecraft-nickname-input"
          value={nickname}
          onChange={(e) => {
            setNickname(e.target.value);
            if (error) setError(null);
          }}
          placeholder="Например, Steve"
          maxLength={16}
          disabled={isSubmitting}
          isError={Boolean(error)}
        />
        {error && (
          <span className="text-error text-xs font-medium">
            {error}
          </span>
        )}
      </div>

      <div className="flex justify-end pt-2">
        <Button
          type="submit"
          variant="primary"
          disabled={isSubmitting || nickname.trim() === currentNickname}
          className="w-full sm:w-auto"
        >
          {isSubmitting ? 'Сохранение...' : 'Сохранить ник'}
        </Button>
      </div>
    </form>
  );
}

