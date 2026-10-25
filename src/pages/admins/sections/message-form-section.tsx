import { Button, ChevronRightIcon, Input, Textarea } from '@/components';

export function MessageFormSection() {
  return (
    <section className="mx-auto flex w-full max-w-7xl flex-col gap-8 px-32 py-16">
      <div className="flex flex-col gap-3">
        <h3 className="text-center text-3xl leading-tight font-bold">
          Хотите стать администратором?
        </h3>
        <h4 className="text-foreground text-center text-xl font-medium">
          Свяжитесь с нами, заполнив форму ниже
        </h4>
      </div>

      <form className="mx-auto flex w-full flex-col gap-6 px-32">
        <div className="flex flex-col gap-3">
          <label htmlFor="nickname" className="text-foreground text-sm font-semibold">
            Никнейм
          </label>
          <Input id="nickname" name="nickname" placeholder="Введите ваш никнейм" />
        </div>

        <div className="flex flex-col gap-3">
          <label htmlFor="message" className="text-foreground text-sm font-semibold">
            Сообщение
          </label>
          <Textarea id="message" name="message" placeholder="Введите ваше сообщение" />
        </div>

        <Button type="submit" variant="primary" trailingIcon={<ChevronRightIcon />}>
          Отправить
        </Button>
      </form>
    </section>
  );
}
