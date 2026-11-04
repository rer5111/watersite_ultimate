import { SkinAvatar } from '@/components/ui';

export type AdminCardProps = {
  username: string;
  uuid: string | null;
};

const ADMIN_FALLBACKS: Record<string, { image: string; description: string }> = {
  leadcone_tnf: {
    image: '/images/skins/lidkon.png',
    description: 'Главный администратор, владелец Waltercraft',
  },
  intervinn: {
    image: '/images/skins/lidkon.png',
    description: 'Главный администратор, владелец Waltercraft',
  },
  rer_5111: {
    image: '/images/skins/rer1511.png',
    description: 'Совладелец сервера, организатор ивентов',
  },
  rer1511: {
    image: '/images/skins/rer1511.png',
    description: 'Совладелец сервера, организатор ивентов',
  },
  kotyarendj: {
    image: '/images/skins/kotyarendj.png',
    description: 'Администратор Discord сервера, организатор ивентов',
  },
  tabller: {
    image: '/images/skins/tabller.png',
    description: 'Локальный стример, администратор Discord сервера',
  },
  cheshirius: {
    image: '/images/skins/cheshirius.png',
    description: 'Администратор Discord сервера, писатель лора',
  },
};

export function AdminCard({ username, uuid }: AdminCardProps) {
  const meta = ADMIN_FALLBACKS[username.toLowerCase()];
  const fallbackImage = meta?.image;
  const description = meta?.description ?? 'Администратор Waltercraft';

  return (
    <div className="bg-card flex flex-col gap-4 p-8">
      <SkinAvatar
        uuid={uuid}
        name={username}
        fallbackImage={fallbackImage}
        mode="full"
        className="aspect-1/2 h-60 w-auto shrink-0 object-scale-down"
      />
      <div className="flex flex-col gap-2">
        <h4 className="text-center text-2xl font-bold">{username}</h4>
        <p className="text-foreground-muted text-center text-base text-pretty">{description}</p>
      </div>
    </div>
  );
}
