import { useNavigate } from 'react-router';
import { MicrosoftDeviceModal } from './microsoft-device-modal';
import { useState } from 'react';
import { paths } from '@/routes';
import { Button } from '@/components';

export function MicrosoftLoginSection() {
  const navigate = useNavigate();
  const [isDeviceModalOpen, setIsDeviceModalOpen] = useState(false);

  const handleDeviceSuccess = () => {
    setIsDeviceModalOpen(false);
    navigate(paths.home);
  };
  return (
    <div className="flex flex-col gap-6">
      <Button
        type="button"
        variant="secondary"
        onClick={() => setIsDeviceModalOpen(true)}
        className="flex w-full items-center justify-center gap-2.5 py-2.5 font-semibold"
      >
        <svg className="size-4 shrink-0" viewBox="0 0 21 21" fill="none">
          <path d="M1 1h9v9H1V1z" fill="#f25022" />
          <path d="M11 1h9v9h-9V1z" fill="#7fba00" />
          <path d="M1 11h9v9H1v-9z" fill="#00a4ef" />
          <path d="M11 11h9v9h-9v-9z" fill="#ffb900" />
        </svg>
        Войти через Microsoft (Лицензия)
      </Button>

      <MicrosoftDeviceModal
        isOpen={isDeviceModalOpen}
        onClose={() => setIsDeviceModalOpen(false)}
        onSuccess={handleDeviceSuccess}
      />

      <div className="relative flex items-center justify-center">
        <div className="border-border w-full border-t" />
        <span className="bg-background text-foreground-muted absolute px-3 text-xs uppercase">
          или
        </span>
      </div>
    </div>
  );
}
