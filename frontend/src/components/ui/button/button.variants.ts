import { cva, type VariantProps } from 'class-variance-authority';

export const buttonVariants = cva(
  [
    'inline-flex items-center justify-center gap-1',
    'cursor-pointer px-4 py-2 font-medium',
    'transition-colors',
    'focus-visible:ring-foreground focus-visible:ring-offset-background',
    'focus-visible:ring-2 focus-visible:ring-offset-3',
    'focus-visible:outline-none',
  ],
  {
    variants: {
      variant: {
        primary: 'bg-primary hover:bg-primary-hover active:bg-primary-active',
        secondary: 'bg-secondary hover:bg-secondary-hover active:bg-secondary-active',
        tertiary: 'hover:bg-tertiary-hover active:bg-tertiary-active',
      },
    },
    defaultVariants: {
      variant: 'primary',
    },
  }
);

export type ButtonVariants = VariantProps<typeof buttonVariants>;
