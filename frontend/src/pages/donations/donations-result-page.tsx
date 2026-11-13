import { useSearchParams } from 'react-router';
import { useDonationStatus } from '@/api';
import { ButtonLink } from '@/components';
import { paths } from '@/routes/paths';

export function DonationsResultPage() {
  const [searchParams] = useSearchParams();
  const donationId = searchParams.get('donationId');
  const { data: donation, isLoading, isError } = useDonationStatus(donationId);

  return (
    <div className="mx-auto flex min-h-[60vh] w-full max-w-2xl flex-col items-center justify-center px-4 py-16 text-center">
      {isLoading && (
        <div className="flex flex-col items-center gap-4">
          <div className="border-primary h-12 w-12 animate-spin rounded-full border-4 border-t-transparent" />
          <h2 className="text-2xl font-bold">Проверяем статус пожертвования...</h2>
        </div>
      )}

      {isError && (
        <div className="bg-card flex flex-col items-center gap-6 rounded-lg p-8 shadow">
          <h2 className="text-destructive text-2xl font-bold">Не удалось получить данные о платеже</h2>
          <p className="text-muted-foreground">
            Возможно, платеж еще обрабатывается или ссылка устарела.
          </p>
          <div className="flex gap-4">
            <ButtonLink to={paths.donate} variant="secondary">
              К пожертвованиям
            </ButtonLink>
            <ButtonLink to={paths.home}>
              На главную
            </ButtonLink>
          </div>
        </div>
      )}

      {!isLoading && !isError && donation && (
        <div className="bg-card flex w-full flex-col items-center gap-6 rounded-lg p-8 shadow">
          {donation.status === 'SUCCEEDED' && (
            <>
              <div className="flex h-16 w-16 items-center justify-center rounded-full bg-green-500/10 text-3xl text-green-500">
                ✓
              </div>
              <h2 className="text-3xl font-bold">Спасибо за пожертвование!</h2>
              <p className="text-muted-foreground max-w-md">
                Ваша поддержка в размере{' '}
                <span className="text-foreground font-semibold">
                  {donation.amountRub} {donation.currency}
                </span>{' '}
                успешно получена. Мы отправили подтверждение в ваши уведомления.
              </p>
              <div className="flex flex-wrap justify-center gap-4 pt-2">
                <ButtonLink to={paths.notifications}>
                  Мои уведомления
                </ButtonLink>
                <ButtonLink to={paths.home} variant="secondary">
                  На главную
                </ButtonLink>
              </div>
            </>
          )}

          {donation.status === 'PENDING' && (
            <>
              <div className="border-primary h-12 w-12 animate-spin rounded-full border-4 border-t-transparent" />
              <h2 className="text-2xl font-bold">Ожидаем подтверждение платежа</h2>
              <p className="text-muted-foreground max-w-md">
                Платеж находится в обработке ЮKassa. Страница автоматически обновится при получении
                статуса.
              </p>
              <div className="pt-2">
                <ButtonLink to={paths.donate} variant="secondary">
                  Вернуться к пожертвованиям
                </ButtonLink>
              </div>
            </>
          )}

          {donation.status === 'CANCELED' && (
            <>
              <div className="text-destructive flex h-16 w-16 items-center justify-center rounded-full bg-red-500/10 text-3xl">
                ✕
              </div>
              <h2 className="text-destructive text-2xl font-bold">Платеж отменен</h2>
              <p className="text-muted-foreground max-w-md">
                Операция пожертвования была отменена или не завершена. Средства не списаны.
              </p>
              <div className="flex gap-4 pt-2">
                <ButtonLink to={paths.donate}>
                  Попробовать снова
                </ButtonLink>
                <ButtonLink to={paths.home} variant="secondary">
                  На главную
                </ButtonLink>
              </div>
            </>
          )}
        </div>
      )}
    </div>
  );
}
