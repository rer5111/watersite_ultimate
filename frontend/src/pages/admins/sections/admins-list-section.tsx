import { useAdmins } from '@/api';
import { Button } from '@/components/ui';
import { AdminCard } from '../components';

export function AdminListSection() {
  const { data: admins = [], isError, refetch } = useAdmins();

  return (
    <section className="mx-auto flex w-full max-w-7xl flex-col gap-4 px-32 py-16">
      <h2 className="py-4 text-center text-4xl leading-tight font-bold">Администрация</h2>
      <div className="mx-auto flex max-w-300 flex-wrap justify-center gap-8 py-4">
        {isError && admins.length === 0 && (
          <div role="status" className="flex flex-col items-center gap-4 text-center">
            <p className="text-foreground-muted">Не удалось загрузить список администрации.</p>
            <Button variant="secondary" onClick={() => void refetch()}>
              Повторить попытку
            </Button>
          </div>
        )}

        {!isError && admins.length === 0 && <p>Список администрации пока пуст.</p>}

        {admins.map((admin) => (
          <div key={admin.username} className="w-80">
            <AdminCard {...admin} />
          </div>
        ))}
      </div>
    </section>
  );
}
