import { Button, Input } from '@/components';
import { paths } from '@/routes';
import { Link } from 'react-router';

export function LoginPage() {
  return (
    <div className="flex flex-col gap-6">
      <header className="flex flex-col gap-3">
        <h1 className="text-4xl font-bold">С возвращением!</h1>
        <p className="text-foreground-muted text-base font-medium">
          Введите свои данные для входа на сайт
        </p>
      </header>

      <form className="flex flex-col gap-4">
        <div className="flex flex-col gap-2">
          <label htmlFor="login" className="text-foreground text-sm font-semibold">
            Логин
          </label>
          <Input id="login" name="login" placeholder="Введите логин" required />
        </div>

        <div className="flex flex-col gap-2">
          <label htmlFor="password" className="text-foreground text-sm font-semibold">
            Пароль
          </label>
          <Input
            id="password"
            name="password"
            type="password"
            placeholder="Введите пароль"
            required
          />
        </div>

        <label className="flex w-fit cursor-pointer items-center gap-3">
          <span className="relative flex size-4.5 shrink-0 items-center justify-center">
            <input
              type="checkbox"
              name="rememberMe"
              className="peer border-border checked:border-primary checked:bg-primary focus-visible:outline-primary size-4.5 cursor-pointer appearance-none rounded-none border bg-transparent transition-colors duration-100 focus-visible:outline-2 focus-visible:outline-offset-2"
            />

            <svg
              viewBox="0 0 16 16"
              fill="none"
              className="pointer-events-none absolute size-4 text-white opacity-0 transition-opacity duration-100 peer-checked:opacity-100"
            >
              <path
                d="m3 8 3 3 7-7"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="butt"
                strokeLinejoin="miter"
              />
            </svg>
          </span>

          <span className="text-foreground-muted text-sm">Запомнить меня</span>
        </label>

        <Button type="submit" className="mt-4">
          Войти
        </Button>
      </form>

      <p className="flex justify-center gap-2 text-sm">
        <span className="text-foreground-muted">Нет аккаунта?</span>
        <Link to={paths.register} className="font-medium transition-all hover:underline">
          Зарегистрироваться
        </Link>
      </p>
    </div>
  );
}
