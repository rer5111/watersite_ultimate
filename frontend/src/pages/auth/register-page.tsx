import { Button, Input } from '@/components';
import { paths } from '@/routes';
import { Link } from 'react-router';

export function RegisterPage() {
  return (
    <div className="flex flex-col gap-6">
      <header className="flex flex-col gap-3">
        <h1 className="text-4xl font-bold">Добро пожаловать!</h1>
        <p className="text-foreground-muted text-base font-medium">
          Придумайте логин и пароль для входа на сайт
        </p>
      </header>

      <form className="flex flex-col gap-4">
        <div className="flex flex-col gap-2">
          <label htmlFor="login" className="text-foreground text-sm font-semibold">
            Логин
          </label>
          <Input id="login" name="login" placeholder="Придумайте логин" required />
        </div>

        <div className="flex flex-col gap-2">
          <label htmlFor="password" className="text-foreground text-sm font-semibold">
            Пароль
          </label>
          <Input
            id="password"
            name="password"
            type="password"
            placeholder="Минимум 8 символов"
            required
          />
        </div>

        <div className="flex flex-col gap-2">
          <label htmlFor="repeatPassword" className="text-foreground text-sm font-semibold">
            Повторите пароль
          </label>
          <Input
            id="repeatPassword"
            name="repeatPassword"
            type="password"
            placeholder="Минимум 8 символов"
            required
          />
        </div>

        <Button type="submit" className="mt-4">
          Зарегистрироваться
        </Button>
      </form>

      <p className="flex justify-center gap-2 text-sm">
        <span className="text-foreground-muted">Уже есть аккаунт?</span>
        <Link to={paths.login} className="font-medium transition-all hover:underline">
          Войти
        </Link>
      </p>
    </div>
  );
}
