import type { ButtonHTMLAttributes, ReactNode } from 'react';
import { buttonVariants, type ButtonVariants } from './button.variants';
import { cn } from '@/lib';

export type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> &
  ButtonVariants & {
    leadingIcon?: ReactNode;
    trailingIcon?: ReactNode;
  };

export function Button({
  className,
  children,
  leadingIcon,
  trailingIcon,
  variant,
  type = 'button',
  ...props
}: ButtonProps) {
  return (
    <button type={type} className={cn(buttonVariants({ variant }), className)} {...props}>
      {leadingIcon}
      {children}
      {trailingIcon}
    </button>
  );
}
