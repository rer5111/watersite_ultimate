import type { InputHTMLAttributes, Ref } from 'react';
import { cn } from '@/lib';

export type CheckboxProps = Omit<InputHTMLAttributes<HTMLInputElement>, 'type'> & {
  ref?: Ref<HTMLInputElement>;
};

export function Checkbox({ className, ref, ...props }: CheckboxProps) {
  return (
    <span className="relative flex size-4.5 shrink-0 items-center justify-center">
      <input
        ref={ref}
        type="checkbox"
        className={cn(
          'peer border-border checked:border-primary checked:bg-primary focus-visible:outline-primary size-4.5 shrink-0 cursor-pointer appearance-none rounded-none border bg-transparent transition-colors duration-100 focus-visible:outline-2 focus-visible:outline-offset-2 disabled:cursor-not-allowed disabled:opacity-50',
          className
        )}
        {...props}
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
  );
}
