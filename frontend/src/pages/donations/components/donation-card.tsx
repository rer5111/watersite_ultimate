import { Button } from '@/components';

const formatPrice = (price: number): string =>
  new Intl.NumberFormat('ru-RU', {
    style: 'currency',
    currency: 'RUB',
    maximumFractionDigits: 0,
  }).format(price);

export type DonationCardProps = {
  image: string;
  title: string;
  price: number;
  onDonate?: () => void;
  isLoading?: boolean;
};

export function DonationCard({ image, title, price, onDonate, isLoading }: DonationCardProps) {
  return (
    <div className="bg-card flex flex-col gap-6 p-8">
      <img
        src={image}
        alt={title}
        loading="eager"
        decoding="async"
        width={80}
        height={160}
        className="mb-4 aspect-1/2 h-40 w-auto shrink-0 object-scale-down text-[0px] text-transparent"
      />
      <div className="flex flex-col gap-4">
        <h4 className="text-center text-xl font-medium">{title}</h4>
        <span className="text-foreground text-center text-3xl font-bold">{formatPrice(price)}</span>
      </div>
      <Button onClick={onDonate} disabled={isLoading}>
        {isLoading ? 'Переход к оплате...' : 'Поддержать'}
      </Button>
    </div>
  );
}
