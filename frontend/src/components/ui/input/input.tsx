import type { InputHTMLAttributes, Ref } from 'react';
import { cn } from '@/lib';

export type InputProps = InputHTMLAttributes<HTMLInputElement> & {
  isError?: boolean;
  ref?: Ref<HTMLInputElement>;
};

export function Input({ className, type = 'text', isError, ref, ...props }: InputProps) {
  return (
    <input
      ref={ref}
      type={type}
      aria-invalid={isError ? 'true' : undefined}
      className={cn(
        'text-foreground bg-input w-full p-3 text-sm leading-6 font-medium transition-all rounded-none',
        'placeholder:text-foreground-subtle placeholder:font-normal',
        'border border-transparent',
        isError
          ? 'border-error text-error placeholder:text-error/60 focus-visible:ring-error focus-visible:ring-2 focus-visible:outline-none'
          : 'focus-visible:ring-foreground focus-visible:ring-2 focus-visible:outline-none',
        className
      )}
      {...props}
    />
  );
}
