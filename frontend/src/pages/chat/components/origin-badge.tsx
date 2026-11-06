import type { SVGProps } from 'react';
import type { ChatMessageOrigin } from '@/api/chat';
import { cn } from '@/lib';

export type OriginBadgeProps = {
  origin: ChatMessageOrigin;
};

function DiscordSmallIcon({ className, ...props }: SVGProps<SVGSVGElement>) {
  return (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      fill="currentColor"
      viewBox="0 0 640 512"
      aria-hidden="true"
      className={className ?? 'size-3'}
      {...props}
    >
      <path d="M524.5 69.8a1.5 1.5 0 0 0-.8-.7A485.1 485.1 0 0 0 404.1 32a1.8 1.8 0 0 0-1.9.9 337.5 337.5 0 0 0-14.9 30.6 447.8 447.8 0 0 0-134.4 0 309.5 309.5 0 0 0-15.1-30.6 1.9 1.9 0 0 0-1.9-.9A483.7 483.7 0 0 0 116.1 69.1a1.7 1.7 0 0 0-.8.7C39.1 183.7 18.2 294.7 28.4 404.4a2 2 0 0 0 .8 1.4A487.7 487.7 0 0 0 176 479.9a1.9 1.9 0 0 0 2.1-.7A348.2 348.2 0 0 0 208.1 430.4a1.9 1.9 0 0 0-1-2.6 321.2 321.2 0 0 1-45.9-21.9 1.9 1.9 0 0 1-.2-3.1c3.1-2.3 6.2-4.7 9.1-7.1a1.8 1.8 0 0 1 1.9-.3c96.2 43.9 200.4 43.9 295.5 0a1.8 1.8 0 0 1 1.9.2c2.9 2.4 6 4.9 9.1 7.2a1.9 1.9 0 0 1-.2 3.1 301.4 301.4 0 0 1-45.9 21.8 1.9 1.9 0 0 0-1 2.6 391.1 391.1 0 0 0 30 48.8 1.9 1.9 0 0 0 2.1.7A486 486 0 0 0 610.7 405.7a1.9 1.9 0 0 0 .8-1.4C623.7 277.6 590.9 167.5 524.5 69.8z" />
    </svg>
  );
}

function MinecraftIcon({ className, ...props }: SVGProps<SVGSVGElement>) {
  return (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      viewBox="0 0 24 24"
      fill="currentColor"
      aria-hidden="true"
      className={className ?? 'size-3'}
      {...props}
    >
      <path d="M4 2h16a2 2 0 0 1 2 2v16a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2zm8 2L4 20h3l1.5-4h7l1.5 4h3L12 4zm0 3.5 2.5 6.5h-5L12 7.5z" />
    </svg>
  );
}

function WebIcon({ className, ...props }: SVGProps<SVGSVGElement>) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth={2}
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
      className={className ?? 'size-3'}
      {...props}
    >
      <circle cx="12" cy="12" r="10" />
      <path d="M2 12h20M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z" />
    </svg>
  );
}

const ORIGIN_CONFIG: Record<
  ChatMessageOrigin,
  { label: string; className: string; icon: typeof WebIcon }
> = {
  WEB: {
    label: 'Сайт',
    className: 'bg-primary/15 text-primary',
    icon: WebIcon,
  },
  DISCORD: {
    label: 'Discord',
    className: 'bg-[#5865F2]/15 text-[#5865F2]',
    icon: DiscordSmallIcon,
  },
  MINECRAFT: {
    label: 'Minecraft',
    className: 'bg-orange-500/15 text-orange-400',
    icon: MinecraftIcon,
  },
};

export function OriginBadge({ origin }: OriginBadgeProps) {
  const config = ORIGIN_CONFIG[origin];
  const Icon = config.icon;

  return (
    <span
      className={cn(
        'inline-flex items-center gap-1 rounded px-1.5 py-0.5 text-[10px] leading-none font-semibold',
        config.className
      )}
    >
      <Icon className="size-2.5" />
      {config.label}
    </span>
  );
}
