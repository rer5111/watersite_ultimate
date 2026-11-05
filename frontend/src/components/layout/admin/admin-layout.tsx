import { Link, NavLink, Outlet, useNavigate } from 'react-router';
import { Button } from '@/components/ui';
import { useAuth } from '@/context';
import { cn } from '@/lib';
import { paths } from '@/routes/paths';

const adminNavItems = [
  { title: 'Пользователи', destination: paths.adminUsers, description: 'Аккаунты и роли' },
  { title: 'События', destination: paths.adminEvents, description: 'Публикация и модерация' },
  { title: 'Рассылки', destination: paths.adminBroadcast, description: 'Уведомления игрокам' },
  { title: 'Заявки', destination: paths.adminRequests, description: 'Повышение ролей' },
];

export function AdminLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  return (
    <div className="bg-background min-h-dvh">
      <div className="mx-auto flex w-full max-w-[100rem] flex-col lg:flex-row">
        <aside className="border-border bg-card flex w-full shrink-0 flex-col gap-6 border-b p-4 lg:min-h-dvh lg:w-64 lg:border-r lg:border-b-0 lg:p-6">
          <div className="flex items-center justify-between gap-4">
            <div className="flex flex-col gap-1">
              <Link
                to={paths.home}
                className="text-foreground hover:text-primary text-lg font-bold tracking-tight transition-colors"
              >
                Waltercraft
              </Link>
              <span className="text-primary text-[11px] font-semibold tracking-widest uppercase">
                Админ-панель
              </span>
            </div>
            <span className="border-primary/40 bg-primary/10 text-primary rounded-none border px-2 py-0.5 text-[10px] font-semibold tracking-wider uppercase">
              Admin
            </span>
          </div>

          <nav aria-label="Разделы админ-панели" className="flex gap-2 overflow-x-auto lg:flex-col">
            {adminNavItems.map((item) => (
              <NavLink
                key={item.destination}
                to={item.destination}
                className={({ isActive }) =>
                  cn(
                    'flex min-w-44 flex-col gap-0.5 rounded-none border px-3 py-2.5 transition-colors lg:min-w-0',
                    isActive
                      ? 'border-primary bg-primary/10 text-foreground'
                      : 'border-border text-foreground-muted hover:bg-surface hover:text-foreground'
                  )
                }
              >
                <span className="text-sm font-semibold">{item.title}</span>
                <span className="text-foreground-subtle text-xs">{item.description}</span>
              </NavLink>
            ))}
          </nav>

          <div className="border-border mt-auto hidden flex-col gap-3 border-t pt-4 lg:flex">
            <div className="flex flex-col gap-0.5">
              <span className="text-foreground-muted text-xs tracking-wider uppercase">
                Вы вошли как
              </span>
              <span className="text-foreground text-sm font-semibold">{user?.username}</span>
            </div>
            <div className="flex flex-col gap-2">
              <Link
                to={paths.home}
                className="text-foreground-muted hover:text-foreground text-sm font-medium transition-colors"
              >
                На сайт
              </Link>
              <Button
                type="button"
                variant="tertiary"
                className="justify-start rounded-none px-0 text-sm"
                onClick={() => {
                  logout();
                  navigate(paths.home);
                }}
              >
                Выйти
              </Button>
            </div>
          </div>
        </aside>

        <main className="min-w-0 flex-1 p-4 md:p-6 lg:p-8">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
