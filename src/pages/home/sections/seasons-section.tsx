import type { Season } from '@/types/season';
import { SeasonCard, SeasonDivider } from '../components';
import { Fragment } from 'react/jsx-runtime';

const seasons: Season[] = [
  {
    id: 1,
    image: '/images/seasons/season-1.png',
    title: '1 сезон',
    startDate: '2022',
    endDate: '2022',
    description:
      'Начало сервера, когда он ещё не был назван Валтеркрафтом. Тогда у нас был Золотой ключик, а сервером управлял игрок Eathless. В этом сезоне начали строить город Ватерграда, а также более мелкие поселения.',
  },
  {
    id: 2,
    image: '/images/seasons/season-2.png',
    title: '2 сезон',
    startDate: '2022',
    endDate: '2023',
    description:
      'Вершина Валтеркрафта, а также начало его новой эры. Eathless передал сервер в руки игроков, и они создали новый сервер — Waltercraft. В этом сезоне завершили строительство Ватерграда, а также провели множество ивентов.',
  },
  {
    id: 3,
    image: '/images/seasons/season-3.png',
    title: '3 сезон',
    startDate: '2023',
    endDate: '2024',
    description:
      'Продолжение эры Waltercraft. Игроки решили отойти от строительства огромного города и сосредоточиться на отдельных базах. Несмотря на некоторую стагнацию, сезон запомнился весёлыми ивентами и развитием индивидуальных построек.',
  },
  {
    id: 4,
    image: '/images/seasons/season-4.png',
    title: '4 сезон',
    startDate: '2024',
    endDate: '2025',
    description:
      'Эра экспериментов. На сервере действовал огромный кастомный модпак, включавший более 100 модов, среди которых Create, Valkyrien Skies и другие контентные модификации. Прошло множество ивентов, однако со временем из-за низкой активности сервер пришлось заморозить.',
  },
  {
    id: 5,
    image: '/images/seasons/season-5.png',
    title: '5 сезон',
    startDate: '2025',
    endDate: 'н. в.',
    description:
      'Возрождение сервера. Waltercraft выходит из заморозки с новой целью — построить единый город Крайенбор и вернуть на сервер дух единства, который когда-то объединял Ватерград. Вместо модификаций вроде Create используются ванильные плагины, сохраняющие привычный игровой процесс без необходимости в масштабном модпаке.',
  },
];

export function SeasonsSection() {
  return (
    <section className="mx-auto flex w-full max-w-7xl flex-col gap-10 px-32 py-16">
      <div className="flex flex-col items-center">
        <h2 className="py-4 text-center text-4xl leading-tight font-bold">Сезоны</h2>

        <p className="w-full max-w-160 py-4 text-center text-lg leading-relaxed font-medium text-pretty">
          На сервере действует система сезонов — каждый длится около 1–2 лет и заканчивается
          крупными событиями. Всего было 5 сезонов, каждый из которых — кусочек истории сервера.
        </p>
      </div>
      <div className="flex flex-col gap-10">
        {seasons.map((season, index) => (
          <Fragment key={season.id}>
            <SeasonCard {...season} imagePosition={index % 2 === 0 ? 'start' : 'end'} />

            {index != seasons.length - 1 && <SeasonDivider isMirrored={index % 2 !== 0} />}
          </Fragment>
        ))}
      </div>
    </section>
  );
}
