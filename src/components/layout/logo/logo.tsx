import { Link } from 'react-router';
import { paths } from '@/routes';
import { cn } from '@/lib';
import { logoVariants, type LogoVariants } from './logo.variants';
import waltercraftLogo from '@/assets/images/waltercraft.png';

type LogoProps = LogoVariants & {
  className?: string;
};

export function Logo({ size, className }: LogoProps) {
  return (
    <Link to={paths.home} className={cn(logoVariants({ size }), className)}>
      <img src={waltercraftLogo} alt="" />

      <span className="font-brand leading-none [text-box:trim-both_cap_alphabetic]">
        WALTERCRAFT
      </span>
    </Link>
  );
}
