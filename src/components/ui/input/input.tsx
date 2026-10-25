import type { InputHTMLAttributes } from 'react';
import { cn } from '@/lib';

export type InputProps = InputHTMLAttributes<HTMLInputElement>;

export function Input({ className, type = 'text', ...props }: InputProps) {
  return (
    <input
      type={type}
      className={cn(
        'text-foreground bg-input w-full p-3 text-sm leading-6 font-medium transition-all',
        'placeholder:text-foreground-subtle placeholder:font-normal',
        'focus-visible:ring-foreground focus-visible:ring-2 focus-visible:outline-none',
        className
      )}
      {...props}
    />
  );
}
