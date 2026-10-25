import { cva, type VariantProps } from 'class-variance-authority';

export const logoVariants = cva('inline-flex items-center select-none [&_img]:aspect-square', {
  variants: {
    size: {
      sm: 'gap-2 [&_img]:size-6 [&_span]:text-lg',
      md: 'gap-3 [&_img]:size-8 [&_span]:text-2xl',
      lg: 'gap-3 [&_img]:size-10 [&_span]:text-4xl',
      xl: 'gap-4 [&_img]:size-16 [&_span]:text-6xl',
    },
  },
  defaultVariants: {
    size: 'md',
  },
});

export type LogoVariants = VariantProps<typeof logoVariants>;
