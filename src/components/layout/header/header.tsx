import { NavLink } from 'react-router';
import { ButtonLink, buttonVariants, DiscordIcon } from '@/components/ui';
import { Logo } from '../logo';
import { paths } from '@/routes';
import { cn } from '@/lib';

interface NavItem {
  title: string;
  destination: string;
}

const navItems: NavItem[] = [
  { title: 'Главная', destination: paths.home },
  { title: 'Администрация', destination: paths.admins },
  { title: 'Пожертвовать', destination: paths.donate },
];

export function Header() {
  return (
    <header className="bg-surface/50 sticky top-0 z-50 backdrop-blur-3xl">
      <div className="mx-auto grid h-16 max-w-7xl grid-cols-[1fr_auto_1fr] items-center px-10">
        <Logo />

        <nav className="flex h-full items-center">
          {navItems.map((item) => (
            <NavLink
              key={item.destination}
              to={item.destination}
              className="group after:bg-primary relative flex h-full items-center px-6 font-medium after:absolute after:inset-x-0 after:bottom-0 after:h-0.5 after:origin-center after:scale-x-0 after:transition-transform after:duration-200 after:content-[''] hover:[font-variation-settings:'wght'_700] hover:after:scale-x-100 focus-visible:ring-2 focus-visible:outline-none focus-visible:ring-inset focus-visible:after:scale-x-100"
            >
              {item.title}
            </NavLink>
          ))}
        </nav>

        <div className="flex justify-end gap-4">
          <a
            href="https://discord.gg/967091313038196796"
            target="_blank"
            rel="noreferrer"
            className={cn(
              buttonVariants({ variant: 'tertiary' }),
              'hover:bg-tertiary-hover/50 active:bg-tertiary-active/50'
            )}
          >
            <DiscordIcon className="me-1 size-4.5" />
            <span>Discord</span>
          </a>
          <ButtonLink to={paths.login} variant="primary">
            Войти
          </ButtonLink>
        </div>
      </div>
    </header>
  );
}
