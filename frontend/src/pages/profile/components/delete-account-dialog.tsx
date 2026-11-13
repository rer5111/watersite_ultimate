import { useCallback, useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router';
import { Button, Input } from '@/components/ui';
import { deleteAccount, ProfileApiError } from '@/api';
import { useAuth } from '@/context';
import { paths } from '@/routes';

export interface DeleteAccountDialogProps {
  isOpen: boolean;
  username: string;
  onClose: () => void;
}

export function DeleteAccountDialog({ isOpen, username, onClose }: DeleteAccountDialogProps) {
  const navigate = useNavigate();
  const { logout } = useAuth();
  const [confirmText, setConfirmText] = useState('');
  const [isDeleting, setIsDeleting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const modalRef = useRef<HTMLDivElement>(null);

  const handleClose = useCallback(() => {
    setConfirmText('');
    setError(null);
    onClose();
  }, [onClose]);

  useEffect(() => {
    if (!isOpen) return;

    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        handleClose();
      }
    };

    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, handleClose]);

  if (!isOpen) return null;

  const isConfirmed = confirmText.trim().toLowerCase() === username.trim().toLowerCase();

  const handleDelete = async () => {
    if (!isConfirmed) return;

    setIsDeleting(true);
    setError(null);

    try {
      await deleteAccount();
      logout();
      navigate(paths.home, { replace: true });
    } catch (err) {
      if (err instanceof ProfileApiError) {
        setError(err.message);
      } else {
        setError('Не удалось удалить аккаунт. Попробуйте позже.');
      }
      setIsDeleting(false);
    }
  };

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby="delete-dialog-title"
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 p-4 backdrop-blur-sm"
    >
      <div
        ref={modalRef}
        className="bg-card border-error/50 w-full max-w-lg border p-6 shadow-2xl md:p-8"
      >
        <div className="flex flex-col gap-4">
          <div className="flex items-start justify-between gap-4">
            <h3 id="delete-dialog-title" className="text-error text-2xl font-bold">
              Удаление аккаунта
            </h3>
            <button
              type="button"
              onClick={handleClose}
              disabled={isDeleting}
              className="text-foreground-muted hover:text-foreground cursor-pointer text-xl font-bold"
              aria-label="Закрыть"
            >
              ✕
            </button>
          </div>

          <p className="text-foreground-muted text-sm leading-relaxed">
            Это действие является <span className="text-error font-semibold">необратимым</span>. Ваш
            аккаунт, привязанный профиль игрока и все персональные данные будут безвозвратно
            удалены.
          </p>

          <div className="flex flex-col gap-2">
            <label htmlFor="delete-confirm-input" className="text-foreground text-sm font-medium">
              Для подтверждения введите ваш логин (<span className="text-error">{username}</span>):
            </label>
            <Input
              id="delete-confirm-input"
              value={confirmText}
              onChange={(e) => {
                setConfirmText(e.target.value);
                if (error) setError(null);
              }}
              placeholder={username}
              disabled={isDeleting}
              autoFocus
              isError={Boolean(error)}
            />
            {error && <span className="text-error text-xs font-medium">{error}</span>}
          </div>

          <div className="flex flex-col-reverse justify-end gap-3 pt-4 sm:flex-row">
            <Button type="button" variant="secondary" onClick={handleClose} disabled={isDeleting}>
              Отмена
            </Button>
            <Button
              type="button"
              variant="primary"
              onClick={() => void handleDelete()}
              disabled={!isConfirmed || isDeleting}
              className="bg-error hover:bg-error/80 text-white"
            >
              {isDeleting ? 'Удаление...' : 'Удалить аккаунт навсегда'}
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}
