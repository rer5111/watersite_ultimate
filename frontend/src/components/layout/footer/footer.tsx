import { paths } from '@/routes';
import { Logo } from '../logo';
import { Link } from 'react-router';

const links = [
  {
    title: 'Разделы',
    items: [
      {
        title: 'Главная',
        href: paths.home,
      },
      {
        title: 'Присоединиться',
        href: paths.join,
      },
      {
        title: 'Администрация',
        href: paths.admins,
      },
      {
        title: 'Пожертвование',
        href: paths.donate,
      },
      {
        title: 'Игровой чат',
        href: paths.chat,
      },
    ],
  },
  {
    title: 'Документация',
    items: [
      {
        title: 'Пользовательское соглашение',
        href: paths.userAgreement,
      },
      {
        title: 'Политика конфиденциальности',
        href: paths.privacyPolicy,
      },
    ],
  },
];

const discordUrl = 'https://discordapp.com/widget?id=967091313038196796&theme=dark';
const minecraftUrl = 'https://crafty.gg/embed/server/c13.play2go.cloud%3A20074';

export function Footer() {
  return (
    <footer className="bg-surface w-full">
      <div className="mx-auto flex max-w-7xl flex-col px-10">
        <div className="flex w-full gap-56 py-16">
          <div className="flex flex-1 flex-col gap-10">
            <Logo size="lg" />
            <iframe
              src={discordUrl}
              title="Discord server"
              className="bg-background h-full w-full max-w-xl rounded-xl border-0"
              sandbox="allow-popups allow-popups-to-escape-sandbox allow-same-origin allow-scripts"
            />
          </div>
          <div className="flex flex-col gap-12">
            <nav className="flex gap-32">
              {links.map(({ title, items }) => (
                <div key={title} className="flex flex-col gap-4">
                  <h2 className="text-foreground text-base font-medium">{title}</h2>
                  <ul className="flex flex-col items-start gap-3">
                    {items.map((item) => (
                      <li key={item.title}>
                        <Link
                          to={item.href}
                          className="text-foreground-subtle hover:text-foreground text-sm font-medium transition-colors"
                        >
                          {item.title}
                        </Link>
                      </li>
                    ))}
                  </ul>
                </div>
              ))}
            </nav>

            <iframe
              className="aspect-2/1 h-auto w-full rounded-xl border-0"
              src={minecraftUrl}
              title="Статус Minecraft-сервера c13.play2go.cloud:20074"
              allow="clipboard-write"
            />
          </div>
        </div>

        <div className="border-border flex w-full flex-col gap-2 border-t px-1 pt-10 pb-16">
          <p className="text-foreground-subtle text-sm leading-relaxed">
            © 2026 Waltercraft, все права защищены
          </p>
          <p className="text-foreground-subtle text-sm leading-relaxed">
            Waltercraft не связан с Mojang AB. Коммерческая деятельность компании соответствует
            Политике Mojang AB
          </p>
        </div>
      </div>
    </footer>
  );
}
