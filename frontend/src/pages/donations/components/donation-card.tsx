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
};

export function DonationCard({ image, title, price }: DonationCardProps) {
  return (
    <div className="bg-card flex flex-col gap-6 p-8">
      <img
        src={image}
        alt={title}
        loading="lazy"
        className="mb-4 aspect-1/2 h-40 w-auto object-scale-down"
      />
      <div className="flex flex-col gap-4">
        <h4 className="text-center text-xl font-medium">{title}</h4>
        <span className="text-foreground text-center text-3xl font-bold">{formatPrice(price)}</span>
      </div>
      <Button>Поддержать</Button>
    </div>
  );
}
