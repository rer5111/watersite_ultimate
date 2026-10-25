import type { TextareaHTMLAttributes } from 'react';
import { cn } from '@/lib';

export type TextareaProps = TextareaHTMLAttributes<HTMLTextAreaElement>;

export function Textarea({ className, ...props }: TextareaProps) {
  return (
    <textarea
      className={cn(
        'text-foreground bg-input h-40 w-full resize-none p-3 text-sm leading-6 font-medium transition-all',
        'placeholder:text-foreground-subtle placeholder:font-normal',
        'focus-visible:ring-foreground focus-visible:ring-2 focus-visible:outline-none',
        className
      )}
      {...props}
    />
  );
}
