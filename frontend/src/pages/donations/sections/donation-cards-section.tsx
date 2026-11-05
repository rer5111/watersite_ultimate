import { DonationCard, type DonationCardProps } from '../components';
import { useDonationTypes } from '@/api';
import { Button } from '@/components/ui';

const donationImages: Record<string, string> = {
  'support-project': '/images/donations/iron-block.png',
  'help-server': '/images/donations/gold-block.png',
  'major-support': '/images/donations/diamond-block.png',
};

export function DonationCardSection() {
  const { data: donationTypes = [], isLoading, isError, refetch } = useDonationTypes();

  return (
    <section className="mx-auto flex w-full max-w-7xl flex-col gap-4 px-32 py-16">
      <div className="flex flex-col items-center gap-4 py-4 text-center">
        <h2 className="text-4xl leading-tight font-bold">Пожертвовать</h2>
        <p className="max-w-2xl text-lg font-medium text-pretty">
          Понравился проект? Вы можете поддержать нас, сделав небольшое денежное пожертвование. Мы
          будем рады любой сумме
        </p>
      </div>
      <div className="mx-auto flex max-w-300 flex-wrap justify-center gap-8 py-4">
        {isLoading && (
          <div className="flex flex-wrap justify-center gap-8 invisible" aria-hidden="true">
            <div className="h-96 w-80" />
            <div className="h-96 w-80" />
            <div className="h-96 w-80" />
          </div>
        )}
        {isError && (
          <div className="flex flex-col items-center gap-3 py-6">
            <p role="alert" className="text-destructive font-medium">
              Не удалось загрузить варианты пожертвования.
            </p>
            <Button variant="secondary" onClick={() => void refetch()}>
              Повторить попытку
            </Button>
          </div>
        )}
        {!isLoading && !isError && donationTypes.length === 0 && (
          <p className="text-muted-foreground">Пока нет доступных вариантов пожертвования.</p>
        )}
        {!isLoading && !isError && donationTypes.map((donation) => {
          const card: DonationCardProps = {
            image: donationImages[donation.code] ?? '/images/donations/diamond-block.png',
            title: donation.name,
            price: donation.priceRub,
          };

          return (
            <div key={donation.id} className="w-80">
              <DonationCard {...card} />
            </div>
          );
        })}
      </div>
    </section>
  );
}
