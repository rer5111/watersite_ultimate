import { ButtonLink } from '@/components/ui';
import { paths } from '@/routes';

export function DonateSection() {
  return (
    <section className="mx-auto mb-16 flex w-full max-w-7xl flex-col gap-4 px-32 py-16">
      <div className="flex flex-col items-center">
        <h2 className="py-4 text-center text-4xl leading-tight font-bold">Поддержите нас</h2>
        <p className="w-full max-w-160 py-4 text-center text-lg leading-relaxed font-medium text-pretty">
          Понравился проект? Вы можете поддержать нас, сделав небольшое денежное пожертвование. Мы
          будем рады любой сумме
        </p>
      </div>

      <ButtonLink to={paths.donate} className="self-center">
        Сделать пожертвование
      </ButtonLink>
    </section>
  );
}
