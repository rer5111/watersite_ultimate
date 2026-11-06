import { useState } from 'react';
import { Button } from '@/components/ui';
import { DeleteAccountDialog } from '../components';

export interface DangerZoneSectionProps {
  username: string;
}

export function DangerZoneSection({ username }: DangerZoneSectionProps) {
  const [isDialogOpen, setIsDialogOpen] = useState(false);

  return (
    <section aria-label="Опасная зона" className="flex flex-col gap-4">
      <div className="bg-card flex flex-col gap-4 p-6 md:p-8">
        <div className="flex flex-col gap-1">
          <h2 className="text-error text-xl font-bold">Опасная зона</h2>
          <p className="text-foreground-muted text-sm">
            Удаление учётной записи необратимо. Все игровые достижения, привязанный аккаунт
            Minecraft и данные доступа будут стёрты навсегда.
          </p>
        </div>

        <div className="flex justify-end pt-2">
          <Button
            type="button"
            variant="secondary"
            onClick={() => setIsDialogOpen(true)}
            className="bg-error hover:bg-error-hover text-white"
          >
            Удалить аккаунт
          </Button>
        </div>
      </div>

      <DeleteAccountDialog
        isOpen={isDialogOpen}
        username={username}
        onClose={() => setIsDialogOpen(false)}
      />
    </section>
  );
}
