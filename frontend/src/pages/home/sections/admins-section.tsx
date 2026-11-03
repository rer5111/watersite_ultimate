import { ButtonLink, ChevronRightIcon } from '@/components/ui';
import { paths } from '@/routes';

export function AdminsSection() {
  return (
    <section className="mx-auto flex w-full max-w-7xl flex-col gap-4 px-32 py-16">
      <div className="flex flex-col items-center">
        <h2 className="py-4 text-center text-4xl leading-tight font-bold">Администрация</h2>
        <p className="w-full max-w-160 py-4 text-center text-lg leading-relaxed font-medium text-pretty">
          На нашем сервере действует определённый свод правил, за соблюдением которых следит
          администрация. Вы можете стать частью команды администраторов, связавшись с нами
        </p>
      </div>

      <ButtonLink
        to={paths.admins}
        className="self-center"
        trailingIcon={<ChevronRightIcon />}
        variant="secondary"
      >
        Узнать больше
      </ButtonLink>
    </section>
  );
}
