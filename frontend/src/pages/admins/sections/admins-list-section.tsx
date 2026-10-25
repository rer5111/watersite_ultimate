import { AdminCard, type AdminCardProps } from '../components';

const admins: AdminCardProps[] = [
  {
    image: '/images/skins/lidkon.png',
    title: 'LeadCone_TNF',
    description: 'Главный администратор, владелец Waltercraft',
  },
  {
    image: '/images/skins/rer1511.png',
    title: 'rer_5111',
    description: 'Совладелец сервера, организатор ивентов',
  },
  {
    image: '/images/skins/kotyarendj.png',
    title: 'Kotyarendj',
    description: 'Администратор Discord сервера, организатор ивентов',
  },
  {
    image: '/images/skins/tabller.png',
    title: 'Tabller',
    description: 'Локальный стример, администратор Discord сервера',
  },
  {
    image: '/images/skins/cheshirius.png',
    title: 'Cheshirius',
    description: 'Администратор Discord сервера, писатель лора',
  },
];

export function AdminListSection() {
  return (
    <section className="mx-auto flex w-full max-w-7xl flex-col gap-4 px-32 py-16">
      <h2 className="py-4 text-center text-4xl leading-tight font-bold">Администрация</h2>
      <div className="mx-auto flex max-w-300 flex-wrap justify-center gap-8 py-4">
        {admins.map((admin) => (
          <div key={admin.title} className="w-80">
            <AdminCard {...admin} />
          </div>
        ))}
      </div>
    </section>
  );
}
