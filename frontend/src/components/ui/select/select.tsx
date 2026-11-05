import type { SelectHTMLAttributes } from 'react';
import { ChevronDownIcon } from '@/components/ui/icons';
import { cn } from '@/lib';

export type SelectProps = SelectHTMLAttributes<HTMLSelectElement> & {
  isError?: boolean;
};

export function Select({ className, children, isError, ...props }: SelectProps) {
  return (
    <div className="relative w-full">
      <select
        aria-invalid={isError ? 'true' : undefined}
        className={cn(
          'text-foreground bg-input w-full appearance-none rounded-none p-3 pr-10 text-sm leading-6 font-medium transition-all',
          'border border-transparent',
          isError
            ? 'border-error text-error focus-visible:ring-error focus-visible:ring-2 focus-visible:outline-none'
            : 'focus-visible:ring-foreground focus-visible:ring-2 focus-visible:outline-none',
          className
        )}
        {...props}
      >
        {children}
      </select>
      <ChevronDownIcon className="text-foreground-muted pointer-events-none absolute top-1/2 right-3 size-4 -translate-y-1/2" />
    </div>
  );
}
