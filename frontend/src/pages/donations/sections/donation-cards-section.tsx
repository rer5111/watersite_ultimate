import { DonationCard, type DonationCardProps } from '../components';
const donations: DonationCardProps[] = [
  {
    image: 'public/images/donations/iron-block.png',
    title: 'Поддержать проект',
    price: 100,
  },
  {
    image: 'public/images/donations/gold-block.png',
    title: 'Помочь серверу',
    price: 250,
  },
  {
    image: 'public/images/donations/diamond-block.png',
    title: 'Большая поддержка',
    price: 500,
  },
];

export function DonationCardSection() {
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
        {donations.map((donation) => (
          <div key={donation.title} className="w-80">
            <DonationCard {...donation} />
          </div>
        ))}
      </div>
    </section>
  );
}
