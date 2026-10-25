import type { ComponentProps, ReactNode } from 'react';
import { Link } from 'react-router';
import { buttonVariants, type ButtonVariants } from './button.variants';
import { cn } from '@/lib';

type ButtonLinkProps = ComponentProps<typeof Link> &
  ButtonVariants & {
    leadingIcon?: ReactNode;
    trailingIcon?: ReactNode;
  };

export function ButtonLink({
  className,
  children,
  leadingIcon,
  trailingIcon,
  variant,
  ...props
}: ButtonLinkProps) {
  return (
    <Link className={cn(buttonVariants({ variant }), className)} {...props}>
      {leadingIcon}
      {children}
      {trailingIcon}
    </Link>
  );
}
