import { Link, Outlet } from 'react-router';
import heroImage from '@/assets/images/hero.png';
import { paths } from '@/routes';
import { cn } from '@/lib';
import { Logo } from './logo';
import { ChevronLeftIcon } from '@/components/ui';
import { useCarousel } from '@/hooks';

const authSlides = [
  {
    image: heroImage,
    label: 'Ночной город',
    title: 'Уникальный мир, созданный игроками',
    description: 'Присоединяйся к нашему сообществу и начни свою историю уже сегодня',
  },
  {
    image: '/images/seasons/season-1.png',
    label: 'Первый сезон',
    title: 'Здесь всё началось',
    description: 'Первые поселения и начало истории, которая объединила игроков',
  },
  {
    image: '/images/seasons/season-2.png',
    label: 'Второй сезон',
    title: 'Время больших городов',
    description: 'Ватерград растёт, а Waltercraft открывает новую эру',
  },
  {
    image: '/images/seasons/season-3.png',
    label: 'Третий сезон',
    title: 'Каждому — своё приключение',
    description: 'Игроки строят базы, развивают свои идеи и создают новые истории',
  },
  {
    image: '/images/seasons/season-4.png',
    label: 'Четвёртый сезон',
    title: 'Эра экспериментов',
    description: 'Большие проекты, модификации и события, которые меняли привычный мир',
  },
  {
    image: '/images/seasons/season-5.png',
    label: 'Пятый сезон',
    title: 'История продолжается',
    description: 'Строим Крайенбор и возвращаем Waltercraft дух единства',
  },
] as const;

interface AuthImageProgressProps {
  isActive: boolean;
  isComplete: boolean;
  progress: number;
}

function AuthImageProgress({ isActive, isComplete, progress }: AuthImageProgressProps) {
  return (
    <span className="bg-foreground/35 relative block h-1 flex-1 overflow-hidden rounded-full">
      <span
        className={cn(
          'bg-foreground absolute inset-y-0 left-0 block rounded-full transition-[width] duration-100 ease-linear',
          isComplete && 'w-full'
        )}
        style={isActive ? { width: `${progress}%` } : undefined}
      />
    </span>
  );
}

export function AuthLayout() {
  const { activeIndex, progress, selectSlide } = useCarousel({
    itemCount: authSlides.length,
    duration: 6000,
  });
  const currentSlide = authSlides[activeIndex];

  return (
    <main className="grid min-h-screen grid-cols-2">
      <section className="flex flex-col items-center justify-start gap-8 pt-24">
        <div className="mx-auto w-full max-w-lg">
          <Link
            to={paths.home}
            className="text-foreground-subtle hover:text-foreground inline-flex items-center gap-2 text-sm transition-colors"
          >
            <ChevronLeftIcon />
            <span>Вернуться на главную</span>
          </Link>
        </div>

        <div className="w-full max-w-lg">
          <Outlet />
        </div>
      </section>

      <section className="relative overflow-hidden">
        {authSlides.map((slide, slideIndex) => (
          <img
            key={slide.image}
            src={slide.image}
            alt=""
            aria-hidden="true"
            className={cn(
              'pointer-events-none absolute inset-0 size-full object-cover object-center brightness-75 transition-opacity duration-300 ease-out select-none',
              slideIndex === activeIndex ? 'opacity-100' : 'opacity-0'
            )}
          />
        ))}

        <div className="pointer-events-none absolute inset-0 bg-linear-to-b from-black/45 via-transparent to-black/90" />
        <div className="pointer-events-none absolute inset-0 bg-linear-to-r from-black/20 via-transparent to-black/15" />

        <div className="pointer-events-none absolute inset-x-0 bottom-0 h-64 mask-[linear-gradient(to_bottom,transparent,black_70%)] backdrop-blur-sm" />

        <div className="absolute top-8 right-16 left-16 z-10 flex items-center justify-between">
          <Logo />
        </div>

        <div className="absolute right-16 bottom-16 left-16 z-10 flex flex-col gap-3">
          <p className="max-w-xl text-2xl leading-tight font-bold text-pretty">
            {currentSlide.title}
          </p>
          <p className="text-foreground-muted text-base leading-relaxed text-pretty">
            {currentSlide.description}
          </p>
        </div>

        <div className="absolute right-16 bottom-6 left-16 z-10 flex gap-2">
          {authSlides.map((slide, slideIndex) => (
            <button
              key={slide.image}
              type="button"
              className="group flex min-h-5 flex-1 cursor-pointer items-center focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-white"
              onClick={() => selectSlide(slideIndex)}
            >
              <AuthImageProgress
                isActive={slideIndex === activeIndex}
                isComplete={slideIndex < activeIndex}
                progress={progress}
              />
            </button>
          ))}
        </div>
      </section>
    </main>
  );
}
