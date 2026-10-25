import { Button } from '@/components/ui';
import heroImage from '@/assets/images/hero.png';

export function HeroSection() {
  return (
    <section className="relative -mt-16">
      <img
        src={heroImage}
        alt=""
        aria-hidden="true"
        className="absolute inset-0 size-full object-cover brightness-75 select-none"
      />

      <div className="to-background via-background/40 pointer-events-none absolute inset-0 bg-linear-to-b from-transparent" />

      <div className="relative mx-auto flex w-full max-w-7xl flex-col items-center justify-center gap-10 pt-40 pb-16">
        <h1 className="font-brand text-6xl">WALTERCRAFT</h1>
        <p className="max-w-xl text-center text-xl font-semibold">
          Ванильный Minecraft сервер с ламповой атмосферой. Присоединяйтесь к нам и станьте частью
          нашей дружной семьи
        </p>
        <Button>Присоединиться</Button>
      </div>
    </section>
  );
}
