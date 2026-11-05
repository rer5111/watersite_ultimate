import type { BadgeTone } from '../admin-utils';

const toneClasses: Record<BadgeTone, string> = {
  success: 'border-primary/40 bg-primary/10 text-primary',
  danger: 'border-error/40 bg-error/10 text-error',
  muted: 'border-border bg-surface text-foreground-muted',
  info: 'border-border bg-surface text-foreground',
};

export interface StatusBadgeProps {
  label: string;
  tone: BadgeTone;
}

export function StatusBadge({ label, tone }: StatusBadgeProps) {
  return (
    <span
      className={`inline-flex rounded-none border px-2 py-0.5 text-[11px] font-semibold tracking-wider uppercase ${toneClasses[tone]}`}
    >
      {label}
    </span>
  );
}
