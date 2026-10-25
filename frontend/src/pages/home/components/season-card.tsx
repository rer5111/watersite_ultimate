import { cn } from '@/lib';
import type { Season } from '@/types';

export type SeasonCardProps = Omit<Season, 'id'> & {
  imagePosition: 'start' | 'end';
};

export function SeasonCard({
  image,
  imagePosition,
  title,
  startDate,
  endDate,
  description,
}: SeasonCardProps) {
  return (
    <div className={cn('flex gap-12', imagePosition === 'start' ? 'flex-row' : 'flex-row-reverse')}>
      <img
        src={image}
        alt={title}
        loading="lazy"
        className="aspect-3/4 w-60 rounded-2xl object-cover"
      />
      <div className="flex max-w-88 flex-col gap-4">
        <h3 className="text-2xl leading-tight font-bold">{`${title} (${startDate}–${endDate})`}</h3>
        <p className="text-foreground-muted text-base leading-relaxed text-pretty">{description}</p>
      </div>
    </div>
  );
}
