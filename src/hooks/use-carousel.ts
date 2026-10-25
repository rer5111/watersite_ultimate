import { useEffect, useRef, useState } from 'react';

export interface UseCarouselOptions {
  itemCount: number;
  duration?: number;
}

export function useCarousel({ itemCount, duration = 6000 }: UseCarouselOptions) {
  const [activeIndex, setActiveIndex] = useState(0);
  const [progress, setProgress] = useState(0);
  const [prefersReducedMotion, setPrefersReducedMotion] = useState(false);
  const [timerResetKey, setTimerResetKey] = useState(0);
  const slideStartedAt = useRef<number | null>(null);

  useEffect(() => {
    const mediaQuery = window.matchMedia('(prefers-reduced-motion: reduce)');
    const updateMotionPreference = () => setPrefersReducedMotion(mediaQuery.matches);

    updateMotionPreference();
    mediaQuery.addEventListener('change', updateMotionPreference);

    return () => mediaQuery.removeEventListener('change', updateMotionPreference);
  }, []);

  useEffect(() => {
    if (prefersReducedMotion || itemCount <= 1) return;

    slideStartedAt.current = Date.now();
    const timer = window.setInterval(() => {
      const startedAt = slideStartedAt.current ?? Date.now();
      const nextProgress = Math.min((Date.now() - startedAt) / duration, 1);

      setProgress(nextProgress * 100);

      if (nextProgress === 1) {
        slideStartedAt.current = Date.now();
        setActiveIndex((curr) => (curr + 1) % itemCount);
        setProgress(0);
      }
    }, 100);

    return () => window.clearInterval(timer);
  }, [prefersReducedMotion, timerResetKey, itemCount, duration]);

  const selectSlide = (index: number) => {
    setActiveIndex(index);
    setProgress(0);
    setTimerResetKey((key) => key + 1);
  };

  return {
    activeIndex,
    progress,
    selectSlide,
    prefersReducedMotion,
  };
}
