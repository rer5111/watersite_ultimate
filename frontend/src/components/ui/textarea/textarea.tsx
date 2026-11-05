import type { TextareaHTMLAttributes } from 'react';
import { cn } from '@/lib';

export type TextareaProps = TextareaHTMLAttributes<HTMLTextAreaElement> & {
  isError?: boolean;
};

export function Textarea({ className, isError, ...props }: TextareaProps) {
  return (
    <textarea
      aria-invalid={isError ? 'true' : undefined}
      className={cn(
        'text-foreground bg-input h-40 w-full resize-none rounded-none p-3 text-sm leading-6 font-medium transition-all',
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
