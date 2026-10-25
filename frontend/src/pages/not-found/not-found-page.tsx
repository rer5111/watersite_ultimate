import { paths } from '@/routes/paths';
import { ButtonLink } from '@/components';
import teto404 from '@/assets/images/teto-404.png';

export function NotFoundPage() {
  return (
    <div className="flex min-h-dvh flex-col items-center justify-center gap-10">
      <img src={teto404} alt="" className="h-40" />
      <div className="mb-4 flex flex-col items-center gap-4">
        <span className="text-primary text-9xl font-black tracking-tighter">404</span>
        <h1 className="max-w-120 text-center text-2xl font-medium">
          Запрашиваемая страница не найдена
        </h1>
      </div>
      <ButtonLink to={paths.home} variant="secondary">
        На главную
      </ButtonLink>
    </div>
  );
}
